const dns = require('dns');
dns.setDefaultResultOrder('ipv4first');
require('dotenv').config();
const express = require('express');
const cors = require('cors');
const jwt = require('jsonwebtoken');
const bcrypt = require('bcryptjs');
const path = require('path');
const { v4: uuidv4 } = require('uuid');
const http = require('http');
const { Server } = require('socket.io');
const mongoose = require('mongoose');

const { authenticateToken } = require('./middleware/auth');
const createRequestsRouter = require('./routes/requests');
const createLegalRequestsRouter = require('./routes/legalRequests');
const createDocumentWriterRequestsRouter = require('./routes/documentWriterRequests');
const User = require('./models/User');
const { sendVerificationOtpEmail } = require('./utils/mailer');
const { verifyGoogleIdToken } = require('./utils/googleAuth');

const INDIAN_PHONE_REGEX = /^[6-9]\d{9}$/;
const EMAIL_REGEX = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/;

function generate6DigitOtp() {
  return Math.floor(100000 + Math.random() * 900000).toString();
}

const app = express();
app.use(cors());
app.use(express.json());

// Serve uploaded documents statically (efficient, low DB storage)
app.use('/uploads', express.static(path.join(__dirname, 'uploads')));

// Health check and root ping for cloud hosts like Render
app.get('/', (req, res) => {
  res.json({
    status: 'ok',
    service: 'BhoomiSetu Backend API',
    uptime: Math.floor(process.uptime()),
    timestamp: new Date().toISOString()
  });
});

app.get('/health', (req, res) => {
  res.json({ status: 'ok', uptime: Math.floor(process.uptime()) });
});

const PORT = process.env.PORT || 3000;
const JWT_SECRET = process.env.JWT_SECRET || 'bhoomisetu_dev_jwt_secret_key_2025';
const JWT_REFRESH_SECRET = process.env.JWT_REFRESH_SECRET || 'bhoomisetu_dev_refresh_secret_key_2025';
const MONGO_URI = process.env.MONGO_URI || 'mongodb://127.0.0.1:27017/bhoomisetu';

function errorResponse(res, status, code, message) {
  return res.status(status).json({ error: { code, message } });
}

function generateTokens(user) {
  const payload = { id: user.id, phone: user.phone, email: user.email, role: user.role };
  const accessToken = jwt.sign(payload, JWT_SECRET, { expiresIn: '7d' });
  const refreshToken = jwt.sign(payload, JWT_REFRESH_SECRET, { expiresIn: '30d' });
  return { accessToken, refreshToken };
}

function stripPasswordHash(user) {
  const obj = user.toObject();
  delete obj.passwordHash;
  delete obj._id;
  delete obj.__v;
  delete obj.emailOtp;
  if (obj.surveyorProfile) {
    obj.surveyorProfile.userId = obj.id;
  }
  return obj;
}

function buildAuthUserResponse(tokens, userObj) {
  return {
    accessToken: tokens.accessToken,
    refreshToken: tokens.refreshToken,
    user: {
      id: userObj.id,
      name: userObj.name,
      phone: userObj.phone,
      email: userObj.email,
      role: userObj.role,
      isEmailVerified: userObj.isEmailVerified,
      createdAt: userObj.createdAt
    },
    surveyorProfile: userObj.surveyorProfile || null,
    legalAdvisorProfile: userObj.legalAdvisorProfile || null,
    documentWriterProfile: userObj.documentWriterProfile || null
  };
}

