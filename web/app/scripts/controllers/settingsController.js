/* SettingsController - App Settings */

var settingsController = angular.module('settingsController', []);

loginController.controller('settingsController', function($scope, $location, appConfig){

    var settingsModel = {
        baseApiUrl:appConfig.baseApiUrl
    }

    $scope.settingsModel = settingsModel;

    $scope.cancel = function(){
        $location.path('/');
    }

    $scope.saveSettings = function(){
        appConfig.baseApiUrl = $scope.settingsModel.baseApiUrl;
    }

});