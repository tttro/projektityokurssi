from django.db import models

# Create your models here.

from mongoengine import *

class Attachment(EmbeddedDocument):
    category = StringField(required=True)
    aid = StringField(required=True)

class Message(Document):
    mid = SequenceField()
    category = StringField()
    sender = EmailField(required=True)
    recipient = EmailField(required=True)
    attachments = ListField(EmbeddedDocumentField(Attachment))
    topic = StringField(required=True)
    message = StringField(required=True)
    timestamp =