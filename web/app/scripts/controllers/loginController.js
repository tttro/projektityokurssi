/* LoginController - handle all login stuff */

var loginController = angular.module('loginController', []);

loginController.controller('loginController', function($scope, $rootScope, $window, $location, appConfig, AuthService){

    if(AuthService.isLoggedIn()) {
        $location.path('/');
    }

    $scope.googleClientId = appConfig.googleClientId;

    $scope.$on('event:google-plus-signin-success', function(event, authResult) {

        console.log('google-plus-signin-success');
        AuthService.getUserInfo(authResult,function(userinfo) {
            appConfig.googleId = userinfo.id;
            appConfig.googleToken = authResult.access_token;
            $scope.$emit('event:setuser');
            $location.path('/');
        });
    });
});