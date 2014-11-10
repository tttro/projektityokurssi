# -*- coding: utf-8 -*-

"""
View for handling the backend REST locationdata requests
++++++++++++++++++++++++++++++++++++++++++++++++++++++++
**This module handles http requests related to location data.**

.. module:: LocationdataREST.views
    :platform: Unix, Windows
    :synopsis: This module handles http requests related to location data.
.. moduleauthor:: Aki Mäkinen <aki.makinen@outlook.com>

"""

import json
import mongoengine
from django.shortcuts import HttpResponse
from django.views.decorators.http import require_http_methods
from pymongo.errors import DuplicateKeyError
import time

from RESThandlers.HandlerInterface.Factory import HandlerFactory
from lbd_backend.LBD_REST_locationdata.decorators import location_collection, this_is_a_login_wrapper_dummy
from lbd_backend.LBD_REST_locationdata.models import MetaDocument, MetaData
from lbd_backend.utils import s_codes, geo_json_scheme_validation


@location_collection
@this_is_a_login_wrapper_dummy
@require_http_methods(["GET", "DELETE", "PUT"])
def single_resource(request, *args, **kwargs):
    """
    This method handles the requests involving a single resource.

    As arguments, the method gets

    .. code-block:: none

       OK: 200
       BAD REQUEST: 400
       FORBIDDEN: 403
       NOT FOUND: 404
       METHOD NOT ALLOWED: 405
       INTERNAL SERVER ERROR: 500

    :param request: Request object
    :param args: arguments defined in :py:mod:`lbd_backend.LBD_REST_locationdata.models`
    :param kwargs: Dictionary (keyword arguments)
    :return: HTTP response. Possible statuses listed above.



    """
    handlerinterface = kwargs["handlerinterface"]

    if "resource" not in kwargs:
        return HttpResponse(status=s_codes["NOTFOUND"])

    #############################################################
    #
    # GET
    #
    #############################################################
    if request.method == "GET":
        # Try to get resource
        # Possible exceptions:
        #       mongoengine.DoesNotExist: Resource not found
        #       mongoengine.MultipleObjectsReturned: Multiple objects returned instead of one
        #       NotImplementedError: REST handler does not support this method or it has not been implemented otherwise
        datatemp = handlerinterface.get_by_id(kwargs["resource"])
        if datatemp is None:
            return HttpResponse(status=s_codes["NOTFOUND"])
        else:
            try:
                metatemp = None
                metatemp = MetaDocument._get_collection().find_one({"feature_id":datatemp["id"]})
            except DuplicateKeyError:
                HttpResponse(status=s_codes["INTERNALERROR"])

            if metatemp is not None:
                datatemp["properties"]["metadata"] = metatemp["meta_data"]

            return HttpResponse(status=s_codes["OK"], content_type="application/json", content=json.dumps(datatemp))

    #############################################################
    #
    # DELETE
    #
    #############################################################
    elif request.method == "DELETE":
        item = handlerinterface.get_by_id(kwargs["resource"])
        if item is not None:
            result = MetaDocument._get_collection().remove({"feature_id": kwargs["resource"]})
            if result["ok"] == 1:
                return HttpResponse(status=s_codes["OK"])
            else:
                return HttpResponse(status=s_codes["INTERNALERROR"])
        else:
            return HttpResponse(status=s_codes["NOTFOUND"])

    #############################################################
    #
    # PUT
    #
    #############################################################
    elif request.method == "PUT":
        body = request.body
        content_json = json.loads(body)
        if geo_json_scheme_validation(content_json):
            try:
                try:
                    temp = MetaDocument.objects().get(feature_id=content_json["id"])
                    temp.meta_data.status = content_json["meta_data"]["status"]
                    temp.meta_data.modified = int(time.time())
                    temp.save()
                except mongoengine.DoesNotExist:
                    try:
                        temp = MetaDocument(open_data_id=DataId(
                                                            id_field_name=content_json["open_data_id"]["id_field_name"],
                                                            document_id=content_json["open_data_id"]["document_id"]
                                        ),
                                        meta_data=MetaData(
                                                            status=content_json["meta_data"]["status"]
                                        )
                        )
                        temp.save()
                    except mongoengine.NotUniqueError:
                        return HttpResponse(status=s_codes["NOTFOUND"])
                except mongoengine.MultipleObjectsReturned:
                    return HttpResponse(status=s_codes["NOTFOUND"])
                return HttpResponse(status=s_codes["OK"])

            except KeyError:
                return HttpResponse(status=s_codes["BAD"])

        else:
            return HttpResponse(status=s_codes["BAD"])

