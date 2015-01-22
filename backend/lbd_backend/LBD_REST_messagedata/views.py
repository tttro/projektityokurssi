# -*- coding: utf-8 -*-

"""
View for handling messages
++++++++++++++++++++++++++
.. module:: MessagedataREST.views
    :platform: Unix, Windows
    :synopsis: This module handles http requests related to message data.
.. moduleauthor:: Aki Mäkinen <aki.makinen@outlook.com>

"""
__author__ = 'Aki Mäkinen'

import time
import json

import mongoengine
from django.http import HttpResponse
from django.views.decorators.http import require_http_methods

from RESThandlers.HandlerInterface.Exceptions import CollectionNotInstalled, ObjectNotFound
from RESThandlers.HandlerInterface.Factory import HandlerFactory
from lbd_backend.LBD_REST_messagedata.models import Message, Attachment
from lbd_backend.LBD_REST_users.models import User
from lbd_backend.utils import s_codes
from lbd_backend.LBD_REST_locationdata.decorators import lbd_require_login


_messagecollection = {
    "type": "MessageCollection",
    "totalMessages": None,
    "messages": list()
}


@lbd_require_login
@require_http_methods(["GET", "DELETE"])
def msg_general(request, *args, **kwargs):
    """
    Handles all message requests (both to single and multiple messages).

    **Supported HTTP methods:**

    * GET
    * DELETE

    :param request: Request object
    :param args: arguments
    :param kwargs: Dictionary (keyword arguments). Known kwargs listed below.

    **The method uses the following kwargs:**

    * category (String)
    * message (Integer)
    * lbduser (User)

    *Category* specifies the message category. This argument is used only if it is specified in the url. Category
    is equivalent to locationdata collection. If this argument is used, it is expected that a locationdata collection
    with the same name exists and is "installed".

    *Message* specifies the message id. Used only if specified in the url.

    :return: HTTP response. Possible statuses are listed in module documentation

    """
    userdata = User.objects.get(user_id=kwargs["lbduser"].user_id)
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


@lbd_require_login
@require_http_methods(["POST"])
def msg_send(request, *args, **kwargs):
    """
    View for sending messages.

    **Supported HTTP methods:**

    * POST

    :param request: Request object
    :param args: arguments
    :param kwargs: Dictionary (keyword arguments). Known kwargs listed below.

    **The method uses the following kwargs:**

    * lbduser (User)

    :return: HTTP response. Possible statuses are listed in module documentation

    """
    userdata = User.objects.get(user_id=kwargs["lbduser"].user_id)
    #############################################################
    #
    # POST
    #
    #############################################################
    if request.method == "POST":
        msg_fields = ["category", "recipient", "attachments", "topic", "message"]
        att_fields = ["category", "id"]

        body = request.body
        try:
            content_json = json.loads(body)
        except ValueError:
            return HttpResponse(status=s_codes["INTERNALERROR"])

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
                    try:
                        result = hinterface.get_by_id(item["id"])
                    except ObjectNotFound:
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
                temp.timestamp = int(time.time())
                temp.save()
            except mongoengine.NotUniqueError:
                return HttpResponse(status=s_codes["INTERNALERROR"])
            except KeyError:
                return HttpResponse(status=s_codes["BAD"])

            return HttpResponse(status=s_codes["OK"])
        else:
            return HttpResponse(status=s_codes["BAD"])


@lbd_require_login
@require_http_methods(["GET"])
def mark_as_read(request, *args, **kwargs):
    """
    View for marking a message read.

    **Supported HTTP methods:**

    * GET

    :param request: Request object
    :param args: arguments
    :param kwargs: Dictionary (keyword arguments). Known kwargs listed below.

    **The method uses the following kwargs:**

    * message (Integer)
    * lbduser (User)

    *Message* specifies the message id. Required.

    :return: HTTP response. Possible statuses are listed in module documentation

    """
    userdata = User.objects.get(user_id=kwargs["lbduser"].user_id)

    query = dict()
    query["recipient"] = userdata.email.lower()
    if "message" in kwargs:
        try:
            query["mid"] = int(kwargs["message"])
        except ValueError:
            return HttpResponse(status=s_codes["BAD"], content_type="application/json; charset=utf-8",
                            content='{"message": "Invalid message id."}')
    try:
        item = Message.objects.get(**query)
    except mongoengine.DoesNotExist:
        return HttpResponse(status=s_codes["NOTFOUND"], content_type="application/json; charset=utf-8",
                            content='{"message": "Message not found."}')

    item.messageread = True
    item.save()

    return HttpResponse(status=s_codes["OK"])


def _beautify_message(msg):
    """
    Function for cleaning the received message. Removes everything not allowed by the specification, adds some required
    fields and renames some to follow the specification.

    :param msg: Received message as dictionary

    :return: Cleaned message.

    """
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