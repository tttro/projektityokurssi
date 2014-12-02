/* ItemController - items functions */


var itemControllers = angular.module('itemControllers', []);

itemControllers.controller('itemController', function($scope, $http, $rootScope, $filter, $timeout, ObjectsLocal){

        $scope.searchQuery = '';

        var orginalItemList = [];
        $scope.items = ObjectsLocal.get();

        /*** Event from service, data is ready
         *  Add markers when all data fetched from server
         * ***/
        $scope.$on('dataIsLoaded', function(e) {
            $scope.items = ObjectsLocal.get();
            orginalItemList = ObjectsLocal.get();
        });

        $scope.itemSearch = function(searchQuery){

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
