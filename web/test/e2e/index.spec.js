describe('angularjs map and object list', function() {
    var ptor;
    ptor = protractor.getInstance();
    ptor.get('http://localhost:63342/web/app/index.html');

    beforeEach(function(){

    });
    afterEach(function(){

    });


    it('should have a title', function() {
        expect(browser.getTitle()).toEqual('Web-client');
    },2000);

    it('should have items on list', function() {
        // delete all cookies
        ptor.manage().deleteAllCookies();
        ptor.sleep(3000);
        var objects = element.all(by.repeater('item in items.features'));
        expect(objects.count()).toBeGreaterThan(0);
    },6000);

    it('should open infowindow on map', function() {
        var titleBar = ptor.findElement(protractor.By.xpath('/html/body/div[1]/div[1]/div[2]/div/div/ng-include/div[2]/ul/li[1]/div[1]'));

        var objects = element.all(by.repeater('item in items.features'));
        expect(objects.count()).toBeGreaterThan(0);
    },6000);


});