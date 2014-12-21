# -*- coding: UTF-8 -*-
import httplib
import json
from unittest import TestCase
import math
from lbd_backend.LBD_REST_locationdata.tests.test_settings import lbdheader, port, url
from lbd_backend.utils import geo_json_scheme_validation

__author__ = 'Aki Mäkinen'


class TestCollectionNear(TestCase):
    def setUp(self):
        self.con = httplib.HTTPConnection(url, port)
        self.headers = lbdheader

    def test_get_valid_request(self):
        print "Running test: valid get request"
        self.con.request("GET", "/locationdata/api/Streetlights/near/?latitude=61.49773020000001&longitude=23.792292",
                         headers=self.headers)
        response = self.con.getresponse()

        print "Response status code: "+str(response.status)
        self.assertEqual(response.status, 200)
        print "Test passed!"

    def test_get_valid_request_with_range(self):
        print "Running test: valid get request"
        self.con.request("GET", "/locationdata/api/Streetlights/near/?latitude=61.49773020000001&longitude=23.792292&range=0.001",
                         headers=self.headers)
        response = self.con.getresponse()

        print "Response status code: "+str(response.status)
        self.assertEqual(response.status, 200)
        print "Test passed!"

    def test_get_missing_latitude(self):
        print "Running test: valid get request"
        self.con.request("GET", "/locationdata/api/Streetlights/near/?longitude=23.792292",
                         headers=self.headers)
        response = self.con.getresponse()

        print "Response status code: "+str(response.status)
        self.assertEqual(response.status, 400)
        print "Test passed!"

    def test_get_missing_longitude(self):
        print "Running test: valid get request"
        self.con.request("GET", "/locationdata/api/Streetlights/near/?latitude=61.49773020000001",
                         headers=self.headers)
        response = self.con.getresponse()

        print "Response status code: "+str(response.status)
        self.assertEqual(response.status, 400)
        print "Test passed!"

    def test_get_missing_lat_and_long(self):
        print "Running test: valid get request"
        self.con.request("GET", "/locationdata/api/Streetlights/near/",
                         headers=self.headers)
        response = self.con.getresponse()

        print "Response status code: "+str(response.status)
        self.assertEqual(response.status, 400)
        print "Test passed!"

    def test_get_invalid_latitude(self):
        print "Running test: valid get request"
        self.con.request("GET", "/locationdata/api/Streetlights/near/?latitude=61.497730sss20000001&longitude=23.792292",
                         headers=self.headers)
        response = self.con.getresponse()

        print "Response status code: "+str(response.status)
        self.assertEqual(response.status, 400)
        print "Test passed!"

    def test_get_invalid_latitude_negative(self):
        print "Running test: valid get request"
        self.con.request("GET", "/locationdata/api/Streetlights/near/?latitude=-61.49773020000001&longitude=23.792292",
                         headers=self.headers)
        response = self.con.getresponse()

        print "Response status code: "+str(response.status)
        self.assertEqual(response.status, 400)
        print "Test passed!"

    def test_get_invalid_longitude(self):
        print "Running test: valid get request"
        self.con.request("GET", "/locationdata/api/Streetlights/near/?latitude=61.49773020000001&longitude=23.7922sss92",
                         headers=self.headers)
        response = self.con.getresponse()

        print "Response status code: "+str(response.status)
        self.assertEqual(response.status, 400)
        print "Test passed!"

    def test_get_invalid_longitude_negative(self):
        print "Running test: valid get request"
        self.con.request("GET", "/locationdata/api/Streetlights/near/?latitude=61.49773020000001&longitude=-23.792292",
                         headers=self.headers)
        response = self.con.getresponse()

        print "Response status code: "+str(response.status)
        self.assertEqual(response.status, 400)
        print "Test passed!"

    def test_get_invalid_latitude_longitude(self):
        print "Running test: valid get request"
        self.con.request("GET", "/locationdata/api/Streetlights/near/?latitude=61.497730sss20000001&longitude=23.7922ss92",
                         headers=self.headers)
        response = self.con.getresponse()

        print "Response status code: "+str(response.status)
        self.assertEqual(response.status, 400)
        print "Test passed!"

    def test_get_invalid_latitude_longitude_negative(self):
        print "Running test: valid get request"
        self.con.request("GET", "/locationdata/api/Streetlights/near/?latitude=-61.49773020000001&longitude=-23.792292",
                         headers=self.headers)
        response = self.con.getresponse()

        print "Response status code: "+str(response.status)
        self.assertEqual(response.status, 400)
        print "Test passed!"

    def test_get_invalid_collection(self):
        print "Running test: nonexistent collection"
        self.con.request("GET", "/locationdata/api/ÄÖÄÖLÄÖLP/?latitude=61.49773020000001&longitude=23.792292",
                         headers=self.headers)
        response = self.con.getresponse()

        print "Response status code: "+str(response.status)
        self.assertEqual(response.status, 404)
        print "Test passed!"

    def test_get_valid_request_content_format(self):
        print "Running test: valid get request"
        self.con.request("GET", "/locationdata/api/Streetlights/?latitude=61.49773020000001&longitude=23.792292",
                         headers=self.headers)
        response = self.con.getresponse()
        contentjson = json.loads(response.read())
        print "Response status code: "+str(response.status)
        self.assertTrue(geo_json_scheme_validation(contentjson))
        print "Test passed!"

    def test_get_valid_request_featurecount(self):
        print "Running test: check featurecount"
        self.con.request("GET", "/locationdata/api/Streetlights/?latitude=61.49773020000001&longitude=23.792292",
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

    def test_get_valid_request_range(self):
        self.con.request("GET", "/locationdata/api/Streetlights/?latitude=61.49773020000001&longitude=23.792292&range=0.001",
                         headers=self.headers)
        response = self.con.getresponse()
        contentjson = json.loads(response.read())
        lat = 61.49773020000001
        long = 23.792292
        range = 0.001

        features = contentjson["features"]
        for item in features:
            item_long = item["geometry"]["coordinates"][0]
            item_lat = item["geometry"]["coordinates"][1]
            a = (item_long-long)**2
            b = (item_lat-lat)**2
            c = math.sqrt(a+b)

            self.assertLessEqual(c, range)

        print "Test passed!"


class TestNearDelete(TestCase):
    """
    Tests assume that GET to near and to full collection work properly.
    Inaddition it is assumed that for each object in external data there exists an entry in metadata

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
        self.con.request("DELETE", "/locationdata/api/Streetlights/near/?latitude=61.49773020000001&longitude=23.792292&range=0.001",
                         headers=self.headers)
        response = self.con.getresponse()
        print "Response status code: "+str(response.status)

        self.assertEqual(response.status, 200)

        self.con = httplib.HTTPConnection(url, port)
        self.con.request("GET", "/locationdata/api/Streetlights/near/?latitude=61.49773020000001&longitude=23.792292&range=0.001",
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
        print "Running test: valid delete request"
        self.con.request("DELETE", "/locationdata/api/Streetasdasdadadlights/near/?latitude=61.49773020000001&longitude=23.792292&range=0.001",
                         headers=self.headers)
        response = self.con.getresponse()
        print "Response status code: "+str(response.status)

        self.assertEqual(response.status, 404)

        self.con = httplib.HTTPConnection(url, port)
        self.con.request("GET", "/locationdata/api/Streetlights/near/?latitude=61.49773020000001&longitude=23.792292&range=0.001",
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

    def test_invalid_latitude(self):
        print "Running test: valid delete request"
        self.con.request("DELETE", "/locationdata/api/Streetlights/near/?latitude=61.497730200sss00001&longitude=23.792292&range=0.001",
                         headers=self.headers)
        response = self.con.getresponse()
        print "Response status code: "+str(response.status)

        self.assertEqual(response.status, 400)

        self.con = httplib.HTTPConnection(url, port)
        self.con.request("GET", "/locationdata/api/Streetlights/",
                         headers=self.headers)
        response = self.con.getresponse()
        contentjson = json.loads(response.read())

        try:
            for item in contentjson["features"]:
                try:
                    if "metadata" not in item["properties"]:
                        self.fail("Metadata exists, deleting all failed")
                except KeyError:
                    pass
        except KeyError:
            self.fail("Error in accessing the GET response. Not valid geojson?")

        print "Test passed!"

    def test_invalid_longitude(self):
        print "Running test: valid delete request"
        self.con.request("DELETE", "/locationdata/api/Streetlights/near/?latitude=61.49773020000001&longitude=23.79sss2292&range=0.001",
                         headers=self.headers)
        response = self.con.getresponse()
        print "Response status code: "+str(response.status)

        self.assertEqual(response.status, 400)

        self.con = httplib.HTTPConnection(url, port)
        self.con.request("GET", "/locationdata/api/Streetlights/",
                         headers=self.headers)
        response = self.con.getresponse()
        contentjson = json.loads(response.read())

        try:
            for item in contentjson["features"]:
                try:
                    if "metadata" not in item["properties"]:
                        self.fail("Metadata exists, deleting all failed")
                except KeyError:
                    pass
        except KeyError:
            self.fail("Error in accessing the GET response. Not valid geojson?")

        print "Test passed!"

    def test_missing_coords(self):
        print "Running test: valid delete request"
        self.con.request("DELETE", "/locationdata/api/Streetlights/near/",
                         headers=self.headers)
        response = self.con.getresponse()
        print "Response status code: "+str(response.status)

        self.assertEqual(response.status, 400)

        self.con = httplib.HTTPConnection(url, port)
        self.con.request("GET", "/locationdata/api/Streetlights/",
                         headers=self.headers)
        response = self.con.getresponse()
        contentjson = json.loads(response.read())

        try:
            for item in contentjson["features"]:
                try:
                    if "metadata" not in item["properties"]:
                        self.fail("Metadata exists, deleting all failed")
                except KeyError:
                    pass
        except KeyError:
            self.fail("Error in accessing the GET response. Not valid geojson?")

        print "Test passed!"