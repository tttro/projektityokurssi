# -*- coding: utf-8 -*-
"""
.. _locdecos:

Decorators for location data REST
+++++++++++++++++++++++++++++++++
**This module contains the decorators for the REST handling the location data**

.. module:: LocationdataREST.decorators
    :platform: Unix, Windows
.. moduleauthor:: Aki Mäkinen <aki.makinen@outlook.com>

"""

__author__ = 'Aki Mäkinen'

import traceback
import httplib
import mongoengine
import json
import sys
from functools import wraps
from django.http import HttpResponse

from lbd_backend.utils import s_codes
from lbd_backend.LBD_REST_users.models import User
from RESThandlers.HandlerInterface.Exceptions import CollectionNotInstalled
from RESThandlers.HandlerInterface.Factory import HandlerFactory


_supported_http_methods = ["GET", "PUT", "POST", "DELETE", "OPTIONS"]

def authenticate_only_methods(lst):
    def inner(func):
        @wraps(func)
        def wrapper(request, *args, **kwargs):
            print "In authenticate_only_methods"
            print lst
            kwargs["authenticate_only_methods"] = lst
            return func(request, *args, **kwargs)
        return wrapper
    return inner


def location_collection(func):
    """
    *Wrapper*

    Checks if the collection in the URL exists and the handler for it is installed.

    Key "handlerinterface" is added to kwargs with a handler object as the value.

    """
    @wraps(func)
    def wrapper(request, *args, **kwargs):
        if "collection" in kwargs:
            try:
                hf = HandlerFactory(kwargs["collection"])
                kwargs["handlerinterface"] = hf.create()
            except CollectionNotInstalled:
                return HttpResponse(status=404)
        else:
            return HttpResponse(status=404)
        try:
            return func(request, *args, **kwargs)
        except NotImplementedError:
            return HttpResponse(status=418)
    return wrapper


def lbd_require_login(func):
    """
    *Wrapper*

    This wrapper is used for authenticating the user with Google OAuth2.

    Key "lbduser" is added to kwargs with User object as value.

    """
    @wraps(func)
    def wrapper(request, *args, **kwargs):
        print "In lbd_require_login"
        if "authenticate_only_methods" in kwargs:
            for item in kwargs["authenticate_only_methods"]:
                if item not in _supported_http_methods:
                    return HttpResponse(status=s_codes["INTERNALERROR"])
            if request.method in kwargs["authenticate_only_methods"]:
                require_authentication_headers = True
            else:
                require_authentication_headers = False
        else:
            print "Requiring auth headers"
            require_authentication_headers = True

        print require_authentication_headers

        if require_authentication_headers:
            if "HTTP_LBD_LOGIN_HEADER" in request.META and "HTTP_LBD_OAUTH_ID" in request.META:
                access_token = request.META["HTTP_LBD_LOGIN_HEADER"]
                userid = request.META["HTTP_LBD_OAUTH_ID"]
                print "ACCESS_TOKEN: "+access_token
                print "USER_ID: "+userid
                url = 'www.googleapis.com'
                h = httplib.HTTPSConnection(url)
                h.request("GET", '/oauth2/v1/tokeninfo?access_token=' + access_token)
                response = h.getresponse()
                res_content = response.read()
                try:
                    result = json.loads(res_content)
                except ValueError:
                    return HttpResponse(status=s_codes["INTERNALERROR"])
                #if True:
                if response.status == 200 and userid == result["user_id"]:
                    print result
                    print response.status
                    print "User matches"
                else:
                    print "ERRORD!"
                    print "STATUS: " + str(response.status)
                    if "error_description" in result:
                        errormsg = result["error_description"]
                    else:
                        errormsg = "Unknown error happened. Oh noes!"
                    print errormsg

                    return HttpResponse(status=s_codes["BAD"], content='{"message": "%s"}' % errormsg,
                                        content_type="application/json; charset=utf-8")

                try:
                    user = User.objects.get(user_id=request.META["HTTP_LBD_OAUTH_ID"])
                    kwargs["lbduser"] = user
                except mongoengine.DoesNotExist:
                    return HttpResponse(status=s_codes["UNAUTH"], content_type="application/json; charset=utf-8",
                                        content='{"message": "Unauthorized"}')
                except Exception as e:
                    print e.message
                    return HttpResponse(status=s_codes["INTERNALERROR"], content_type="application/json; charset=utf-8",
                                        content='{"message": "Internal error: %s"}' % e.message)

                return func(request, *args, **kwargs)

            else:
                return HttpResponse(status=s_codes["BAD"], content_type="application/json; charset=utf-8",
                                    content='{"message": "Missing header or headers!"}')
        else:
            return func(request, *args, **kwargs)
    return wrapper
