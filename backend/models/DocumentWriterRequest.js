const mongoose = require('mongoose');

const clientDocumentSchema = new mongoose.Schema({
  id: { type: String, required: true },
  documentType: {
    type: String,
    required: true,
    enum: [
      'LINK_DOC',             // Previous / Parent Sale Deeds
      'PATTADAR_PASSBOOK',    // Pattadar Passbook / 1B
      'ADANGAL',              // Adangal / Pahani
      'ENCUMBRANCE_CERTIFICATE', // EC Form 15/16
      'MUTATION_ORDER',       // Revenue mutation proceedings
      'LAYOUT_PLAN',          // Approved Layout or FMB sketch
      'ID_PROOF',             // Buyer/Seller Aadhaar/PAN cards
      'TAX_RECEIPT',          // Property Tax receipt
      'OTHER'
    ]
  },
  title: { type: String, required: true },
  originalFileName: { type: String, required: true },
  fileUrl: { type: String, required: true }, // e.g. /uploads/documents/doc_xxx.pdf
  fileSize: { type: Number, default: 0 },
  mimeType: { type: String, default: 'application/octet-stream' },
  uploadedAt: { type: Number, default: () => Date.now() }
}, { _id: false });

const draftDocumentSchema = new mongoose.Schema({
  id: { type: String, required: true },
  title: { type: String, required: true }, // e.g. "Final Sale Deed Draft v1"
  originalFileName: { type: String, required: true },
  fileUrl: { type: String, required: true },
  fileSize: { type: Number, default: 0 },
  mimeType: { type: String, default: 'application/octet-stream' },
  notes: { type: String, default: '' },     // Special instructions / notes by writer
  uploadedAt: { type: Number, default: () => Date.now() },
  isFinal: { type: Boolean, default: true }
}, { _id: false });

const documentWriterRequestSchema = new mongoose.Schema({
  userId: { type: String, required: true, index: true },
  userName: { type: String, required: true },
  userPhone: { type: String, required: true }, // Redacted until accepted
  writerId: { type: String, required: true, index: true },
  writerName: { type: String, required: true },
  writerPhone: { type: String, required: true }, // Redacted until accepted
  writerLicense: { type: String, default: 'N/A' },
  district: { type: String, required: true, index: true },
  mandal: { type: String, default: '' },
  village: { type: String, default: '' },
  surveyNumber: { type: String, default: '' },
  deedType: {
    type: String,
    required: true,
    enum: [
      'SALE_DEED',
      'GIFT_DEED',
      'PARTITION_DEED',
      'MORTGAGE_DEED',
      'WILL',
      'SALE_AGREEMENT',
      'LEASE_DEED',
      'SETTLEMENT_DEED',
      'RELEASE_DEED',
      'OTHER'
    ],
    default: 'SALE_DEED'
  },
  propertyTitle: { type: String, required: true },
  description: { type: String, required: true },
  status: {
    type: String,
    enum: ['PENDING', 'ACCEPTED', 'REJECTED', 'DRAFT_UPLOADED', 'COMPLETED'],
    default: 'PENDING',
    index: true
  },
  clientDocuments: { type: [clientDocumentSchema], default: [] },
  draftDocuments: { type: [draftDocumentSchema], default: [] },
  createdAt: { type: Number, default: () => Date.now() },
  acceptedAt: { type: Number, default: null },
  completedAt: { type: Number, default: null },
  updatedAt: { type: Number, default: () => Date.now() }
});

module.exports = mongoose.model('DocumentWriterRequest', documentWriterRequestSchema);
