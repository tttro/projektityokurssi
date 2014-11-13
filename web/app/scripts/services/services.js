var dataServices = angular.module('dataServices', ['ngResource']);
var baseUrl = 'http://lbdbackend.ignorelist.com/locationdata/api/';
var dataSet = null;
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
            //console.log('Streetlights/inarea/?xbottomleft='+sw.lng()+'&ybottomleft='+sw.lat()+'&ytopright='+ne.lat()+'&xtopright='+ne.lng());
            $http.get(baseUrl + 'Streetlights/inarea/?xbottomleft='+sw.lng()+'&ybottomleft='+sw.lat()+'&ytopright='+ne.lat()+'&xtopright='+ne.lng(),
                {headers: {"LBD_LOGIN_HEADER": "SimoSahkari"}}
            )
                .success(function(data){
                    callback(data);
                })
                .error(function(){
                    console.log("error:StreetlightInarea");
                });
            return retData;
        }
    };
});

dataServices.factory('StreetlightTest', ['$http','$rootScope', function($http, $rootScope){

    var dataStorage;
    return {

        fetchData: function(callback) {


                $http({
                    method: 'GET',
                    url: baseUrl + 'Streetlights/inarea/?xbottomleft=23.63&ybottomleft=61.51&ytopright=61.52&xtopright=23.65',
                    //url: 'data/demo.json',
                    headers: {
                        "LBD_LOGIN_HEADER" : "SimoSahkari"
                    }
                })
                .success(function(data){
                    callback(data);
                })
                .error(function(data,status,header,config){
                    console.log("error:streetlight " + data);
                    console.log("error:streetlight " + status);
                    console.log("error:streetlight " + header('LBD_LOGIN_HEADER'));
                    console.log("error:streetlight " + config);
                });
        }
    };
}]);

dataServices.factory('Data', function($rootScope){
    return {
        get: function() {
            return dataSet;
        },
        set: function(newDataSet){
            dataSet = newDataSet;
        }
    }
});
