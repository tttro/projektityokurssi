/* ItemController - items functions */


var itemController = angular.module('itemController', []);

itemController.controller('itemController', function($scope, $http, $rootScope, $filter, $timeout, ObjectsLocal, ObjectsService, notify){


    var orginalItemList = [];
    var geoCoder = new google.maps.Geocoder();
    var filter;
    $scope.address = '';
    $scope.searchQuery = '';
    $scope.selectedItem = null;
    $scope.searchMethod = 'all'
    $scope.showSearchResultText = false;
    $scope.searchResultText = '';

    orginalItemList= ObjectsLocal.get();
    $scope.items = orginalItemList

    /*** Event from service, data is ready
     *  Add markers when all data fetched from server
     * ***/
    $scope.$on('dataIsLoaded', function(e) {
        orginalItemList = ObjectsLocal.get();
        $scope.items = orginalItemList;
        $scope.showSearchResultText = false;
        $scope.searchResultText = '';
    });

    $scope.itemSearch = function(searchQuery){

        $scope.showSearchResultText = false;
        $scope.searchResultText = '';

        // MAP
        if( $scope.searchMethod === 'map' )
        {
            $scope.items = getFilteredObjectList(searchQuery);
        }
        // ALL
        else
        {
            if(searchQuery.length >= 3){
                $scope.showLoadingIcon = true;
                ObjectsService.search(searchQuery, function(data){

                    if(data.totalResults > 0) {

                        ObjectsLocal.set(data.results); // Set data to local dataset
                        orginalItemList = data.results;
                        $scope.items = data.results;

                        $scope.showSearchResultText = true;
                        $scope.searchResultText = data.totalResults + ' search results';

                        $rootScope.$broadcast('searchResultsIsLoaded'); // notify mapController that new items are loaded

                    }
                    else {
                        //TODO: no items found
                        $scope.showSearchResultText = true;
                        $scope.searchResultText = 'No results found';
                        $scope.items = [];
                    }

                    $scope.showLoadingIcon = false;

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
        $scope.showSearchResultText = false;
        $scope.searchResultText = '';
    }

    $scope.saveNote = function(item){

        // If metadata does not exist, create empty one
        if(!item.properties.metadata)
        {
            var metadata = {
                info:'',
                status:''
            }
            item.properties['metadata'] = metadata;
        }
        else if(item.properties.metadata && !item.properties.metadata.status)
        {
            item.properties.metadata['status'] = '';
        }

        ObjectsService.put(item);
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
                    notify('No results found');
                    $scope.address = "";
                }
            } else {
                notify('Geocoder failed due to: ' + status);
                $scope.address = '';
            }
        });

        $scope.showLoadingIcon = false;
    }

    function getFilteredObjectList(searchQuery){

        var filteredList = $filter('filter')(orginalItemList.features,{$:searchQuery},false);

        var reStructure = {
            totalFeatures:orginalItemList.totalFeatures,
            type:orginalItemList.type,
            items:{
                features:{
                }
            }
        }

        reStructure.features = filteredList;

        return reStructure;
    }

});
