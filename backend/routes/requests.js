const express = require('express');
const { v4: uuidv4 } = require('uuid');
const SurveyRequest = require('../models/SurveyRequest');
const ChatMessage = require('../models/ChatMessage');
const { authenticateToken } = require('../middleware/auth');

function toRequestDto(doc) {
  return {
    id: doc._id.toString(),
    requesterId: doc.requesterId,
    requesterName: doc.requesterName,
    requesterPhone: doc.requesterPhone,
    district: doc.district,
    mandal: doc.mandal,
    village: doc.village,
    surveyNumber: doc.surveyNumber,
    description: doc.description,
    status: doc.status,
    surveyorId: doc.surveyorId,
    surveyorName: doc.surveyorName,
    createdAt: doc.createdAt,
    acceptedAt: doc.acceptedAt
  };
}

function toMessageDto(doc) {
  return {
    id: doc._id.toString(),
    requestId: doc.requestId,
    senderId: doc.senderId,
    senderName: doc.senderName,
    text: doc.text,
    sentAt: doc.sentAt
  };
}

function errorResponse(res, status, code, message) {
  return res.status(status).json({ error: { code, message } });
}

/**
 * Takes the Socket.io server instance so this router can emit live updates:
 * - 'new_request' to everyone in the 'surveyors' room when a request is created
 * - 'request_updated' to a specific request's room on accept/complete
 * - 'new_message' to a specific request's room when a chat message is sent
 * Clients join rooms via socket events handled in server.js.
 */
function createRequestsRouter(io) {
  const router = express.Router();

  // Create a request (the "requester" is whoever is logged in -- could be a
  // landowner or, in principle, another surveyor requesting help).
  router.post('/', authenticateToken, async (req, res) => {
    const { requesterName, requesterPhone, district, mandal, village, surveyNumber, description } = req.body;

    if (!requesterName || !requesterPhone || !district || !mandal || !description) {
      return errorResponse(res, 400, 'INVALID_INPUT', 'requesterName, requesterPhone, district, mandal, and description are required.');
    }

    const doc = await SurveyRequest.create({
      requesterId: req.user.id,
      requesterName,
      requesterPhone,
      district,
      mandal,
      village: village || '',
      surveyNumber: surveyNumber || '',
      description
    });

    const dto = toRequestDto(doc);
    io.to('surveyors').emit('new_request', dto);
    return res.status(201).json(dto);
  });

  // Browse open requests, optionally filtered by district/mandal (surveyor view).
  router.get('/', authenticateToken, async (req, res) => {
    const { district, mandal, status } = req.query;
    const filter = { status: status || 'OPEN' };
    if (district) filter.district = district;
    if (mandal) filter.mandal = mandal;

    const docs = await SurveyRequest.find(filter).sort({ createdAt: -1 });
    return res.status(200).json(docs.map(toRequestDto));
  });

  // The logged-in user's own submitted requests.
  router.get('/mine', authenticateToken, async (req, res) => {
    const docs = await SurveyRequest.find({ requesterId: req.user.id }).sort({ createdAt: -1 });
    return res.status(200).json(docs.map(toRequestDto));
  });

  // The logged-in surveyor's accepted jobs.
  router.get('/assigned', authenticateToken, async (req, res) => {
    const docs = await SurveyRequest.find({ surveyorId: req.user.id }).sort({ createdAt: -1 });
    return res.status(200).json(docs.map(toRequestDto));
  });

  // Surveyor accepts an open request.
  router.post('/:id/accept', authenticateToken, async (req, res) => {
    const { surveyorName } = req.body;
    if (!surveyorName) {
      return errorResponse(res, 400, 'INVALID_INPUT', 'surveyorName is required.');
    }

    const doc = await SurveyRequest.findById(req.params.id);
    if (!doc) return errorResponse(res, 404, 'NOT_FOUND', 'Request not found.');
    if (doc.status !== 'OPEN') {
      return errorResponse(res, 409, 'ALREADY_TAKEN', 'This request has already been accepted or is no longer open.');
    }

    doc.status = 'ACCEPTED';
    doc.surveyorId = req.user.id;
    doc.surveyorName = surveyorName;
    doc.acceptedAt = Date.now();
    await doc.save();

    const dto = toRequestDto(doc);
    io.to(`request_${dto.id}`).emit('request_updated', dto);
    io.to('surveyors').emit('request_updated', dto); // so other surveyors see it disappear from the open list
    return res.status(200).json(dto);
  });

  // Only the requester can mark the job complete.
  router.post('/:id/complete', authenticateToken, async (req, res) => {
    const doc = await SurveyRequest.findById(req.params.id);
    if (!doc) return errorResponse(res, 404, 'NOT_FOUND', 'Request not found.');
    if (doc.requesterId !== req.user.id) {
      return errorResponse(res, 403, 'FORBIDDEN', 'Only the requester can complete this request.');
    }

    doc.status = 'COMPLETED';
    await doc.save();

    const dto = toRequestDto(doc);
    io.to(`request_${dto.id}`).emit('request_updated', dto);
    return res.status(200).json(dto);
  });

  // Chat history for one request -- only the two participants can read it.
  router.get('/:id/messages', authenticateToken, async (req, res) => {
    const doc = await SurveyRequest.findById(req.params.id);
    if (!doc) return errorResponse(res, 404, 'NOT_FOUND', 'Request not found.');
    if (doc.requesterId !== req.user.id && doc.surveyorId !== req.user.id) {
      return errorResponse(res, 403, 'FORBIDDEN', 'You are not a participant in this request.');
    }

    const messages = await ChatMessage.find({ requestId: req.params.id }).sort({ sentAt: 1 });
    return res.status(200).json(messages.map(toMessageDto));
  });

  router.post('/:id/messages', authenticateToken, async (req, res) => {
    const { senderName, text } = req.body;
    if (!senderName || !text || !text.trim()) {
      return errorResponse(res, 400, 'INVALID_INPUT', 'senderName and non-empty text are required.');
    }

    const doc = await SurveyRequest.findById(req.params.id);
    if (!doc) return errorResponse(res, 404, 'NOT_FOUND', 'Request not found.');
    if (doc.requesterId !== req.user.id && doc.surveyorId !== req.user.id) {
      return errorResponse(res, 403, 'FORBIDDEN', 'You are not a participant in this request.');
    }
    if (doc.status !== 'ACCEPTED') {
      return errorResponse(res, 409, 'NOT_ACCEPTED', 'Chat is only available once a request has been accepted.');
    }

    const message = await ChatMessage.create({
      requestId: req.params.id,
      senderId: req.user.id,
      senderName,
      text: text.trim()
    });

    const dto = toMessageDto(message);
    io.to(`request_${req.params.id}`).emit('new_message', dto);
    return res.status(201).json(dto);
  });

  return router;
}

module.exports = createRequestsRouter;
