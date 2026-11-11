const mongoose = require('mongoose');

const documentItemSchema = new mongoose.Schema({
  id: { type: String, required: true },
  documentType: { 
    type: String, 
    required: true,
    enum: [
      'SALE_DEED',               // Registered Sale Deed / Link Title Documents
      'PATTADAR_PASSBOOK',        // Pattadar Passbook / 1B Record of Rights
      'ADANGAL',                 // Adangal / Pahani extract
      'ENCUMBRANCE_CERTIFICATE', // EC Form 15/16 from IGRS
      'MUTATION_ORDER',          // Mutation Order / Proceedings
      'LAYOUT_PLAN',             // Approved Layout / Survey Sketch (FMB)
      'TAX_RECEIPT',             // Property Tax / Land Revenue receipt
      'OTHER'
    ]
  },
  title: { type: String, required: true },
  originalFileName: { type: String, required: true },
  fileUrl: { type: String, required: true }, // lightweight URL path, e.g. /uploads/documents/doc_xxx.pdf
  fileSize: { type: Number, default: 0 },    // bytes
  mimeType: { type: String, default: 'application/octet-stream' },
  uploadedByRole: { type: String, default: 'USER' },
  uploadedAt: { type: Number, default: () => Date.now() },
  verificationStatus: { 
    type: String, 
    enum: ['PENDING', 'VERIFIED', 'NEEDS_CHANGES', 'REJECTED'],
    default: 'PENDING'
  },
  remarks: { type: String, default: '' }
}, { _id: false });

const legalAdvisorRequestSchema = new mongoose.Schema({
  userId: { type: String, required: true, index: true },
  userName: { type: String, required: true },
  userPhone: { type: String, required: true }, // Exposed only after advisor accepts
  advisorId: { type: String, required: true, index: true },
  advisorName: { type: String, required: true },
  advisorPhone: { type: String, required: true }, // Exposed only after advisor accepts
  advisorEnrollment: { type: String, required: true },
  district: { type: String, required: true, index: true },
  mandal: { type: String, default: '' },
  village: { type: String, default: '' },
  surveyNumber: { type: String, default: '' },
  propertyTitle: { type: String, required: true },
  description: { type: String, required: true },
  status: {
    type: String,
    enum: ['PENDING', 'ACCEPTED', 'REJECTED', 'VERIFIED', 'NEEDS_CHANGES', 'COMPLETED'],
    default: 'PENDING',
    index: true
  },
  verificationSummary: { type: String, default: '' }, // Running/Final legal advice & required changes
  documents: { type: [documentItemSchema], default: [] },
  createdAt: { type: Number, default: () => Date.now() },
  acceptedAt: { type: Number, default: null },
  updatedAt: { type: Number, default: () => Date.now() }
});

module.exports = mongoose.model('LegalAdvisorRequest', legalAdvisorRequestSchema);
