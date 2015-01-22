# -*- coding: utf-8 -*-
"""
.. _handlerinterface:


Decorators for location data REST
+++++++++++++++++++++++++++++++++
**This is the interface a new handler class has to implement**

.. module:: Handlers.Interface.base
    :platform: Unix, Windows
.. moduleauthor:: Aki Mäkinen <aki.makinen@outlook.com>

"""

__author__ = 'Aki Mäkinen'


class HandlerBase(object):
    """
    Class HandlerBase

    Interface and the base class for Handler classes. If duplication of the third party data source is required,
    the data structure should be defined in corresponding model for the handler class. For example of a model, see
    Streetlight handler model.

    If any method is unsupported, it should return NotImplementedError.
    """

    @property
    @staticmethod
    def handler_id(self):
        """
        Returns the id field name in the original data.

        :return: **String** JSON document containing the id field name. Contains field "identifier_field_name".
        """
        raise NotImplementedError("Not Implemented")

    def __init__(self):
        """
        If data duplication is required, the data model should be set in here.

        :return: -
        """
        raise NotImplementedError("Not Implemented")


    def update_db(self):
        """
        Gets content from rest and inserts to duplication database.
        Do not call if duplication database is not used.
        :exception GeneridDBError: Should be raised if something went wrong with the transaction.
        :exception NotImplementedError: Raised if the method is not implemented.
        :return: **Boolean** True if successful, False if failed.
        """
        raise NotImplementedError("Not Implemented")

    # Insert_to_db
    #
    def insert_to_db(self, jsonitem):
        """
        Inserts jsonItem to database.
        Content of jsonItem depends on the Handler implementation

        :param jsonitem: **Dictionary** Item to be added into the open data database in GeoJSON format
        :exception GeneridDBError: Should be raised if something went wrong with the transaction.
        :exception NotImplementedError: Raised if the method is not implemented.
        :return: **Boolean** True if successful, False if failed.
        """
        raise NotImplementedError("Not Implemented")


    def get_near(self, longitude, latitude, nrange):
        """
        Returns all items from database within range from each other

        :param longitude: **Float** Longitude of the center
        :param latitude: **Float** Latitude of the center
        :param nrange: **Float** Radius of the circle
        :exception NotImplementedError: Raised if the method is not implemented.
        :return: **Dictionary** Results as GeoJSON Feature Collection. (Even if no results)
        """
        raise NotImplementedError("Not Implemented")


    def get_within_rectangle(self, xtop_right, ytop_right, xbottom_left, ybottom_left, mini=False):
        """
        Returns all items inside a rectangular area.

        :param xtop_right: **Float** longitude of top left corner
        :param ytop_right: **Float** latitude of top left corner
        :param xbottom_left: **Float** longitude of bottom left corner
        :param ybottom_left: **Float** latitude of bottom left corner
        :param mini: **Boolean** Return only minimum amount of data
        :exception NotImplementedError: Raised if the method is not implemented.
        :return: **Dictionary** Returns a GeoJSON Feature Collection. (Even if no results)
        """
        raise NotImplementedError("Get_within_rectangle not supported or implemented.")


    def get_all(self, mini=False):
        """
        Return all items from REST or database as GeoJson feature collection.

        :param mini: **Boolean** Return only minimum amount of data
        :exception NotImplementedError: Raised if the method is not implemented.
        :return: **Dictionary** Returns a GeoJson Feature Collection. (Even if no results)
        """
        raise NotImplementedError("Not Implemented")


    def delete_all(self):
        """
        Delete all items in open data source or in local database.

        :exception GeneridDBError: Should be raised if something went wrong with the transaction.
        :exception NotImplementedError: Raised if the method is not implemented.
        :return: None
        """
        raise NotImplementedError("Not Implemented")


    def delete_item_by_id(self, iid):
        """
        Delete item by GeoJSON id.

        :param iid: **String** A id of a GeoJSON item
        :exception GeneridDBError: Should be raised if something went wrong with the transaction.
        :exception NotImplementedError: Raised if the method is not implemented.
        :return: None
        """
        raise NotImplementedError("Not Implemented")


    # Deletes all objects within range from a coordinate
    def delete_near(self, latitude, longitude, nrange):
        """
        Delete items within a circular area.

        :param latitude: **Float** Latitude of the center
        :param longitude: **Float** Longitude of the center
        :param nrange: **Float** Radius of the circle
        :exception GeneridDBError: Should be raised if something went wrong with the transaction.
        :exception NotImplementedError: Raised if the method is not implemented.
        :return: None
        """
        raise NotImplementedError("Deletion near location is not implemented")


    def get_item_count(self):
        """
        Returns the amount of items in open data source.

        :exception NotImplementedError: Raised if the method is not implemented.
        :exception ObjectNotFound: Raised if no results.
        :return: **Integer** Amount of items
        """
        raise NotImplementedError("Not Implemented")


    def get_by_id(self, iid):
        """
        Get item by GeoJSON id.

        :param iid: **String** GeoJSON id of the object
        :exception NotImplementedError: Raised if the method is not implemented.
        :return: **Dictionary** GeoJSON Feature
        """
        raise NotImplementedError("Not Implemented")

    def search(self, phrase, limit, field):
        """
        Search from the data source.

        :param phrase: **String** Search phrase
        :param limit: **Integer** Limit the amount of results to *limit*
        :param field: **String** Limit to a certain field. If not specified or set as None,
                      then search from all fields **Default:** *None*.
        :return: **Dictionary** GeoJSON Feature Collection. (Even if no results)
        """
        raise NotImplementedError("Search from REST not Implemented")

    def get_field_names(self):
        """
        Returns a flattened version of data source document structure.

        :return: **List** List of fields in data source document.
        """
        raise NotImplementedError("This function cannot be called for the base class")