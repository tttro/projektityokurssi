describe('angularjs map and object list', function() {
    var ptor;
    ptor = protractor.getInstance();
    ptor.get('');

    beforeEach(function(){

    });
    afterEach(function(){

    });


    it('should find same object title from the map infowindow', function() {

        var objects = element.all(by.repeater('item in items.features')); // Get all objects
        ptor.sleep(2000);
        expect(objects.count()).toBeGreaterThan(0); // Objects count shod be more than zero

        var title = ptor.findElement(protractor.By.xpath('/html/body/div[1]/div[1]/div[2]/div/div/ng-include/div[2]/ul/li[1]/div[1]')); // Find first object from list
        title.click(); // Click a first object on list

        ptor.findElement(protractor.By.xpath('/html/body/div[1]/div[1]/div[2]/div/div/ng-include/div[2]/ul/li[1]/div[2]/div[2]/button')).click(); // Click a show on map -button
        ptor.sleep(1000);

        var h4title = ptor.findElement(protractor.By.xpath('//*[@id="map"]/div/div[1]/div[3]/div[4]/div/div[2]/div/div/div/h4')); // Get infowindow title
        expect(h4title.getText()).toBe(title.getText()); // Title should be the same

    },6000);

    it('should find same object title from the streetview infowindow', function() {
        ptor.findElement(protractor.By.id('panel')).click(); // Find first object from list
        ptor.sleep(1000);
        ptor.findElement(protractor.By.xpath('/html/body/div[1]/div[1]/div[2]/div/div/ng-include/div[2]/ul/li[1]/div[2]/div[2]/button')).click(); // Click a show on map -button
        ptor.sleep(1000);
        var title = ptor.findElement(protractor.By.xpath('/html/body/div[1]/div[1]/div[2]/div/div/ng-include/div[2]/ul/li[1]/div[1]')); // Find first object from list

        var h4title = ptor.findElement(protractor.By.xpath('//*[@id="map"]/div[2]/div[1]/div/div[8]/div/div[2]/div/div/div/h4')); // Get infowindow title
        expect(h4title.getText()).toBe(title.getText()); // Title should be the same
    }, 4000);


});