# -*- coding: utf-8 -*-

"""
View for handling the backend REST locationdata requests
++++++++++++++++++++++++++++++++++++++++++++++++++++++++
.. module:: LocationdataREST.views
    :platform: Unix, Windows
    :synopsis: This module handles http requests related to location data.
.. moduleauthor:: Aki Mäkinen <aki.makinen@outlook.com>

**This module handles http requests related to location data.**

Possible HTTP response status codes are:

.. code-block:: none

       OK: 200
       BAD REQUEST: 400
       FORBIDDEN: 403
       NOT FOUND: 404
       METHOD NOT ALLOWED: 405
       INTERNAL SERVER ERROR: 500

Status 200 is returned when request is valid and handled successfully while 400 is returned when the request
does not match the defined format or there is some other inconsistency in the query.

.. note::

    In case of PUT and POST, status 200 does not guarantee that any data has changed in database.

Status 400 is returned when request body does not match the defined format or there is some other inconsistency in
the request.

Status 500 means that something went wrong when handling the request.

Client should be able to handle these responses and should not crash in case some undefined status is returned for
reasons unknown.

"""

import json
import mongoengine
from django.shortcuts import HttpResponse
from django.views.decorators.http import require_http_methods
from pymongo.errors import DuplicateKeyError
import time

from lbd_backend.LBD_REST_locationdata.decorators import location_collection, this_is_a_login_wrapper_dummy
from lbd_backend.LBD_REST_locationdata.models import MetaDocument, MetaData
from lbd_backend.utils import s_codes, geo_json_scheme_validation


@location_collection
@this_is_a_login_wrapper_dummy
@require_http_methods(["GET", "DELETE", "PUT"])
def single_resource(request, *args, **kwargs):
    """
    REST single resource (in certain collection) request handler.

    **Supported HTTP methods:**

    * GET
    * DELETE
    * PUT

    :param request: Request object
    :param args: arguments
    :param kwargs: Dictionary (keyword arguments). Known kwargs listed below.

    **The method uses the following kwargs:**

    * collection (String)
    * resource (String)
    * handlerinterface (Handler object)
    * lbduser (String) (dummy user login)

    :return: HTTP response. Possible statuses are listed in module documentation

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
        datatemp = handlerinterface.get_by_id(kwargs["resource"])
        if datatemp is None:
            return HttpResponse(status=s_codes["NOTFOUND"])
        else:
            try:
                metatemp = None
                metatemp = MetaDocument._get_collection().find_one({"feature_id": datatemp["id"]})
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
        print "Before Geo Valid"
        content_json = json.loads(body)
        if geo_json_scheme_validation(content_json):
            print "After Geo valid"
            try:
                if handlerinterface.get_by_id(kwargs["resource"]) is None:
                    return HttpResponse(status=s_codes["NOTFOUND"])
                elif content_json["id"] != kwargs["resource"] or handlerinterface.get_by_id(content_json["id"]) is None:
                    return HttpResponse(status=s_codes["BAD"])
                else:
                    pass
                try:
                    temp = MetaDocument.objects().get(feature_id=content_json["id"])
                    temp.meta_data.status = content_json["properties"]["metadata"]["status"]
                    temp.meta_data.modified = int(time.time())
                    temp.meta_data.modifier = kwargs["lbduser"]
                    temp.save()
                except mongoengine.DoesNotExist:
                    try:
                        temp = MetaDocument(feature_id = content_json["id"],
                                            collection = kwargs["collection"],
                                            meta_data = MetaData(
                                                status = content_json["properties"]["metadata"]["status"],
                                                modified = int(time.time()),
                                                modifier = kwargs["lbduser"]
                                            ))
                        temp.save()
                    except mongoengine.NotUniqueError:
                        return HttpResponse(status=s_codes["INTERNALERROR"])
                except mongoengine.MultipleObjectsReturned:
                    return HttpResponse(status=s_codes["INTERNALERROR"])
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
    REST main collection request handler.

    **Supported HTTP methods:**

    * GET
    * DELETE
    * PUT
    * POST

    :param request: Request object
    :param args: arguments
    :param kwargs: Dictionary (keyword arguments). Known kwargs listed below.

    **The method uses the following kwargs:**

    * collection (String)
    * handlerinterface (Handler object)
    * lbduser (String) (dummy user login)

    **Supported URL parameter:**

    * mini (True or False): Return minimum amount of data (response must still be valid GeoJSON

    """
    handlerinterface = kwargs["handlerinterface"]
    colle = kwargs["collection"]

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
            items = _addmeta(items, colle)

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
            body = request.body
            content_json = json.loads(body)
            if geo_json_scheme_validation(content_json):
                try:
                    opendata = handlerinterface.get_all()
                    idlist = []

                    if opendata is not None:
                        for item in opendata["features"]:
                            idlist.append(item["id"])

                    for feature in content_json["features"]:
                        if feature["id"] in idlist:
                            try:
                                temp = MetaDocument(feature_id=feature["id"],
                                                    collection=kwargs["collection"],
                                                    meta_data=MetaData(
                                                        status=feature["properties"]["metadata"]["status"],
                                                        modified=int(time.time()),
                                                        modifier=kwargs["lbduser"]
                                                    ))
                                temp.save()
                            except mongoengine.NotUniqueError:
                                pass
                except KeyError:
                    return HttpResponse(status=s_codes["BAD"])
            else:
                return HttpResponse(status=s_codes["BAD"])
            return HttpResponse(status=s_codes["OK"])
        else:
            return HttpResponse(status=s_codes["INTERNALERROR"])

    #############################################################
    #
    # POST
    #
    #############################################################
    elif request.method == "POST":
        body = request.body
        content_json = json.loads(body)
        if geo_json_scheme_validation(content_json):
            try:
                temp = MetaDocument(feature_id=content_json["id"], collection=kwargs["collection"],
                                    meta_data=MetaData(
                                        status=content_json["properties"]["metadata"]["status"],
                                        modified=int(time.time()), modifier=kwargs["lbduser"]
                                    ))
                temp.save()
            except mongoengine.NotUniqueError:
                pass
            except KeyError:
                return HttpResponse(status=s_codes["BAD"])

            return HttpResponse(status=s_codes["OK"])
        else:
            return HttpResponse(status=s_codes["BAD"])


