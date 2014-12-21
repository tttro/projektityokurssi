# -*- coding: utf-8 -*-
"""
Decorators for location data REST
+++++++++++++++++++++++++++++++++
**This module contains the decorators for the REST handling the location data**

.. module:: LocationdataREST.decorators
    :platform: Unix, Windows
.. moduleauthor:: Aki Mäkinen <aki.makinen@outlook.com>

"""

import mongoengine
from lbd_backend.LBD_REST_users.models import User

import json
import httplib2
from oauth2client.client import flow_from_clientsecrets, FlowExchangeError


__author__ = 'Aki Mäkinen'

from functools import wraps
from django.http import HttpResponse
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
                user = User.objects.get(user_id=request.META["HTTP_LBD_OAUTH_ID"])
                kwargs["lbduser"] = user
                return func(request, *args, **kwargs)
            except mongoengine.DoesNotExist:
                return HttpResponse(status=401)

        else:
            access_token = request.META["HTTP_LBD_LOGIN_HEADER"]
            print access_token
        if "HTTP_LBD_OAUTH_ID" not in request.META:
            return HttpResponse(status=400)

        else:
            userid = request.META["HTTP_LBD_OAUTH_ID"]
        url = ('https://www.googleapis.com/oauth2/v1/tokeninfo?access_token=%s'
               % access_token)
        h = httplib2.Http()
        result = json.loads(h.request(url, 'GET')[1])
        # If there was an error in the access token info, abort.
        if result.get('error') is not None:
            print result
            return HttpResponse(status=400)
        if userid == result["user_id"]:
            print "User matches"
        #TODO: Match the user to a user in database
        print result
        return func(request, *args, **kwargs)

        # users = ["SimoSahkari",
        #          "TiinaTeekkari",
        #          "HeliHumanisti",
        #          "TeePannu"]
        # if "HTTP_LBD_LOGIN_HEADER" in request.META:
        #     if request.META["HTTP_LBD_LOGIN_HEADER"] in users:
        #         kwargs["lbduser"] = request.META["HTTP_LBD_LOGIN_HEADER"]
        #         return func(request, *args, **kwargs)
        #     else:
        #         return HttpResponse(status=403)
        # else:
        #     return HttpResponse(status=400)

    return wrapper