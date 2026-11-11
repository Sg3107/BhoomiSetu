const { OAuth2Client } = require('google-auth-library');

const client = new OAuth2Client(process.env.GOOGLE_CLIENT_ID);

/**
 * Verifies a Google ID token from the mobile client.
 * Returns { email, name, picture, googleId, emailVerified } on success.
 */
const verifyGoogleIdToken = async (idToken) => {
  if (!idToken) throw new Error('Google ID token is required');

  const ticket = await client.verifyIdToken({
    idToken,
    audience: process.env.GOOGLE_CLIENT_ID || undefined
  });

  const payload = ticket.getPayload();
  if (!payload || !payload.email) {
    throw new Error('Invalid Google token payload');
  }

  return {
    email: payload.email.toLowerCase().trim(),
    name: payload.name || `${payload.given_name || ''} ${payload.family_name || ''}`.trim() || 'User',
    picture: payload.picture || null,
    googleId: payload.sub,
    emailVerified: payload.email_verified || true
  };
};

module.exports = {
  verifyGoogleIdToken
};
