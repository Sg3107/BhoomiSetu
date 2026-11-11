const express = require('express');
const LandRecord = require('../models/LandRecord');

function createLandRecordsRouter() {
  const router = express.Router();

  // Helper for consistent error responses
  function errorResponse(res, status, code, message) {
    return res.status(status).json({ error: { code, message }, message });
  }

  // 1. POST /api/v1/land-records/search
  // Search verified MeeBhoomi records in database cache
  router.post('/search', async (req, res) => {
    const { district, mandal, village, surveyNumber, recordType } = req.body;

    if (!district || !mandal || !village || !surveyNumber) {
      return errorResponse(res, 400, 'INVALID_INPUT', 'District, Mandal, Village, and Survey Number are required.');
    }

    const cleanDistrict = district.trim();
    const cleanMandal = mandal.trim();
    const cleanVillage = village.trim();
    const cleanSurveyNo = surveyNumber.trim();
    const cleanType = (recordType || 'ADANGAL').toUpperCase();

    try {
      const record = await LandRecord.findOne({
        district: new RegExp(`^${cleanDistrict}$`, 'i'),
        mandal: new RegExp(`^${cleanMandal}$`, 'i'),
        village: new RegExp(`^${cleanVillage}$`, 'i'),
        surveyNumber: new RegExp(`^${cleanSurveyNo}$`, 'i')
      });

      if (record) {
        return res.status(200).json({
          source: 'BhoomiSetu Verified Registry Cache',
          isLiveQuery: false,
          record: record
        });
      }

      return errorResponse(
        res,
        404,
        'RECORD_NOT_FOUND',
        `No local verified record found for Survey '${cleanSurveyNo}' in ${cleanVillage}, ${cleanMandal}. Please use the 'Open Official MeeBhoomi Portal' button to view live records on meebhoomi.ap.gov.in.`
      );
    } catch (err) {
      console.error('Error fetching land record:', err);
      return errorResponse(res, 500, 'SERVER_ERROR', 'Failed to fetch land record.');
    }
  });

  // 2. POST /api/v1/land-records/ec-search
  // Search Encumbrance Certificate records in database cache
  router.post('/ec-search', async (req, res) => {
    const { district, mandal, village, surveyNumber } = req.body;

    if (!district || !surveyNumber) {
      return errorResponse(res, 400, 'INVALID_INPUT', 'District and Survey Number are required for EC lookup.');
    }

    try {
      const cleanSurveyNo = surveyNumber.trim();
      const cleanDistrict = district.trim();

      const query = {
        district: new RegExp(`^${cleanDistrict}$`, 'i'),
        surveyNumber: new RegExp(`^${cleanSurveyNo}$`, 'i')
      };
      if (mandal && mandal.trim()) query.mandal = new RegExp(`^${mandal.trim()}$`, 'i');
      if (village && village.trim()) query.village = new RegExp(`^${village.trim()}$`, 'i');

      const record = await LandRecord.findOne(query);

      if (record && record.ecTransactions && record.ecTransactions.length > 0) {
        return res.status(200).json({
          surveyNumber: record.surveyNumber,
          district: record.district,
          village: record.village,
          mandal: record.mandal,
          status: record.encumbranceStatus || 'Nil Encumbrance (Clean Title)',
          source: 'AP IGRS Registry Gateway',
          period: '01-Jan-1983 to Present',
          transactions: record.ecTransactions
        });
      }

      return errorResponse(
        res,
        404,
        'EC_NOT_FOUND',
        `No Encumbrance Certificate records found for Survey '${cleanSurveyNo}' in ${cleanDistrict}. Please use the 'Open Official AP IGRS Portal' button to view on the official portal.`
      );
    } catch (err) {
      console.error('Error fetching EC:', err);
      return errorResponse(res, 500, 'SERVER_ERROR', 'Failed to retrieve Encumbrance Certificate.');
    }
  });

  return router;
}

module.exports = createLandRecordsRouter;
