/*Some global variables for signInCallback function to store variable values on*/
var token = "";
var google_id = "";
var error = "";

/* See startAuth() to read how we get here */
/*This function receives the authentication token.
The authentication token is used to get the logged in user's google id using Google API */
function signInCallback(authResult) {
    if (authResult['status']['signed_in']) {
        
        if (authResult['access_token'] === undefined) {
            console.log("No access token!");
            error = "No access token received";
            return;
        }

        token = authResult['access_token'];

        function makeApiCall() {
            $.ajax({
            url: 'https://www.googleapis.com/oauth2/v2/userinfo',
            type: 'GET',
            crossDomain: true,
            success: function(data) {
                google_id = data['id'];
            },
            error: function() { console.log("Google API call failed!"); error = "Google API call failed"; },
            beforeSend: function (request) { request.setRequestHeader("Authorization", 'Bearer ' + authResult['access_token']); }
            });
        }   

        //Call on Google API to get user_id required by the backend API call
        makeApiCall();

    } else if (authResult['error']) {
        if (authResult['error'] == "immediate_failed" ||
                authResult['error'] == "user_signed_out") {
            console.log("Automatic authorization failed");
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
}

/* startAuth():
This function starts the Google+ Sign In authentication task.
- If the user is 1) signed in and 2) given our app permissions before, this function runs in the
background with out any actions required from the user.
- If user is signed out, a window will open to prompt the user to choose a Google account to sign in with.
- If user hasn't given our app permissions, a consent screen will pop up to ask for the user to grant
the app the required permissions.
As a result of this function signInCallback() function will be called.
NOTE:
gapi.auth.signIn() parameters have some important values, for example Google Developer Console client id.
Notice that a wrong client id or wrong scopes can result in some weird errors. Remember to change these values when needed.
Really, these parameters should be read from static variables stored somewhere. Change this if you like.
*/
function startAuth() {
    gapi.auth.signIn({
                    'clientid' : '388919682787-vf743dghif9m483leu0se5raajqsfaan.apps.googleusercontent.com',
                    'cookiepolicy' : 'single_host_origin',
                    'callback' : 'signInCallback',
                    'scope': 'profile email'
                    })
}

/* getInfo():
This function returns:
- Google authentication token
- Google id
- Possible error
  OR
  "Not done" as an error message if the asynchronic token and
  id fetching tasks haven't finished yet
*/
//NOTE NOTE NOTE NOTE NOTE NOTE NOTE NOTE NOTE NOTE NOTE NOTE NOTE
/*DO NOT call this function directly before calling isFinished function and it has returned true
(if you do, prepare to handle with the "Not done" and clearing the setInterval task)*/
function getInfo() {
    //Are we done here?
    if( token == "" || google_id == "" ) {
        //If there would be an error message we would be done
        if( error == "" ) {
            error = "Not done"
        }
    }
    else {
        //If "Not done" was set at some point, clear it here
        if( error == "Not done" ) {
            error = ""
        }
    }
    
    var loginInfo = {token: token, google_id: google_id, error: error};
    
    console.log("Token: " + loginInfo.token + " ID: " + loginInfo.google_id + " Error: " + loginInfo.error);
    return loginInfo;
}

/*isFinished():
Use this function to test if token and google_id values are already set
*/
function isFinished() {
    var info = getInfo();
    if (info.error == "Not done") {
        return false;
    }
    else {  
        return true;
    }
}
