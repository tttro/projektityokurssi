/**
 * Test for services. Server, backend.
 */

'use strict';

describe("services", function(){

    var scope;//we'll use this scope in our tests

    //mock Application to allow us to inject our own dependencies
    beforeEach(angular.mock.module('app'));
    //mock the controller for the same reason and include $rootScope and $controller
    beforeEach(angular.mock.inject(function($rootScope, $controller){
        //create an empty scope
        scope = $rootScope.$new();
        //declare the controller and inject our empty scope
        $controller('dataServices', {$scope: scope});
    }));
    // tests start here


    it("should work", function(){
        expect(true).toBe(true);
    });
});
