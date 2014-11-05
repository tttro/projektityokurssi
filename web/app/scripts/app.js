'use strict';

// Init main application
var app = angular.module('app',[
    'ngRoute',
    'dataServices',
    'itemControllers',
    'mapControllers',
    'messageControllers'
]);

// App's route configurations
app.config(['$routeProvider',
    function($routeProvider) {
        $routeProvider.
            when('/', {
                templateUrl: 'views/main.html'
            }).
            /* //TODO
            when('/:targetId', {
                templateUrl: 'foo.html',
                controller: 'jee'
            }).*/
            otherwise({
                redirectTo: '/'
            });
}]);
