import json
import mongoengine
from django.http import Http404
from django.shortcuts import HttpResponse
from django.views.decorators.http import require_http_methods

from RESThandlers.HandlerInterface.Factory import HandlerFactory

from lbd_backend.LBD_REST_locationdata.decorators import restifier
from lbd_backend.LBD_REST_locationdata.models import MetaDocument, DataId, MetaData
from lbd_backend.utils import combine_data_meta

@restifier
@require_http_methods(["GET", "DELETE", "PUT"])
def single_resource(request, *args, **kwargs):
    handlerinterface = kwargs["handlerinterface"]

    if "resource" not in kwargs:
        return HttpResponse(status=404)

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
                return HttpResponse(status=404)

            combined = combine_data_meta(datatemp, metatemp)
            result = json.dumps(combined)

            return HttpResponse(content=result, status=200)
        except (mongoengine.DoesNotExist, mongoengine.MultipleObjectsReturned) as e:
            return HttpResponse(status=404)
        except NotImplementedError:
            return HttpResponse(status=418)


    elif request.method == "DELETE":
        # DUMMY FOR NOW
        return HttpResponse(status=418)

        # try:
        #     metaobject = MetaDocument.objects.get(open_data_id__document_id=kwargs["resource"])
        # except MetaDocument.DoesNotExist:
        #     metaobject = None
        #
        # try:
        #     odobject = handlerinterface.get_by_handler_id(kwargs["resource"]) #TODO: Add existance check to handler interface
        # except NotImplementedError:
        #     odobject = None
        #
        # if odobject is None:
        #     return HttpResponse(status=404)
        # else:
        #     odobject.delete()
        #     if metaobject is not None:
        #         metaobject.delete()
        #     return HttpResponse(status= 200)

    elif request.method == "PUT":
        body = request.body

        content_json = json.loads(body)
        try:
            temp = MetaDocument.objects().get(open_data_id__document_id=content_json["open_data_id"]["document_id"])
            temp.meta_data.status = content_json["meta_data"]["status"]
        except mongoengine.DoesNotExist:
            temp = MetaDocument(open_data_id=DataId(
                                                id_field_name=content_json["open_data_id"]["id_field_name"], #TODO: Check validity
                                                document_id=content_json["open_data_id"]["document_id"] #TODO: Check existance
                            ),
                            meta_data=MetaData(
                                                status=content_json["meta_data"]["status"]
                            )
            )
        except mongoengine.MultipleObjectsReturned:
            return HttpResponse(status=404)

        temp.save()
        return HttpResponse(status=200)


@restifier
@require_http_methods(["GET", "DELETE", "PUT", "POST"])
def collection(request, *args, **kwargs):
    handlerinterface = kwargs["handlerinterface"]
    if request.method == "GET":
        return HttpResponse(content=handlerinterface.get_all(), status=200)
    elif request.method == "DELETE":
        if handlerinterface.delete_all():
            return HttpResponse(status=200)
        else:
            return HttpResponse(status=404)
    elif request.method == "PUT":
        if handlerinterface.delete_all():
            pass # TODO: Insert clump of data from somewhere
        else:
            return HttpResponse(status=404) #TODO: Or some better code, like 500
    elif request.method == "POST":
        pass #TODO: Insert new item to db (Note: not only to metadata)
    return HttpResponse("")


@restifier
@require_http_methods(["GET", "DELETE"])
def collection_near(request, *args, **kwargs):
    handlerinterface = kwargs["handlerinterface"]
    if request.method == "GET":
        latitude = request.GET.get('latitude', None)
        longitude = request.GET.get('longitude', None)
        range = request.GET.get('range', None)

        if latitude is None or longitude is None:
            return HttpResponse(status=404)
        else:
            if range is None:
                cont = handlerinterface.get_near(latitude, longitude)
            else:
                cont = handlerinterface.get_near(latitude, longitude, range)

            datajson = json.loads(cont)
            combined = list()
            for item in datajson:
                try:
                    meta = MetaDocument.objects().get(open_data_id__document_id=item["feature_id"]).to_json() #TODO Make the item["..."] generic by asking field name from handler
                except MetaDocument.DoesNotExist:
                    meta = "{}"
                temp = combine_data_meta(item, json.loads(meta))
                combined.append(temp)
            combinedjson = json.dumps(combined)
            return HttpResponse(status=200, content=combinedjson)

    elif request.method == "DELETE":
        return HttpResponse(status=418) #TODO PLACEHOLDER
        # latitude = request.GET.get('latitude', None)
        # longitude = request.GET.get('longitude', None)
        # range = request.GET.get('range', None)
        #
        # if latitude is None or longitude is None:
        #     return HttpResponse(status=404)
        # else:
        #     if range is None:
        #         result = handlerinterface.delete_near(latitude, longitude)
        #     else:
        #         result = handlerinterface.delete_near(latitude, longitude, range)
        #
        #     if result:
        #         return HttpResponse(status=200)
        #     else:
        #         return HttpResponse(status=404)



def getjson(request):
    fac = HandlerFactory("Streetlights").create()
    fac.update_db()
    return HttpResponse("FOOBAR")
# @restifier
# def get_near(request):
#     (23.795199257764725, 61.503697166613755)
#
#     return HttpResponse(content="Jotain saattoi tapahtua")
