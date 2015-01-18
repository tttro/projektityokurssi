'use strict';
var appDirectives = angular.module('appDirectives', []);

// Based on https://github.com/sirkitree/angular-directive.g-signin
appDirectives.directive('googlePlusSignin', function($window){

    var ending = /\.apps\.googleusercontent\.com$/;

    return {
        restrict: 'E',
        transclude: true,
        template: '<span></span>',
        replace: true,
        link: function (scope, element, attrs, ctrl, linker) {
            attrs.clientid += (ending.test(attrs.clientid) ? '' : '.apps.googleusercontent.com');

            attrs.$set('data-clientid', attrs.clientid);

            // Some default values, based on prior versions of this directive
            var defaults = {
                callback: 'signinCallback',
                cookiepolicy: 'single_host_origin',
                scope: 'profile email',
                width: 'wide',
            };

            defaults.clientid = attrs.clientid;

            // Provide default values if not explicitly set
            angular.forEach(Object.getOwnPropertyNames(defaults), function(propName) {
                if (!attrs.hasOwnProperty(propName)) {
                    attrs.$set('data-' + propName, defaults[propName]);
                }
            });

            // Default language
            // Supported languages: https://developers.google.com/+/web/api/supported-languages
            attrs.$observe('language', function(value){
                $window.___gcfg = {
                    lang: value ? value : 'en'
                };
            });

            // Asynchronously load the G+ SDK.
            var po = document.createElement('script'); po.type = 'text/javascript'; po.async = true;
            po.src = 'https://apis.google.com/js/client:platform.js';
            var s = document.getElementsByTagName('script')[0]; s.parentNode.insertBefore(po, s);

            linker(function(el, tScope){
                po.onload = function() {
                    if (el.length) {
                        element.append(el);
                    }
                    gapi.signin.render(element[0], defaults);
                };
            });
        }
    }
}).
run(['$window','$rootScope',function($window, $rootScope) {
    $window.signinCallback = function (authResult) {

        if (authResult && authResult.access_token){
            $rootScope.$broadcast('event:google-plus-signin-success', authResult);
        } else {
            $rootScope.$broadcast('event:google-plus-signin-failure', authResult);
        }

    };
}]);

