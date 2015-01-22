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
    defaultPosition: [61.51241, 23.634931],
    baseApiUrl: 'https://lbdbackend.ignorelist.com/',
    dataTypeUrl:'locationdata/api/',
    dataCollectionUrl:'Streetlights',
    dataPlaygroundsUrl: 'https://lbdbackend.ignorelist.com/locationdata/api/',
    googleToken: 'ABCDEFG',
    googleId: '',
    nearRange: '0.0001',
    inareaRange: 0.1, // 0.1 - 1
    defaultZoom: 17,
    searchLimit: 10, // Result max count
    googleClientId:'388919682787-3ckv87e4aibqnt788a4omio7d3j3665e.apps.googleusercontent.com'
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