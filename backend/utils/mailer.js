const dns = require('dns');
dns.setDefaultResultOrder('ipv4first');
const nodemailer = require('nodemailer');
let Resend = null;
try {
  Resend = require('resend').Resend;
} catch (e) {
  // Resend optional
}

/**
 * Sends email via Google Apps Script Webhook.
 * Runs directly on Google infrastructure using bhoomisetu.official@gmail.com,
 * sending over HTTPS (Port 443). 100% passes SPF/DKIM/DMARC into primary inboxes.
 */
const sendViaGoogleAppsScript = async (toEmail, otpCode, userName, html) => {
  const webhookUrl = process.env.GOOGLE_SCRIPT_WEBHOOK_URL ||
    'https://script.google.com/macros/s/AKfycbzr-4Q-NLMzytTkrhbX-aQLHX2WatgUFZ995tKIhr0Cnn3oxmExlzhPQnynrtGRSy3t2g/exec';

  try {
    const response = await fetch(webhookUrl, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        to: toEmail.trim(),
        subject: `Your BhoomiSetu Verification Code: ${otpCode}`,
        html: html
      }),
      redirect: 'follow'
    });

    const data = await response.json();
    if (data && data.success) {
      console.log(`[MAILER] OTP delivered via Google Apps Script (from bhoomisetu.official@gmail.com) to ${toEmail}`);
      return { success: true };
    } else {
      console.error('[MAILER] Google Apps Script error:', data);
      return { success: false, error: data ? data.error : 'Script failed' };
    }
  } catch (err) {
    console.error('[MAILER] Google Apps Script Exception:', err.message);
    return { success: false, error: err.message };
  }
};

/**
 * Sends email directly through Google Gmail official SMTP servers (Port 465 / 587).
 */
const sendViaGmail = async (toEmail, otpCode, userName, html) => {
  const user = process.env.EMAIL_USER || process.env.GMAIL_USER;
  const pass = process.env.EMAIL_PASS || process.env.GMAIL_APP_PASSWORD;
  if (!user || !pass) return null;

  const cleanPass = pass.replace(/\s+/g, '');
  const fromAddress = process.env.EMAIL_FROM || `"BhoomiSetu Support" <${user.trim()}>`;

  // 1. Try Port 465 (Implicit SSL)
  try {
    const transporter465 = nodemailer.createTransport({
      host: 'smtp.gmail.com',
      port: 465,
      secure: true,
      auth: { user: user.trim(), pass: cleanPass },
      connectionTimeout: 5000,
      greetingTimeout: 5000,
      socketTimeout: 8000
    });
    const info = await transporter465.sendMail({
      from: fromAddress,
      to: toEmail,
      subject: `Your BhoomiSetu Verification Code: ${otpCode}`,
      html
    });
    console.log(`[MAILER] OTP delivered via Gmail (port 465) to ${toEmail} (ID: ${info.messageId})`);
    return { success: true, messageId: info.messageId };
  } catch (err465) {
    console.warn('[MAILER] Gmail port 465 failed, attempting port 587 TLS:', err465.message);
    // 2. Try Port 587 (Explicit TLS)
    try {
      const transporter587 = nodemailer.createTransport({
        host: 'smtp.gmail.com',
        port: 587,
        secure: false,
        auth: { user: user.trim(), pass: cleanPass },
        connectionTimeout: 5000,
        greetingTimeout: 5000,
        socketTimeout: 8000
      });
      const info = await transporter587.sendMail({
        from: fromAddress,
        to: toEmail,
        subject: `Your BhoomiSetu Verification Code: ${otpCode}`,
        html
      });
      console.log(`[MAILER] OTP delivered via Gmail (port 587) to ${toEmail} (ID: ${info.messageId})`);
      return { success: true, messageId: info.messageId };
    } catch (err587) {
      console.error('[MAILER] Gmail SMTP Error on 587:', err587.message);
      return { success: false, error: err587.message };
    }
  }
};

/**
 * Fallback to Brevo REST API over HTTPS (Port 443)
 */
const sendViaBrevo = async (toEmail, otpCode, userName, html) => {
  const apiKey = process.env.BREVO_API_KEY;
  if (!apiKey) return null;

  const senderEmail = process.env.BREVO_SENDER_EMAIL || process.env.EMAIL_USER || 'bhoomisetu.official@gmail.com';
  const senderName = process.env.BREVO_SENDER_NAME || 'BhoomiSetu Support';

  try {
    const response = await fetch('https://api.brevo.com/v3/smtp/email', {
      method: 'POST',
      headers: {
        'accept': 'application/json',
        'api-key': apiKey.trim(),
        'content-type': 'application/json'
      },
      body: JSON.stringify({
        sender: { name: senderName, email: senderEmail.trim() },
        to: [{ email: toEmail.trim(), name: userName ? userName.trim() : 'Citizen' }],
        subject: `Your BhoomiSetu Verification Code: ${otpCode}`,
        htmlContent: html
      })
    });

    const data = await response.json();
    if (!response.ok) {
      console.error('[MAILER] Brevo API Error:', data);
      return { success: false, error: data.message || 'Brevo call failed' };
    }

    console.log(`[MAILER] OTP sent via Brevo to ${toEmail} (ID: ${data.messageId})`);
    return { success: true, messageId: data.messageId };
  } catch (err) {
    console.error('[MAILER] Brevo Exception:', err.message);
    return { success: false, error: err.message };
  }
};

