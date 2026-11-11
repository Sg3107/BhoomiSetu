const express = require('express');
const multer = require('multer');
const path = require('path');
const fs = require('fs');
const { v4: uuidv4 } = require('uuid');

const DocumentWriterRequest = require('../models/DocumentWriterRequest');
const ChatMessage = require('../models/ChatMessage');
const User = require('../models/User');
const { authenticateToken } = require('../middleware/auth');
const { uploadToCloudinary } = require('../utils/cloudinary');

// Ensure upload directory exists
const uploadDir = path.join(__dirname, '..', 'uploads', 'documents');
if (!fs.existsSync(uploadDir)) {
  fs.mkdirSync(uploadDir, { recursive: true });
}

// Multer storage for documents & drafts
const storage = multer.diskStorage({
  destination: (req, file, cb) => {
    cb(null, uploadDir);
  },
  filename: (req, file, cb) => {
    const ext = path.extname(file.originalname).toLowerCase();
    const safeName = `doc_${Date.now()}_${uuidv4().substring(0, 8)}${ext}`;
    cb(null, safeName);
  }
});

const upload = multer({
  storage,
  limits: { fileSize: 30 * 1024 * 1024 }, // 30MB max per document
  fileFilter: (req, file, cb) => {
    const allowed = /pdf|jpg|jpeg|png|webp|doc|docx|rtf|txt/;
    const ext = path.extname(file.originalname).toLowerCase().replace('.', '');
    if (allowed.test(ext) || allowed.test(file.mimetype)) {
      cb(null, true);
    } else {
      cb(new Error('Only PDF, DOC, DOCX, TXT, and Image files are allowed.'));
    }
  }
});

function errorResponse(res, status, code, message) {
  return res.status(status).json({ error: { code, message } });
}

// Security: Phone numbers are redacted unless request is accepted
function toWriterRequestDto(doc, viewingUserId) {
  const isAccepted = ['ACCEPTED', 'DRAFT_UPLOADED', 'COMPLETED'].includes(doc.status);
  const isRequester = doc.userId === viewingUserId;
  const isWriter = doc.writerId === viewingUserId;

  let revealedUserPhone = null;
  let revealedWriterPhone = null;

  if (isAccepted) {
    revealedUserPhone = doc.userPhone;
    revealedWriterPhone = doc.writerPhone;
  } else {
    if (isRequester) revealedUserPhone = doc.userPhone;
    if (isWriter) revealedWriterPhone = doc.writerPhone;
  }

  return {
    id: doc._id.toString(),
    userId: doc.userId,
    userName: doc.userName,
    userPhone: revealedUserPhone,
    writerId: doc.writerId,
    writerName: doc.writerName,
    writerPhone: revealedWriterPhone,
    writerLicense: doc.writerLicense || 'N/A',
    district: doc.district,
    mandal: doc.mandal,
    village: doc.village,
    surveyNumber: doc.surveyNumber,
    deedType: doc.deedType,
    propertyTitle: doc.propertyTitle,
    description: doc.description,
    status: doc.status,
    clientDocuments: doc.clientDocuments || [],
    draftDocuments: doc.draftDocuments || [],
    createdAt: doc.createdAt,
    acceptedAt: doc.acceptedAt,
    completedAt: doc.completedAt,
    updatedAt: doc.updatedAt
  };
}

