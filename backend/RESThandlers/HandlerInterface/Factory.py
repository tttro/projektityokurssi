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