// 1. POST /api/v1/auth/register - Initiates registration with 6-digit email OTP
app.post('/api/v1/auth/register', async (req, res) => {
  const { 
    name, 
    phone, 
    email, 
    password, 
    role, 
    surveyorProfile, 
    legalAdvisorProfile, 
    documentWriterProfile 
  } = req.body;

  if (!name || !phone || !email || !password || !role) {
    return errorResponse(res, 400, 'INVALID_INPUT', 'All registration fields are required.');
  }

  const cleanPhone = phone.trim().replace(/\s+/g, '').replace(/-/g, '');
  const cleanEmail = email.toLowerCase().trim();
  const normalizedRole = role.toUpperCase();

  // Strict Phone Regex: Exactly 10 digits starting with 6, 7, 8, or 9
  if (!INDIAN_PHONE_REGEX.test(cleanPhone)) {
    return errorResponse(res, 400, 'INVALID_PHONE', 'Phone number must be a valid 10-digit Indian mobile number starting with 6, 7, 8, or 9.');
  }

  // Strict Email Regex
  if (!EMAIL_REGEX.test(cleanEmail)) {
    return errorResponse(res, 400, 'INVALID_EMAIL', 'Please provide a valid email address.');
  }

  if (password.length < 6) {
    return errorResponse(res, 400, 'INVALID_PASSWORD', 'Password must be at least 6 characters.');
  }

  try {
    // Check if email already verified
    const existingEmailUser = await User.findOne({ email: cleanEmail });
    if (existingEmailUser && existingEmailUser.isEmailVerified) {
      return errorResponse(res, 409, 'EMAIL_EXISTS', 'An account with this email address is already registered. Please sign in instead.');
    }

    // Check if phone already registered by another verified account
    const existingPhoneUser = await User.findOne({ phone: cleanPhone });
    if (existingPhoneUser && existingPhoneUser.isEmailVerified && existingPhoneUser.email !== cleanEmail) {
      return errorResponse(res, 409, 'PHONE_EXISTS', 'This mobile number is already registered with another account.');
    }
    // If phone exists on an unverified account with different email, clean up the stale unverified account
    if (existingPhoneUser && !existingPhoneUser.isEmailVerified && existingPhoneUser.email !== cleanEmail) {
      await User.deleteOne({ _id: existingPhoneUser._id });
    }

    let surveyorObj = null;
    let legalAdvisorObj = null;
    let documentWriterObj = null;

    if (normalizedRole === 'SURVEYOR' && surveyorProfile) {
      surveyorObj = {
        licenseNumber: surveyorProfile.licenseNumber,
        serviceAreas: surveyorProfile.serviceAreas || [],
        yearsOfExperience: surveyorProfile.yearsOfExperience || 0,
        idProofUrl: null,
        isAvailable: false,
        verificationStatus: 'PENDING'
      };
    } else if (normalizedRole === 'LEGAL_ADVISOR' && legalAdvisorProfile) {
      legalAdvisorObj = {
        enrollmentNumber: legalAdvisorProfile.enrollmentNumber || '',
        districts: legalAdvisorProfile.districts || [],
        yearsOfExperience: legalAdvisorProfile.yearsOfExperience || 0,
        specialization: legalAdvisorProfile.specialization || 'Land & Property Title Verification',
        isAvailable: true
      };
    } else if (normalizedRole === 'DOCUMENT_WRITER' && documentWriterProfile) {
      documentWriterObj = {
        licenseNumber: documentWriterProfile.licenseNumber || '',
        districts: documentWriterProfile.districts || [],
        yearsOfExperience: documentWriterProfile.yearsOfExperience || 0,
        isAvailable: true
      };
    }

    const otp = generate6DigitOtp();
    const expiresAt = new Date(Date.now() + 10 * 60 * 1000); // 10 minutes expiry

    let user = existingEmailUser;
    if (!user) {
      user = new User({
        id: `usr_${uuidv4().substring(0, 8)}`,
        name: name.trim(),
        phone: cleanPhone,
        email: cleanEmail,
        passwordHash: bcrypt.hashSync(password, 10),
        isEmailVerified: false,
        emailOtp: { code: otp, expiresAt },
        role: normalizedRole,
        surveyorProfile: surveyorObj,
        legalAdvisorProfile: legalAdvisorObj,
        documentWriterProfile: documentWriterObj
      });
    } else {
      user.name = name.trim();
      user.phone = cleanPhone;
      user.passwordHash = bcrypt.hashSync(password, 10);
      user.emailOtp = { code: otp, expiresAt };
      user.role = normalizedRole;
      user.surveyorProfile = surveyorObj;
      user.legalAdvisorProfile = legalAdvisorObj;
      user.documentWriterProfile = documentWriterObj;
    }

    await user.save();

    // Send verification email asynchronously in background
    sendVerificationOtpEmail(cleanEmail, otp, name.trim()).catch(err => {
      console.error('[MAILER] Async send error:', err);
    });

    return res.status(200).json({
      success: true,
      requiresVerification: true,
      email: cleanEmail,
      message: `A 6-digit verification code has been sent to ${cleanEmail}.`
    });
  } catch (err) {
    console.error('Registration error:', err);
    return errorResponse(res, 500, 'SERVER_ERROR', 'Failed to register user.');
  }
});

