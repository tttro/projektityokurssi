/* MessageController - message functions */

var messageController = angular.module('messageController', []);

messageController.controller('messageController', function($scope, MessageService){


    MessageService.get(function(messages){
        $scope.messageList = messages;
        $scope.showLoadingIcon = false;
    });

    var dummyData = [
        {
            'topic':'This is a message title',
            'date':'11.11.2014',
            'message': 'Lorem ipsum dolor sit amet, consectetur adipiscing elit. Nulla eget eros sed metus cursus tristique. In finibus purus sit amet mauris rutrum, nec lacinia nunc pulvinar. Fusce diam lorem, ultricies at arcu vel, vestibulum hendrerit libero. Nunc sit amet est eget lorem imperdiet auctor feugiat eget mauris.'
        },
        {
            'topic':'Lorem ipsum dolor sit amet',
            'date':'1.11.2014',
            'message': 'Lorem ipsum dolor sit amet, consectetur adipiscing elit. Nulla eget eros sed metus cursus tristique. In finibus purus sit amet mauris rutrum, nec lacinia nunc pulvinar. Fusce diam lorem, ultricies at arcu vel, vestibulum hendrerit libero. Nunc sit amet est eget lorem imperdiet auctor feugiat eget mauris.'
        },
        {

            'topic':'Test test',
            'date':'5.10.2014',
            'message': 'Lorem ipsum dolor sit amet, consectetur adipiscing elit. Nulla eget eros sed metus cursus tristique. In finibus purus sit amet mauris rutrum, nec lacinia nunc pulvinar. Fusce diam lorem, ultricies at arcu vel, vestibulum hendrerit libero. Nunc sit amet est eget lorem imperdiet auctor feugiat eget mauris.'
        }

    ];



    $scope.selectedItem = null;

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