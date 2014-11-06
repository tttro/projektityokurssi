var dataServices = angular.module('dataServices', ['ngResource']);
var baseUrl = 'http://lbdbackend.ignorelist.com/locationdata/api/';
dataServices.factory('StreetlightNear', function($http){
    return {
        get: function(lat, lng, callback){
            var retData = null;
            $http.get(baseUrl + 'Streetlights/near/?latitude='+ lat +'&longitude='+ lng +'&range=0.0001')
                .success(function(data){
                    callback(data);
                })
                .error(function(){
                    console.log("error:StreetlightNear", error);
                });
            return retData;
        }
    };
});
dataServices.factory('StreetlightInarea', function($http){
    return {
        get: function(bounds, callback){
            var retData = null;
            var ne = bounds.getNorthEast();
            var sw = bounds.getSouthWest();
            $http.get(baseUrl + 'Streetlights/inarea/?xbottomleft='+ne.lat()+'&ybottomleft=+'+ne.lng()+'+&ytopright='+sw.lat()+'&xtopright='+sw.lat())
                .success(function(data){
                    callback(data);
                })
                .error(function(){
                    console.log("error:StreetlightInarea", error);
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
                $http.get(baseUrl + 'Streetlights/inarea/?xbottomleft=23.63&ybottomleft=61.51&ytopright=61.52&xtopright=23.65',
                    {cache: true})
                .success(function(data){
                    dataStorage = data;
                    callback(data);
                })
                .error(function(){
                    console.log("error:streetlight", error);
                });

            return dataStorage;
        }
    };
}]);
