const mongoose = require('mongoose');

const surveyorProfileSchema = new mongoose.Schema({
  licenseNumber: { type: String, required: true },
  serviceAreas: { type: [String], default: [] },
  yearsOfExperience: { type: Number, default: 0 },
  idProofUrl: { type: String, default: null },
  isAvailable: { type: Boolean, default: false },
  verificationStatus: { type: String, default: 'PENDING' }
}, { _id: false });

const legalAdvisorProfileSchema = new mongoose.Schema({
  enrollmentNumber: { type: String, required: true },
  districts: { type: [String], default: [] },
  yearsOfExperience: { type: Number, default: 0 },
  specialization: { type: String, default: 'Land & Property Verification' },
  isAvailable: { type: Boolean, default: true }
}, { _id: false });

const documentWriterProfileSchema = new mongoose.Schema({
  licenseNumber: { type: String, default: '' },
  districts: { type: [String], default: [] },
  yearsOfExperience: { type: Number, default: 0 },
  isAvailable: { type: Boolean, default: true }
}, { _id: false });

const userSchema = new mongoose.Schema({
  id: { type: String, required: true, unique: true },
  name: { type: String, required: true, trim: true },
  phone: { type: String, trim: true, sparse: true },
  email: { type: String, required: true, unique: true, lowercase: true, trim: true },
  passwordHash: { type: String, default: null },
  isEmailVerified: { type: Boolean, default: false },
  emailOtp: {
    code: { type: String, default: null },
    expiresAt: { type: Date, default: null }
  },
  googleId: { type: String, default: null, sparse: true },
  role: { 
    type: String, 
    enum: ['USER', 'SURVEYOR', 'LEGAL_ADVISOR', 'DOCUMENT_WRITER', 'ADMIN'], 
    default: 'USER' 
  },
  surveyorProfile: { type: surveyorProfileSchema, default: null },
  legalAdvisorProfile: { type: legalAdvisorProfileSchema, default: null },
  documentWriterProfile: { type: documentWriterProfileSchema, default: null },
  createdAt: { type: Number, default: () => Date.now() }
});

module.exports = mongoose.model('User', userSchema);

