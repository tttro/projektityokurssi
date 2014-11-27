# -*- coding: UTF-8 -*-
import mongoengine

__author__ = 'Aki Mäkinen'

import httplib
import json

from unittest import TestCase
from lbd_backend.utils import geo_json_scheme_validation
from lbd_backend.LBD_REST_locationdata.tests.test_settings import url, port, lbdheader
#from lbd_backend.populate_meta import populate_meta

class TestCollectionGet(TestCase):
    def setUp(self):
        self.con = httplib.HTTPConnection(url, port)
        self.headers = lbdheader

    def test_get_valid_request(self):
        print "Running test: valid get request"
        self.con.request("GET", "/locationdata/api/Streetlights/",
                         headers=self.headers)
        response = self.con.getresponse()

        print "Response status code: "+str(response.status)
        self.assertEqual(response.status, 200)
        print "Test passed!"

    def test_get_invalid_collection(self):
        print "Running test: nonexistent collection"
        self.con.request("GET", "/locationdata/api/ÄÖÄÖLÄÖLP/",
                         headers=self.headers)
        response = self.con.getresponse()

        print "Response status code: "+str(response.status)
        self.assertEqual(response.status, 404)
        print "Test passed!"

    def test_get_valid_request_content_format(self):
        print "Running test: valid get request"
        self.con.request("GET", "/locationdata/api/Streetlights/",
                         headers=self.headers)
        response = self.con.getresponse()
        contentjson = json.loads(response.read())
        print "Response status code: "+str(response.status)
        self.assertTrue(geo_json_scheme_validation(contentjson))
        print "Test passed!"

    def test_get_valid_request_featurecount(self):
        print "Running test: check featurecount"
        self.con.request("GET", "/locationdata/api/Streetlights/",
                         headers=self.headers)
        response = self.con.getresponse()
        contentjson = json.loads(response.read())

        print "Response status code: "+str(response.status)
        try:
            featurecount = int(contentjson["totalFeatures"])
            self.assertEqual(featurecount, len(contentjson["features"]))
        except KeyError:
            print "No totalFeatures in GeoJSON. What now?"
        print "Test passed!"


class TestCollectionPost(TestCase):
    """
    Tests for function collection POST

    setUp assumes that deleting a single item works
    """
    def setUp(self):
        print "Preparing system for new test..."
        self.con = httplib.HTTPConnection(url, port)
        self.headers = lbdheader
        self.con.request("DELETE", "/locationdata/api/Streetlights/WFS_KATUVALO.405172",
                         headers=self.headers)
        response = self.con.getresponse()
        self.assertEqual(response.status, 200, "Prepare failed...")

        print "Reinitializing connection..."
        self.con = httplib.HTTPConnection(url, port)

        print "System prepared!"

    def test_post_valid_request(self):
        print "Running test: valid post"
        jsonstring ='{"geometry": {"type": "Point", "coordinates": [23.643239226767022, 61.519112683582854]}, "id": ' \
                    '"WFS_KATUVALO.405172", "type": "Feature", "properties": {"NIMI": "XPWR_6769212", ' \
                    '"LAMPPU_TYYPPI_KOODI": "100340", "TYYPPI_KOODI": "105007", "KATUVALO_ID": 405172, ' \
                    '"LAMPPU_TYYPPI": "ST 100 (SIEMENS)", "metadata": {"status": "teststatus", "modifier": ' \
                    '"HeliHumanisti", "modified": 1415705418}}, "geometry_name": "GEOLOC"}'

        self.con.request("POST", "/locationdata/api/Streetlights/", jsonstring,
                         headers=self.headers)
        response = self.con.getresponse()

        self.assertEqual(response.status, 200)
        print "Response status code: "+str(response.status)
        self.con = httplib.HTTPConnection(url, port)
        self.con.request("GET", "/locationdata/api/Streetlights/WFS_KATUVALO.405172",
                         headers=self.headers)

        response = self.con.getresponse()
        contentjson = json.loads(response.read())

        try:
            print contentjson["properties"]["metadata"]
        except KeyError:
            self.fail("Properties or metadata not in received GeoJSON")

        print "Test passed!"

    def test_post_invalid_resource(self):
        print "Running test: invalid resource"
        jsonstring ='{"geometry": {"type": "Point", "coordinates": [23.643239226767022, 61.519112683582854]}, "id": ' \
                    '"WFS_KATUVALO.999172", "type": "Feature", "properties": {"NIMI": "XPWR_6769212", ' \
                    '"LAMPPU_TYYPPI_KOODI": "100340", "TYYPPI_KOODI": "105007", "KATUVALO_ID": 405172, ' \
                    '"LAMPPU_TYYPPI": "ST 100 (SIEMENS)", "metadata": {"status": "teststatus", "modifier": ' \
                    '"HeliHumanisti", "modified": 1415705418}}, "geometry_name": "GEOLOC"}'

        self.con.request("POST", "/locationdata/api/Streetlights/", jsonstring,
                         headers=self.headers)
        response = self.con.getresponse()
        print "Response status code: "+str(response.status)
        self.assertEqual(response.status, 404)

        print "Test passed!"

    def test_post_missing_metadata(self):
        print "Running test: missing metadata"
        jsonstring ='{"geometry": {"type": "Point", "coordinates": [23.643239226767022, 61.519112683582854]}, "id": ' \
                    '"WFS_KATUVALO.405172", "type": "Feature", "properties": {"NIMI": "XPWR_6769212", ' \
                    '"LAMPPU_TYYPPI_KOODI": "100340", "TYYPPI_KOODI": "105007", "KATUVALO_ID": 405172, ' \
                    '"LAMPPU_TYYPPI": "ST 100 (SIEMENS)"}, "geometry_name": "GEOLOC"}'

        self.con.request("POST", "/locationdata/api/Streetlights/", jsonstring,
                         headers=self.headers)
        response = self.con.getresponse()
        print "Response status code: "+str(response.status)
        self.assertEqual(response.status, 400)

        print "Test passed!"

    def test_post_missing_id(self):
        print "Running test: missing id"
        jsonstring ='{"geometry": {"type": "Point", "coordinates": [23.643239226767022, 61.519112683582854]}, ' \
                    '"type": "Feature", "properties": {"NIMI": "XPWR_6769212", ' \
                    '"LAMPPU_TYYPPI_KOODI": "100340", "TYYPPI_KOODI": "105007", "KATUVALO_ID": 405172, ' \
                    '"LAMPPU_TYYPPI": "ST 100 (SIEMENS)", "metadata": {"status": "teststatus", "modifier": ' \
                    '"HeliHumanisti", "modified": 1415705418}}, "geometry_name": "GEOLOC"}'

        self.con.request("POST", "/locationdata/api/Streetlights/", jsonstring,
                         headers=self.headers)
        response = self.con.getresponse()
        print "Response status code: "+str(response.status)
        self.assertEqual(response.status, 400)

        print "Test passed!"


