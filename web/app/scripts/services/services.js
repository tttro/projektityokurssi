// Global settings
var dataServices = angular.module('dataServices', ['ngResource']);
var baseUrl = 'http://lbdbackend.ignorelist.com/locationdata/api/';
var dataSet = null;

// Get Streetlight-data from near
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

// Get Streetlight data from Inarea
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

// Get TEST data
dataServices.factory('StreetlightTest', ['$http', function($http){
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
                    alert("Sorry, Data Load Failure :(")
                    console.log("error:streetlight " + data);
                    console.log("error:streetlight " + status);
                    console.log("error:streetlight " + header('LBD_LOGIN_HEADER'));
                    console.log("error:streetlight " + config);
                });
        }
    };
}]);

// A local data storage (model) which controllers uses
dataServices.factory('Data', function(){
    return {
        get: function() {
            return dataSet;
        },
        set: function(newDataSet){
            dataSet = newDataSet;
        }
    }
});
