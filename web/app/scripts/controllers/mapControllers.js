var mapControllers = angular.module('mapControllers', []);

mapControllers.controller('mapController', function($scope, $window, StreetlightTest){

    var defaultPoint = new google.maps.LatLng(61.51241, 23.634931); // Tampere

    // Init map
    var mapOptions = {
        zoom: 15,
        center: defaultPoint,
        streetViewControl: false, zoomControl: true,
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


    /*** Init google maps events ***/

    var geoButton = document.getElementById('btnGeolocation');
    google.maps.event.addDomListener(geoButton, 'click', function(){

        if(navigator.geolocation)
        {

            navigator.geolocation.getCurrentPosition(function(position){
                var currentPosition =  new google.maps.LatLng(position.coords.latitude, position.coords.longitude);
                $scope.map.setCenter(currentPosition);

            }, function() {
                handleNoGeolocation(true);
            });

        }
    });

    google.maps.event.addListener($scope.map, 'idle', function(){
        $scope.loading = false;
    });



    $scope.markers = [];

    // Default marker
    var defaultMarker = new google.maps.Marker({
        map: $scope.map,
        position: defaultPoint,
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

    /*** StreetView ***/

    var panorama;
    $scope.btnText = "Streetview";

    panorama = $scope.map.getStreetView();
    panorama.setPosition(defaultPoint);
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
            $scope.btnText = "2D map";
        } else {
            panorama.setVisible(false);
            $scope.btnText = "StreetView";
        }

    }

    /*** Event from service, data is ready
     *  Add markers when all data fetched from server
     * ***/
    $scope.$on('dataIsLoaded', function() {
        var data;
        StreetlightTest.fetchData(function(results) {
            loadMarkers(results);
        });

    });


    $scope.$on('showMarker',function(event, data){
        var markerId = data;
        var marker = $scope.markers[markerId];
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

        angular.forEach(data.features, function(value,key){
            //console.log(value.id + " / " + value.geometry.coordinates[1]);
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

        marker.content = "<div class='infowindow'><h3>"+item.id+"</h3><p>"+ item.properties.NIMI + "<br>" + item.properties.LAMPPU_TYYPPI_KOODI + "<br>" + item.properties.LAMPPU_TYYPPI+"</p></div>"; // TODO

        // add click event for marker
        google.maps.event.addListener(marker, 'click', function() {

            $scope.map.setCenter(marker.getPosition());
            var iWindow = new google.maps.InfoWindow();

            if(openedMarkerWindow != null)// Close a previous window
            {
                openedMarkerWindow.close();
            }
            if ($scope.map.getStreetView().getVisible()) {
                iWindow.setContent(marker.content);
                iWindow.open($scope.map.getStreetView(), this);
            } else {
                iWindow.setContent(marker.content);
                iWindow.open($scope.map, this);
            }

            openedMarkerWindow = iWindow;

        });

        $scope.markers[item.id] = marker; // Add marker into list
    }


});