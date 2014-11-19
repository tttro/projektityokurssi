/* MessageController - message functions */

var messageControllers = angular.module('messageControllers', []);

messageControllers.controller('messageController', function($scope, MessageDataService){

    var dummyData = [
        {
            'title':'This is a message title',
            'date':'11.11.2014',
            'content': 'Lorem ipsum dolor sit amet, consectetur adipiscing elit. Nulla eget eros sed metus cursus tristique. In finibus purus sit amet mauris rutrum, nec lacinia nunc pulvinar. Fusce diam lorem, ultricies at arcu vel, vestibulum hendrerit libero. Nunc sit amet est eget lorem imperdiet auctor feugiat eget mauris.'
        },
        {
            'title':'Lorem ipsum dolor sit amet',
            'date':'1.11.2014',
            'content': 'Lorem ipsum dolor sit amet, consectetur adipiscing elit. Nulla eget eros sed metus cursus tristique. In finibus purus sit amet mauris rutrum, nec lacinia nunc pulvinar. Fusce diam lorem, ultricies at arcu vel, vestibulum hendrerit libero. Nunc sit amet est eget lorem imperdiet auctor feugiat eget mauris.'
        },
        {

            'title':'Test test',
            'date':'5.10.2014',
            'content': 'Lorem ipsum dolor sit amet, consectetur adipiscing elit. Nulla eget eros sed metus cursus tristique. In finibus purus sit amet mauris rutrum, nec lacinia nunc pulvinar. Fusce diam lorem, ultricies at arcu vel, vestibulum hendrerit libero. Nunc sit amet est eget lorem imperdiet auctor feugiat eget mauris.'
        }

    ];

    $scope.messageList = dummyData;


});