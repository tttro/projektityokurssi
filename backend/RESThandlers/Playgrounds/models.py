# -*- coding: utf-8 -*-
"""
.. module:: Handlers.Playgrounds.models
    :platform: Unix, Windows
.. moduleauthor:: Aki Mäkinen <aki.makinen@outlook.com>

"""

__author__ = 'Aki Mäkinen'

from mongoengine import *

class Geometry(EmbeddedDocument):
    type = StringField()
    coordinates = ListField()

class Properties(EmbeddedDocument):
    VIHERALUEEN_OSAN_ID = IntField()
    ALUE_NIMI = StringField()
    OSA_ALUE_NIMI = StringField()
    ALUE_SIJ = StringField()
    ALUEEN_OSAN_SIJ = StringField()
    PINTA_ALA = IntField()
    TILAAJA = StringField()
    KUNNOSSAPITAJA = StringField()
    URAKKA_ALUE = StringField()
    ERITYISKAYTTO = StringField()
    KAYTTOLK = StringField()
    KAUPUNGINOSA = StringField()
    TOIMLK = StringField()
    HOITOLK = StringField()
    KP_KAUSI = StringField()

class Playgrounds(Document):
    type = StringField()
    feature_id = StringField(unique=True)
    geometry =  EmbeddedDocumentField(Geometry)
    geometry_name = StringField()
    properties = EmbeddedDocumentField(Properties)