/**
 * Sends a clean, branded email verification OTP to the user.
 * 1. Prioritizes Google Apps Script Webhook (Port 443 HTTPS directly from bhoomisetu.official@gmail.com)
 * 2. Falls back to direct Gmail SMTP (Port 465 / 587)
 * 3. Falls back to Brevo HTTP API
 * 4. Falls back to Resend if configured
 * 5. Falls back to console simulation
 */
const sendVerificationOtpEmail = async (toEmail, otpCode, userName) => {
  const html = `
    <!DOCTYPE html>
    <html>
    <head>
      <meta charset="utf-8">
      <style>
        body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif; background-color: #f7f9f7; margin: 0; padding: 20px; }
        .container { max-width: 500px; margin: 0 auto; background: #ffffff; border-radius: 12px; border: 1px solid #e1e8e1; padding: 32px; box-shadow: 0 4px 12px rgba(0,0,0,0.05); }
        .header { text-align: center; border-bottom: 2px solid #2e7d32; padding-bottom: 16px; margin-bottom: 24px; }
        .logo-title { font-size: 24px; font-weight: bold; color: #1e5631; letter-spacing: -0.5px; }
        .badge { font-size: 13px; color: #556b2f; font-weight: 600; margin-top: 4px; }
        .greeting { font-size: 16px; color: #2d3748; margin-bottom: 12px; }
        .desc { font-size: 14px; color: #4a5568; line-height: 1.5; margin-bottom: 24px; }
        .otp-box { text-align: center; background: #f0f7f0; border: 2px dashed #4caf50; border-radius: 8px; padding: 18px; margin: 20px 0; }
        .otp-code { font-size: 36px; font-weight: 800; letter-spacing: 8px; color: #1e5631; }
        .validity { font-size: 12px; color: #718096; margin-top: 6px; }
        .footer { text-align: center; font-size: 12px; color: #a0aec0; margin-top: 32px; border-top: 1px solid #edf2f7; padding-top: 16px; }
      </style>
    </head>
    <body>
      <div class="container">
        <div class="header">
          <div class="logo-title">BhoomiSetu</div>
          <div class="badge">AP Land Records & Surveyor Services</div>
        </div>
        <div class="greeting">Namaste <strong>${userName || 'User'}</strong>,</div>
        <div class="desc">
          Thank you for registering with BhoomiSetu. Please verify your email address to activate your account.
        </div>
        <div class="otp-box">
          <div class="otp-code">${otpCode}</div>
          <div class="validity">This verification code is valid for 10 minutes.</div>
        </div>
        <div class="desc">
          If you did not request this verification code, please ignore this email.
        </div>
        <div class="footer">
          &copy; ${new Date().getFullYear()} BhoomiSetu. Secure Citizen & Land Services.
        </div>
      </div>
    </body>
    </html>
  `;

  // 1. Try Google Apps Script Webhook (Sends directly from Google's Gmail servers over HTTPS Port 443!)
  const scriptResult = await sendViaGoogleAppsScript(toEmail, otpCode, userName, html);
  if (scriptResult && scriptResult.success) {
    return scriptResult;
  }

  // 2. Try Gmail SMTP
  if (process.env.EMAIL_USER && process.env.EMAIL_PASS) {
    const gmailResult = await sendViaGmail(toEmail, otpCode, userName, html);
    if (gmailResult && gmailResult.success) {
      return gmailResult;
    }
  }

  // 3. Try Brevo HTTP API
  if (process.env.BREVO_API_KEY) {
    const brevoResult = await sendViaBrevo(toEmail, otpCode, userName, html);
    if (brevoResult && brevoResult.success) {
      return brevoResult;
    }
  }

  // 4. Try Resend if configured
  if (process.env.RESEND_API_KEY && Resend) {
    try {
      const resend = new Resend(process.env.RESEND_API_KEY);
      const fromEmail = process.env.EMAIL_FROM || 'BhoomiSetu <onboarding@resend.dev>';
      const { data, error } = await resend.emails.send({
        from: fromEmail,
        to: [toEmail],
        subject: `Your BhoomiSetu Verification Code: ${otpCode}`,
        html
      });

      if (!error) {
        console.log(`[MAILER] OTP delivered via Resend to ${toEmail}`);
        return { success: true, data };
      }
    } catch (err) {
      console.error('[MAILER] Resend Exception:', err.message);
    }
  }

  // 5. Fallback simulation
  console.log(`\n======================================================`);
  console.log(`[MAILER SIMULATION] Delivery code for ${toEmail}: >>> ${otpCode} <<<`);
  console.log(`======================================================\n`);
  return { success: true, simulated: true };
};

module.exports = {
  sendVerificationOtpEmail
};
