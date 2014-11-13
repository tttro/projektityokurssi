'use strict';

// Init main application
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
