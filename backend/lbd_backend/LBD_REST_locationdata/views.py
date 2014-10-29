import json
import mongoengine

from django.shortcuts import HttpResponse
from django.views.decorators.http import require_http_methods
from pymongo.errors import DuplicateKeyError
import time

from RESThandlers.HandlerInterface.Factory import HandlerFactory

from lbd_backend.LBD_REST_locationdata.decorators import location_collection
from lbd_backend.LBD_REST_locationdata.models import MetaDocument, MetaData
from lbd_backend.utils import s_codes

@location_collection
@require_http_methods(["GET", "DELETE", "PUT"])
def single_resource(request, *args, **kwargs):
    handlerinterface = kwargs["handlerinterface"]

    if "resource" not in kwargs:
        return HttpResponse(status=s_codes["NOTFOUND"])

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


    elif request.method == "DELETE":
        try:
            metaobject = MetaDocument.objects.get(open_data_id__document_id=kwargs["resource"])
        except MetaDocument.DoesNotExist:
            return HttpResponse(status=s_codes["NOTFOUND"])

        if metaobject is not None:
            metaobject.delete()
            return HttpResponse(status=s_codes["OK"])

    elif request.method == "PUT":
        body = request.body

        content_json = json.loads(body)
        try:
            temp = MetaDocument.objects().get(open_data_id__document_id=content_json["open_data_id"]["document_id"])
            temp.meta_data.status = content_json["meta_data"]["status"]
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


@location_collection
@require_http_methods(["GET", "DELETE", "PUT", "POST"])
def collection(request, *args, **kwargs):
    handlerinterface = kwargs["handlerinterface"]

    urlmeta = request.GET.get("meta", "")
    if urlmeta.lower() == "true":
        meta = True
    else:
        meta = False

    if request.method == "GET":
        #start = time.time()*1000

        urlmini = request.GET.get("mini", "")
        if urlmini.lower() == "true":
            mini = True
        else:
            mini = False

        items = handlerinterface.get_all(mini)

        if meta and not mini:
            metaitems = MetaDocument._get_collection().aggregate(
            {"$project": {
                "_id": 0,
                "feature_id": 1,
                "collection": 1,
                "meta_data": {
                    "status": 1
                }
            }})

            for item in items["features"]:

                temp = next((i for i in metaitems["result"] if i["feature_id"] == item["id"] ), None)
                if temp is not None:
                    item["properties"]["metadata"] = temp["meta_data"]

        #end = time.time()*1000
        #print(end-start)
        return HttpResponse(content=json.dumps(items), status=s_codes["OK"], content_type="application/json")

    elif request.method == "DELETE":
        MetaDocument.objects().delete()
        if MetaDocument.objects().count() == 0:
            return HttpResponse(status=s_codes["OK"])
        else:
            return HttpResponse(status=s_codes["INTERNALERROR"])

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
            return HttpResponse(status=s_codes["INTERNALERROR"]) #TODO: Or some better code, like 500
    elif request.method == "POST":
        itemjson = json.loads(request.body)
        try:
            temp = MetaDocument(open_data_id=DataId(
                                                        id_field_name=itemjson["open_data_id"]["id_field_name"],
                                                        document_id=itemjson["open_data_id"]["document_id"]
                                    ),
                                    meta_data=MetaData(
                                                        status=itemjson["meta_data"]["status"]
                                    )
                    )
            temp.save()
        except mongoengine.NotUniqueError:
            return HttpResponse(status=s_codes["NOTFOUND"])
        return HttpResponse(status=s_codes["OK"])

    return HttpResponse(status=s_codes["TEAPOT"])


@location_collection
@require_http_methods(["GET", "DELETE"])
def collection_near(request, *args, **kwargs):
    handlerinterface = kwargs["handlerinterface"]

    urlmeta = request.GET.get("meta", "")
    if urlmeta.lower() == "true":
        meta = True
    else:
        meta = False

    if request.method == "GET":
        latitude = request.GET.get('latitude', None)
        longitude = request.GET.get('longitude', None)
        nrange = request.GET.get('range', None)

        urlmini = request.GET.get("mini", "")
        if urlmini.lower() == "true":
            mini = True
        else:
            mini = False

        if latitude is None or longitude is None:
            return HttpResponse(status=s_codes["NOTFOUND"])
        else:
            if nrange is None:
                items = handlerinterface.get_near(latitude, longitude, mini=mini)
            else:
                items = handlerinterface.get_near(latitude, longitude, nrange, mini=mini)
            if meta:
                for item in items["features"]:
                    try:
                        metatemp = MetaDocument._get_collection().find_one({"feature_id":item["id"]})
                        item["properties"]["metadata"] = metatemp["meta_data"]
                    except DuplicateKeyError:
                        HttpResponse(status=s_codes["INTERNALERROR"])

            return HttpResponse(status=s_codes["OK"], content=json.dumps(items), content_type="application/json")

    elif request.method == "DELETE":
        latitude = request.GET.get('latitude', None)
        longitude = request.GET.get('longitude', None)
        nrange = request.GET.get('range', None)

        if latitude is None or longitude is None:
            return HttpResponse(status=s_codes["NOTFOUND"])
        else:
            if nrange is None:
                result = handlerinterface.delete_near(latitude, longitude)
            else:
                result = handlerinterface.delete_near(latitude, longitude, nrange)

            if result:
                return HttpResponse(status=s_codes["OK"])
            else:
                return HttpResponse(status=s_codes["NOTFOUND"])


@location_collection
@require_http_methods(["GET"])
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

    if urlmini.lower() == "true":
        mini = True
    else:
        mini = False

    objects = handlerinterface.get_within_rectangle(xtop_right, ytop_right, xbottom_left, ybottom_left, mini)

    return HttpResponse(status=s_codes["OK"], content=json.dumps(objects), content_type="application/json")


def getjson(request):
    fac = HandlerFactory("Streetlights").create()

    return HttpResponse(fac.get_all_mini(), status=s_codes["OK"])


# @restifier
# def get_near(request):
#     (23.795199257764725, 61.503697166613755)
#
#     return HttpResponse(content="Jotain saattoi tapahtua")
