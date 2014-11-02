var dataServices = angular.module('dataServices', ['ngResource']);

dataServices.factory('Streetlight', ['$resource', function($resource){
        return $resource('http://lbdbackend.ignorelist.com/locationdata/api/Streetlights/:itemId', {}, {
            query: {method:'GET', params:{itemId:'@id'}, isArray:true}
        });
}]);
dataServices.factory('StreetlightAll', ['$resource', function($resource){
    return $resource('http://lbdbackend.ignorelist.com/locationdata/api/Streetlights/:itemId', {}, {});
}]);
dataServices.factory('StreetlightNear', ['$resource', function($resource){
    return $resource('http://lbdbackend.ignorelist.com/locationdata/api/Streetlights/:itemId', {}, {
        query: {method:'GET', params:{itemId:'@id'}, isArray:true}
    });
}]);

dataServices.factory('StreetlightTest', ['$http','$rootScope', function($http, $rootScope){
    var dataStorage;
    return {

        fetchData: function(callback) {
            dataStorage =
                $http.get('http://lbdbackend.ignorelist.com/locationdata/api/Streetlights/near/?latitude=23.643239226767022&longitude=61.519112683582854&range=0.0001',
                    {cache: true})
                .success(function(data){
                    dataStorage = data;
                    callback(data);
                })
                .error(function(){
                    console.log("error:streelight", error);
                });

            return dataStorage;
        }
    };
}]);
