# Firebase setup (Ranking)

## 1) Security first

If a Firebase Admin SDK key was exposed, revoke it immediately in Firebase Console:

1. Go to `Project settings` -> `Service accounts`.
2. Find the leaked key and delete/revoke it.
3. Generate a new key only for backend usage.

Never put Admin SDK private keys inside Android app code or resources.

## 2) Android client setup (this app)

1. In Firebase Console, open `Project settings` -> `General`.
2. In `Your apps`, select/add Android app with package name `dev.podatek.chemia`.
3. Download `google-services.json`.
4. Copy the file to: `app/google-services.json`.
5. Sync Gradle and build the app.

## 3) Firestore structure used by app

Collection: `ranking`

Document ID: `<playerId>` (stable hash from Android ID)

Fields:
- `playerId` (string)
- `playerName` (string, e.g. `Gracz-001`)
- `totalPoints` (number)
- `quizzesPlayed` (number)
- `bestPercentage` (number)
- `updatedAt` (number, epoch millis)

## 4) Firestore index

Create composite index for query ordering:
- `totalPoints` DESC
- `updatedAt` ASC

The app uses this order in ranking fetch.

## 5) Firestore rules (starter)

Use `firestore.rules` from this repository as a base.
Adjust if you later add Firebase Authentication.

