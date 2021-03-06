# -*- coding: utf-8 -*-

"""
View for handling the backend REST locationdata requests
++++++++++++++++++++++++++++++++++++++++++++++++++++++++
.. module:: LocationdataREST.views
    :platform: Unix, Windows
    :synopsis: This module handles http requests related to location data.
.. moduleauthor:: Aki Mäkinen <aki.makinen@outlook.com>

**This module handles http requests related to location data.**

For all possible HTTP statuses, see :ref:`REST documentation <restdoc>`.

Status 200 is returned when request is valid and handled successfully while 400 is returned when the request conten
is malformed or there is some other issues with the request..

.. note::

    In case of PUT and POST, status 200 does not guarantee that any data has changed in database.

Status 400 is returned when request body does not match the defined format or there is some other inconsistency in
the request.

Status 500 means that something went wrong when handling the request.

Client should be able to handle these responses and should not crash in case some undefined status is returned for
reasons unknown.

.. note::
   For kwargs added by the decorators, see :ref:`the decorator documentation <locdecos>`.

"""

__author__ = 'Aki Mäkinen'


import json
import time

import mongoengine
from django.shortcuts import HttpResponse
from django.views.decorators.http import require_http_methods
from pymongo.errors import DuplicateKeyError

from lbd_backend.LBD_REST_locationdata.decorators import location_collection, lbd_require_login, \
    authenticate_only_methods
from lbd_backend.LBD_REST_locationdata.models import MetaDocument, MetaData
from lbd_backend.utils import s_codes, geo_json_scheme_validation
from RESThandlers.HandlerInterface.Exceptions import ObjectNotFound


@require_http_methods(["GET"])
def api(request):
    """
    This view returns the installed open data sources as JSON.

    **Supported HTTP methods:**

    * GET

    :return: HTTP response.
    """
    from RESThandlers.HandlerInterface.Factory import HandlerFactory
    sources = HandlerFactory.get_installed()
    temp = list()
    for k, v in sources.iteritems():
        v["description"] = v["name"]
        v["name"] = k
        temp.append(v)
    installed_sources_json = json.dumps(temp)
    return HttpResponse(content=installed_sources_json, content_type="application/json; charset=utf-8")

