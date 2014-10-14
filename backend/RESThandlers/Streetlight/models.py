from mongoengine import *

class Geometry(EmbeddedDocument):
    type = StringField()
    coordinates = ListField()

class Properties(EmbeddedDocument):
    KATUVALO_ID = IntField()
    NIMI = StringField()
    TYYPPI_KOODI = StringField()
    TYYPPI = StringField()
    LAMPPU_TYYPPI_KOODI = StringField()
    LAMPPU_TYYPPI = StringField()

class Streetlights(Document):
    type = StringField()
    feature_id = StringField(unique=True)
    geometry =  EmbeddedDocumentField(Geometry)
    geometry_name = StringField()
    properties = EmbeddedDocumentField(Properties)

