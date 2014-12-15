/* ItemController - items functions */


var itemControllers = angular.module('itemControllers', []);

itemControllers.controller('itemController', function($scope, $http, $rootScope, $filter, $timeout, ObjectsLocal, ObjectsService){


    var orginalItemList = [];
    var geoCoder = new google.maps.Geocoder();

    $scope.address = '';
    $scope.searchQuery = '';
    $scope.selectedItem = null;
    $scope.searchMethod = 'map'

    $scope.items = ObjectsLocal.get();

    /*** Event from service, data is ready
     *  Add markers when all data fetched from server
     * ***/
    $scope.$on('dataIsLoaded', function(e) {
        $scope.items = ObjectsLocal.get();
        orginalItemList = $scope.items;
    });

    $scope.itemSearch = function(searchQuery){
        // MAP
        if( $scope.searchMethod === 'map' )
        {
             if(searchQuery != "" ) {
                $scope.items.features = $filter('filter')($scope.items.features,{$:searchQuery}, false);
             }
             else {
                $scope.items = orginalItemList;
             }
        }
        // ALL
        else
        {
            if(searchQuery.length >= 3){
                ObjectsService.search(searchQuery, function(data){

                    // Check results count
                    var itemCount = 0;
                    for(var i in data.results.features){
                        itemCount++;
                    }

                    if(itemCount > 0) {

                        ObjectsLocal.set(data.results); // Set data to local dataset
                        $scope.items = data.results;
                        $rootScope.$broadcast('searchResultsIsLoaded'); // notify mapController that new items are loaded
                    }
                    else {
                        //TODO: no items found
                    }

                });
            }

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

    $scope.setSelectedValue = function(searchMethod){
        $scope.searchMethod =  searchMethod;
    }

    $scope.checkEmpty = function(searchQuery){
        if(searchQuery.length == 0){
            $scope.items = orginalItemList;
        }
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
