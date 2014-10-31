'use strict';
var itemControllers = angular.module('itemControllers', []);

itemControllers.controller('itemController', function($scope, $http, StreetlightTest){


    $scope.items = StreetlightTest.getData(); // Get local data

    if (!$scope.items) {
        $scope.loading = true;
        StreetlightTest.fetchData(function(results) {
            $scope.items = results;
            $scope.loading = false;
        });
    }



});