@location_collection
@this_is_a_login_wrapper_dummy
@require_http_methods(["GET", "DELETE"])
def collection_near(request, *args, **kwargs):
    """
    REST subcollection "near" request handler. Handles objects in certain range of given coordinates

    **Supported HTTP methods:**

    * GET
    * DELETE

    :param request: Request object
    :param args: arguments
    :param kwargs: Dictionary (keyword arguments). Known kwargs listed below.

    **The method uses the following kwargs:**

    * collection (String)
    * handlerinterface (Handler object)
    * lbduser (String) (dummy user login)

    **Supported URL parameter:**

    * latitude (Float): the latitude of the center **REQUIRED**
    * longitude (Float): the longitude of the center **REQUIRED**
    * range (Float): the radius of the area
    * mini (True or False): Return minimum amount of data (response must still be valid GeoJSON

    """
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
    colle = kwargs["collection"]

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
                items = _addmeta(items, colle)
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
    """
    REST subcollection "inarea" request handler. Handles objects inside a rectangular area.

    **Supported HTTP methods:**

    * GET
    * DELETE

    :param request: Request object
    :param args: arguments
    :param kwargs: Dictionary (keyword arguments). Known kwargs listed below.

    **The method uses the following kwargs:**

    * collection (String)
    * handlerinterface (Handler object)
    * lbduser (String) (dummy user login)

    **Supported URL parameter:**

    * xbottomleft (Float): The x-coordinate of the bottom left corner of the area
    * ybottomleft (Float): The y-coordinate of the bottom left corner of the area
    * xtopright (Float): The x-coordinate of the top right corner of the area
    * ytopright (Float): The y-coordinate of the top right corner of the area
    * mini (True or False): Return minimum amount of data (response must still be valid GeoJSON

    """
    try:
        xtop_right = float(request.GET.get('xtopright', None))
        ytop_right = float(request.GET.get('ytopright', None))
        xbottom_left = float(request.GET.get('xbottomleft', None))
        ybottom_left = float(request.GET.get('ybottomleft', None))
    except (TypeError, ValueError):
        return HttpResponse(status=s_codes["NOTFOUND"])
    urlmini = request.GET.get('mini', "")

    handlerinterface = kwargs["handlerinterface"]
    colle = kwargs["collection"]

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
                items = _addmeta(items, colle)

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

            f = MetaDocument._get_collection().remove({'feature_id': {'$in': itemlist}})
            if f["ok"] == 1:
                return HttpResponse(status=s_codes["OK"])
            else:
                return HttpResponse(status=s_codes["INTERNALERROR"])
        else:
            return HttpResponse(staus=s_codes["NOTFOUND"])


def _addmeta(items, coll):
    metaitems = MetaDocument._get_collection().aggregate([
        {"$match":
             {"collection": coll}
        },
        {"$project":
             {"_id": 0,
              "feature_id": 1,
              "collection": 1,
              "meta_data":
                  {"status": 1,
                   "modified": 1,
                   "modifier": 1
                  }
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
