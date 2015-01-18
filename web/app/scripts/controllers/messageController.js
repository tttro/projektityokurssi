/* MessageController - message functions */

var messageController = angular.module('messageController', []);

messageController.controller('messageController', function($scope, $filter,MessageService, notify){


    var messageModel = [];
    var orginalMessageList = [];

    var messageSend = {
        to:{
            email:''
        },
        topic:'',
        message:''
    }

    messageModel.messageSend = messageSend;

    var updateMessages = function(){
        $scope.showLoadingIcon = true;
        // Get user's messages
        MessageService.get(function(messages){
            //messageModel.messageList = messages;
            $scope.messageModel.messageList = messages;
            orginalMessageList = messages;
            $scope.showLoadingIcon = false;
        });
    }
    /*** Logic ***/
    updateMessages();


    // Get user of application
    MessageService.getUsers(function(users){
        messageModel.userList = users;
    });

    $scope.selectedItem = null;

    $scope.messageModel = messageModel;

    /*** Button click handlers ***/

    $scope.send = function(){

        var messageModel = $scope.messageModel.messageSend;
        if(messageModel.to.email == '' || messageModel.topic == '' || messageModel.message == '')
        {
            notify('Please fill out all fields before sending');
        }
        else
        {
            MessageService.send(messageModel);

            $scope.messageModel.messageSend.topic = '';
            $scope.messageModel.messageSend.message = '';
        }

    }

    $scope.delete = function(message, index){

        MessageService.delete(message.id, function(){
            updateMessages();
        });

    }

    $scope.reply = function(message){

        // Find sender from list
        var indexVal = 0;
        angular.forEach($scope.messageModel.userList, function(value, key) {

            if (value.email === message.sender) {
                $scope.messageModel.messageSend.to = $scope.messageModel.userList[indexVal];
            }
            indexVal++;

        });

        $scope.messageModel.messageSend.topic = 'RE: ' + message.topic;

    };

    $scope.updateMessages = function(){
        updateMessages();
    }

    /* Accordion */
    $scope.click = function(message){

        // If
        if($scope.selectedItem === message){
            $scope.selectedItem = null;
        }
        else {
            $scope.selectedItem = message;
        }

    }

    $scope.isSelected = function(message){

        return $scope.selectedItem === message;
    }


    // Search
    $scope.messageSearch = function(searchQuery) {
        $scope.messageList = getFilteredObjectList(searchQuery);
    }


    var getFilteredObjectList = function(searchQuery){

        var filteredList = $filter('filter')(orginalMessageList.messages,{$:searchQuery},false);

        return filteredList;
    }

});