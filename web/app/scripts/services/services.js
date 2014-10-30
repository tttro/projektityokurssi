var dataServices = angular.module('dataServices', ['ngResource']);

dataServices.factory('Item', ['$resource', function($resource){
        return $resource('http://lbdbackend.ignorelist.com/locationdata/api/Streetlights/:itemId', {}, {
            query: {method:'GET', params:{itemId:'@id'}, isArray:true}
        });
}]);
