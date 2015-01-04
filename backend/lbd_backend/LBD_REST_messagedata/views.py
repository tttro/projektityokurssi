# -*- coding: utf-8 -*-
import json
from django.http import HttpResponse
import mongoengine
from lbd_backend.LBD_REST_locationdata.models import MetaDocument
from lbd_backend.LBD_REST_messagedata.models import Message, Attachment
from lbd_backend.LBD_REST_users.models import User
from lbd_backend.utils import s_codes

__author__ = 'Aki Mäkinen'

from django.views.decorators.http import require_http_methods
from lbd_backend.LBD_REST_locationdata.decorators import this_is_a_login_wrapper_dummy

@this_is_a_login_wrapper_dummy
@require_http_methods(["GET", "DELETE"])
def msg_single(request, *args, **kwargs):
    userdata = User.objects.get(user_id=kwargs["lbduser"].user_id)
    if request.method == "GET":
        item = Message.objects(recipient=userdata.email, mid=kwargs["message"]).as_pymongo()
        return HttpResponse(content=json.dumps(item), status=s_codes["OK"], content_type="application/json")
    elif request.method == "DELETE":
        item = Message.objects(recipient=userdata.email, mid=kwargs["message"]).delete()
        return HttpResponse(status=s_codes["OK"])


@this_is_a_login_wrapper_dummy
@require_http_methods(["GET", "DELETE"])
def msg_collection(request, *args, **kwargs):

    userdata = User.objects.get(user_id=kwargs["lbduser"].user_id)
    print userdata.email
    #############################################################
    #
    # GET
    #
    #############################################################
    if request.method == "GET":
        items = Message.objects(recipient=userdata.email).as_pymongo()
        temp = list()
        for item in items:
            item.pop("_id")
            temp.append(item)
        return HttpResponse(content=json.dumps(temp), status=s_codes["OK"], content_type="application/json")
    #############################################################
    #
    # DELETE
    #
    #############################################################
    elif request.method == "DELETE":
        Message.objects(recipient=userdata.email).delete()
        if Message.objects(recipient=userdata.email).count() == 0:
            return HttpResponse(status=s_codes["OK"])
        else:
            return HttpResponse(status=s_codes["INTERNALERROR"])


@this_is_a_login_wrapper_dummy
@require_http_methods(["POST"])
def msg_send(request, *args, **kwargs):
    userdata = User.objects.get(user_id=kwargs["lbduser"].user_id)
    print userdata.email
    #############################################################
    #
    # POST
    #
    #############################################################
    if request.method == "POST":
        msg_fields = ["category", "recipient", "attachments", "topic", "message"]
        att_fields = ["category", "aid"]

        body = request.body
        content_json = json.loads(body)
        if not ("recipient" in content_json and "topic" in content_json and "message" in content_json):
            return HttpResponse(status=s_codes["BAD"])

        del_from_msg = list()
        for field in content_json:
            if field not in msg_fields:
                del_from_msg.append(field)

        for item in del_from_msg:
            del content_json[item]

        del_from_att = list()
        if "attachments" in content_json:
            for item in content_json["attachments"]:
                if not ("category" in item or "aid" in item):
                    return HttpResponse(status=s_codes["BAD"])
                for fieldname, value in item.iteritems:
                    if fieldname not in att_fields:
                        del_from_att.append(fieldname)
                    else:
                        try:
                            MetaDocument.objects.get(feature_id=value)
                        except mongoengine.DoesNotExist:
                            return HttpResponse(status=s_codes["BAD"],
                                                content_json='{"message": "Attachment item not found"}',
                                                content_type="application/json")

        for item in del_from_att:
            try:
                index = content_json["attachments"].index(item)
                del content_json["attachments"][index]
            except ValueError:
                pass
        content_json["sender"] = userdata.email
        content_json["recipient"] = content_json["recipient"].lower()
        try:
            recipientdata = User.objects.get(email=content_json["recipient"])
        except mongoengine.DoesNotExist:
            return HttpResponse(content="Recipient does not exist", status=s_codes["BAD"])

        if True: # PLACEHOLDER
            try:
                attlist = list()
                for att in content_json["attachments"]:
                    attlist.append(Attachment(**att))
                temp = Message(**content_json)
                temp.attachements = attlist
                temp.save()
            except mongoengine.NotUniqueError:
                pass
            except KeyError:
                return HttpResponse(status=s_codes["BAD"])

            return HttpResponse(status=s_codes["OK"])
        else:
            return HttpResponse(status=s_codes["BAD"])
