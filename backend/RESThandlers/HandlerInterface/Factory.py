__author__ = 'xc-'

from RESThandlers.Sources import _installedSources
from RESThandlers.HandlerInterface.Exceptions import CollectionNotInstalled


class HandlerFactory(object):
    def __init__(self, collection):
        if collection in _installedSources:
            self._collection_ = collection
        else:
            raise CollectionNotInstalled

    def create(self):
        return _installedSources[self._collection_]["handler"]()

    def get_id_field_name(self):
        return _installedSources[self._collection_]["id_field"]