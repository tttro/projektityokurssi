var mapControllers = angular.module('mapControllers', []);

mapControllers.controller('mapController', function($scope, $window, StreetlightTest, StreetlightNear){

    var defaultPoint = new google.maps.LatLng(61.51241, 23.634931); // Tampere
    $scope.userLocationMarker = null;
    $scope.btnGeolocation = true;
    var markerClusterer = null;
    var markers = [];

    // Init map
    var mapOptions = {
        zoom: 15,
        center: defaultPoint,
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

    $scope.map = new google.maps.Map(document.getElementById('map'), mapOptions);
    var infoWindow = new google.maps.InfoWindow();

    /*** Init google maps events ***/

    var geoButton = document.getElementById('btnGeolocation');
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
    $scope.map.controls[google.maps.ControlPosition.TOP_LEFT].push(btnGeolocation);
    $scope.map.controls[google.maps.ControlPosition.TOP_LEFT].push(input);

    google.maps.event.addDomListener(geoButton, 'click', function(){

       if(navigator.geolocation)
        {

            navigator.geolocation.getCurrentPosition(function(position){
                $scope.loading = true;
                console.log(position.coords.latitude+", " +position.coords.longitude);
                var currentPosition =  new google.maps.LatLng(position.coords.latitude, position.coords.longitude);

                createGeoMarker(currentPosition);

                $scope.map.setCenter(currentPosition);

                $scope.loading = false;

            }, function() {
                handleNoGeolocation(true);
            });

        }
    });

    google.maps.event.addListener($scope.map, 'idle', function(){
        $scope.loading = false;
        var bounds = $scope.map.getBounds();
        var zoomLevel = $scope.map.getZoom();
        //console.log("bounds:"+bounds+" zoom:"+zoomLevel);
        if(zoomLevel > 16)
        {
            /*var dummyMarkerCount = 200;
            var dummyMarkersByBound = [];
            for(var i = 0; i<dummyMarkerCount; i++)
            {
                dummyMarkersByBound[i] = getRandom_marker(bounds);
            }*/

            markers = [];
            StreetlightInarea.get(bounds,function(results) {
                markers  = results;

                loadMarkers(markers);

            });


        }
    });

    createGeoMarker(defaultPoint);




    /*** StreetView ***/

    var panorama;
    $scope.btnText = "Streetview";

    panorama = $scope.map.getStreetView();
    panorama.setPosition(defaultPoint);
    panorama.setOptions({ enableCloseButton: false });
    panorama.setPov(/** @type {google.maps.StreetViewPov} */({
        heading: 50,
        pitch: 0,
        zoom : 0
    }));

    // Toggle button for streetview/2D map
    $scope.toggleStreetview = function(item, event){
        var toggle = panorama.getVisible();
        if (toggle == false) {
            panorama.setVisible(true);
            $scope.btnText = "Go back to 2D map";
            $scope.btnGeolocation = false;
        } else {
            panorama.setVisible(false);
            $scope.btnText = "Go to StreetView";
            $scope.btnGeolocation = true;
        }

    }

    /*** Event from service, data is ready
     *  Add markers when all data fetched from server
     * ***/
    $scope.$on('dataIsLoaded', function(e) {

        StreetlightTest.fetchData(function(results) {
            loadMarkers(results);
        });

    });


    $scope.$on('showMarker',function(event, data){
        var markerId = data;
        var marker = markers[markerId];
        google.maps.event.trigger(marker, 'click');
        panorama.setPosition(marker.getPosition());
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
        markers = [];
        if (markerClusterer) {
            markerClusterer.clearMarkers();
        }
        angular.forEach(data.features, function(value,key){
            createMarker(value);
        });
        markerClusterer = new MarkerClusterer($scope.map, markers); // Create clusterers

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

        marker.content = "<div class='infowindow'><h3>"+item.id+"</h3><p>"+ item.properties.NIMI + "<br>" + item.properties.LAMPPU_TYYPPI_KOODI + "<br>" + item.properties.LAMPPU_TYYPPI+"</p></div>"; // TODO

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
    function clearMarkers(markers){
        for (var i in markers) {
            markers[i].setMap(null);
            console.log(markers[i]);
        }
        markerClusterer.clearMarkers();
    }

    function getRandom_marker(bounds) {
        var lat_min = bounds.getSouthWest().lat(),
            lat_range = bounds.getNorthEast().lat() - lat_min,
            lng_min = bounds.getSouthWest().lng(),
            lng_range = bounds.getNorthEast().lng() - lng_min;

        var marker = new google.maps.Marker({
            map: $scope.map,
            position: new google.maps.LatLng(lat_min + (Math.random() * lat_range),
                lng_min + (Math.random() * lng_range)),
            title: 'Item',
            icon: {
                path: google.maps.SymbolPath.CIRCLE,
                fillColor: 'red',
                fillOpacity: 0.6,
                scale: 5,
                strokeColor: 'black',
                strokeWeight: 1
            }
        });
        marker.content = "testmarker";
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
            }

            openedMarkerWindow = infoWindow;

        });

        return marker;
    }
});