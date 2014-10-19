__author__ = 'xc-'

# Class HandlerBase
# Interface and base for Handler classes. If REST or duplication database does not support a function (for example
# fetching items near a coordinate), it should not be implemented. Unsupported or unimplemented features raise
# NotImplementedError exception
class HandlerBase(object):
    @property
    @staticmethod
    def handler_id(self):
        raise NotImplementedError("Not Implemented")

    def __init__(self):
        raise NotImplementedError("Not Implemented")

    # Update db
    # Gets content from rest and inserts to duplication database.
    # Do not call if duplication database is not used.
    def update_db(self):
        raise NotImplementedError("Not Implemented")

    # Insert_to_db
    # Inserts jsonItem to database.
    # Content of jsonItem depends on the Handler implementation
    def insert_to_db(self, jsonItem):
        raise NotImplementedError("Not Implemented")

    # Get_near
    # Parameters:
    #       float latitude
    #       float lognitude
    #       float range
    # Returns all items from database within range from each other
    # Return a JSON string
    def get_near(self, latitude, longitude, range):
        raise NotImplementedError("Not Implemented")

    # Get_within_rectangle:
    # Parameters:
    #       float xtop: longitude of top left corner
    #       float ytop: latitude of top left corner
    #       float xbottom: longitude of bottom left corner
    #       float ybottom: latitude of bottom left corner
    # Returns all items within a defined rectangle
    # Returns a JSON string
    def get_within_rectangle(self, xtop, ytop, xbottom, ybottom):
        raise NotImplementedError("Get_within_rectangle not supported or implemented.")

    # Get_all
    # Returns all items in REST or in duplication database
    def get_all(self):
        raise NotImplementedError("Not Implemented")

    # Delete all
    # Return values:
    #       Boolean: True if all were deleted, False if objects remain in db after deletion
    def delete_all(self):
        raise NotImplementedError("Not Implemented")

    # Delete_item_by_handler_json
    # Parameters:
    #       string jsonitem: json string containing the item to be deleted from REST or duplication database
    # Deletes the given item from REST or duplication database
    # No return value
    def delete_item_by_handler_json(self, jsonitem):
        raise NotImplementedError("Not Implemented")

    # Delete_item_by_handler_id
    # Parameters:
    #       string iid: id used in the REST to identify different objects
    # Deletes the item with the given id from REST
    def delete_item_by_handler_id(self, iid):
        raise NotImplementedError("Not Implemented")


    # Function delete_item_by_id
    # Parameters:
    #       iid: a MongoDB id
    # Return values:
    #       Boolean: True if one item was deleted, False if deletion failed
    def delete_item_by_id(self, iid):
        raise NotImplementedError("Not Implemented")

    # Delete_near
    # Parameters:
    #       float latitude
    #       float longitude
    #       float range
    # Deletes all objects within range from a coordinate
    def delete_near(self, latitude, longitude, range):
        raise NotImplementedError("Deletion near location is not implemented")

    # Get_item_count
    # Returns REST object count (or the amount of objects in duplication database)
    def get_item_count(self):
        raise NotImplementedError("Not Implemented")

    # Get_by_id
    # Parameters:
    #       string iid: a database id of the object
    # Returns the object as a JSON string
    def get_by_id(self, iid):
        raise NotImplementedError("Not Implemented")

    # Get_by_handler_json
    # Parameters:
    #       string jsonitem:
    def get_by_handler_json(self, jsonitem):
        raise NotImplementedError("Not Implemented")


    def get_by_handler_id(self, iid):
        raise NotImplementedError("Not Implemented")

    def search_from_rest(self, phrase):
        raise NotImplementedError("Search fw wrom REST not Implemented")