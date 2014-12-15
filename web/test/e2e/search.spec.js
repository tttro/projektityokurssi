describe('angularjs object search', function() {
    var ptor;
    var searchInput = element(by.id('searchBox'));
    var searchButton = element(by.id('searchBtn'));
    var searchAll = element(by.id('radioAll'));


    beforeEach(function(){
        ptor = protractor.getInstance();
        ptor.get('');

    });
    afterEach(function(){

    });


    it('should find an object from map', function() {


        ptor.sleep(2000);

        searchInput.sendKeys('403044');
        searchButton.click();
        ptor.sleep(500);
        var objects = element.all(by.repeater('item in items.features')); // Get all objects
        expect(objects.count()).toBe(1); // Objects count should be 1





    },3000);

    it('should find an object from db', function() {
        ptor.sleep(2000);
        searchAll.click();
        searchInput.sendKeys('1234');
        searchButton.click();
        ptor.sleep(500);
        var title = ptor.findElement(protractor.By.xpath('/html/body/div[1]/div[1]/div[2]/div/div/ng-include/div[2]/ul/li[1]/div[1]/div[1]')); // Find first object from list
        expect(title.getText()).toBe('WFS_KATUVALO.371234');
    }, 5000);


});