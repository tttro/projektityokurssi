# -*- coding: utf-8 -*-

__author__ = 'Aki Mäkinen'

import json
from django.http import HttpResponse
from django.views.decorators.http import require_http_methods
import mongoengine

from RESThandlers.HandlerInterface.Exceptions import CollectionNotInstalled
from RESThandlers.HandlerInterface.Factory import HandlerFactory
from lbd_backend.LBD_REST_messagedata.models import Message, Attachment
from lbd_backend.LBD_REST_users.models import User
from lbd_backend.utils import s_codes
from lbd_backend.LBD_REST_locationdata.decorators import this_is_a_login_wrapper_dummy


_messagecollection = {
    "type": "MessageCollection",
    "totalMessages": None,
    "messages": list()
}


@this_is_a_login_wrapper_dummy
@require_http_methods(["GET", "DELETE"])
def msg_single(request, *args, **kwargs):
    userdata = User.objects.get(user_id=kwargs["lbduser"].user_id)
    if request.method == "GET":
        try:
            item = Message.objects(recipient=userdata.email.lower(), mid=kwargs["message"]).as_pymongo()
            if len(item) > 1:
                return HttpResponse(status=s_codes["INTERNALERROR"])
            else:
                msg = item[0]
            msg.pop("_id")
            msg.pop("user_id")
        except mongoengine.DoesNotExist:
            return HttpResponse(status=s_codes["NOTFOUND"])

        return HttpResponse(content=json.dumps(_beautify_message(msg)), status=s_codes["OK"],
                            content_type="application/json; charset=utf-8")

    elif request.method == "DELETE":
        try:
            Message.objects(recipient=userdata.email.lower(), mid=kwargs["message"]).delete()
        except mongoengine.DoesNotExist:
            return HttpResponse(status=s_codes["NOTFOUND"])

        if Message.objects(recipient=userdata.email.lower(), mid=kwargs["message"]).count() == 0:
            return HttpResponse(status=s_codes["OK"])
        else:
            return HttpResponse(status=s_codes["INTERNALERROR"], content_type="application/json; charset=utf-8",
                                content='{"message": "Internal error. Something went wrong when '
                                        'trying to delete the message"}')


@this_is_a_login_wrapper_dummy
@require_http_methods(["GET", "DELETE"])
def msg_general(request, *args, **kwargs):
    userdata = User.objects.get(user_id=kwargs["lbduser"].user_id)
    print userdata.email
    single_msg = False
    query = dict()
    query["recipient"] = userdata.email.lower()
    if "category" in kwargs:
        installed_categories = HandlerFactory.get_installed()
        if kwargs["category"] not in installed_categories:
            return HttpResponse(status=s_codes["NOTFOUND"])

        query["category"] = kwargs["category"]
    if "message" in kwargs:
        try:
            query["mid"] = int(kwargs["message"])
        except ValueError:
            return HttpResponse(status=s_codes["BAD"])
        single_msg = True

    items = Message.objects(**query).as_pymongo()
    itemcount = len(items)
    #############################################################
    #
    # GET
    #
    #############################################################
    if request.method == "GET":
        temp = list()
        for item in items:
            temp.append(_beautify_message(item))

        if single_msg:
            if itemcount > 1:
                return HttpResponse(status=s_codes["INTERNALERROR"])
            elif itemcount == 0:
                return HttpResponse(status=s_codes["NOTFOUND"])
            else:
                resp = temp[0]
        else:
            resp = _messagecollection
            resp["totalMessages"] = len(temp)
            resp["messages"] = temp

        return HttpResponse(content=json.dumps(resp), status=s_codes["OK"], content_type="application/json; charset=utf-8")
    #############################################################
    #
    # DELETE
    #
    #############################################################
    elif request.method == "DELETE":
        if single_msg:
            if itemcount > 1:
                return HttpResponse(status=s_codes["INTERNALERROR"])
            elif itemcount == 0:
                return HttpResponse(status=s_codes["NOTFOUND"])
            else:
                pass

        Message.objects(**query).delete()
        if Message.objects(**query).count() == 0:
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
        att_fields = ["category", "id"]

        body = request.body
        content_json = json.loads(body)
        if not ("recipient" in content_json and "topic" in content_json and "message" in content_json):
            return HttpResponse(status=s_codes["BAD"], content='{"message": "Malformed MessageJSON"}',
                                content_type="application/json; charset=utf-8")
        if ("attachments" in content_json and "category" not in content_json):
            return HttpResponse(status=s_codes["BAD"], content='{"message": "Malformed MessageJSON"}',
                                content_type="application/json; charset=utf-8")

        del_from_msg = list()
        for field in content_json:
            if field not in msg_fields:
                del_from_msg.append(field)

        for item in del_from_msg:
            del content_json[item]

        if "attachments" in content_json:
            if not isinstance(content_json["attachments"], list):
                return HttpResponse(status=s_codes["BAD"],
                                            content='{"message": "Malformed MessageJSON."}',
                                            content_type="application/json; charset=utf-8")

            attlist = list()
            for item in content_json["attachments"]:
                temp = dict()
                try:
                    if item["category"] != content_json["category"]:
                        return HttpResponse(status=s_codes["BAD"],
                                            content='{"message": "Attachment item and message categories do not match."}',
                                            content_type="application/json; charset=utf-8")
                    hf = HandlerFactory(item["category"])
                    hinterface = hf.create()
                    result = hinterface.get_by_id(item["id"])

                    if result is None:
                        return HttpResponse(status=s_codes["BAD"],
                                            content='{"message": "Attachment item not found."}',
                                            content_type="application/json; charset=utf-8")

                    temp["aid"] = item["id"]
                    temp["category"] = item["category"]
                except KeyError:
                    return HttpResponse(status=s_codes["BAD"],
                                            content='{"message": "Malformed MessageJSON."}',
                                            content_type="application/json; charset=utf-8")

                except CollectionNotInstalled:
                    return HttpResponse(status=s_codes["BAD"],
                                            content='{"message": "Location collection not found."}',
                                            content_type="application/json; charset=utf-8")


                attlist.append(temp)
            content_json["attachments"] = attlist

        content_json["sender"] = userdata.email
        content_json["recipient"] = content_json["recipient"].lower()

        try:
            recipientdata = User.objects.get(email=content_json["recipient"])
        except mongoengine.DoesNotExist:
            return HttpResponse(content='{"message": "Recipient not found."}', status=s_codes["BAD"],
                                content_type="application/json; charset=utf-8")

        if True: # PLACEHOLDER
            try:
                temp = Message(**content_json)
                if "attachments" in content_json:
                    attlist = list()
                    for att in content_json["attachments"]:
                        attlist.append(Attachment(**att))
                    temp.attachments = attlist
                temp.save()
            except mongoengine.NotUniqueError:
                return HttpResponse(status=s_codes["INTERNALERROR"])
            except KeyError:
                return HttpResponse(status=s_codes["BAD"])

            return HttpResponse(status=s_codes["OK"])
        else:
            return HttpResponse(status=s_codes["BAD"])

def _beautify_message(msg):
    msg["type"] = "Message"
    msg.pop("_id")
    msg["id"] = msg.pop("mid")
    if "attachments" in msg:
        if len(msg["attachments"]) > 0:
            for item in msg["attachments"]:
                item["id"] = item.pop("aid")
        elif len(msg["attachments"]) == 0:
            del msg["attachments"]
    return msg