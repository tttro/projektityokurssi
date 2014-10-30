'use strict';
var itemControllers = angular.module('itemControllers', []);

itemControllers.controller('itemController', function($scope, $http, $log){
    $scope.loading = true;
    $http.get('http://lbdbackend.ignorelist.com/locationdata/api/Streetlights/near/?latitude=23.643239226767022&longitude=61.519112683582854&range=0.0001')
        .success(function(data){
            $scope.items = data;
            console.log(data);
            $scope.loading = false;
        }).error(function(data, status){
            console.log('Error loading data: itemController');
        });
});