class TestCollectionDelete(TestCase):
    """
    Tests for function collection DELETE

    Tests assume that there is a way to populate metadata database for testing. Because of this,
    these tests should only be run locally, or manually if testing production server initial setup.
    """
    mongodb_name = "lbd_backend"

    @classmethod
    def setUpClass(cls):
        from mongoengine.connection import connect, disconnect
        disconnect()
        connect(cls.mongodb_name)

    @classmethod
    def tearDownClass(cls):
        from mongoengine.connection import disconnect
        disconnect()

    def setUp(self):
        from lbd_backend.populate_meta import populate_meta
        print "Preparing system for new test..."
        print "Populating metadata db..."
        populate_meta()
        self.con = httplib.HTTPConnection(url, port)
        self.headers = lbdheader

    def test_valid_request(self):
        print "Running test: valid delete request"
        self.con.request("DELETE", "/locationdata/api/Streetlights/",
                         headers=self.headers)
        response = self.con.getresponse()
        print "Response status code: "+str(response.status)

        self.assertEqual(response.status, 200)

        self.con = httplib.HTTPConnection(url, port)
        self.con.request("GET", "/locationdata/api/Streetlights/",
                         headers=self.headers)
        response = self.con.getresponse()
        contentjson = json.loads(response.read())

        try:
            for item in contentjson["features"]:
                try:
                    if "metadata" in item["properties"]:
                        self.fail("Metadata exists, deleting all failed")
                except KeyError:
                    pass
        except KeyError:
            self.fail("Error in accessing the GET response. Not valid geojson?")

        print "Test passed!"

    def test_invalid_collection(self):
        print "Running test: trying to delete nonexistent collection"
        self.con.request("DELETE", "/locationdata/api/ÖÖÖLLLÄÄÄ/",
                         headers=self.headers)
        response = self.con.getresponse()
        print "Response status code: "+str(response.status)
        self.assertEqual(response.status, 404)

        print "Test passed!"


