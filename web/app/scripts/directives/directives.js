var appDirectives = angular.module('appDirectives', []);

// Place for custom directives
// Not used anymore
appDirectives.directive('tabinner', function($window) {
    return {
        restrict: "A",
        link: function(scope, element, attributes){

          /*  var searchformHeight = document.getElementById('itemSearch').offsetHeight;
            var tabsHeight = document.getElementById('tabs').offsetHeight;
            var listHeight = $window.innerHeight - (searchformHeight + tabsHeight + 50);
            console.log($window.innerHeight +" / " + tabsHeight+" / " + searchformHeight);

            element.css('height', listHeight + 'px');*/
        }
    }
});