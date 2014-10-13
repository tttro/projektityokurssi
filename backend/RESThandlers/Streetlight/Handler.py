__author__ = 'xc-'

import urllib
import json

from RESThandlers.HandlerInterface.HandlerBaseClass import HandlerBase
from RESThandlers.Streetlight.models import Streetlights, Geometry, Properties


class StreetlightHandler(HandlerBase):
    @property
    @staticmethod
    def handler_id(self):
        return json.dumps({
            "identifier_field_name": "feature_id"
        })


    def __init__(self):
        self.modelobject = Streetlights



    def update_db(self):
        req = urllib.urlopen('http://tampere.navici.com/tampere_wfs_geoserver/opendata/ows?service=WFS&version=1.0.0&request=GetFeature&typeName=opendata:WFS_KATUVALO&outputFormat=json&srsName=EPSG:4326', proxies={})
        data = json.loads(req.read())
        return self.insert_to_db(data)


    def insert_to_db(self, jsonItem):
        itemsInserted = 0
        for item in jsonItem["features"]:
            temp = Streetlights(type=item["type"],
                                  feature_id=item["id"],
                                  geometry=Geometry(type=item["geometry"]["type"],
                                                    coordinates=item["geometry"]["coordinates"]
                                  ),
                                  geometry_name=item["geometry_name"],
                                  properties=Properties(KATUVALO_ID=item["properties"]["KATUVALO_ID"],
                                                        NIMI=item["properties"]["NIMI"],
                                                        TYYPPI_KOODI=item["properties"]["TYYPPI_KOODI"],
                                                        TYYPPI=item["properties"]["TYYPPI"],
                                                        LAMPPU_TYYPPI_KOODI=item["properties"]["LAMPPU_TYYPPI_KOODI"],
                                                        LAMPPU_TYYPPI=item["properties"]["LAMPPU_TYYPPI"]
                                  )
            )
            temp.save()
            itemsInserted += 1

        return itemsInserted


    def get_near(self, latitude, longitude, range=0.001):
        results = self.modelobject.objects(geometry__geo_within_center=[(float(latitude), float(longitude)), range]).to_json()
        return results


    def get_all(self):
        return self.modelobject.objects().to_json()

    # Return values:
    #       Boolean: True if all were deleted, False if objects remain in db after deletion
    def delete_all(self):
        obj = self.modelobject.objects()
        items = obj.count()
        deleted = obj.delete()
        if items == deleted:
            return True
        else:
            return False

    # Function delete_item
    # Parameters:
    #       jsonitem: a Python json object (returned by json.loads())
    def delete_item_by_handler_json(self, jsonitem):
        self.modelobject.objects().get(feature_id=jsonitem["feature_id"]).delete()


    def delete_item_by_handler_id(self, iid):
        self.modelobject.objects().get(feature_id=iid).delete()


    # Function delete_item_by_id
    # Parameters:
    #       iid: a MongoDB id
    def delete_item_by_id(self, iid):
        result = self.modelobject.objects().get(id=iid).delete()


    # Function get_item_count
    # Return values:
    #       Integer: number of items in table
    def get_item_count(self):
        return self.modelobject.objects().count()


    def get_by_id(self, iid):
        return self.modelobject.objects(id=iid).to_json()


    def get_by_handler_id(self, iid):
        return self.modelobject.objects().get(feature_id=iid).to_json()


    def get_by_handler_json(self, jsonitem):
        return self.modelobject.objects().get(feature_id=jsonitem["feature_id"]).to_json()

