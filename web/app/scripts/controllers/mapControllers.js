/* MapController - a map and points functions */

var mapControllers = angular.module('mapControllers', []);

mapControllers.controller('mapController', function($scope, $window,$rootScope, $timeout, ItemDataService, Data, appConfig){

    // Init
    var currentPosition = new google.maps.LatLng(appConfig.defaultPosition[0], appConfig.defaultPosition[1]); // Tampere
    var markerClusterer = null;
    var markers = [];
    var currentBounds = null;
    var infoWindow = new google.maps.InfoWindow();
    var panorama = null;
    var itemPreLoadArea = {
        sw:null,
        ne:null
    }

    var geoCoder = new google.maps.Geocoder();

    $scope.userLocationMarker = null;
    $scope.btnGeolocation = true;


    // Init map
    var mapOptions = {
        zoom: appConfig.defaultZoom,
        center: currentPosition,
        streetViewControl: false,
        zoomControl: true,
        zoomControlOptions: {
            style: google.maps.ZoomControlStyle.LARGE,
            position: google.maps.ControlPosition.TOP_RIGHT
        },
        panControl: true,
        panControlOptions: {
            position: google.maps.ControlPosition.TOP_RIGHT
        }

    }

    // Set map
    $scope.map = new google.maps.Map(document.getElementById('map'), mapOptions);

    /*** Init google maps events ***/

    var geoButton = document.getElementById('btnGeolocation');

    // Init Search-box
    var input = /** @type {HTMLInputElement} */(
        document.getElementById('pac-input'));
    var searchBox = new google.maps.places.SearchBox(
        /** @type {HTMLInputElement} */(input));

    // [START region_getplaces]
    // Listen for the event fired when the user selects an item from the
    // pick list.
    google.maps.event.addListener(searchBox, 'places_changed', function() {
        var places = searchBox.getPlaces();

        if (places.length == 0) {
            return;
        }

        var bounds = new google.maps.LatLngBounds();

        for (var i = 0, place; place = places[i]; i++) {
            bounds.extend(place.geometry.location);

        }

        $scope.map.fitBounds(bounds);
    });

    // Add tool-buttons on map
    $scope.map.controls[google.maps.ControlPosition.TOP_LEFT].push(btnGeolocation);
    $scope.map.controls[google.maps.ControlPosition.TOP_LEFT].push(input);

    // Geolocation button event-handler
    google.maps.event.addDomListener(geoButton, 'click', function(){

       if(navigator.geolocation)
        {

            navigator.geolocation.getCurrentPosition(function(position){
                $scope.showLoadingIcon = true;
                console.log(position.coords.latitude+", " +position.coords.longitude);
                var currentPosition =  new google.maps.LatLng(position.coords.latitude, position.coords.longitude);

                createGeoMarker(currentPosition);
                $scope.map.setZoom(appConfig.defaultZoom);
                $scope.map.setCenter(currentPosition);

                $scope.showLoadingIcon = false;

                }, function() {
                handleNoGeolocation(true);
            });

        }
    });

    // Event handler when Maps is loaded and ready
    function calculateItemLoadArea(currentBounds) {
        var curNe = currentBounds.getNorthEast(); // bottom left
        var curSw = currentBounds.getSouthWest(); // top right

        var viewportWidth = (curSw.lat() - curNe.lat()) * appConfig.inareaRange;
        var viewportHeight = (curNe.lng() - curSw.lng()) * appConfig.inareaRange;

        // Add points over viewport
        var newSwLat = curSw.lat() + viewportWidth;
        var newSwLng = curSw.lng() - viewportHeight;
        var newNeLat = curNe.lat() - viewportWidth;
        var newNeLng = curNe.lng() + viewportHeight;

        itemPreLoadArea.sw =  new google.maps.LatLng(newSwLat, newSwLng);
        itemPreLoadArea.ne = new google.maps.LatLng(newNeLat, newNeLng);

    }


    google.maps.event.addListener($scope.map, 'idle', function(){

        var viewportBounds = $scope.map.getBounds();
        var zoomLevel = $scope.map.getZoom();

        if(zoomLevel >= appConfig.defaultZoom && compareBounds(itemPreLoadArea,viewportBounds)
            && !panorama.getVisible() && !$scope.showLoadingIcon && !infoWindow.isOpen())
        {

            $scope.showLoadingIcon = true;

            calculateItemLoadArea(viewportBounds);

            ItemDataService.getInarea(itemPreLoadArea,function(results) {
                Data.set(results);

                loadMarkers(results);

                $rootScope.$broadcast('dataIsLoaded');
                $scope.showLoadingIcon = false;

            });

        }
    });


    /*** StreetView ***/


    $scope.btnText = "Go to StreetView";

    panorama = $scope.map.getStreetView();
    panorama.setPosition(currentPosition);
    panorama.setOptions({ enableCloseButton: false });
    panorama.setPov(/** @type {google.maps.StreetViewPov} */({
        heading: 40,
        pitch: -5,
        zoom : 0
    }));

    // Toggle button for streetview/2D map
    $scope.toggleStreetview = function(item, event){
        var toggle = panorama.getVisible();
        if (toggle == false) {
            panorama.setVisible(true);
            $scope.btnText = "Go back to 2D map";
            $scope.btnGeolocation = false;
            openedMarkerWindow.open(panorama);
        } else {
            panorama.setVisible(false);
            $scope.btnText = "Go to StreetView";
            $scope.btnGeolocation = true;
        }

    }

    // Event handler for item-list click
    $scope.$on('showMarker',function(event, data){
        var markerId = data;
        var marker = markers[markerId];
        google.maps.event.trigger(marker, 'click');
    });


    /*** Helper functions ***/
    var handleNoGeolocation = function (error) {
        if(error) {
            alert("Geolocation service failed.");
        }
        else {
            alert("Your browser doesn't support geolocation.");
        }
    }

    var loadMarkers =  function (data) {

        clearMarkers(markers);

        angular.forEach(data.features, function(value,key){
            createMarker(value);
        });

    }

    var openedMarkerWindow = null;
    var createMarker = function (item) {

        var marker = new google.maps.Marker({
            map: $scope.map,
            position: new google.maps.LatLng(
                item.geometry.coordinates[1], // Lat
                item.geometry.coordinates[0]), // Lng
            title: item.id,
            icon: {
                path: google.maps.SymbolPath.CIRCLE,
                fillColor: 'red',
                fillOpacity: 0.6,
                scale: 5,
                strokeColor: 'black',
                strokeWeight: 1
            }
        });

        var markerDetails = "";
        var itemProperties = item.properties;

        // Go through all item's properties and put them into table for an infowindow
        for(key in itemProperties){

            if(key != 'metadata'){
                markerDetails += "<tr><th>"+key+"</th><td>"+itemProperties[key]+"</td></tr>";
            }
        }

        marker.content = "<div class='infowindow'>" +
            "<h4>"+item.id+"</h4>"+
        "<table class='markerDetails'>"+markerDetails+"</table></div>";



        // add click event for marker
        google.maps.event.addListener(marker, 'click', function() {


            $scope.map.setCenter(marker.getPosition());
            panorama.setPosition(marker.getPosition());

            if(openedMarkerWindow != null)// Close a previous window
            {
                openedMarkerWindow.close();
            }
            if ($scope.map.getStreetView().getVisible()) {
                infoWindow.setContent(marker.content);
                infoWindow.open($scope.map.getStreetView(), this);
            } else {
                infoWindow.setContent(marker.content);
                infoWindow.open($scope.map, this);
                $scope.map.setZoom(17);
            }

            openedMarkerWindow = infoWindow;

        });

        markers[item.id] = marker; // Add marker into list
    }

    function createGeoMarker(gPoint){
        if($scope.userLocationMarker != null) {
            $scope.userLocationMarker.setMap(null);
        }
        var marker = new google.maps.Marker({
            map: $scope.map,
            position: gPoint,
            title: 'Item',
            icon: {
                path: google.maps.SymbolPath.CIRCLE,
                fillColor: 'blue',
                fillOpacity: 0.6,
                scale: 7,
                strokeColor: 'black',
                strokeWeight: 1
            }
        });
        $scope.userLocationMarker = marker;
    }
    var clearMarkers = function(markers){
        for (var i in markers) {
            markers[i].setMap(null);
        }
        markers = [];
    }

    /* Check is an user inside same map-rectangle */
    var compareBounds = function(itemPreLoadArea, newBounds){

        if(itemPreLoadArea.ne == null || itemPreLoadArea.sw == null){
            return true;
        }
        var curNe = itemPreLoadArea.ne;
        var newNe = newBounds.getNorthEast();
        var curSw = itemPreLoadArea.sw;
        var newSw = newBounds.getSouthWest();

        if(curSw.lng() < newSw.lng() && curSw.lat() < newSw.lat() &&
            curNe.lng() > newNe.lng() && curNe.lat() > newNe.lat()) {
            return false;
        }
        return true;

    }

    function getAddressByCoords(latlng){
        geoCoder.geocode({'latLng': latlng}, function(results, status) {
            if (status == google.maps.GeocoderStatus.OK) {

                if (results[1]) {
                   return results[0].formatted_address;

                } else {
                    alert('No results found');
                    return "";
                }
            } else {
                alert('Geocoder failed due to: ' + status);
            }
        });
    }

    google.maps.InfoWindow.prototype.isOpen = function(){
        var map = infoWindow.getMap();
        return (map !== null && typeof map !== "undefined");
    }

});