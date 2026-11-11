const express = require('express');
const multer = require('multer');
const path = require('path');
const fs = require('fs');
const { v4: uuidv4 } = require('uuid');

const LegalAdvisorRequest = require('../models/LegalAdvisorRequest');
const ChatMessage = require('../models/ChatMessage');
const User = require('../models/User');
const { authenticateToken } = require('../middleware/auth');
const { uploadToCloudinary } = require('../utils/cloudinary');

// Ensure upload directory exists
const uploadDir = path.join(__dirname, '..', 'uploads', 'documents');
if (!fs.existsSync(uploadDir)) {
  fs.mkdirSync(uploadDir, { recursive: true });
}

// Multer storage for efficient local disk storage (keeps database footprint tiny)
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
  limits: { fileSize: 25 * 1024 * 1024 }, // 25MB max per document
  fileFilter: (req, file, cb) => {
    const allowed = /pdf|jpg|jpeg|png|webp|doc|docx/;
    const ext = path.extname(file.originalname).toLowerCase().replace('.', '');
    if (allowed.test(ext) || allowed.test(file.mimetype)) {
      cb(null, true);
    } else {
      cb(new Error('Only PDF, JPG, PNG, and DOC files are allowed.'));
    }
  }
});

function errorResponse(res, status, code, message) {
  return res.status(status).json({ error: { code, message } });
}

// Security: Phone numbers are redacted unless request is accepted
function toLegalRequestDto(doc, viewingUserId) {
  const isAccepted = ['ACCEPTED', 'VERIFIED', 'NEEDS_CHANGES', 'COMPLETED'].includes(doc.status);
  const isRequester = doc.userId === viewingUserId;
  const isAdvisor = doc.advisorId === viewingUserId;

  let revealedUserPhone = null;
  let revealedAdvisorPhone = null;

  if (isAccepted) {
    revealedUserPhone = doc.userPhone;
    revealedAdvisorPhone = doc.advisorPhone;
  } else {
    // If not accepted yet, only owner sees their own phone number
    if (isRequester) revealedUserPhone = doc.userPhone;
    if (isAdvisor) revealedAdvisorPhone = doc.advisorPhone;
  }

  return {
    id: doc._id.toString(),
    userId: doc.userId,
    userName: doc.userName,
    userPhone: revealedUserPhone,
    advisorId: doc.advisorId,
    advisorName: doc.advisorName,
    advisorPhone: revealedAdvisorPhone,
    advisorEnrollment: doc.advisorEnrollment,
    district: doc.district,
    mandal: doc.mandal,
    village: doc.village,
    surveyNumber: doc.surveyNumber,
    propertyTitle: doc.propertyTitle,
    description: doc.description,
    status: doc.status,
    verificationSummary: doc.verificationSummary || '',
    documents: doc.documents || [],
    createdAt: doc.createdAt,
    acceptedAt: doc.acceptedAt,
    updatedAt: doc.updatedAt
  };
}

