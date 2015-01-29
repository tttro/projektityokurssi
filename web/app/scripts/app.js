/* Main application which start up the app and app settings*/
'use strict';

// Init main application and used modules
var app = angular.module('app',[
    'ngRoute',
    'ngSanitize',
    'mainController',
    'dataServices',
    'cgNotify',
    'itemController',
    'mapController',
    'messageController',
    'loginController',
    'settingsController',
    'appDirectives'
])
// Configs
.constant('appConfig', {
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
})
.config(function( $httpProvider, $routeProvider, $locationProvider, appConfig ){

    $routeProvider
        .when('/', {
            templateUrl: 'views/main.html'
        })
        .when('/login', {
            templateUrl: 'views/login.html',
            controller: 'loginController'
        })
        .when('/settings', {
            templateUrl: 'views/settings.html',
            controller: 'settingsController'
        })
        .otherwise({
            redirectTo: '/'
        });

});

// When the app loads
app.run(function($location, AuthService){

    if(!AuthService.isLoggedIn()){
        $location.path('/login');
    }

});
