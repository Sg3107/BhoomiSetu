const jwt = require('jsonwebtoken');

const JWT_SECRET = process.env.JWT_SECRET || 'bhoomisetu_dev_jwt_secret_key_2025';

/**
 * Same Bearer-token check already used inline in GET /users/me, pulled out
 * so the new survey-request routes can share it instead of duplicating it.
 */
function authenticateToken(req, res, next) {
  const authHeader = req.headers.authorization;
  if (!authHeader || !authHeader.startsWith('Bearer ')) {
    return res.status(401).json({
      error: { code: 'UNAUTHORIZED', message: 'Bearer token is missing or malformed.' }
    });
  }

  const token = authHeader.split(' ')[1];
  try {
    const decoded = jwt.verify(token, JWT_SECRET);
    req.user = decoded; // { id, phone, role }
    next();
  } catch (err) {
    return res.status(401).json({
      error: { code: 'EXPIRED_TOKEN', message: 'Access token is invalid or expired.' }
    });
  }
}

module.exports = { authenticateToken };