function createDocumentWriterRequestsRouter(io) {
  const router = express.Router();

  // 1. POST /api/v1/document-writer-requests - Create a deed writing request
  router.post('/', authenticateToken, async (req, res) => {
    try {
      const {
        writerId,
        district,
        mandal,
        village,
        surveyNumber,
        deedType,
        propertyTitle,
        description
      } = req.body;

      if (!writerId || !district || !propertyTitle || !description) {
        return errorResponse(res, 400, 'INVALID_INPUT', 'writerId, district, propertyTitle, and description are required.');
      }

      // Fetch writer profile
      const writer = await User.findOne({ id: writerId, role: 'DOCUMENT_WRITER' });
      if (!writer) {
        return errorResponse(res, 404, 'WRITER_NOT_FOUND', 'Selected Document Writer was not found.');
      }

      // Fetch user profile
      const user = await User.findOne({ id: req.user.id });
      if (!user) {
        return errorResponse(res, 404, 'USER_NOT_FOUND', 'User account not found.');
      }

      const doc = await DocumentWriterRequest.create({
        userId: user.id,
        userName: user.name,
        userPhone: user.phone,
        writerId: writer.id,
        writerName: writer.name,
        writerPhone: writer.phone,
        writerLicense: writer.documentWriterProfile?.licenseNumber || 'N/A',
        district,
        mandal: mandal || '',
        village: village || '',
        surveyNumber: surveyNumber || '',
        deedType: deedType || 'SALE_DEED',
        propertyTitle,
        description,
        status: 'PENDING'
      });

      const dto = toWriterRequestDto(doc, req.user.id);

      // Realtime notification to writer
      io.to(`writer_${writerId}`).emit('new_writer_request', dto);

      return res.status(201).json(dto);
    } catch (err) {
      console.error('Error creating document writer request:', err);
      return errorResponse(res, 500, 'SERVER_ERROR', 'Failed to submit document writing request.');
    }
  });

  // 2. GET /api/v1/document-writer-requests/mine - Requests created by client
  router.get('/mine', authenticateToken, async (req, res) => {
    try {
      const docs = await DocumentWriterRequest.find({ userId: req.user.id }).sort({ createdAt: -1 });
      return res.status(200).json(docs.map(d => toWriterRequestDto(d, req.user.id)));
    } catch (err) {
      console.error('Error fetching client requests:', err);
      return errorResponse(res, 500, 'SERVER_ERROR', 'Failed to retrieve your writing requests.');
    }
  });

  // 3. GET /api/v1/document-writer-requests/writer - Requests assigned to logged-in Document Writer
  router.get('/writer', authenticateToken, async (req, res) => {
    try {
      const docs = await DocumentWriterRequest.find({ writerId: req.user.id }).sort({ createdAt: -1 });
      return res.status(200).json(docs.map(d => toWriterRequestDto(d, req.user.id)));
    } catch (err) {
      console.error('Error fetching writer requests:', err);
      return errorResponse(res, 500, 'SERVER_ERROR', 'Failed to retrieve writer requests.');
    }
  });

  // 4. GET /api/v1/document-writer-requests/:id - Single request detail
  router.get('/:id', authenticateToken, async (req, res) => {
    try {
      const doc = await DocumentWriterRequest.findById(req.params.id);
      if (!doc) return errorResponse(res, 404, 'NOT_FOUND', 'Writing request not found.');

      if (doc.userId !== req.user.id && doc.writerId !== req.user.id) {
        return errorResponse(res, 403, 'FORBIDDEN', 'You do not have access to this request.');
      }

      return res.status(200).json(toWriterRequestDto(doc, req.user.id));
    } catch (err) {
      console.error('Error fetching writing request detail:', err);
      return errorResponse(res, 500, 'SERVER_ERROR', 'Failed to retrieve request details.');
    }
  });

  // 5. POST /api/v1/document-writer-requests/:id/accept - Writer accepts the request
  router.post('/:id/accept', authenticateToken, async (req, res) => {
    try {
      const doc = await DocumentWriterRequest.findById(req.params.id);
      if (!doc) return errorResponse(res, 404, 'NOT_FOUND', 'Writing request not found.');

      if (doc.writerId !== req.user.id) {
        return errorResponse(res, 403, 'FORBIDDEN', 'Only the assigned Document Writer can accept this request.');
      }

      if (doc.status !== 'PENDING') {
        return errorResponse(res, 409, 'INVALID_STATE', `Request cannot be accepted from state: ${doc.status}`);
      }

      doc.status = 'ACCEPTED';
      doc.acceptedAt = Date.now();
      doc.updatedAt = Date.now();
      await doc.save();

      const dto = toWriterRequestDto(doc, req.user.id);

      io.to(`writer_request_${doc._id}`).emit('writer_request_updated', dto);
      io.to(`user_${doc.userId}`).emit('writer_request_accepted', dto);

      return res.status(200).json(dto);
    } catch (err) {
      console.error('Error accepting writing request:', err);
      return errorResponse(res, 500, 'SERVER_ERROR', 'Failed to accept request.');
    }
  });

  // 6. POST /api/v1/document-writer-requests/:id/reject - Writer declines
  router.post('/:id/reject', authenticateToken, async (req, res) => {
    try {
      const doc = await DocumentWriterRequest.findById(req.params.id);
      if (!doc) return errorResponse(res, 404, 'NOT_FOUND', 'Writing request not found.');

      if (doc.writerId !== req.user.id) {
        return errorResponse(res, 403, 'FORBIDDEN', 'Only the assigned Document Writer can decline this request.');
      }

      doc.status = 'REJECTED';
      doc.updatedAt = Date.now();
      await doc.save();

      const dto = toWriterRequestDto(doc, req.user.id);
      io.to(`writer_request_${doc._id}`).emit('writer_request_updated', dto);

      return res.status(200).json(dto);
    } catch (err) {
      console.error('Error declining writing request:', err);
      return errorResponse(res, 500, 'SERVER_ERROR', 'Failed to decline request.');
    }
  });

  // 7. POST /api/v1/document-writer-requests/:id/documents - Client uploads supporting documents
  router.post('/:id/documents', authenticateToken, upload.single('file'), async (req, res) => {
    try {
      const doc = await DocumentWriterRequest.findById(req.params.id);
      if (!doc) return errorResponse(res, 404, 'NOT_FOUND', 'Writing request not found.');

      if (doc.userId !== req.user.id) {
        return errorResponse(res, 403, 'FORBIDDEN', 'Only the client can upload source/supporting documents.');
      }

      if (!req.file) {
        return errorResponse(res, 400, 'FILE_REQUIRED', 'Please select a document file to upload.');
      }

      const documentType = req.body.documentType || 'OTHER';
      const title = req.body.title || req.file.originalname;

      // Upload to Cloudinary if configured; fallback to local disk
      let fileUrl = `/uploads/documents/${req.file.filename}`;
      const cloudResult = await uploadToCloudinary(req.file.path, 'bhoomisetu_documents/writer_client_docs');
      if (cloudResult && cloudResult.url) {
        fileUrl = cloudResult.url;
      }

      const newDocItem = {
        id: `doc_${uuidv4().substring(0, 8)}`,
        documentType,
        title,
        originalFileName: req.file.originalname,
        fileUrl,
        fileSize: req.file.size,
        mimeType: req.file.mimetype,
        uploadedAt: Date.now()
      };

      doc.clientDocuments.push(newDocItem);
      doc.updatedAt = Date.now();
      await doc.save();

      const dto = toWriterRequestDto(doc, req.user.id);
      io.to(`writer_request_${doc._id}`).emit('writer_document_uploaded', {
        requestId: doc._id.toString(),
        document: newDocItem
      });

      return res.status(201).json({
        message: 'Document uploaded successfully',
        document: newDocItem,
        request: dto
      });
    } catch (err) {
      console.error('Error uploading client document:', err);
      return errorResponse(res, 500, 'SERVER_ERROR', err.message || 'Failed to upload document.');
    }
  });

  // 8. POST /api/v1/document-writer-requests/:id/drafts - Writer uploads drafted deed / final draft
  router.post('/:id/drafts', authenticateToken, upload.single('file'), async (req, res) => {
    try {
      const doc = await DocumentWriterRequest.findById(req.params.id);
      if (!doc) return errorResponse(res, 404, 'NOT_FOUND', 'Writing request not found.');

      if (doc.writerId !== req.user.id) {
        return errorResponse(res, 403, 'FORBIDDEN', 'Only the assigned Document Writer can upload the drafted deed.');
      }

      if (!req.file) {
        return errorResponse(res, 400, 'FILE_REQUIRED', 'Please select the deed draft file to upload.');
      }

      const title = req.body.title || `${doc.deedType.replace(/_/g, ' ')} Draft`;
      const notes = req.body.notes || '';
      const isFinal = req.body.isFinal === 'true' || req.body.isFinal === true;

      // Upload to Cloudinary if configured; fallback to local disk
      let fileUrl = `/uploads/documents/${req.file.filename}`;
      const cloudResult = await uploadToCloudinary(req.file.path, 'bhoomisetu_documents/writer_drafts');
      if (cloudResult && cloudResult.url) {
        fileUrl = cloudResult.url;
      }

      const newDraftItem = {
        id: `draft_${uuidv4().substring(0, 8)}`,
        title,
        originalFileName: req.file.originalname,
        fileUrl,
        fileSize: req.file.size,
        mimeType: req.file.mimetype,
        notes,
        uploadedAt: Date.now(),
        isFinal
      };

      doc.draftDocuments.push(newDraftItem);
      doc.status = 'DRAFT_UPLOADED';
      doc.updatedAt = Date.now();
      await doc.save();

      const dto = toWriterRequestDto(doc, req.user.id);
      io.to(`writer_request_${doc._id}`).emit('draft_uploaded', {
        requestId: doc._id.toString(),
        draft: newDraftItem,
        request: dto
      });
      io.to(`user_${doc.userId}`).emit('writer_request_updated', dto);

      return res.status(201).json({
        message: 'Deed draft uploaded successfully',
        draft: newDraftItem,
        request: dto
      });
    } catch (err) {
      console.error('Error uploading draft deed:', err);
      return errorResponse(res, 500, 'SERVER_ERROR', err.message || 'Failed to upload deed draft.');
    }
  });

  // 9. POST /api/v1/document-writer-requests/:id/complete - Mark request as completed
  router.post('/:id/complete', authenticateToken, async (req, res) => {
    try {
      const doc = await DocumentWriterRequest.findById(req.params.id);
      if (!doc) return errorResponse(res, 404, 'NOT_FOUND', 'Writing request not found.');

      if (doc.userId !== req.user.id && doc.writerId !== req.user.id) {
        return errorResponse(res, 403, 'FORBIDDEN', 'Only participants can complete this request.');
      }

      doc.status = 'COMPLETED';
      doc.completedAt = Date.now();
      doc.updatedAt = Date.now();
      await doc.save();

      const dto = toWriterRequestDto(doc, req.user.id);
      io.to(`writer_request_${doc._id}`).emit('writer_request_updated', dto);

      return res.status(200).json(dto);
    } catch (err) {
      console.error('Error completing request:', err);
      return errorResponse(res, 500, 'SERVER_ERROR', 'Failed to complete request.');
    }
  });

  // 10. GET /api/v1/document-writer-requests/:id/messages - Chat history
  router.get('/:id/messages', authenticateToken, async (req, res) => {
    try {
      const doc = await DocumentWriterRequest.findById(req.params.id);
      if (!doc) return errorResponse(res, 404, 'NOT_FOUND', 'Writing request not found.');

      if (doc.userId !== req.user.id && doc.writerId !== req.user.id) {
        return errorResponse(res, 403, 'FORBIDDEN', 'You are not a participant in this request.');
      }

      const messages = await ChatMessage.find({ requestId: `docwriter_${req.params.id}` }).sort({ sentAt: 1 });
      return res.status(200).json(messages.map(m => ({
        id: m._id.toString(),
        requestId: req.params.id,
        senderId: m.senderId,
        senderName: m.senderName,
        text: m.text,
        sentAt: m.sentAt
      })));
    } catch (err) {
      console.error('Error fetching chat messages:', err);
      return errorResponse(res, 500, 'SERVER_ERROR', 'Failed to load messages.');
    }
  });

  // 11. POST /api/v1/document-writer-requests/:id/messages - Send chat message
  router.post('/:id/messages', authenticateToken, async (req, res) => {
    try {
      const { text, senderName } = req.body;
      if (!text || !text.trim()) {
        return errorResponse(res, 400, 'INVALID_INPUT', 'Message text cannot be empty.');
      }

      const doc = await DocumentWriterRequest.findById(req.params.id);
      if (!doc) return errorResponse(res, 404, 'NOT_FOUND', 'Writing request not found.');

      if (doc.userId !== req.user.id && doc.writerId !== req.user.id) {
        return errorResponse(res, 403, 'FORBIDDEN', 'You are not a participant in this request.');
      }

      if (doc.status === 'PENDING') {
        return errorResponse(res, 409, 'NOT_ACCEPTED', 'Chat is only unlocked after the Document Writer accepts the request.');
      }

      const message = await ChatMessage.create({
        requestId: `docwriter_${req.params.id}`,
        senderId: req.user.id,
        senderName: senderName || req.user.name || 'User',
        text: text.trim()
      });

      const messageDto = {
        id: message._id.toString(),
        requestId: req.params.id,
        senderId: message.senderId,
        senderName: message.senderName,
        text: message.text,
        sentAt: message.sentAt
      };

      io.to(`writer_request_${req.params.id}`).emit('new_writer_message', messageDto);

      return res.status(201).json(messageDto);
    } catch (err) {
      console.error('Error sending message:', err);
      return errorResponse(res, 500, 'SERVER_ERROR', 'Failed to send message.');
    }
  });

  return router;
}

module.exports = createDocumentWriterRequestsRouter;
