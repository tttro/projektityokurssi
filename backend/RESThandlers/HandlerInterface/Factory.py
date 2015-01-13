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
        """
        Sets the collection.

        :param collection: **String** Collection name
        """
        if collection in _installedSources:
            self._collection_ = collection
        else:
            raise CollectionNotInstalled

    def create(self):
        """
        Creates and return a handler object.

        :return: Handler object
        """
        return _installedSources[self._collection_]["handler"]()

    @staticmethod
    def get_installed():
        """
        **Static**

        This method returns the installed open data services as dictionary where "name" is the name of the service (used
        when creating a new handler object) and "fields" tells what elements the service provides.
        :return: dictionary
        """
        collectionlist = {}
        for k, v in _installedSources.iteritems():
            instance = _installedSources[k]["handler"]()
            instance_fields = instance.get_field_names()
            collectionlist[k] = {
                "name": v["name"],
                "fields": instance_fields
            }

        return collectionlist
