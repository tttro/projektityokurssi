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
    $http.defaults.headers.common['LBD_LOGIN_HEADER'] = 'SimoSahkari';
    var dataStorage;
    var config = {
        withCredentials:false,
        cache:true,
        headers:  {'LBD_LOGIN_HEADER' : 'SimoSahkari'}
    };

    return {

        fetchData: function(callback) {
            //$http.defaults.headers.common['LBD_LOGIN_HEADER'] = 'SimoSahkari';
            http://lbdbackend.ignorelist.com/locationdata/api/Streetlights/WFS_KATUVALO.405172
            dataStorage =
                $http.get(baseUrl + 'Streetlights/inarea/?xbottomleft=23.63&ybottomleft=61.51&ytopright=61.52&xtopright=23.65')
                .success(function(data){
                    dataStorage = data;
                    callback(data);
                })
                .error(function(data,status,header,config){
                    console.log("error:streetlight " + data);
                    console.log("error:streetlight " + status);
                    console.log("error:streetlight " + header);
                    console.log("error:streetlight " + config);
                });

            return dataStorage;
        }
    };
}]);
