__author__ = 'xc-'

import mongoengine


class MetaData(mongoengine.DynamicEmbeddedDocument):
    status = mongoengine.StringField()


class MetaDocument(mongoengine.DynamicDocument):
    feature_id = mongoengine.StringField(unique=True, required=True)
    collection = mongoengine.StringField(required=True)
    meta_data = mongoengine.EmbeddedDocumentField(MetaData)
