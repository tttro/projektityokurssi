# -*- coding: utf-8 -*-

"""
Model containing the metadata database structure
++++++++++++++++++++++++++++++++++++++++++++++++
.. module:: LocationdataREST.models
    :platform: Unix, Windows
    :synopsis: This module contains the metadata database descriptions
.. moduleauthor:: Aki Mäkinen <aki.makinen@outlook.com>

"""

import mongoengine

class MetaData(mongoengine.DynamicEmbeddedDocument):
    """
    Fields:

    * status: StringField **REQUIRED**
    * modified: IntField **REQUIRED** **AUTOMATIC**
    * modifier: IntField **REQUIRED** **AUTOMATIC**

    **Status** is a string describing the status of the object. It is always required if metadata for the
    object is defined.

    **Modified** is a timestamp (seconds from epoch) and is generated automatically by the system. Always required.

    **Modifier** is the id of the user that modified the metadata item. Always required, inserted by the system.

    """
    status = mongoengine.StringField(required=True)
    modified = mongoengine.IntField(required=True)
    modifier = mongoengine.StringField(required=True)

class MetaDocument(mongoengine.DynamicDocument):
    """
    Fields

    * feature_id: StringField **REQUIRED** **UNIQUE**
    * collection: StringField **REQUIRED**
    * meta_data: EmbeddeDocumentField(MetaData) **REQUIRED**

    **Feature_id** is a string that combines the metadata to an object. Simulates a foreign key.

    **Collection_id** is a string that tells the collection where the metadata belongs.

    **Meta_data** is an embedded document.

    """
    feature_id = mongoengine.StringField(unique=True, required=True)
    collection = mongoengine.StringField(required=True)
    meta_data = mongoengine.EmbeddedDocumentField(MetaData, required=True)