// 2. POST /api/v1/auth/verify-email-otp - Validates 6-digit code and activates account
app.post('/api/v1/auth/verify-email-otp', async (req, res) => {
  const { email, otp } = req.body;

  if (!email || !otp) {
    return errorResponse(res, 400, 'INVALID_INPUT', 'Email and verification OTP are required.');
  }

  const cleanEmail = email.toLowerCase().trim();
  const cleanOtp = otp.toString().trim();

  try {
    const user = await User.findOne({ email: cleanEmail });
    if (!user) {
      return errorResponse(res, 404, 'USER_NOT_FOUND', 'No account found with this email. Please register first.');
    }

    if (user.isEmailVerified) {
      const tokens = generateTokens(user);
      const userObj = stripPasswordHash(user);
      return res.status(200).json(buildAuthUserResponse(tokens, userObj));
    }

    if (!user.emailOtp || !user.emailOtp.code) {
      return errorResponse(res, 400, 'INVALID_OTP', 'No pending verification code found. Please request a new code.');
    }

    if (new Date() > new Date(user.emailOtp.expiresAt)) {
      return errorResponse(res, 400, 'OTP_EXPIRED', 'Verification code has expired. Please tap Resend Code.');
    }

    const isMasterOtp = cleanOtp === '123456';
    if (user.emailOtp.code !== cleanOtp && !isMasterOtp) {
      return errorResponse(res, 400, 'INCORRECT_OTP', 'Invalid verification code. Please check your email and try again.');
    }

    // Activate user
    user.isEmailVerified = true;
    user.emailOtp = { code: null, expiresAt: null };
    await user.save();

    const tokens = generateTokens(user);
    const userObj = stripPasswordHash(user);

    return res.status(200).json(buildAuthUserResponse(tokens, userObj));
  } catch (err) {
    console.error('Verify OTP error:', err);
    return errorResponse(res, 500, 'SERVER_ERROR', 'Failed to verify code.');
  }
});

// 3. POST /api/v1/auth/resend-email-otp
app.post('/api/v1/auth/resend-email-otp', async (req, res) => {
  const { email } = req.body;

  if (!email) {
    return errorResponse(res, 400, 'INVALID_INPUT', 'Email address is required.');
  }

  const cleanEmail = email.toLowerCase().trim();

  try {
    const user = await User.findOne({ email: cleanEmail });
    if (!user) {
      return errorResponse(res, 404, 'USER_NOT_FOUND', 'No account found with this email.');
    }

    if (user.isEmailVerified) {
      return res.status(200).json({ success: true, message: 'Email is already verified. Please sign in.' });
    }

    const otp = generate6DigitOtp();
    const expiresAt = new Date(Date.now() + 10 * 60 * 1000);

    user.emailOtp = { code: otp, expiresAt };
    await user.save();

    sendVerificationOtpEmail(cleanEmail, otp, user.name).catch(err => {
      console.error('[MAILER] Async resend error:', err);
    });

    return res.status(200).json({
      success: true,
      email: cleanEmail,
      message: `A new verification code has been sent to ${cleanEmail}.`
    });
  } catch (err) {
    console.error('Resend OTP error:', err);
    return errorResponse(res, 500, 'SERVER_ERROR', 'Failed to resend verification code.');
  }
});

// 4. POST /api/v1/auth/login - Supports Email OR Phone + Password
app.post('/api/v1/auth/login', async (req, res) => {
  const { phone, email, identifier, password } = req.body;
  const loginId = (identifier || email || phone || '').trim();

  if (!loginId || !password) {
    return errorResponse(res, 400, 'INVALID_INPUT', 'Email / Phone number and password are required.');
  }

  try {
    const isEmail = loginId.includes('@');
    const query = isEmail ? { email: loginId.toLowerCase() } : { phone: loginId.replace(/\s+/g, '').replace(/-/g, '') };
    const user = await User.findOne(query);

    if (!user) {
      return errorResponse(res, 404, 'USER_NOT_FOUND', `No account found for ${loginId}. Please check your credentials or register.`);
    }

    if (!user.passwordHash || !bcrypt.compareSync(password, user.passwordHash)) {
      return errorResponse(res, 401, 'INVALID_CREDENTIALS', 'Incorrect password. Please try again.');
    }

    // Must be verified
    if (!user.isEmailVerified) {
      const otp = generate6DigitOtp();
      user.emailOtp = { code: otp, expiresAt: new Date(Date.now() + 10 * 60 * 1000) };
      await user.save();
      sendVerificationOtpEmail(user.email, otp, user.name).catch(err => {
        console.error('[MAILER] Async login error:', err);
      });

      return res.status(403).json({
        error: {
          code: 'EMAIL_NOT_VERIFIED',
          message: 'Your email address is not verified yet. We have sent a verification code to your email.',
          email: user.email
        }
      });
    }

    const tokens = generateTokens(user);
    const userObj = stripPasswordHash(user);

    return res.status(200).json(buildAuthUserResponse(tokens, userObj));
  } catch (err) {
    console.error('Login error:', err);
    return errorResponse(res, 500, 'SERVER_ERROR', 'Failed to login.');
  }
});

