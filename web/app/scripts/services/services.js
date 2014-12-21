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
                url: appConfig.baseApiUrl + appConfig.dataTypeUrl + appConfig.dataCollectionUrl +'/near/?latitude='+ lat+ +'&longitude='+ lng +'&range='+appConfig.nearRange
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
                    url: appConfig.baseApiUrl + appConfig.dataTypeUrl + appConfig.dataCollectionUrl +'/inarea/?xbottomleft='+sw.lng()+'&ybottomleft='+sw.lat()+'&ytopright='+ne.lat()+'&xtopright='+ne.lng()
                })
                .success(function(data){
                    callback(data);
                })
                .error(function(data,status,header,config){
                    errorHandler(data,status,header,config,appConfig.dataTypeUrl);
                });

            return retData;
        },
        search: function(searchQuery, callback){

            var searchPostContent= {
                'from':'id',
                'search':searchQuery,
                'limit':appConfig.searchLimit
            }

            $http({
                method: 'POST',
                url: appConfig.baseApiUrl + appConfig.dataTypeUrl + appConfig.dataCollectionUrl +'/search/',
                data: searchPostContent
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
dataServices.factory('MessageService', function($http, appConfig){
    return {
        get: function(callback) {
            $http({
                method: 'GET',
                url: appConfig.baseApiUrl + 'messagedata/api/messages/'
            })
            .success(function(data){
                callback(data);
            })
            .error(function(data,status,header,config){
                errorHandler(data,status,header,config,appConfig.dataTypeUrl);
            });
        },
        send: function(messageObject, callback){

            var message = {
                'type':'Message',
                'recipient': messageObject.to,
                'topic': messageObject.topic,
                'message': messageObject.message
            }

            $http({
                method: 'POST',
                url: appConfig.baseApiUrl + 'messagedata/api/send/',
                data: message
            })
            .success(function(data){
                callback(data);
            })
            .error(function(data,status,header,config){
                errorHandler(data,status,header,config,appConfig.dataTypeUrl);
            });
        }
    }
});

// GOOGLE SIGN IN
dataServices.factory('AuthService', function($rootScope,$http,appConfig){
    var user = null;
    return {

        login: function(){
            return true; // TODO: ask from the backend
        },
        isLoggedIn: function(){
            return !!user;
        },
        getUserInfo: function(authResult, callback){

            delete $http.defaults.headers.common['LBD_LOGIN_HEADER'];
            delete $http.defaults.headers.common['LBD_OAUTH_ID'];

            $http({
                method: 'GET',
                url: 'https://www.googleapis.com/oauth2/v2/userinfo',
                headers: {
                    'Authorization': 'Bearer '+authResult.access_token
                },
                data: authResult
            }).success(function(userinfo){
                user = {
                    id: userinfo.id,
                    name: userinfo.name,
                    email: userinfo.email,
                    picture: userinfo.picture,
                    locale: userinfo.locale
                };

                // Add Google Account info to all http requests
                $http.defaults.headers.common = {
                    'LBD_LOGIN_HEADER' : authResult.access_token,
                    'LBD_OAUTH_ID' : user.id
                };

                callback(userinfo);
            })
            .error(function(data,status,header,config){
                errorHandler(data,status,header,config);
            });
        },
        logout: function(){
            user = null;
        },
        getUser: function(){
            return user;
        }
    }
});

// Private
var errorHandler = function(data,status,header,config,dataTypeUrl) {
    alert("Sorry, Data Load Failure");
    console.log("Data Load Failure: "+dataTypeUrl +" " + status+" "+config);
}