class TestCollectionPut(TestCase):
    """
    Tests for function collection PUT

    Tests assume that deleting a collection works properly
    """
    mongodb_name = "lbd_backend"

    putjson_valid = '{"totalFeatures": 2, "type": "FeatureCollection", "features": [{"geometry": {"type": "Point", ' \
              '"coordinates": [23.643239226767022, 61.519112683582854]}, "id": "WFS_KATUVALO.405172", "type": ' \
              '"Feature", "properties": {"NIMI": "XPWR_6769212", "TYYPPI_KOODI": "105007", "KATUVALO_ID": 405172,' \
              '"metadata": {"status": "SNAFU", "modifier": "Seppo S\u00e4hk\u00e4ri", "modified": 1414665004}, ' \
              '"LAMPPU_TYYPPI": "ST 100 (SIEMENS)", "LAMPPU_TYYPPI_KOODI": "100340"}, "geometry_name": "GEOLOC"}, ' \
              '{"geometry": {"type": "Point", "coordinates": [23.643239226767022, 61.519112683582854]}, "id": ' \
              '"WFS_KATUVALO.405171", "type": "Feature", "properties": {"NIMI": "XPWR_6769211", "LAMPPU_TYYPPI_KOODI": ' \
              '"100340", "TYYPPI_KOODI": "105007", "KATUVALO_ID": 405171, "LAMPPU_TYYPPI": "ST 100 (SIEMENS)", ' \
              '"metadata": {"status": "SNAFU", "modifier": "Seppo S\u00e4hk\u00e4ri", "modified": 1414665004}}, ' \
              '"geometry_name": "GEOLOC"}]}'

    putjson_missing_meta = '{"totalFeatures": 2, "type": "FeatureCollection", "features": [{"geometry": {"type": "Point", ' \
              '"coordinates": [23.643239226767022, 61.519112683582854]}, "id": "WFS_KATUVALO.405172", "type": ' \
              '"Feature", "properties": {"NIMI": "XPWR_6769212", "TYYPPI_KOODI": "105007", "KATUVALO_ID": 405172,' \
              '"LAMPPU_TYYPPI": "ST 100 (SIEMENS)", "LAMPPU_TYYPPI_KOODI": "100340"}, "geometry_name": "GEOLOC"}, ' \
              '{"geometry": {"type": "Point", "coordinates": [23.643239226767022, 61.519112683582854]}, "id": ' \
              '"WFS_KATUVALO.405171", "type": "Feature", "properties": {"NIMI": "XPWR_6769211", "LAMPPU_TYYPPI_KOODI": ' \
              '"100340", "TYYPPI_KOODI": "105007", "KATUVALO_ID": 405171, "LAMPPU_TYYPPI": "ST 100 (SIEMENS)", ' \
              '"metadata": {"status": "SNAFU", "modifier": "Seppo S\u00e4hk\u00e4ri", "modified": 1414665004}}, ' \
              '"geometry_name": "GEOLOC"}]}'

    @classmethod
    def setUpClass(cls):
        from mongoengine.connection import connect, disconnect
        disconnect()
        connect(cls.mongodb_name)

    @classmethod
    def tearDownClass(cls):
        from mongoengine.connection import disconnect
        disconnect()

    def setUp(self):
        from lbd_backend.populate_meta import populate_meta
        print "Preparing system for new test..."
        print "Populating metadata db..."
        populate_meta()
        self.con = httplib.HTTPConnection(url, port)
        self.headers = lbdheader

    def test_valid_request(self):
        print "Running test: valid request"
        self.con.request("PUT", "/locationdata/api/Streetlights/", self.putjson_valid, headers=self.headers)
        response = self.con.getresponse()

        print "Response status code: "+str(response.status)
        print "Response: "+response.read()
        self.assertEqual(response.status, 200)

        self.con.request("GET", "/locationdata/api/Streetlights/", headers=self.headers)
        response = self.con.getresponse()

        contentjson = json.loads(response.read())
        for item in contentjson["features"]:
            try:
                f = item["properties"]["metadata"]
                if item["id"] == "WFS_KATUVALO.405172" or item["id"] == "WFS_KATUVALO.405171":
                    pass
                else:
                    self.fail("Metadata found on item that shouldn't have it!")
            except KeyError:
                pass

        print "Test passed!"

    def test_missing_meta_status(self):
        print "Running test: item missing metadata, check status"
        self.con.request("PUT", "/locationdata/api/Streetlights/", self.putjson_missing_meta, headers=self.headers)
        response = self.con.getresponse()

        print "Response status code: "+str(response.status)
        print "Response: "+response.read()
        self.assertEqual(response.status, 400)

        print "Test passed!"

    def test_missing_meta_changes(self):
        print "Running test: item missing metadata, check changes on db"
        self.con.request("GET", "/locationdata/api/Streetlights/", headers=self.headers)
        original = []
        for i in json.loads(self.con.getresponse().read())["features"]:
            if "metadata" in i["properties"]:
                original.append(i["id"])

        self.con.request("PUT", "/locationdata/api/Streetlights/", self.putjson_missing_meta, headers=self.headers)
        response = self.con.getresponse()

        print "Response status code: "+str(response.status)
        print "Response: "+response.read()

        self.con.request("GET", "/locationdata/api/Streetlights/", headers=self.headers)
        response = self.con.getresponse()

        contentjson = json.loads(response.read())

        after = []
        for i in contentjson["features"]:
            if "metadata" in i["properties"]:
                after.append(i["id"])

        print "Items with metadata before: "+str(len(original))
        print "Items with metadata after: "+str(len(after))

        self.assertEqual(len(original), len(after), "After a failed PUT, the items with metadata have changed!")
        for i in after:
            if i not in original:
                self.fail("After a failed PUT, the items with metadata have changed!")

        print "Test passed!"
