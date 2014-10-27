__author__ = 'xc-'

import mongoengine


class DataId(mongoengine.EmbeddedDocument):
    id_field_name = mongoengine.StringField()
    document_id = mongoengine.StringField()


class MetaData(mongoengine.DynamicEmbeddedDocument):
    status = mongoengine.StringField()


class MetaDocument(mongoengine.Document):
    open_data_id = mongoengine.EmbeddedDocumentField(DataId)
    meta_data = mongoengine.EmbeddedDocumentField(MetaData)
    collection = mongoengine.StringField()