/* MessageController - message functions */

var messageController = angular.module('messageController', []);

messageController.controller('messageController', function($scope, $filter, $timeout,MessageService, notify){


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


    /*** Logic ***/

    $scope.showLoadingIcon = true;
    // Get user's messages
    MessageService.get(function(messages){
        messageModel.messageList = messages;
        orginalMessageList = messages;
        $scope.showLoadingIcon = false;
        console.log(messages);
    });



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

    $scope.delete = function(id){
        MessageService.delete(id,function(){

        });
        $scope.showLoadingIcon = true;


        MessageService.get(function(messages){
            messageModel.messageList = messages;
            orginalMessageList = messages;
            $scope.showLoadingIcon = false;
        });

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

        var filteredList = $filter('filter')(orginalMessageList,{$:searchQuery},false);

        return filteredList;
    }

});