@location_collection
@this_is_a_login_wrapper_dummy
@require_http_methods(["GET", "DELETE", "PUT", "POST"])
def collection(request, *args, **kwargs):
    """
    This is collection handler
    """
    handlerinterface = kwargs["handlerinterface"]
    collection = kwargs["collection"]

    #############################################################
    #
    # GET
    #
    #############################################################
    if request.method == "GET":
        urlmini = request.GET.get("mini", "")
        if urlmini.lower() == "true":
            mini = True
        else:
            mini = False

        items = handlerinterface.get_all(mini)

        if not mini:
            items = _addmeta(items, collection)

        return HttpResponse(content=json.dumps(items), status=s_codes["OK"], content_type="application/json")

    #############################################################
    #
    # DELETE
    #
    #############################################################
    elif request.method == "DELETE":
        MetaDocument.objects().delete()
        if MetaDocument.objects().count() == 0:
            return HttpResponse(status=s_codes["OK"])
        else:
            return HttpResponse(status=s_codes["INTERNALERROR"])

    #############################################################
    #
    # PUT
    #
    #############################################################
    elif request.method == "PUT":
        MetaDocument.objects().delete()
        if MetaDocument.objects().count() == 0:
            coljson = json.loads(request.body)
            itemlist = list()
            for item in coljson:
                temp = MetaDocument(open_data_id=DataId(
                                                    id_field_name=item["open_data_id"]["id_field_name"],
                                                    document_id=item["open_data_id"]["document_id"]
                                ),
                                meta_data=MetaData(
                                                    status=item["meta_data"]["status"]
                                )
                )
                itemlist.append(temp)

            MetaDocument.objects().insert(itemlist)
            return HttpResponse(status=s_codes["OK"])
        else:
            return HttpResponse(status=s_codes["INTERNALERROR"])

    #############################################################
    #
    # POST
    #
    #############################################################
    elif request.method == "POST":
        return HttpResponse(status=s_codes["TEAPOT"])


@location_collection
@this_is_a_login_wrapper_dummy
@require_http_methods(["GET", "DELETE"])
def collection_near(request, *args, **kwargs):
    try:
        latitude = float(request.GET.get('latitude', None))
        longitude = float(request.GET.get('longitude', None))
    except (TypeError, ValueError):
        return HttpResponse(status=s_codes["NOTFOUND"])

    try:
        nrange = float(request.GET.get('range', None))
    except (TypeError, ValueError):
        nrange = None

    handlerinterface = kwargs["handlerinterface"]
    collection = kwargs["collection"]

    #############################################################
    #
    # GET
    #
    #############################################################
    if request.method == "GET":
        urlmini = request.GET.get("mini", "")
        if urlmini.lower() == "true":
            mini = True
        else:
            mini = False

        if nrange is None:
            items = handlerinterface.get_near(longitude, latitude, mini=mini)
        else:
            items = handlerinterface.get_near(longitude, latitude, nrange, mini=mini)
        if items is not None:
            if not mini:
                items = _addmeta(items, collection)
            return HttpResponse(status=s_codes["OK"], content=json.dumps(items), content_type="application/json")
        else:
            return HttpResponse(status=s_codes["NOTFOUND"])

    #############################################################
    #
    # DELETE
    #
    #############################################################
    elif request.method == "DELETE":
        if nrange is None:
            result = handlerinterface.get_near(longitude, latitude)
        else:
            result = handlerinterface.get_near(longitude, latitude, nrange)

        if result is not None:
            itemlist = []
            for r in result["features"]:
                itemlist.append(r["id"])

            f = MetaDocument._get_collection().remove({'feature_id':{'$in':itemlist}})
            if f["ok"] == 1:
                return HttpResponse(status=s_codes["OK"])
            else:
                return HttpResponse(status=s_codes["INTERNALERROR"])
        else:
            return HttpResponse(staus=s_codes["NOTFOUND"])


@location_collection
@this_is_a_login_wrapper_dummy
@require_http_methods(["GET", "DELETE"])
def collection_inarea(request, *args, **kwargs):
    try:
        xtop_right = float(request.GET.get('xtopright', None))
        ytop_right = float(request.GET.get('ytopright', None))
        xbottom_left = float(request.GET.get('xbottomleft', None))
        ybottom_left = float(request.GET.get('ybottomleft', None))
    except (TypeError, ValueError):
        return HttpResponse(status=s_codes["NOTFOUND"])
    urlmini = request.GET.get('mini', "")

    handlerinterface = kwargs["handlerinterface"]
    collection = kwargs["collection"]

    #############################################################
    #
    # GET
    #
    #############################################################
    if request.method == "GET":
        if urlmini.lower() == "true":
            mini = True
        else:
            mini = False

        items = handlerinterface.get_within_rectangle(xtop_right, ytop_right, xbottom_left, ybottom_left, mini)
        if items is not None:
            if not mini:
                items = _addmeta(items, collection)
            return HttpResponse(status=s_codes["OK"], content=json.dumps(items), content_type="application/json")
        else:
            return HttpResponse(status=s_codes["NOTFOUND"])

    #############################################################
    #
    # DELETE
    #
    #############################################################
    elif request.method == "DELETE":
        result = handlerinterface.get_within_rectangle(xtop_right, ytop_right, xbottom_left, ybottom_left)

        if result is not None:
            itemlist = []
            for r in result["features"]:
                itemlist.append(r["id"])

            f = MetaDocument._get_collection().remove({'feature_id':{'$in':itemlist}})
            if f["ok"] == 1:
                return HttpResponse(status=s_codes["OK"])
            else:
                return HttpResponse(status=s_codes["INTERNALERROR"])
        else:
            return HttpResponse(staus=s_codes["NOTFOUND"])

def _addmeta(items, collection):
    metaitems = MetaDocument._get_collection().aggregate([
        {"$match":
             {"collection": collection}
        },
        {"$project":
             {"_id": 0,
              "feature_id": 1,
              "collection": 1,
              "meta_data":
                  {"status": 1,
                   "modified": 1,
                   "modifier": 1}
             }
        }
    ])
    if int(metaitems["ok"]) == 1 and len(metaitems) > 0:
        tempdict = {}
        for item in metaitems["result"]:
            tempdict[item["feature_id"]] = item["meta_data"]

        for item in items["features"]:
            if item["id"] in tempdict:
                item["properties"]["metadata"] = tempdict[item["id"]]

    return items

