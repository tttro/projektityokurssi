/* MapController - a map and points functions */

var mapController = angular.module('mapController', []);

mapController.controller('mapController', function($scope, $window,$rootScope, $timeout, ObjectsService, ObjectsLocal, notify, appConfig){

    // Init
    var currentPosition = new google.maps.LatLng(appConfig.defaultPosition[0], appConfig.defaultPosition[1]); // Tampere
    var currentMarker = null;
    var markers = [];
    var infoWindow = new google.maps.InfoWindow();
    var panorama = null;
    var itemPreLoadArea = { sw: null, ne: null }
    var markerIcon = {
        path: google.maps.SymbolPath.CIRCLE,
        fillColor: '#184663',
        fillOpacity: 0.6,
        scale: 5,
        strokeColor: "#184663",
        strokeWeight: 1
    }
    var markerIconHighlight = {
        path: google.maps.SymbolPath.CIRCLE,
        fillColor: 'yellow',
        fillOpacity: 0.6,
        scale: 5,
        strokeColor: 'black',
        strokeWeight: 1
    }
    var streetViewService = new google.maps.StreetViewService();

    $scope.userLocationMarker = null;
    $scope.btnGeolocation = true;


    initializeMap(currentPosition);


    initializeMapButtons();



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

            ObjectsService.getInarea(itemPreLoadArea,function(results) {
                var itemCount = 0;
                for(var i in results.features){
                    itemCount++;
                }
                console.log('Count of items:'+itemCount);

                if(itemCount > 300 ) {
                    if(confirm('Items are more than 300, do you want to load them anyway? '))
                    {
                        ObjectsLocal.set(results);

                        loadMarkers(results);

                        $rootScope.$broadcast('dataIsLoaded');
                    }
                }
                else
                {
                    ObjectsLocal.set(results);

                    loadMarkers(results);

                    $rootScope.$broadcast('dataIsLoaded');
                }


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
        heading: 0,
        pitch: 0,
        zoom : 0
    }));

    // Toggle button for streetview/2D map
    $scope.toggleStreetview = function(){
        var toggle = panorama.getVisible();
        if (toggle == false) {
            panorama.setVisible(true);
            $scope.btnText = "Go back to 2D map";
            $scope.btnGeolocation = false;

            //openedMarkerWindow.close();
            //openedMarkerWindow.open(panorama);

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

    function initializeMap(currentPosition) {

        // Initialize google maps
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

        // Custom property for infowindow which tell is that window open or not
        google.maps.InfoWindow.prototype.isOpen = function(){
            var map = infoWindow.getMap();
            return (map !== null && typeof map !== "undefined");
        }
    }

    function initializeMapButtons() {
        /*** Init google maps events ***/

        var geoButton = document.getElementById('btnGeolocation');

        // Init Search-box
        var input = (document.getElementById('pac-input'));
        var searchBox = new google.maps.places.SearchBox((input));

        // Address search
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
                var map = $scope.map;
                navigator.geolocation.getCurrentPosition(function(position){
                    $scope.showLoadingIcon = true;
                    //console.log(position.coords.latitude+", " +position.coords.longitude);
                    var currentPosition =  new google.maps.LatLng(position.coords.latitude, position.coords.longitude);

                    createGeoMarker(currentPosition);
                    map.setZoom(appConfig.defaultZoom);
                    map.setCenter(currentPosition);
                    $scope.map = map;
                    $scope.showLoadingIcon = false;

                }, function() {
                    handleNoGeolocation(true);
                });

            }
        });

        // Infowindow close-click
        google.maps.event.addListener(infoWindow,'closeclick',function(){
            currentMarker.setIcon(markerIcon);
        });

    }

    // An error handler for user's geolocation button
    var handleNoGeolocation = function (error) {
        if(error) {
            notify("Geolocation service failed.");
        }
        else {
            notify("Your browser doesn't support geolocation.");
        }
    }

    var loadMarkers =  function (data) {

        var objectsFeatures = data.features; // Select features from data

        clearMarkers(markers); // Clear markers from the map

        // Loop through each objects and make it a marker
        for(var i in objectsFeatures){
            createMarker(objectsFeatures[i]);
        }

    }

    var openedMarkerWindow = null;
    var createMarker = function (item) {

        var marker = new google.maps.Marker({
            map: $scope.map,
            position: new google.maps.LatLng(
                item.geometry.coordinates[1], // Lat
                item.geometry.coordinates[0]), // Lng
            title: item.id,
            icon: markerIcon
        });

        var markerDetails = "";
        var itemProperties = item.properties;

        // Go through all item's properties and put them into table and show on an infowindow
        for(key in itemProperties){

            if(key != 'metadata'){
                markerDetails += "<tr>" +
                "<th>"+key+"</th>" +
                "<td>"+itemProperties[key]+"</td>" +
                "</tr>";
            }
        }

        marker.content =
            "<div class='infowindow'>" +
            "<h4>" + item.id + "</h4>"+
            "<table class='markerDetails'>" + markerDetails + "</table>" +
            "</div>";



        // add click event for marker
        google.maps.event.addListener(marker, 'click', function() {


            $scope.map.setCenter(marker.getPosition());
            panorama.setPosition(marker.getPosition());
            setPov(marker);

            highlightMarker(marker);

            if(openedMarkerWindow != null)// Close a previous window
            {
                openedMarkerWindow.close();
            }

            if ($scope.map.getStreetView().getVisible()) {
                infoWindow.setContent(marker.content);
                infoWindow.open(panorama, this);

            } else {
                infoWindow.setContent(marker.content);
                infoWindow.open($scope.map, this);
                $scope.map.setZoom(17);
            }

            openedMarkerWindow = infoWindow;

        });

        currentMarker = marker;
        markers[item.id] = marker; // Add marker into list
    }

    function highlightMarker(marker){

        // Set all to default state
        for(var i in markers){
            markers[i].setIcon(markerIcon);
        }

        marker.setIcon(markerIconHighlight); // Highlight clicked icon

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
                fillColor: 'lightblue',
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

    function setPov(marker) {

        var lookTo = marker.getPosition();

        streetViewService.getPanoramaByLocation(panorama.getPosition(), 50, function(panoData) {
            // if the function returned a result
            if (panoData != null) {
                // the GPS coordinates of the streetview camera position
                var panoCenter = panoData.location.latLng;
                // this is where the magic happens!
                // the "computeHeading" function calculates the heading with the two GPS coordinates entered as parameters
                var heading = google.maps.geometry.spherical.computeHeading(panoCenter, lookTo);
                // now we know the heading (camera direction, elevation, zoom, etc) set this as parameters to the panorama object
                var pov = panorama.getPov();
                pov.heading = heading;
                pov.zoom = 0;
                pov.pitch = +10;
                panorama.setPov(pov);


            }
            else {
                notify("StreetView is not available at this marker");
            }
        });
    }

    $scope.$on('searchResultsIsLoaded', function(e) {
        var searchResults = ObjectsLocal.get();
        loadMarkers(searchResults);

    });


});