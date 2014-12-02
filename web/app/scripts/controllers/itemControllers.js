/* ItemController - items functions */


var itemControllers = angular.module('itemControllers', []);

itemControllers.controller('itemController', function($scope, $http, $rootScope, $filter, $timeout, ObjectsLocal){


    var orginalItemList = [];
    var geoCoder = new google.maps.Geocoder();

    $scope.address = '';
    $scope.searchQuery = '';
    $scope.selectedItem = null;

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


    /* Accordion */
    $scope.click = function(item){


        if($scope.selectedItem === item){
            $scope.selectedItem = null;
        }
        else {
            $scope.selectedItem = item;
            $scope.address = "";
            $scope.showLoadingIcon = true;
            getAddressByCoords(item);
        }


    }

    $scope.isSelected = function(item){

        return $scope.selectedItem === item;
    }

    function getAddressByCoords(item){

        var latlng = new google.maps.LatLng(
            item.geometry.coordinates[1], // Lat
            item.geometry.coordinates[0]); // Lng

        geoCoder.geocode({'latLng': latlng}, function(results, status) {
            if (status == google.maps.GeocoderStatus.OK) {

                if (results[1]) {


                    $scope.$apply(function(){ //let angular know the changes

                        $scope.address = results[0].formatted_address;
                    });

                } else {
                    alert('No results found');
                    $scope.address = "";
                }
            } else {
                alert('Geocoder failed due to: ' + status);
                $scope.address = '';
            }
        });

        $scope.showLoadingIcon = false;
    }

});
