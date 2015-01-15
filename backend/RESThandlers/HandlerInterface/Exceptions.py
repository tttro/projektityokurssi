# -*- coding: utf-8 -*-
"""
.. module:: Handlers.Interface.Exceptions
    :platform: Unix, Windows
.. moduleauthor:: Aki Mäkinen <aki.makinen@outlook.com>

"""
__author__ = 'Aki Mäkinen'

from exceptions import Exception

class ObjectNotFound(Exception):
    pass

class MultipleObjectsFound(Exception):
    pass

class GenericDBError(Exception):
    pass

class CollectionNotInstalled(Exception):
    pass
