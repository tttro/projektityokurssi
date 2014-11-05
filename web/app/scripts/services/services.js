var dataServices = angular.module('dataServices', ['ngResource']);
var baseUrl = 'http://lbdbackend.ignorelist.com/locationdata/api/';
dataServices.factory('StreetlightNear', function($http){
    return {
        get: function(lat, lng){
            var retData = null;
            $http.get(baseUrl + 'Streetlights/near/?latitude='+ lat +'&longitude='+ lng +'&range=0.0001')
                .success(function(data){
                    retData = data;
                })
                .error(function(){
                    console.log("error:StreetlightNear", error);
                });
            return retData;
        }
    };
});

dataServices.factory('StreetlightTest', ['$http','$rootScope', function($http, $rootScope){
    var dataStorage;
    return {

        fetchData: function(callback) {
            dataStorage =
                $http.get('data/demo.json',
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
