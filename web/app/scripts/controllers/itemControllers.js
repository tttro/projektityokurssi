'use strict';
var itemControllers = angular.module('itemControllers', []);

itemControllers.controller('itemController', function($scope, $http, $rootScope, StreetlightTest){

    //$scope.items = StreetlightTest.getData(); // Get local data


        $scope.loading = true;
        StreetlightTest.fetchData(function(results) {
            $scope.items = results;
            $scope.loading = false;
            $rootScope.$broadcast('dataIsLoaded');
        });

        $scope.showMarker = function(markerId){
            $rootScope.$broadcast('showMarker', markerId); // delegate marker id to map
        }

});
