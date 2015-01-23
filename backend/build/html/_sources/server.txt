Server and WSGI
===============

This documentation itself does not guide how to setup a HTTP server or WSGI. However, we (the development team) can give
you some suggestions what software to use.

One quite popular HTTP server is **Apache HTTP Server** (http://httpd.apache.org/). However, when testing the performance
on Raspberry Pi where the server's role is emphasized when determining response times (of course this software is not
meant to run on Raspberry Pi ;) ).

Much better performance was achieved with **nginx** (http://nginx.org/). Response time was drastically decreased with it and the configuration
is simple and relatively quick.

With nginx, best result in terms of configuration and performance was achieved with uWSGI (http://uwsgi-docs.readthedocs.org/en/latest/index.html).

Of course you do not have to use nginx and uwsgi, but it worked well for the developers.
