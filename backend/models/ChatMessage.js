const mongoose = require('mongoose');

const chatMessageSchema = new mongoose.Schema({
  requestId: { type: String, required: true, index: true },
  senderId: { type: String, required: true },
  senderName: { type: String, required: true },
  text: { type: String, required: true },
  sentAt: { type: Number, default: () => Date.now() }
});

module.exports = mongoose.model('ChatMessage', chatMessageSchema);
