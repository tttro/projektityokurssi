/* generalControllers - generalStuff */

var mainController = angular.module('mainController', ['ngRoute']);

mainController.controller('mainController', function($scope, $route, $routeParams, $location, AuthService){

    $scope.$route = $route;
    $scope.$location = $location;
    $scope.$routeParams = $routeParams;

    // When login was successful login ctrl sends an event
    $scope.$on('event:setuser', function(event) {
        $scope.user = AuthService.getUser();
    });


});