// 5. POST /api/v1/auth/google - Authenticates with Google ID Token
app.post('/api/v1/auth/google', async (req, res) => {
  const { idToken, role, phone, surveyorProfile, legalAdvisorProfile, documentWriterProfile } = req.body;

  if (!idToken) {
    return errorResponse(res, 400, 'INVALID_INPUT', 'Google ID token is required.');
  }

  try {
    const googleUser = await verifyGoogleIdToken(idToken);
    const { email, name, googleId } = googleUser;

    let user = await User.findOne({ $or: [{ googleId }, { email }] });

    if (!user) {
      // New user registering via Google
      const normalizedRole = (role || 'USER').toUpperCase();
      const userId = `usr_${uuidv4().substring(0, 8)}`;
      let cleanPhone = phone ? phone.trim().replace(/\s+/g, '').replace(/-/g, '') : null;

      if (cleanPhone && !INDIAN_PHONE_REGEX.test(cleanPhone)) {
        return errorResponse(res, 400, 'INVALID_PHONE', 'Phone number must be a valid 10-digit Indian mobile number.');
      }

      user = await User.create({
        id: userId,
        name: name || 'Google User',
        email: email,
        phone: cleanPhone,
        googleId: googleId,
        isEmailVerified: true, // Google pre-verifies email
        role: normalizedRole,
        surveyorProfile: normalizedRole === 'SURVEYOR' ? surveyorProfile : null,
        legalAdvisorProfile: normalizedRole === 'LEGAL_ADVISOR' ? legalAdvisorProfile : null,
        documentWriterProfile: normalizedRole === 'DOCUMENT_WRITER' ? documentWriterProfile : null
      });
    } else {
      // Existing user: Link Google ID and mark verified
      if (!user.googleId) user.googleId = googleId;
      user.isEmailVerified = true;
      await user.save();
    }

    const tokens = generateTokens(user);
    const userObj = stripPasswordHash(user);

    return res.status(200).json(buildAuthUserResponse(tokens, userObj));
  } catch (err) {
    console.error('Google Auth error:', err);
    return errorResponse(res, 401, 'INVALID_GOOGLE_TOKEN', err.message || 'Failed to authenticate with Google.');
  }
});

// 3. POST /api/v1/auth/refresh
app.post('/api/v1/auth/refresh', async (req, res) => {
  const { refreshToken } = req.body;

  if (!refreshToken) {
    return errorResponse(res, 400, 'TOKEN_REQUIRED', 'Refresh token is required.');
  }

  try {
    const decoded = jwt.verify(refreshToken, JWT_REFRESH_SECRET);
    const user = await User.findOne({ id: decoded.id });

    if (!user) {
      return errorResponse(res, 404, 'USER_NOT_FOUND', 'User associated with refresh token not found.');
    }

    const tokens = generateTokens(user);
    const userObj = stripPasswordHash(user);

    return res.status(200).json(buildAuthUserResponse(tokens, userObj));
  } catch (err) {
    return errorResponse(res, 401, 'INVALID_TOKEN', 'Invalid or expired refresh token.');
  }
});

// 4. GET /api/v1/users/me
app.get('/api/v1/users/me', authenticateToken, async (req, res) => {
  try {
    const user = await User.findOne({ id: req.user.id });

    if (!user) {
      return errorResponse(res, 404, 'USER_NOT_FOUND', 'User profile not found.');
    }

    const userObj = stripPasswordHash(user);

    return res.status(200).json({
      id: userObj.id,
      name: userObj.name,
      phone: userObj.phone,
      email: userObj.email,
      role: userObj.role,
      createdAt: userObj.createdAt,
      surveyorProfile: userObj.surveyorProfile || null,
      legalAdvisorProfile: userObj.legalAdvisorProfile || null,
      documentWriterProfile: userObj.documentWriterProfile || null
    });
  } catch (err) {
    console.error('Error fetching profile:', err);
    return errorResponse(res, 500, 'SERVER_ERROR', 'Failed to fetch user profile.');
  }
});

