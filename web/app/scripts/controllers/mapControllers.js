var mapControllers = angular.module('mapControllers', []);

mapControllers.controller('mapController', function($scope, $window){


    var defaultPoint = new google.maps.LatLng(61.497978, 23.764931); // Tampere

    // Get users location
    if($window.navigator.geolocation) {
        $window.navigator.geolocation.getCurrentPosition(function(position){
            defaultPoint = new google.maps.LatLng(position.coords.latitude, position.coords.longitude);
        });
    }

    var mapOptions = {
        zoom: 13,
        center: defaultPoint
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

});