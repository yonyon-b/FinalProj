/**
 * Import function triggers from their respective submodules:
 *
 * const {onCall} = require("firebase-functions/v2/https");
 * const {onDocumentWritten} = require("firebase-functions/v2/firestore");
 *
 * See a full list of supported triggers at https://firebase.google.com/docs/functions
 */

const functions = require("firebase-functions/v1");
const admin = require("firebase-admin");
admin.initializeApp();

exports.sendChatNotification = functions.database
  .ref("/chats/{chatId}/messages/{messageId}")
  .onCreate(async (snapshot, context) => {
    const message = snapshot.val();
    const senderId = message.senderId;
    const text = message.text;
    const chatId = context.params.chatId;

    // Determine the receiver's ID by removing the sender's ID from the chatId
    const uids = chatId.split("_");
    const receiverId = uids[0] === senderId ? uids[1] : uids[0];

    // Fetch the receiver's FCM token from the database
    const userSnapshot = await admin.database().ref(`/users/${receiverId}/fcmToken`).once("value");
    const fcmToken = userSnapshot.val();

    if (!fcmToken) {
        console.log("No token for user, cannot send notification.");
        return null;
    }

    // Prepare the FCM Payload
    const payload = {
      token: fcmToken,
      data: {
        senderId: senderId,
        message: text
      }
    };

    // Send the push notification
    try {
      await admin.messaging().send(payload);
      console.log("Notification sent successfully");
    } catch (error) {
      console.error("Error sending notification:", error);
    }
  });

// For cost control, you can set the maximum number of containers that can be
// running at the same time. This helps mitigate the impact of unexpected
// traffic spikes by instead downgrading performance. This limit is a
// per-function limit. You can override the limit for each function using the
// `maxInstances` option in the function's options, e.g.
// `onRequest({ maxInstances: 5 }, (req, res) => { ... })`.
// NOTE: setGlobalOptions does not apply to functions using the v1 API. V1
// functions should each use functions.runWith({ maxInstances: 10 }) instead.
// In the v1 API, each function can only serve one request per container, so
// this will be the maximum concurrent request count.

//setGlobalOptions({ maxInstances: 10 });

// Create and deploy your first functions
// https://firebase.google.com/docs/functions/get-started

// exports.helloWorld = onRequest((request, response) => {
//   logger.info("Hello logs!", {structuredData: true});
//   response.send("Hello from Firebase!");
// });
