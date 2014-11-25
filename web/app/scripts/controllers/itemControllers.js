/* ItemController - items functions */


var itemControllers = angular.module('itemControllers', []);

itemControllers.controller('itemController', function($scope, $http, $rootScope, $filter, $timeout, Data){

        $scope.searchQuery = '';
        $scope.items = Data.get();
        var orginalItemList = [];

        /*** Event from service, data is ready
         *  Add markers when all data fetched from server
         * ***/
        $scope.$on('dataIsLoaded', function(e) {
            $timeout(function() {
                $scope.items = Data.get();
                orginalItemList = Data.get();
            }, 100)

        });

        $scope.itemSearch = function(searchQuery){
            console.log(searchQuery);
            if(searchQuery != "" ) {
                $scope.items.features = $filter('filter')($scope.items.features,{$:searchQuery}, false);
            }
            else {
                $scope.items = orginalItemList;
            }
        }

        $scope.showMarker = function(markerId){
            $rootScope.$broadcast('showMarker', markerId); // delegate marker id to map
        }
});
