/**
 * Test for services. Server, backend.
 */

'use strict';

describe('Unit: main services', function(){

    var scope;//we'll use this scope in our tests
    var Services;
    var appConfig;

    //mock Application to allow us to inject our own dependencies
    beforeEach(function() {
        angular.module('app',[]);
        module('dataServices');
        inject(function ($injector){
            Services = $injector.get('ObjectsService');
            appConfig = $injector.get('app.constant.appConfig');

        });
    });

    it("should work", function(){
        expect(true).toBe(true);
    });
});
