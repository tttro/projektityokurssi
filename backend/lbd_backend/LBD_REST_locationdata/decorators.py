# -*- coding: utf-8 -*-
"""
Decorators for location data REST
+++++++++++++++++++++++++++++++++
**This module contains the decorators for the REST handling the location data**

.. module:: LocationdataREST.decorators
    :platform: Unix, Windows
.. moduleauthor:: Aki Mäkinen <aki.makinen@outlook.com>

"""
from lbd_backend.utils import s_codes

__author__ = 'Aki Mäkinen'

import httplib
import mongoengine
import json
import httplib2
from functools import wraps
from django.http import HttpResponse

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


def this_is_a_login_wrapper_dummy(func):
    """
    *Wrapper*

    .. note::

        A dummy wrapper for user login. For demo purposes only

    """
    @wraps(func)
    def wrapper(request, *args, **kwargs):

        if "HTTP_LBD_LOGIN_HEADER" in request.META and "HTTP_LBD_OAUTH_ID" in request.META:
            try:
                #############################################################################################
                access_token = request.META["HTTP_LBD_LOGIN_HEADER"]
                print access_token
                userid = request.META["HTTP_LBD_OAUTH_ID"]
                url = 'www.googleapis.com'
                h = httplib.HTTPSConnection(url)
                h.request("GET", '/oauth2/v1/tokeninfo?access_token=' + access_token)
                response = h.getresponse()
                res_content = response.read()
                result = json.loads(res_content)

                # If there was an error in the access token info, abort.
                # if result.get('error') is not None:
                #     print result
                    #return HttpResponse(status=400)
                print result
                print response.status
                
                if response.status == 200 and userid == result["user_id"]:
                    print "User matches"
                else:
                    print "ERRORD!"
                    print "STATUS: " + str(response.status)
                    return HttpResponse(status=s_codes["BAD"])

                try:
                    user = User.objects.get(user_id=request.META["HTTP_LBD_OAUTH_ID"])
                    kwargs["lbduser"] = user
                except mongoengine.DoesNotExist:
                    return HttpResponse(status=s_codes["UNAUTH"])
                except Exception as e:
                    #template = "An exception of type {0} occured. Arguments:\n{1!r}"
                    #print template.format(type(e).__name__, e.args)
                    print e.message
                    return HttpResponse(status=s_codes["INTERNALERROR"])

                infohttp = httplib.HTTPSConnection("www.googleapis.com")
                infohttp.request("GET", 'https://www.googleapis.com/plus/v1/people/' +
                                 request.META["HTTP_LBD_OAUTH_ID"] + '?key=AIzaSyC0Px_GBDPrefu4IK_lC7iyH86njxEu79A'
                                                                     '&fields=emails',
                                 headers={"Authorization": "Bearer "+ request.META["HTTP_LBD_LOGIN_HEADER"]})
                inforesp = infohttp.getresponse()
                info_content = inforesp.read()
                info_result = json.loads(info_content)
                print info_result

                return func(request, *args, **kwargs)

            except mongoengine.DoesNotExist:
                return HttpResponse(status=401)

        else:
            return HttpResponse(status=400)

    return wrapper
