# -*- coding: utf-8 -*-
"""
.. module:: LocationdataREST.decorators
    :platform: Unix, Windows
.. moduleauthor:: Aki Mäkinen <aki.makinen@outlook.com>

"""

__author__ = 'Aki Mäkinen'

from functools import wraps
from django.http import HttpResponse
from RESThandlers.HandlerInterface.Exceptions import CollectionNotInstalled
from RESThandlers.HandlerInterface.Factory import HandlerFactory


def location_collection(func):
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
    @wraps(func)
    def wrapper(request, *args, **kwargs):
        users = ["SimoSahkari",
                 "TiinaTeekkari",
                 "HeliHumanisti",
                 "TeePannu"]
        if "HTTP_LBD_LOGIN_HEADER" in request.META:
            if request.META["HTTP_LBD_LOGIN_HEADER"] in users:
                return func(request, *args, **kwargs)
            else:
                return HttpResponse(status=403)
        else:
            return HttpResponse(status=400)
    return wrapper