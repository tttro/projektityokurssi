var mapControllers = angular.module('mapControllers', []);

mapControllers.controller('mapController', function($scope, $window, StreetlightTest){


    var defaultPoint = new google.maps.LatLng(61.497978, 23.764931); // Tampere

    /*** Get user geolocation from browser ***/
    if($window.navigator.geolocation)
    {
        $window.navigator.geolocation.getCurrentPosition(function(position){
            defaultPoint = new google.maps.LatLng(position.coords.latitude, position.coords.longitude)
        }, function() {
           handleNoGeolocation(true);
        });

    }

    // Init map
    var mapOptions = {
        zoom: 13,
        center: defaultPoint,
        streetViewControl: false
    }

    $scope.map = new google.maps.Map(document.getElementById('map'), mapOptions);

    var markers = [];

    // Default marker
    var defaultMarker = new google.maps.Marker({
        map: $scope.map,
        position: defaultPoint,
        title: 'Item',
        icon: {
            path: google.maps.SymbolPath.CIRCLE,
            fillColor: 'red',
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
        heading: 265,
        pitch: 0
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

    /*** Event from service, data is raedy
     *  Add markers when all data fetched from server
     * ***/
    $scope.$on('dataIsLoaded', function() {
        loadMarkers(StreetlightTest.getData());
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
        //console.log(item.geometry.coordinates[1]);
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

        marker.content = ""; // TODO

        // add click event for marker
        google.maps.event.addListener(marker, 'click', function() {

            $scope.map.setCenter(marker.getPosition());
            var iWindow = new google.maps.InfoWindow();
            iWindow.setContent(marker.content);

            if(openedMarkerWindow != null) {
                openedMarkerWindow.close(); // Close a previous window
            }

            openedMarkerWindow = iWindow;
            iWindow.open($scope.map, marker);

        });

        markers.push(marker); // Add marker into list
    }
});

