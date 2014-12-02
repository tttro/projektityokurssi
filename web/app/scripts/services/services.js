// Global settings
var dataServices = angular.module('dataServices', ['ngResource']);
var dataSet = null;

dataServices.factory('ObjectsService', function($http, appConfig){
    return {

        getSingle : function(itemId, callback){
            //TODO fetch single data
        },
        getNear: function(lat, lng, callback){
            var retData = null;

            $http({
                method: 'GET',
                url: appConfig.baseApiUrl + appConfig.dataTypeUrl +'/near/?latitude='+ lat+ +'&longitude='+ lng +'&range='+appConfig.nearRange,
                headers: appConfig.headerLogin
            })
            .success(function(data){
                callback(data);
            })
            .error(function(data,status,header,config){
                errorHandler(data,status,header,config,appConfig.dataTypeUrl);
            });

            return retData;
        },
        getInarea: function(bounds, callback){
            var retData = null;
            var ne = bounds.ne;
            var sw = bounds.sw;

            $http({
                method: 'GET',
                url: appConfig.baseApiUrl + appConfig.dataTypeUrl +'/inarea/?xbottomleft='+sw.lng()+'&ybottomleft='+sw.lat()+'&ytopright='+ne.lat()+'&xtopright='+ne.lng(),
                headers: appConfig.headerLogin

            })
            .success(function(data){
                callback(data);
            })
            .error(function(data,status,header,config){
                errorHandler(data,status,header,config,appConfig.dataTypeUrl);
            });

            return retData;
        },

        getTestData: function(callback) {

            $http({
                method: 'GET',
                url: appConfig.baseApiUrl + appConfig.dataTypeUrl + '/inarea/?xbottomleft=23.63&ybottomleft=61.51&ytopright=61.52&xtopright=23.65',
                //url: 'data/demo.json',
                headers: appConfig.headerLogin
            })
            .success(function(data){
                callback(data);
            })
            .error(function(data,status,header,config){
                errorHandler(data,status,header,config,appConfig.dataTypeUrl);
            });
        }
    };
});

// A local data storage (model) which controllers uses
dataServices.factory('ObjectsLocal', function(){
    return {
        get: function() {
            return dataSet;
        },
        set: function(newDataSet){
            dataSet = newDataSet;
        }
    }
});

// Message service
dataServices.factory('MessageDataService', function(){
    return {
        get: function() {

        },
        set: function(){

        }
    }
});

var errorHandler = function(data,status,header,config,dataTypeUrl) {
    alert("Sorry, Data Load Failure");
    console.log("Data Load Failure: "+dataTypeUrl +" " + status);
}
