# F-TEAM
*Application developed for the Embedded Systems course in Computer Engineering year 2019/2020*

### Compiling

Compiling this source code requires adding your SHA-1 digital signature to our firebase project if you want to use Google Sign In. In order to do this,
sign in with these account credentials:<br>

email: embededprog@gmail.com (yes there are no typos)<br>
password: androidst1

and click on https://console.firebase.google.com/u/1/project/findsoccerplayers. Then go to "project settings" and add your digital fingerprint obtained from Android Studio
(double click on signingReport at usually <i> Gradle -> FTEAM -> Tasks -> android -> signingReport</i>)

### Requirements
Minimum OS: Android 7.0 (API 24)

### Usage
You can register either using our application, or log in directly with Google (provided either you installed the application from any APK or you did the compiling step above); never do both since our current release does not ask you to confirm your e-mail: hence, if you register successfully with someone else's google mail, and later this user logs in with google, you won't be able to use that account for security reasons.
Our application will instantly ask you to set a preferred position and distance to search for available matches (these can be changed from the settings later).
Log out is available from the settings, and for the current realease, is the only way to turn off notifications of new messages.
