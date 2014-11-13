# -*- coding: UTF-8 -*-
__author__ = 'Aki Mäkinen'

import httplib
import json

from unittest import TestCase
from lbd_backend.utils import geo_json_scheme_validation
from lbd_backend.LBD_REST_locationdata.tests.test_settings import url, port, lbdheader

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
        self.assertEqual(response.status, 400)

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

    # def test_post_conflicting_resource_id(self):
    #     print "Running test: conflicting resource id"
    #     jsonstring ='{"geometry": {"type": "Point", "coordinates": [23.643239226767022, 61.519112683582854]}, "id": ' \
    #                 '"WFS_KATUVALO.405171", "type": "Feature", "properties": {"NIMI": "XPWR_6769212", ' \
    #                 '"LAMPPU_TYYPPI_KOODI": "100340", "TYYPPI_KOODI": "105007", "KATUVALO_ID": 405172, ' \
    #                 '"LAMPPU_TYYPPI": "ST 100 (SIEMENS)", "metadata": {"status": "teststatus", "modifier": ' \
    #                 '"HeliHumanisti", "modified": 1415705418}}, "geometry_name": "GEOLOC"}'
    #
    #     self.con.request("POST", "/locationdata/api/Streetlights/WFS_KATUVALO.405172", jsonstring,
    #                      headers=self.headers)
    #     response = self.con.getresponse()
    #     print "Response status code: "+str(response.status)
    #     self.assertEqual(response.status, 405)
    #
    #     print "Test passed!"