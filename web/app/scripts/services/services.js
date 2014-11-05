var dataServices = angular.module('dataServices', ['ngResource']);

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
