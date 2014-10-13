__author__ = 'xc-'


class HandlerBase(object):
    @property
    @staticmethod
    def handler_id(self):
        raise NotImplementedError("Not Implemented")

    def __init__(self):
        raise NotImplementedError("Not Implemented")


    def update_db(self):
        raise NotImplementedError("Not Implemented")


    def insert_to_db(self, jsonItem):
        raise NotImplementedError("Not Implemented")


    def get_near(self, latitude, longitude, range):
        raise NotImplementedError("Not Implemented")


    def get_all(self):
        raise NotImplementedError("Not Implemented")

    # Return values:
    #       Boolean: True if all were deleted, False if objects remain in db after deletion
    def delete_all(self):
        raise NotImplementedError("Not Implemented")


    def delete_item_by_handler_json(self, jsonitem):
        raise NotImplementedError("Not Implemented")


    def delete_item_by_handler_id(self, iid):
        raise NotImplementedError("Not Implemented")


    # Function delete_item_by_id
    # Parameters:
    #       iid: a MongoDB id
    # Return values:
    #       Boolean: True if one item was deleted, False if deletion failed
    def delete_item_by_id(self, iid):
        raise NotImplementedError("Not Implemented")


    def delete_near(self, latitude, longitude, range):
        raise NotImplementedError("Deletion near location is not implemented")


    def get_item_count(self):
        raise NotImplementedError("Not Implemented")


    def get_by_id(self, iid):
        raise NotImplementedError("Not Implemented")


    def get_by_handler_json(self, jsonitem):
        raise NotImplementedError("Not Implemented")


    def get_by_handler_id(self, iid):
        raise NotImplementedError("Not Implemented")

    def search_from_rest(self, phrase):
        raise NotImplementedError("Search from REST not Implemented")