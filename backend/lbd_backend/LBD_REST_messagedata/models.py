# -*- coding: utf-8 -*-

"""
Model for message
++++++++++++++++++++++++++++++++++++++++++++++++
.. module:: MessagedataREST.models
    :platform: Unix, Windows
    :synopsis: This module contains the message database descriptions
.. moduleauthor:: Aki Mäkinen <aki.makinen@outlook.com>

"""
__author__ = 'Aki Mäkinen'

from mongoengine import *


class Attachment(EmbeddedDocument):
    """
    Fields:

    * category: StringField **REQUIRED**
    * aid: StringField **REQUIRED**

    **category** is the name of the locationdata collection to which the attachment refers.

    **aid** is the id (not mongodb id, but the back-end's id) of the object to which the attachment refers ("foreignkey").

    """
    category = StringField(required=True)
    aid = StringField(required=True)


class Message(Document):
    """
    Fields:

    * mid: SequenceField **AUTOMATIC**
    * category: StringField
    * sender: EmailField **REQUIRED**
    * recipient: EmailField **REQUIRED**
    * attachments: ListField(EmbeddedDocumentField(Attachment))
    * topic: StringField **REQUIRED**
    * message: StringField **REQUIRED**
    * messageread: BooleanField **REQUIRED**
    * timestamp: IntField **REQUIRED**

    **Mid** is the id of the message. Generated automatically.

    **Category** is the name of the locationdata collection to which the message refers.

    **Sender** is the email address of the sender.

    **Recipient** is the email address of the recipient.

    **Attachment** is a list of Attachment objects.

    **Topic** is the topic of the message.

    **Message** is the message content.

    **Messageread** tells if the message has been read or not. (True or False) (False by default)

    **Timestamp** tells when the message was sent. Timestamp is in seconds from Unix Epoch on January 1st, 1970 at UTC.

    """
    mid = SequenceField()
    category = StringField()
    sender = EmailField(required=True)
    recipient = EmailField(required=True)
    attachments = ListField(EmbeddedDocumentField(Attachment))
    topic = StringField(required=True)
    message = StringField(required=True)
    timestamp = IntField(required=True)
    messageread = BooleanField(default=False)