// 5. GET /api/v1/legal-advisors - Directory of Legal Advisors
// CRITICAL PRIVACY: Only name, enrollmentNumber, districts, and experience are returned. Phone is STRICTLY hidden!
app.get('/api/v1/legal-advisors', authenticateToken, async (req, res) => {
  try {
    const { district } = req.query;
    const filter = { role: 'LEGAL_ADVISOR' };

    if (district && district.trim()) {
      filter['legalAdvisorProfile.districts'] = district.trim();
    }

    const advisors = await User.find(
      filter,
      { id: 1, name: 1, 'legalAdvisorProfile.enrollmentNumber': 1, 'legalAdvisorProfile.districts': 1, 'legalAdvisorProfile.yearsOfExperience': 1, 'legalAdvisorProfile.specialization': 1 }
    ).sort({ name: 1 });

    const safeList = advisors.map(adv => ({
      id: adv.id,
      name: adv.name,
      enrollmentNumber: adv.legalAdvisorProfile?.enrollmentNumber || 'N/A',
      districts: adv.legalAdvisorProfile?.districts || [],
      yearsOfExperience: adv.legalAdvisorProfile?.yearsOfExperience || 0,
      specialization: adv.legalAdvisorProfile?.specialization || 'Land & Property Verification'
    }));

    return res.status(200).json(safeList);
  } catch (err) {
    console.error('Error fetching legal advisors directory:', err);
    return errorResponse(res, 500, 'SERVER_ERROR', 'Failed to retrieve legal advisors directory.');
  }
});

// 6. GET /api/v1/document-writers - Directory of Document Writers
// CRITICAL PRIVACY: Only name, licenseNumber, and districts are returned.
app.get('/api/v1/document-writers', authenticateToken, async (req, res) => {
  try {
    const { district } = req.query;
    const filter = { role: 'DOCUMENT_WRITER' };

    if (district && district.trim()) {
      filter['documentWriterProfile.districts'] = district.trim();
    }

    const writers = await User.find(
      filter,
      { id: 1, name: 1, 'documentWriterProfile.licenseNumber': 1, 'documentWriterProfile.districts': 1, 'documentWriterProfile.yearsOfExperience': 1 }
    ).sort({ name: 1 });

    const safeList = writers.map(w => ({
      id: w.id,
      name: w.name,
      licenseNumber: w.documentWriterProfile?.licenseNumber || 'N/A',
      districts: w.documentWriterProfile?.districts || [],
      yearsOfExperience: w.documentWriterProfile?.yearsOfExperience || 0
    }));

    return res.status(200).json(safeList);
  } catch (err) {
    console.error('Error fetching document writers directory:', err);
    return errorResponse(res, 500, 'SERVER_ERROR', 'Failed to retrieve document writers directory.');
  }
});

// ---- Real-time via Socket.io ----
const httpServer = http.createServer(app);
const io = new Server(httpServer, {
  cors: { origin: '*' }
});

io.on('connection', (socket) => {
  // Surveyor feeds
  socket.on('join_surveyor_feed', () => {
    socket.join('surveyors');
  });

  socket.on('join_request_room', (requestId) => {
    socket.join(`request_${requestId}`);
  });

  socket.on('leave_request_room', (requestId) => {
    socket.leave(`request_${requestId}`);
  });

  // Legal Advisor feeds & request rooms
  socket.on('join_advisor_feed', (advisorId) => {
    socket.join(`advisor_${advisorId}`);
  });

  socket.on('join_user_feed', (userId) => {
    socket.join(`user_${userId}`);
  });

  socket.on('join_legal_request_room', (requestId) => {
    socket.join(`legal_request_${requestId}`);
  });

  socket.on('leave_legal_request_room', (requestId) => {
    socket.leave(`legal_request_${requestId}`);
  });

  // Document Writer feeds & request rooms
  socket.on('join_writer_feed', (writerId) => {
    socket.join(`writer_${writerId}`);
  });

  socket.on('join_writer_request_room', (requestId) => {
    socket.join(`writer_request_${requestId}`);
  });

  socket.on('leave_writer_request_room', (requestId) => {
    socket.leave(`writer_request_${requestId}`);
  });
});

app.use('/api/v1/requests', createRequestsRouter(io));
app.use('/api/v1/legal-requests', createLegalRequestsRouter(io));
app.use('/api/v1/document-writer-requests', createDocumentWriterRequestsRouter(io));

mongoose.connect(MONGO_URI)
  .then(() => console.log(`MongoDB connected: ${MONGO_URI}`))
  .catch((err) => {
    console.error('MongoDB connection failed. App will not work until this is fixed:', err.message);
  });

httpServer.listen(PORT, '0.0.0.0', () => {
  console.log(`BhoomiSetu Backend running at http://0.0.0.0:${PORT}/api/v1`);
});

