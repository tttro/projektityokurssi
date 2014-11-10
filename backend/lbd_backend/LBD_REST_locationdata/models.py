# -*- coding: utf-8 -*-

"""
Model containing the metadata database structure
++++++++++++++++++++++++++++++++++++++++++++++++
.. module:: LocationdataREST.modules
    :platform: Unix, Windows
    :synopsis: This module contains the metadata database descriptions
.. moduleauthor:: Aki Mäkinen <aki.makinen@outlook.com>

"""

import mongoengine

class MetaData(mongoengine.DynamicEmbeddedDocument):
    """
    FOOBAR

    .. class:: MetaData
    """
    status = mongoengine.StringField(required=True)
    modified = mongoengine.IntField()
    modifier = mongoengine.StringField()

class MetaDocument(mongoengine.DynamicDocument):
    """
    FOOBAR

    .. class:: MetaDocument
    """
    feature_id = mongoengine.StringField(unique=True, required=True)
    collection = mongoengine.StringField(required=True)
    meta_data = mongoengine.EmbeddedDocumentField(MetaData)
