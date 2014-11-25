/* Main application which start up the app */
'use strict';

// Init main application and used modules
var app = angular.module('app',[
    'ngRoute',
    'dataServices',
    'itemControllers',
    'mapControllers',
    'messageControllers',
    'appDirectives'
])
// Constants variables
.constant('appConfig', {
    defaultPosition: [61.51241, 23.634931],
    baseApiUrl: 'http://lbdbackend.ignorelist.com/locationdata/api/',
    dataTypeUrl:'Streetlights',
    headerLogin: {'LBD_LOGIN_HEADER':'SimoSahkari'},
    nearRange: '0.0001',
    inareaRange: 0.7 // 0.1 - 1
})
.config(function($logProvider){
    $logProvider.debugEnabled(true);
});
