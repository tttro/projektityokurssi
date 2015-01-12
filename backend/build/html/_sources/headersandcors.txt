.. headersandcors:

Headers and CORS
================

Headers
-------

The back-end uses two un-standard headers for user authentication and authorization. Both of these are required for most
functionalities.

================ ===========================================================
Header name      Explanation
================ ===========================================================
LBD_LOGIN_HEADER Google OAuth authentication token.
LBD_OAUTH_ID     Google id.
================ ===========================================================


CORS
----

CORS or Cross-origin resource sharing mechanism allows resources to be requested from other domains. In order to be able to
use the back-end for example with AngularJS from another domain, some headers needed to be added. This is done in cors-middleware.

Cors middleware adds support for HTTP OPTIONS method and adds *Access-Control-Allow-Origin*, *Access-Control-Allow-Credentials* and
*Access-Control-Allow-Headers* to the request.