/* SettingsController - App Settings */

var settingsController = angular.module('settingsController', []);

settingsController.controller('settingsController', function($scope, $location,$timeout, ObjectsService,AuthService,notify, appConfig){

    if(!AuthService.isLoggedIn()){
        $location.path('/login');
    }

    var settingsModel = {
        baseApiUrl:appConfig.baseApiUrl,
        collections:'',
        collectionModel:appConfig.dataCollectionUrl,
        user: AuthService.getUser()
    }

    ObjectsService.getCollections(function(result){
        console.log(result);
        $scope.settingsModel.collections = result;
    });

    $scope.settingsModel = settingsModel;

    $scope.cancel = function(){
        $location.path('/');
    }

    $scope.saveSettings = function(){

        appConfig.baseApiUrl = $scope.settingsModel.baseApiUrl; // Datasource url
        appConfig.dataCollectionUrl = $scope.settingsModel.collectionModel; // Datasource url

        notify('Settings have been saved');
        $timeout(function(){
            $location.path('/');
        },2000);
    }

});