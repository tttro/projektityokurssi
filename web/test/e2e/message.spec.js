describe('angularjs messages', function() {

    var ptor = protractor.getInstance();
    ptor.get('');
    ptor.sleep(1000);

    var gLogin = ptor.findElement(protractor.By.xpath('//*[@id="___signin_0"]/button'));
    gLogin.click();
    ptor.getAllWindowHandles().then(function(handles) {
        popUpHandle = handles[1];
        parentHandle = handles[0];
        return handlesDone = true;
    });
    var emailInput =  ptor.driver.findElement(By.id('Email'));
   // emailInput.sendKeys('user@googleappsdomain.com');

    var passwordInput =  ptor.driver.findElement(By.id('Passwd'));
   // passwordInput.sendKeys('pa$sWo2d');  //you should not commit this to VCS

    //var messageTab = ptor.findElement(protractor.By.xpath('//*[@id="tabs"]/ul/li[2]'));
   // messageTab.click();
   // ptor.sleep(500);

    beforeEach(function(){

    });
    afterEach(function(){

    });


    it('should click message-tab and load users messages', function() {

        //expect(objects.count()).toBe(1); // Objects count should be 1


    },3000);




});