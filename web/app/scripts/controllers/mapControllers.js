var mapControllers = angular.module('mapControllers', []);

mapControllers.controller('mapController', function($scope, $window, StreetlightTest){


    var defaultPoint = new google.maps.LatLng(61.497978, 23.764931); // Tampere

    if($window.navigator.geolocation)
    {
        $window.navigator.geolocation.getCurrentPosition(function(position){
            defaultPoint = new google.maps.LatLng(position.coords.latitude, position.coords.longitude)
        }, function() {
           handleNoGeolocation(true);
        });

    }

    var mapOptions = {
        zoom: 13,
        center: defaultPoint,
        streetViewControl: false
    }

    $scope.map = new google.maps.Map(document.getElementById('map'), mapOptions);

    var marker = new google.maps.Marker({
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

    var panorama;
    $scope.btnText = "Streetview";

    panorama = $scope.map.getStreetView();
    panorama.setPosition(defaultPoint);
    panorama.setPov(/** @type {google.maps.StreetViewPov} */({
        heading: 265,
        pitch: 0
    }));

    $scope.toggleStreetview = function(item, event){
        var toggle = panorama.getVisible();
        if (toggle == false) {
            panorama.setVisible(true);
            $scope.btnText = "2D map";
        } else {
            panorama.setVisible(false);
            $scope.btnText = "Streetview";
        }

    }

    function handleNoGeolocation(error) {
        if(error) {
            alert("Geolocation service failed.");
        }
        else {
            alert("Your browser doesn't support geolocation.");
        }
    }
});