function createLegalRequestsRouter(io) {
  const router = express.Router();

  // 1. POST /api/v1/legal-requests - User creates a legal verification request
  router.post('/', authenticateToken, async (req, res) => {
    try {
      const { 
        advisorId, 
        district, 
        mandal, 
        village, 
        surveyNumber, 
        propertyTitle, 
        description 
      } = req.body;

      if (!advisorId || !district || !propertyTitle || !description) {
        return errorResponse(res, 400, 'INVALID_INPUT', 'advisorId, district, propertyTitle, and description are required.');
      }

      // Fetch advisor profile
      const advisor = await User.findOne({ id: advisorId, role: 'LEGAL_ADVISOR' });
      if (!advisor) {
        return errorResponse(res, 404, 'ADVISOR_NOT_FOUND', 'Selected Legal Advisor was not found.');
      }

      // Fetch user profile
      const user = await User.findOne({ id: req.user.id });
      if (!user) {
        return errorResponse(res, 404, 'USER_NOT_FOUND', 'User account not found.');
      }

      const doc = await LegalAdvisorRequest.create({
        userId: user.id,
        userName: user.name,
        userPhone: user.phone,
        advisorId: advisor.id,
        advisorName: advisor.name,
        advisorPhone: advisor.phone,
        advisorEnrollment: advisor.legalAdvisorProfile?.enrollmentNumber || 'N/A',
        district,
        mandal: mandal || '',
        village: village || '',
        surveyNumber: surveyNumber || '',
        propertyTitle,
        description,
        status: 'PENDING'
      });

      const dto = toLegalRequestDto(doc, req.user.id);
      
      // Notify advisor in real-time
      io.to(`advisor_${advisorId}`).emit('new_legal_request', dto);

      return res.status(201).json(dto);
    } catch (err) {
      console.error('Error creating legal request:', err);
      return errorResponse(res, 500, 'SERVER_ERROR', 'Failed to submit legal verification request.');
    }
  });

  // 2. GET /api/v1/legal-requests/mine - Requests created by logged-in user
  router.get('/mine', authenticateToken, async (req, res) => {
    try {
      const docs = await LegalAdvisorRequest.find({ userId: req.user.id }).sort({ createdAt: -1 });
      return res.status(200).json(docs.map(d => toLegalRequestDto(d, req.user.id)));
    } catch (err) {
      console.error('Error fetching user requests:', err);
      return errorResponse(res, 500, 'SERVER_ERROR', 'Failed to retrieve your legal requests.');
    }
  });

  // 3. GET /api/v1/legal-requests/advisor - Requests assigned to logged-in Legal Advisor
  router.get('/advisor', authenticateToken, async (req, res) => {
    try {
      const docs = await LegalAdvisorRequest.find({ advisorId: req.user.id }).sort({ createdAt: -1 });
      return res.status(200).json(docs.map(d => toLegalRequestDto(d, req.user.id)));
    } catch (err) {
      console.error('Error fetching advisor requests:', err);
      return errorResponse(res, 500, 'SERVER_ERROR', 'Failed to retrieve advisor requests.');
    }
  });

  // 4. GET /api/v1/legal-requests/:id - Single request detail
  router.get('/:id', authenticateToken, async (req, res) => {
    try {
      const doc = await LegalAdvisorRequest.findById(req.params.id);
      if (!doc) return errorResponse(res, 404, 'NOT_FOUND', 'Legal request not found.');

      if (doc.userId !== req.user.id && doc.advisorId !== req.user.id) {
        return errorResponse(res, 403, 'FORBIDDEN', 'You do not have access to this request.');
      }

      return res.status(200).json(toLegalRequestDto(doc, req.user.id));
    } catch (err) {
      console.error('Error fetching legal request:', err);
      return errorResponse(res, 500, 'SERVER_ERROR', 'Failed to retrieve request details.');
    }
  });

  // 5. POST /api/v1/legal-requests/:id/accept - Advisor accepts the request
  router.post('/:id/accept', authenticateToken, async (req, res) => {
    try {
      const doc = await LegalAdvisorRequest.findById(req.params.id);
      if (!doc) return errorResponse(res, 404, 'NOT_FOUND', 'Legal request not found.');

      if (doc.advisorId !== req.user.id) {
        return errorResponse(res, 403, 'FORBIDDEN', 'Only the assigned Legal Advisor can accept this request.');
      }

      if (doc.status !== 'PENDING') {
        return errorResponse(res, 409, 'INVALID_STATE', `Request cannot be accepted from state: ${doc.status}`);
      }

      doc.status = 'ACCEPTED';
      doc.acceptedAt = Date.now();
      doc.updatedAt = Date.now();
      await doc.save();

      const dto = toLegalRequestDto(doc, req.user.id);

      // Notify user and advisor rooms
      io.to(`legal_request_${doc._id}`).emit('legal_request_updated', dto);
      io.to(`user_${doc.userId}`).emit('legal_request_accepted', dto);

      return res.status(200).json(dto);
    } catch (err) {
      console.error('Error accepting legal request:', err);
      return errorResponse(res, 500, 'SERVER_ERROR', 'Failed to accept request.');
    }
  });

  // 6. POST /api/v1/legal-requests/:id/reject - Advisor declines the request
  router.post('/:id/reject', authenticateToken, async (req, res) => {
    try {
      const doc = await LegalAdvisorRequest.findById(req.params.id);
      if (!doc) return errorResponse(res, 404, 'NOT_FOUND', 'Legal request not found.');

      if (doc.advisorId !== req.user.id) {
        return errorResponse(res, 403, 'FORBIDDEN', 'Only the assigned Legal Advisor can reject this request.');
      }

      doc.status = 'REJECTED';
      doc.updatedAt = Date.now();
      await doc.save();

      const dto = toLegalRequestDto(doc, req.user.id);
      io.to(`legal_request_${doc._id}`).emit('legal_request_updated', dto);

      return res.status(200).json(dto);
    } catch (err) {
      console.error('Error rejecting legal request:', err);
      return errorResponse(res, 500, 'SERVER_ERROR', 'Failed to decline request.');
    }
  });

  // 7. POST /api/v1/legal-requests/:id/documents - Upload verification document
  router.post('/:id/documents', authenticateToken, upload.single('file'), async (req, res) => {
    try {
      const doc = await LegalAdvisorRequest.findById(req.params.id);
      if (!doc) return errorResponse(res, 404, 'NOT_FOUND', 'Legal request not found.');

      if (doc.userId !== req.user.id) {
        return errorResponse(res, 403, 'FORBIDDEN', 'Only the client / property buyer can upload documents for verification.');
      }


      if (!req.file) {
        return errorResponse(res, 400, 'FILE_REQUIRED', 'Please select a document file to upload.');
      }

      const documentType = req.body.documentType || 'OTHER';
      const title = req.body.title || req.file.originalname;

      // Upload to Cloudinary if configured; fallback to local disk
      let fileUrl = `/uploads/documents/${req.file.filename}`;
      const cloudResult = await uploadToCloudinary(req.file.path, 'bhoomisetu_documents/legal_verifications');
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
        uploadedByRole: doc.userId === req.user.id ? 'USER' : 'LEGAL_ADVISOR',
        uploadedAt: Date.now(),
        verificationStatus: 'PENDING',
        remarks: ''
      };

      doc.documents.push(newDocItem);
      doc.updatedAt = Date.now();
      await doc.save();

      const dto = toLegalRequestDto(doc, req.user.id);
      io.to(`legal_request_${doc._id}`).emit('document_uploaded', { requestId: doc._id.toString(), document: newDocItem });

      return res.status(201).json({
        message: 'Document uploaded successfully',
        document: newDocItem,
        request: dto
      });
    } catch (err) {
      console.error('Error uploading document:', err);
      return errorResponse(res, 500, 'SERVER_ERROR', err.message || 'Failed to upload document.');
    }
  });

  // GET /api/v1/legal-requests/documents/download/:filename - Force file download as attachment
  router.get('/documents/download/:filename', (req, res) => {
    try {
      const filename = path.basename(req.params.filename);
      const filePath = path.join(uploadDir, filename);
      if (!fs.existsSync(filePath)) {
        return errorResponse(res, 404, 'NOT_FOUND', 'File not found on server.');
      }
      const downloadName = req.query.name || filename;
      return res.download(filePath, downloadName);
    } catch (err) {
      console.error('Error downloading file:', err);
      return errorResponse(res, 500, 'SERVER_ERROR', 'Failed to download file.');
    }
  });

  // 8. PATCH /api/v1/legal-requests/:id/documents/:docId - Advisor reviews individual document
  router.patch('/:id/documents/:docId', authenticateToken, async (req, res) => {
    try {
      const { verificationStatus, remarks } = req.body;
      const doc = await LegalAdvisorRequest.findById(req.params.id);
      if (!doc) return errorResponse(res, 404, 'NOT_FOUND', 'Legal request not found.');

      if (doc.advisorId !== req.user.id) {
        return errorResponse(res, 403, 'FORBIDDEN', 'Only the assigned Legal Advisor can review documents.');
      }

      const targetDoc = doc.documents.find(d => d.id === req.params.docId);
      if (!targetDoc) {
        return errorResponse(res, 404, 'DOCUMENT_NOT_FOUND', 'Document not found in this request.');
      }

      if (verificationStatus) targetDoc.verificationStatus = verificationStatus;
      if (remarks !== undefined) targetDoc.remarks = remarks;

      doc.updatedAt = Date.now();
      await doc.save();

      const dto = toLegalRequestDto(doc, req.user.id);
      io.to(`legal_request_${doc._id}`).emit('document_reviewed', {
        requestId: doc._id.toString(),
        docId: req.params.docId,
        verificationStatus: targetDoc.verificationStatus,
        remarks: targetDoc.remarks
      });

      return res.status(200).json(dto);
    } catch (err) {
      console.error('Error reviewing document:', err);
      return errorResponse(res, 500, 'SERVER_ERROR', 'Failed to update document verification status.');
    }
  });

  // 9. POST /api/v1/legal-requests/:id/review - Advisor submits overall legal review / advice
  router.post('/:id/review', authenticateToken, async (req, res) => {
    try {
      const { status, verificationSummary } = req.body;
      const doc = await LegalAdvisorRequest.findById(req.params.id);
      if (!doc) return errorResponse(res, 404, 'NOT_FOUND', 'Legal request not found.');

      if (doc.advisorId !== req.user.id) {
        return errorResponse(res, 403, 'FORBIDDEN', 'Only the assigned Legal Advisor can submit verification review.');
      }

      if (status) doc.status = status; // e.g. VERIFIED, NEEDS_CHANGES, COMPLETED
      if (verificationSummary) doc.verificationSummary = verificationSummary;

      doc.updatedAt = Date.now();
      await doc.save();

      const dto = toLegalRequestDto(doc, req.user.id);
      io.to(`legal_request_${doc._id}`).emit('legal_request_updated', dto);

      return res.status(200).json(dto);
    } catch (err) {
      console.error('Error submitting review:', err);
      return errorResponse(res, 500, 'SERVER_ERROR', 'Failed to submit legal review.');
    }
  });

  // 10. GET /api/v1/legal-requests/:id/messages - Get chat history for legal request
  router.get('/:id/messages', authenticateToken, async (req, res) => {
    try {
      const doc = await LegalAdvisorRequest.findById(req.params.id);
      if (!doc) return errorResponse(res, 404, 'NOT_FOUND', 'Legal request not found.');

      if (doc.userId !== req.user.id && doc.advisorId !== req.user.id) {
        return errorResponse(res, 403, 'FORBIDDEN', 'You are not a participant in this request.');
      }

      const messages = await ChatMessage.find({ requestId: `legal_${req.params.id}` }).sort({ sentAt: 1 });
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

  // 11. POST /api/v1/legal-requests/:id/messages - Send chat message
  router.post('/:id/messages', authenticateToken, async (req, res) => {
    try {
      const { text, senderName } = req.body;
      if (!text || !text.trim()) {
        return errorResponse(res, 400, 'INVALID_INPUT', 'Message text cannot be empty.');
      }

      const doc = await LegalAdvisorRequest.findById(req.params.id);
      if (!doc) return errorResponse(res, 404, 'NOT_FOUND', 'Legal request not found.');

      if (doc.userId !== req.user.id && doc.advisorId !== req.user.id) {
        return errorResponse(res, 403, 'FORBIDDEN', 'You are not a participant in this request.');
      }

      if (doc.status === 'PENDING') {
        return errorResponse(res, 409, 'NOT_ACCEPTED', 'Chat is only unlocked after the Legal Advisor accepts the request.');
      }

      const message = await ChatMessage.create({
        requestId: `legal_${req.params.id}`,
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

      io.to(`legal_request_${req.params.id}`).emit('new_legal_message', messageDto);

      return res.status(201).json(messageDto);
    } catch (err) {
      console.error('Error sending message:', err);
      return errorResponse(res, 500, 'SERVER_ERROR', 'Failed to send message.');
    }
  });

  return router;
}

module.exports = createLegalRequestsRouter;
