const mongoose = require('mongoose');

const ecTransactionSchema = new mongoose.Schema({
  docNumber: { type: String, required: true },
  registrationDate: { type: String, required: true },
  nature: { type: String, required: true }, // Sale Deed, Gift, Mortgage, Settlement, etc.
  executant: { type: String, required: true }, // Seller / Transferor
  claimant: { type: String, required: true }, // Buyer / Transferee
  extent: { type: String, default: '' },
  considerationAmount: { type: String, default: '₹ 0' }
}, { _id: false });

const landRecordSchema = new mongoose.Schema({
  id: { type: String, required: true, unique: true },
  surveyNumber: { type: String, required: true },
  district: { type: String, required: true },
  mandal: { type: String, required: true },
  village: { type: String, required: true },
  recordType: { type: String, enum: ['ADANGAL', '1B', 'EC'], default: 'ADANGAL' },
  ownerName: { type: String, required: true },
  fatherOrHusbandName: { type: String, default: '' },
  khataNumber: { type: String, default: '' },
  extent: { type: String, required: true },
  classification: { type: String, default: 'Dry (Metta)' }, // Wet, Dry, Government, etc.
  landNature: { type: String, default: 'Patta Land (Ryotwari)' },
  waterSource: { type: String, default: 'Borewell / Natural Rain' },
  occupantName: { type: String, default: '' }, // అనుభవదారు పేరు
  occupantFatherName: { type: String, default: '' }, // అనుభవదారు తండ్రి పేరు
  natureOfPossession: { type: String, default: 'Self Title / Direct Purchase' }, // అనుభవ స్వభావం
  possessionExtent: { type: String, default: '' }, // అనుభవ విస్తీర్ణం
  cropDetails: { type: String, default: 'Paddy / Commercial Crops' }, // పండిస్తున్న పంట
  taxAmount: { type: String, default: '₹ 45.00' }, // శిస్తు / భూమి పన్ను
  bankLiabilities: { type: String, default: 'Nil (Clean / No Bank Mortgages)' }, // బ్యాంకు రుణాలు / తనఖా
  courtCases: { type: String, default: 'Clear (No Injunctions / Disputes)' }, // కోర్టు వివాదాలు
  is22AProhibited: { type: Boolean, default: false }, // 22-A నిషేధం
  columnDetails: { type: Array, default: [] }, // Array of { colNo, headerTelugu, headerEnglish, value }
  allColumns: { type: Map, of: String, default: {} }, // Dynamic map of all extracted columns
  marketValuePerAcre: { type: String, default: '₹ 35,00,000' },
  encumbranceStatus: { type: String, default: 'Nil Encumbrance (Clean Title)' },
  ecTransactions: { type: [ecTransactionSchema], default: [] },
  source: { type: String, default: 'AP MeeBhoomi Digital Registry' },
  status: { type: String, default: 'VERIFIED' },
  rawSourceSnapshot: { type: String, default: '' },
  fetchedAt: { type: Number, default: () => Date.now() }
});

// Index for fast lookups
landRecordSchema.index({ district: 1, mandal: 1, village: 1, surveyNumber: 1 });

module.exports = mongoose.model('LandRecord', landRecordSchema);
