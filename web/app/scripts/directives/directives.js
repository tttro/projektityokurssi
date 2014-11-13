var appDirectives = angular.module('appDirectives', []);

appDirectives.directive('tabinner', function($window) {
    return {
        restrict: "A",
        link: function(scope, element, attributes){

            var searchformHeight = document.getElementById('itemSearch').offsetWidth;
            var tabsHeight = document.getElementById('tabs').offsetWidth;
            var listHeight = $window.outerHeight - (searchformHeight);
            //console.log($window.outerHeight +" / " + tabsHeight+" / " + searchformHeight);

            element.css('height', listHeight + 'px');
        }
    }
});