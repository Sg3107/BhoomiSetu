const mongoose = require('mongoose');

const surveyRequestSchema = new mongoose.Schema({
  requesterId: { type: String, required: true },
  requesterName: { type: String, required: true },
  requesterPhone: { type: String, required: true },
  district: { type: String, required: true },
  mandal: { type: String, required: true },
  village: { type: String, default: '' },
  surveyNumber: { type: String, default: '' },
  description: { type: String, required: true },
  status: {
    type: String,
    enum: ['OPEN', 'ACCEPTED', 'COMPLETED', 'CANCELLED'],
    default: 'OPEN'
  },
  surveyorId: { type: String, default: null },
  surveyorName: { type: String, default: null },
  createdAt: { type: Number, default: () => Date.now() },
  acceptedAt: { type: Number, default: null }
});

module.exports = mongoose.model('SurveyRequest', surveyRequestSchema);
