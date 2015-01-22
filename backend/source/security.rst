Security
========

Django provides some security for the user. In combination with MongoDB (no SQL injections) the software is relatively
secure to use with open data. However the authentication needs to be secured against for example man-in-the-middle attacks.
Good way to achieve security is to use HTTPS. HTTPS is already enabled from Django settings so you only need to see
what needs to be done to nginx/uwsgi to start using it.
