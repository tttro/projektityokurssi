// Global settings
var dataServices = angular.module('dataServices', ['ngResource']);
var dataSet = null;


dataServices.factory('ObjectsService', function($http, appConfig, notify){
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
                errorHandler(data,'location',appConfig.dataTypeUrl,notify);
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
                        errorHandler(data,'location',appConfig.dataTypeUrl,notify);
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
                    errorHandler(data,'location',appConfig.dataTypeUrl,notify);
            });

        },
        getCollections: function(callback){
            $http({
                method: 'GET',
                url: appConfig.dataPlaygroundsUrl
            })
                .success(function(data){
                    callback(data);
                })
                .error(function(data){
                    errorHandler(data,'playground',appConfig.dataTypeUrl,notify);
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
dataServices.factory('MessageService', function($http, appConfig,notify){
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
                    errorHandler(data,'message',appConfig.dataTypeUrl,notify);
            });
        },
        send: function(messageObject){

            var message = {
                'type':'Message',
                'recipient': messageObject.to,
                'topic': messageObject.topic,
                'message': messageObject.message,
                'category': appConfig.dataCollectionUrl
            }

            $http({
                method: 'POST',
                url: appConfig.baseApiUrl + 'messagedata/api/send/',
                data: message
            })
            .success(function(data){
                notify('A message has been sent to ' + messageObject.to);
            })
            .error(function(data,status,header,config){
                notify('A message failed to send to ' + messageObject.to);
            });
        },
        delete: function(id){
            $http({
                method: 'DELETE',
                url: appConfig.baseApiUrl + 'messagedata/api/messages/',
                data: {
                    'mid':id
                }
            })
            .success(function(data){
                notify('Message has been deleted');
            })
            .error(function(data,status,header,config){
                notify('Failed to delete message');
            });
        }
    }
});

// GOOGLE SIGN IN
dataServices.factory('AuthService', function($rootScope,$http,notify,appConfig){
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
                    'LBD_OAUTH_ID' : userinfo.id
                };

                callback(userinfo);
            })
            .error(function(data,status,header,config){
                errorHandler(data,'auth',appConfig.dataTypeUrl,notify);
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
var errorHandler = function(data,message,dataTypeUrl,notify) {
    //alert("Sorry, Data Load Failure");
    notify('Failed to load '+message+' data from the server! Please try again later.');
    console.log("Data Load Failure: "+dataTypeUrl +" " + status);
}
