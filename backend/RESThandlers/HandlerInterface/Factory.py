__author__ = 'xc-'
from RESThandlers.Sources import _installedSources


class HandlerFactory(object):
    def __init__(self, collection):
        self._collection_ = collection


    def create(self):
        return _installedSources[self._collection_]["handler"]()


    def get_id_field_name(self):
        return _installedSources[self._collection_]["id_field"]