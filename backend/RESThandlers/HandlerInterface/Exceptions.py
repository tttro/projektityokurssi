__author__ = 'xc-'

from exceptions import Exception

class ObjectNotFound(Exception):
    pass

class MultipleObjectsFound(Exception):
    pass

class GenericDBError(Exception):
    pass