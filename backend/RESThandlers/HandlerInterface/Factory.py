# -*- coding: utf-8 -*-
"""
.. module:: Handlers.Interface.Factory
    :platform: Unix, Windows
.. moduleauthor:: Aki Mäkinen <aki.makinen@outlook.com>

"""
__author__ = 'Aki Mäkinen'

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

    @staticmethod
    def get_installed():
        collectionlist = {}
        for k, v in _installedSources.iteritems():
            instance = _installedSources[k]["handler"]()
            instance_fields = instance.get_field_names()
            collectionlist[k] = {
                "name": v["name"],
                "fields": instance_fields
            }

        return collectionlist
