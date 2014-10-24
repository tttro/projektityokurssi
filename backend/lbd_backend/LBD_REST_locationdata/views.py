import json
import mongoengine

from django.shortcuts import HttpResponse
from django.views.decorators.http import require_http_methods

from RESThandlers.HandlerInterface.Factory import HandlerFactory

from lbd_backend.LBD_REST_locationdata.decorators import location_collection
from lbd_backend.LBD_REST_locationdata.models import MetaDocument, DataId, MetaData
from lbd_backend.utils import combine_data_meta, s_codes

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
        try:
            datatemp = json.loads(handlerinterface.get_by_handler_id(kwargs["resource"]))

            try:
                metatemp = json.loads(MetaDocument.objects().get(open_data_id__document_id=kwargs["resource"]).to_json())
            except mongoengine.DoesNotExist:
                metatemp = " "
            except mongoengine.MultipleObjectsReturned:
                return HttpResponse(status=s_codes["NOTFOUND"])

            combined = combine_data_meta(datatemp, metatemp)
            result = json.dumps(combined)

            return HttpResponse(content=result, status=s_codes["OK"], content_type="application/json")
        except (mongoengine.DoesNotExist, mongoengine.MultipleObjectsReturned) as e:
            return HttpResponse(status=s_codes["NOTFOUND"])


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
    if request.method == "GET":
        urlmini = request.GET.get("mini", "")
        if urlmini.lower() == "true":
            items = json.loads(handlerinterface.get_all_mini())
        else:
            items = json.loads(handlerinterface.get_all())

        itemlist = list()
        for item in items:
            try:
                temp = MetaDocument.objects().get(open_data_id__document_id=kwargs["id_field_name"]).to_json()
            except (mongoengine.DoesNotExist, mongoengine.MultipleObjectsReturned):
                temp = "{}"
            itemlist.append(combine_data_meta(item, json.loads(temp)))
            if  len(itemlist) == 1000:
                break

        return HttpResponse(content=json.dumps(itemlist), status=s_codes["OK"], content_type="application/json")

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
    if request.method == "GET":
        latitude = request.GET.get('latitude', None)
        longitude = request.GET.get('longitude', None)
        range = request.GET.get('range', None)

        if latitude is None or longitude is None:
            return HttpResponse(status=s_codes["NOTFOUND"])
        else:
            if range is None:
                cont = handlerinterface.get_near(latitude, longitude)
            else:
                cont = handlerinterface.get_near(latitude, longitude, range)

            datajson = json.loads(cont)
            combined = list()
            for item in datajson:
                try:
                    meta = MetaDocument.objects().get(open_data_id__document_id=
                                                      kwargs["id_field_name"]).to_json() #TODO Make the item["..."] generic by asking field name from handler
                except MetaDocument.DoesNotExist:
                    meta = "{}"
                temp = combine_data_meta(item, json.loads(meta))
                combined.append(temp)
            combinedjson = json.dumps(combined)
            return HttpResponse(status=s_codes["OK"], content=combinedjson)

    elif request.method == "DELETE":
        latitude = request.GET.get('latitude', None)
        longitude = request.GET.get('longitude', None)
        range = request.GET.get('range', None)

        if latitude is None or longitude is None:
            return HttpResponse(status=s_codes["NOTFOUND"])
        else:
            if range is None:
                result = handlerinterface.delete_near(latitude, longitude)
            else:
                result = handlerinterface.delete_near(latitude, longitude, range)

            if result:
                return HttpResponse(status=s_codes["OK"])
            else:
                return HttpResponse(status=s_codes["NOTFOUND"])


@location_collection
@require_http_methods(["GET"])
def collection_inarea(request, *args, **kwargs):

    xtop_right = float(request.GET.get('xtopright'))
    ytop_right = float(request.GET.get('ytopright'))
    xbottom_left = float(request.GET.get('xbottomleft'))
    ybottom_left = float(request.GET.get('ybottomleft'))

    handlerinterface = kwargs["handlerinterface"]

    objects = handlerinterface.get_within_rectangle_mini(xtop_right, ytop_right, xbottom_left, ybottom_left)
    obsjson = json.loads(objects)
    itemlist = list()

    for item in obsjson:
        try:
            metob = MetaDocument.objects().get(open_data_id__document_id=item["id"]).to_json()
            metobjson = json.loads(metob)
        except mongoengine.DoesNotExist:
            metobjson = {}


        itemlist.append(combine_data_meta(item, metobjson))

    return HttpResponse(status=s_codes["OK"], content=json.dumps(itemlist), content_type="application/json")


def getjson(request):
    fac = HandlerFactory("Streetlights").create()

    return HttpResponse(fac.get_all_mini(), status=s_codes["OK"])


# @restifier
# def get_near(request):
#     (23.795199257764725, 61.503697166613755)
#
#     return HttpResponse(content="Jotain saattoi tapahtua")