@require_http_methods(["GET", "DELETE", "PUT"])
@location_collection
@authenticate_only_methods(["DELETE", "PUT", "POST"])
@lbd_require_login
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

    **In addition to the kwargs added by the decorators, this view uses the following:**

    * collection (String)

        * Location data collection name

    * resource (String)

        * Resource id

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
        try:
            datatemp = handlerinterface.get_by_id(kwargs["resource"])
        except ObjectNotFound:
            return HttpResponse(status=s_codes["NOTFOUND"])

        try:
            metatemp = None
            metatemp = MetaDocument._get_collection().find_one({"feature_id": datatemp["id"]})
        except DuplicateKeyError as e:
            HttpResponse(status=s_codes["INTERNALERROR"], content='{"message":"%s"}' % e.message,
                         content_type="application/json")

        if metatemp is not None:
            datatemp["properties"]["metadata"] = metatemp["meta_data"]

        return HttpResponse(status=s_codes["OK"], content_type="application/json; charset=utf-8", content=json.dumps(datatemp))

    #############################################################
    #
    # DELETE
    #
    #############################################################
    elif request.method == "DELETE":
        try:
            item = handlerinterface.get_by_id(kwargs["resource"])
        except ObjectNotFound:
            return HttpResponse(status=s_codes["NOTFOUND"])

        result = MetaDocument._get_collection().remove({"feature_id": kwargs["resource"]})
        if result["ok"] == 1:
            return HttpResponse(status=s_codes["OK"])
        else:
            return HttpResponse(status=s_codes["INTERNALERROR"])



    #############################################################
    #
    # PUT
    #
    #############################################################
    elif request.method == "PUT":
        body = request.body
        try:
            content_json = json.loads(body)
        except ValueError:
            return HttpResponse(status=s_codes["BAD"])


        if geo_json_scheme_validation(content_json):
            try:
                if content_json["id"] != kwargs["resource"]:
                    return HttpResponse(status=s_codes["BAD"], content='{"message":"Bad JSON. Id does not match the resource in the URL."}',
                                        content_type="application/json")
                try:
                    handlerinterface.get_by_id(kwargs["resource"])
                    handlerinterface.get_by_id(content_json["id"])
                except ObjectNotFound:
                    return HttpResponse(status=s_codes["NOTFOUND"])


                content_json["properties"]["metadata"]["modified"] = int(time.time())
                content_json["properties"]["metadata"]["modifier"] = kwargs["lbduser"].email
                try:
                    temp = MetaDocument.objects().get(feature_id=content_json["id"])
                    temp.meta_data = MetaData(**content_json["properties"]["metadata"])
                    temp.save()
                except mongoengine.DoesNotExist:
                    try:
                        temp = MetaDocument(feature_id = content_json["id"],
                                            collection = kwargs["collection"],
                                            meta_data = MetaData(
                                                **content_json["properties"]["metadata"]
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


@require_http_methods(["GET", "DELETE", "PUT", "POST"])
@location_collection
@authenticate_only_methods(["DELETE", "PUT", "POST"])
@lbd_require_login
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

    **In addition to the kwargs added by the decorators, this view uses the following:**

    * collection (String)

        * Location data collection name

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

        return HttpResponse(content=json.dumps(items), status=s_codes["OK"], content_type="application/json; charset=utf-8")

    #############################################################
    #
    # DELETE
    #
    #############################################################
    elif request.method == "DELETE":
        try:
            MetaDocument.objects().delete()
        except Exception as e:
            return HttpResponse(status=s_codes["OK"], content='{"message":"%s"}' % e.message,
                                content_type="application/json")

    #############################################################
    #
    # PUT
    #
    #############################################################
    elif request.method == "PUT":
        MetaDocument.objects().delete()
        if MetaDocument.objects().count() == 0:
            body = request.body
            try:
                content_json = json.loads(body)
            except ValueError:
                return HttpResponse(status=s_codes["BAD"])
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
                                                        modifier=kwargs["lbduser"].email
                                                    ))
                                temp.save()
                            except mongoengine.NotUniqueError:
                                pass
                except KeyError as e:
                    return HttpResponse(status=s_codes["BAD"], content='{"message":"%s"}' % e.message,
                                        content_type="application/json")
            else:
                return HttpResponse(status=s_codes["BAD"], content='{"message":"Bad JSON"}',
                                        content_type="application/json")
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
        try:
            content_json = json.loads(body)
        except ValueError:
            return HttpResponse(status=s_codes["INTERNALERROR"])
        if geo_json_scheme_validation(content_json):
            try:
                temp = MetaDocument(feature_id=content_json["id"], collection=kwargs["collection"],
                                    meta_data=MetaData(
                                        status=content_json["properties"]["metadata"]["status"],
                                        modified=int(time.time()), modifier=kwargs["lbduser"].email
                                    ))
                temp.save()
            except mongoengine.NotUniqueError:
                pass
            except KeyError:
                return HttpResponse(status=s_codes["BAD"])

            return HttpResponse(status=s_codes["OK"])
        else:
            return HttpResponse(status=s_codes["BAD"])

@require_http_methods(["GET", "DELETE"])
@location_collection
@authenticate_only_methods(["DELETE", "PUT", "POST"])
@lbd_require_login
def collection_near(request, *args, **kwargs):
    """
    REST subcollection "near" request handler. Handles objects in certain range of given coordinates

    **Supported HTTP methods:**

    * GET
    * DELETE

    :param request: Request object
    :param args: arguments
    :param kwargs: Dictionary (keyword arguments). Known kwargs listed below.

    **In addition to the kwargs added by the decorators, this view uses the following:**

    * collection (String)

        * Location data collection name

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
        return HttpResponse(status=s_codes["BAD"])

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

        if not mini:
            items = _addmeta(items, colle)
        return HttpResponse(status=s_codes["OK"], content=json.dumps(items), content_type="application/json; charset=utf-8")


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


        itemlist = []
        for r in result["features"]:
            itemlist.append(r["id"])

        f = MetaDocument._get_collection().remove({'feature_id':{'$in':itemlist}})
        if f["ok"] == 1:
            return HttpResponse(status=s_codes["OK"])
        else:
            return HttpResponse(status=s_codes["INTERNALERROR"], content='{"message":"Something went wrong when making the query."}',
                            content_type="application/json; charset=utf-8")


@require_http_methods(["GET", "DELETE"])
@location_collection
@authenticate_only_methods(["DELETE", "PUT", "POST"])
@lbd_require_login
def collection_inarea(request, *args, **kwargs):
    """
    REST subcollection "inarea" request handler. Handles objects inside a rectangular area.

    **Supported HTTP methods:**

    * GET
    * DELETE

    :param request: Request object
    :param args: arguments
    :param kwargs: Dictionary (keyword arguments). Known kwargs listed below.

    **In addition to the kwargs added by the decorators, this view uses the following:**

    * collection (String)

        * Location data collection name

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
    except (TypeError, ValueError) as e:
        return HttpResponse(status=s_codes["BAD"], content='{"message":"%s"}' % e.message,
                            content_type="application/json; charset=utf-8")
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
        if not mini:
            items = _addmeta(items, colle)

        return HttpResponse(status=s_codes["OK"], content=json.dumps(items), content_type="application/json; charset=utf-8")


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
                return HttpResponse(status=s_codes["INTERNALERROR"], content='{"message":"Something went wrong when'
                                                                             'making the query."}',
                                content_type="application/json; charset=utf-8")
        else:
            return HttpResponse(staus=s_codes["NOTFOUND"], content='{"message":"Nothing was found from the area, '
                                                                   'therefore nothing was deleted."}',
                                content_type="application/json; charset=utf-8")


