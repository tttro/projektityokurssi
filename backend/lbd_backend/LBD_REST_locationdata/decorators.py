# -*- coding: utf-8 -*-
"""
Decorators for location data REST
+++++++++++++++++++++++++++++++++
**This module contains the decorators for the REST handling the location data**

.. module:: LocationdataREST.decorators
    :platform: Unix, Windows
.. moduleauthor:: Aki Mäkinen <aki.makinen@outlook.com>

"""

__author__ = 'Aki Mäkinen'

import httplib
import mongoengine
import json
from functools import wraps
from django.http import HttpResponse

from lbd_backend.utils import s_codes
from lbd_backend.LBD_REST_users.models import User
from RESThandlers.HandlerInterface.Exceptions import CollectionNotInstalled
from RESThandlers.HandlerInterface.Factory import HandlerFactory


def location_collection(func):
    """
    *Wrapper*
    Checks if the collection in the URL exists and the handler for it is installed.

    Sets kwarg to the handler object for the collection.

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

    """
    @wraps(func)
    def wrapper(request, *args, **kwargs):
        if "HTTP_LBD_LOGIN_HEADER" in request.META and "HTTP_LBD_OAUTH_ID" in request.META:
            access_token = request.META["HTTP_LBD_LOGIN_HEADER"]
            userid = request.META["HTTP_LBD_OAUTH_ID"]

            url = 'www.googleapis.com'
            h = httplib.HTTPSConnection(url)
            h.request("GET", '/oauth2/v1/tokeninfo?access_token=' + access_token)
            response = h.getresponse()
            res_content = response.read()
            try:
                result = json.loads(res_content)
            except ValueError:
                return HttpResponse(status=s_codes["INTERNALERROR"])
            if True:
            #if response.status == 200 and userid == result["user_id"]:
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

                return HttpResponse(status=s_codes["BAD"], content='{"message": "%s"}' % errormsg,
                                    content_type="application/json; charset=utf-8")

            try:
                user = User.objects.get(user_id=request.META["HTTP_LBD_OAUTH_ID"])
                kwargs["lbduser"] = user
            except mongoengine.DoesNotExist:
                return HttpResponse(status=s_codes["UNAUTH"], content_type="application/json; charset=utf-8",
                                    content='{"message": "Unauthorized"}')
            except Exception as e:
                #template = "An exception of type {0} occured. Arguments:\n{1!r}"
                #print template.format(type(e).__name__, e.args)
                print e.message
                return HttpResponse(status=s_codes["INTERNALERROR"], content_type="application/json; charset=utf-8",
                                    content='{"message": "Internal error: %s"}' % e.message)

            return func(request, *args, **kwargs)

        else:
            return HttpResponse(status=s_codes["BAD"], content_type="application/json; charset=utf-8",
                                content='{"message": "Missing header or headers!"}')

    return wrapper
