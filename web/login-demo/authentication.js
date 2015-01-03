/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
var token = "";
var google_id = "";
var error = "";
var done = false;
    
function signInCallback(authResult) {
    if (authResult['status']['signed_in']) {
        console.log("User is now signed in to our service with Google account");
        if (authResult['access_token'] === undefined) {
            console.log("No access token!");
            error = "No access token received";
            return;
        }
        console.log("Google returned access token:" + authResult['access_token']);
        token = authResult['access_token'];

        function makeApiCall() {
            $.ajax({
            url: 'https://www.googleapis.com/oauth2/v2/userinfo',
            type: 'GET',
            crossDomain: true,
            success: function(data) {
                google_id = data['id'];
                console.log("id: " + google_id);
            },
            error: function() { console.log("Google API call failed!"); error = "Google API call failed"; },
            beforeSend: function (request) { request.setRequestHeader("Authorization", 'Bearer ' + authResult['access_token']); }
            });
        }   
        // Hide the sign-in button now that the user is authorized, for example:
        $('#signinButton').attr('style', 'display: none');
        //Call on Google API to get user_id required by the backend API call
        makeApiCall();

    } else if (authResult['error']) {
        if (authResult['error'] == "immediate_failed" ||
                authResult['error'] == "user_signed_out") {
            window.location.href = "http://stackoverflow.com";
        }
        else if (authResult['error'] == "access_denied") {
            console.log("User gave no permission");
            error = "User gave no permission for the app";
        }
        else {
            // There was an error.
            // Possible error codes:
            //   "access_denied" - User denied access to your app
            //   "immediate_failed" - Could not automatially log in the user
            error = "Some unknown error";
            console.log('There was an error: ' + authResult['error']);
        }
    }
    if( error == "" ) {
        error = "-";
    }
    console.log('Tsädäm ' + token + '-----' + google_id + '-----'+ error);
}

function isSet(stop)
{
    if( token == "" || google_id == "" || error == "")
    {
        done = false;
    }
    done = true;
    window.clearInterval(stop)
}
    
function getAuthInfo() {
    
    gapi.auth.signIn({
                    'clientid' : '561371657443-clfpgostcgejj669qs9hu5f7hh9oetv5.apps.googleusercontent.com',
                    'cookiepolicy' : 'single_host_origin',
                    'callback' : 'signInCallback',
                    'scope': 'profile email'
                    })
    
    console.log("info " + token + google_id + error);
    var loginInfo = {token: token, google_id: google_id, error: error};
    return loginInfo;
}

