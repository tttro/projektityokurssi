WEB-CLIENT
==========
* AngularJS 1.2.28
MIT https://github.com/angular/angular.js/blob/master/LICENSE
* Bootstrap 
3.3.2
MIT https://github.com/twbs/bootstrap/blob/master/LICENSE
* Google Maps JavaScript API v3
 https://developers.google.com/maps/documentation/javascript/usage
* Google Plus Sign-in based on https://github.com/sirkitree/angular-directive.g-signin
* Angular-Notify https://github.com/cgross/angular-notify
* HTML5 Shiv 3.7.2 MIT

Installing
-----------------
Copy app-folder content and change settings into app.js

Settings
-----------------
web/scripts/app.js
```javascript
defaultPosition: [long, lat], // default position when app start up
baseApiUrl: '', // server base url (required)
dataTypeUrl:'locationdata/api/', // data type
dataCollectionUrl:'', // collection type
dataPlaygroundsUrl: '', // playgrounds
googleToken: '', // this for testing
googleId: '', // this for testing
nearRange: '0.0001', // marker range
inareaRange: 0.1, // 0.1 - 1 
defaultZoom: 17, // Google Maps default zoom value
searchLimit: 10, // Max items count
googleClientId:'' // Google Client Id for google SignIn (required)
```
