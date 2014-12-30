/* MessageController - message functions */

var messageController = angular.module('messageController', []);

messageController.controller('messageController', function($scope, MessageService, notify){

    var message = {
        to:'tero.taipale@lbd.net',
        topic:'',
        message:''
    }

    $scope.showLoadingIcon = true;
    $scope.messageModel = message;

    MessageService.get(function(messages){
        console.log(messages);
        $scope.messageList = messages;
        $scope.showLoadingIcon = false;
    });

    $scope.selectedItem = null;

    $scope.send = function(){
        var messageModel = $scope.messageModel;

        if(messageModel.to == '' || messageModel.topic == '' || messageModel.message == '')
        {
            notify('Please fill out all fields before sending');
        }
        else
        {
            MessageService.send(messageModel);
        }

    }
    $scope.delete = function(id){
        //MessageService.delete(id);
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


});