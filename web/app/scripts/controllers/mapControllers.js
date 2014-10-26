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
        zoom: 15,
        center: defaultPoint
    }

    $scope.map = new google.maps.Map(document.getElementById('map'), mapOptions);
});