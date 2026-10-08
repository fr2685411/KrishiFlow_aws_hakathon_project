/**
 * KrishiFlow Admin Utility: Set Custom User Claims for Role-Based Access Control (RBAC)
 * 
 * Usage:
 *   1. Initialize service account key: export GOOGLE_APPLICATION_CREDENTIALS="serviceAccountKey.json"
 *   2. Run script: node set-user-claims.js <USER_UID> <ROLE>
 * 
 * Available Roles:
 *   - MANDI_MANAGER : Can publish benchmark APMC mandi rates and verify lot inspections
 *   - ADMIN         : Full platform administrative access
 *   - FARMER        : Agricultural producer
 *   - VENDOR        : Commercial buyer / food processor
 */

const admin = require('firebase-admin');

if (!admin.apps.length) {
  admin.initializeApp();
}

const args = process.argv.slice(2);
if (args.length < 2) {
  console.log('Usage: node set-user-claims.js <UID> <ROLE>');
  console.log('Example: node set-user-claims.js 4kL89mX0qZ MANDI_MANAGER');
  process.exit(1);
}

const [uid, role] = args;
const allowedRoles = ['MANDI_MANAGER', 'ADMIN', 'FARMER', 'VENDOR'];

if (!allowedRoles.includes(role)) {
  console.error(`Invalid role '${role}'. Allowed roles: ${allowedRoles.join(', ')}`);
  process.exit(1);
}

async function setRoleClaim() {
  try {
    // 1. Set Custom User Claim on Firebase Authentication token
    await admin.auth().setCustomUserClaims(uid, { role: role });
    console.log(`✅ Successfully set custom claim { role: "${role}" } for user: ${uid}`);

    // 2. Also ensure Firestore user profile document stays in sync
    const db = admin.firestore();
    const userRef = db.collection('krishiflow_users').doc(uid);
    await userRef.set({
      role: role,
      updatedAt: admin.firestore.FieldValue.serverTimestamp()
    }, { merge: true });
    console.log(`✅ Synced role "${role}" in Firestore /krishiflow_users/${uid}`);

    console.log('\nNote: The user will need to refresh their ID token (e.g. log out and log back in, or call user.getIdToken(true)) for the new claim to take effect in security rules.');
  } catch (error) {
    console.error('❌ Error setting custom user claim:', error);
    process.exit(1);
  }
}

setRoleClaim();