@require_http_methods(["POST"])
@location_collection
@authenticate_only_methods(["DELETE", "PUT", "POST"])
@lbd_require_login
def search_from_rest(request, *args, **kwargs):
    """
    This view searches for the given search phrase from the database. Currently only search from id field is supported.
    For json format, see :ref:`Search <searchjson>`.

    **Supported HTTP methods:**

    * POST

    :param request: Request object
    :param args: Arguments
    :param kwargs: Keyword arguments
    :return: HTTP response
    """
    try:
        contentjson = json.loads(request.body)
    except ValueError as e:
        return HttpResponse(status=s_codes["BAD"], content_type="applicetion/json; charset=utf-8",
                            content='{"message":"%s"}' % e.message)

    if not all (key in contentjson for key in ("from", "search", "limit")):
        return HttpResponse(status=s_codes["BAD"], content_type="applicetion/json; charset=utf-8",
                            content='{"message":"A field is missing from the JSON document"}')

    if  not (isinstance(contentjson["search"], str) or isinstance(contentjson["search"], unicode)) or \
        not isinstance(contentjson["limit"], int) or not (isinstance(contentjson["from"], str) or
                                                                      isinstance(contentjson["from"], unicode)):
        return HttpResponse(status=s_codes["BAD"], content_type="applicetion/json; charset=utf-8",
                            content='{"message":"Bad JSON!"}')
    try:
        limit = int(contentjson["limit"])
    except ValueError:
        return HttpResponse(status=s_codes["BAD"], content_type="applicetion/json; charset=utf-8",
                            content='{"message":"Result limit must be an integer."}')

    original_search_phrase = contentjson["search"]
    allowed_chars = "[A-Za-z0-9\\.\\@]"
    search_regex = contentjson["search"].replace("?", allowed_chars).replace("*", allowed_chars+"*")

    handlerinterface = kwargs["handlerinterface"]
    try:
        totalresults, results = handlerinterface.search(search_regex, limit, contentjson["from"])
    except NotImplementedError:
        return HttpResponse(status=s_codes["BAD"], content='{"messge":"NotImplementedError was raised. This may be the result of '
                                                'trying to search from unsupported field. It might also be that search has'
                                                'not been implemented for this collection."}',
                            content_type="application/json; charset=utf-8")
    except ValueError as e:
        return HttpResponse(status=s_codes["BAD"], content='{"message":"%s"}' % e.message,
                            content_type="application/json; charset=utf-8")
    results = _addmeta(results, kwargs["collection"])
    resultjson = contentjson
    resultjson["totalResults"] = totalresults
    resultjson["results"] = results



    return HttpResponse(json.dumps(resultjson))

def _addmeta(items, coll):
    """
    This function adds metadata to an open data object.

    :param items: Open data objects as dictionaries
    :param coll: Open data collection
    :return: Open data with metadata added to it.
    """
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
                   "modifier": 1,
                   "info": 1
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

@location_collection
@authenticate_only_methods(["GET"])
def update_db(request, *args, **kwargs):
    origin = request.get_host()
    splitted = origin.split(":")

    if (splitted[0] == "127.0.0.1" or splitted[0] == "localhost") and "HTTP_LBD_INTERNAL_REST_CALL" in request.META:
        if request.META["HTTP_LBD_INTERNAL_REST_CALL"] == "curlcall":
            handlerinterface = kwargs["handlerinterface"]
            result = handlerinterface.update_db()
            if result:
                metaitems = MetaDocument.objects(collection=kwargs["collection"])
                for item in metaitems:
                    try:
                        locobj = handlerinterface.get_by_id(item.feature_id)
                    except ObjectNotFound:
                        item.delete()
                return HttpResponse(content="Updated: "+kwargs["collection"]+" at "+str(time.time()), status=s_codes["OK"])
            else:
                return HttpResponse(content="Updating: "+kwargs["collection"]+" at "+str(time.time()) + "FAILED!",
                                    status=s_codes["INTERNALERROR"])
        else:
            return HttpResponse(status=s_codes["NOTFOUND"])
    else:
        return HttpResponse(status=s_codes["NOTFOUND"])