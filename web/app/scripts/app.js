/* Main application which start up the app */
'use strict';

// Init main application and modules
var app = angular.module('app',[
    'ngRoute',
    'dataServices',
    'itemControllers',
    'mapControllers',
    'messageControllers',
    'appDirectives'
]);

app.config(function($logProvider){
    $logProvider.debugEnabled(true);
});
