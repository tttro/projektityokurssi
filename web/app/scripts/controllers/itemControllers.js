'use strict';
var itemControllers = angular.module('itemControllers', []);

itemControllers.controller('itemController', function($scope, $http, $rootScope, $timeout, Data){


        /*** Event from service, data is ready
         *  Add markers when all data fetched from server
         * ***/
        $scope.$on('dataIsLoaded', function(e) {
            $scope.loading = true;
            $scope.items = Data.get();
            $scope.loading = false;
        });

        $scope.showMarker = function(markerId){
            $rootScope.$broadcast('showMarker', markerId); // delegate marker id to map
        }

});
