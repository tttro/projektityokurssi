# -*- coding: UTF-8 -*-
import json
#from time import sleep
from unittest import TestCase
#from django.test import Client
#import urllib2
import httplib
from lbd_backend.utils import geo_json_scheme_validation

__author__ = 'Aki Mäkinen'

httpconn = httplib.HTTPConnection("localhost", 8000)

class TestSingleResourceGet(TestCase):
    """
    Tests for function single_resource GET

    Tests assume that in the database exists Streetlights with id 405171 and 405172.
    The first one is expected not to have meta data attached to it while the latter one
    is expected to have meta data.
    """
    def __init__(self, *args, **kwargs):
        self.con = httpconn
        super(TestSingleResourceGet, self).__init__(*args, **kwargs)

    def test_get_valid_request(self):
        print "Running test: valid get request"
        self.con.request("GET", "/locationdata/api/Streetlights/WFS_KATUVALO.405171",
                         headers={"LBD_LOGIN_HEADER": "TiinaTeekkari"})
        response = self.con.getresponse()
        print "Response status code: "+str(response.status)
        print "Response content: "+response.read()
        self.assertEqual(response.status, 200)
        print "Test passed!"

    def test_get_resource_does_not_exist(self):
        print "Running test: resource does not exist"
        self.con.request("GET", "/locationdata/api/Streetlights/WFS_KATUVALO.995171",
                         headers={"LBD_LOGIN_HEADER": "TiinaTeekkari"})
        response = self.con.getresponse()
        print "Response status code: "+str(response.status)
        print "Response content: "+response.read()
        self.assertEqual(response.status, 404)
        print "Test passed!"

    # TODO: Move to login decorator tests
    def test_get_no_login_header(self):
        print "Running test: no login header"
        self.con.request("GET", "/locationdata/api/Streetlights/WFS_KATUVALO.405171")
        response = self.con.getresponse()
        print "Response status code: "+str(response.status)
        print "Response content: "+response.read()
        self.assertEqual(response.status, 400)
        print "Test passed!"

    def test_get_collection_does_not_exist(self):
        print "Running test: collection does not exist"
        self.con.request("GET", "/locationdata/api/FOOBARISTA/WFS_KATUVALO.405171",
                         headers={"LBD_LOGIN_HEADER": "TiinaTeekkari"})
        response = self.con.getresponse()
        print "Response status code: "+str(response.status)
        print "Response content: "+response.read()
        self.assertEqual(response.status, 404)
        print "Test passed!"

    def test_get_valid_request_content_check_no_meta(self):
        print "Running test: Check that content is valid GeoJSON, no metadata"
        self.con.request("GET", "/locationdata/api/Streetlights/WFS_KATUVALO.405171",
                         headers={"LBD_LOGIN_HEADER": "TiinaTeekkari"})
        response = self.con.getresponse()
        response_text = response.read()
        print "Response status code: "+str(response.status)
        print "Response content: "+response_text
        content_json = json.loads(response_text)
        self.assertTrue(geo_json_scheme_validation(content_json))
        print "Test passed!"

    def test_get_valid_request_content_check_with_meta(self):
        print "Running test: Check that content is valid GeoJSON, with metadata"
        self.con.request("GET", "/locationdata/api/Streetlights/WFS_KATUVALO.405172",
                         headers={"LBD_LOGIN_HEADER": "TiinaTeekkari"})
        response = self.con.getresponse()
        response_text = response.read()
        print "Response status code: "+str(response.status)
        print "Response content: "+response_text
        content_json = json.loads(response_text)
        self.assertTrue(geo_json_scheme_validation(content_json))
        print "Test passed!"


class TestSingleResourcePut(TestCase):
    """
    Tests for function single_resource PUT

    Tests assume that in the database exists Streetlights with id 405171 and 405172.
    The first one is expected not to have meta data attached to it while the latter one
    is expected to have meta data.

    test_put_valid_request_* tests are the most important ones. They test that the REST handles a valid request correctly.

    """
    def __init__(self, *args, **kwargs):
        self.con = httpconn
        super(TestSingleResourcePut, self).__init__(*args, **kwargs)

    def test_put_valid_request_change_user(self):
        """
        Prerequisites: Metadata modifier in database should be different than the one in LBD_LOGIN_HEADER.
        If not, this test is useless.
        """
        print "Running test: valid put request, change user"

        user = "SimoSahkari"  # TiinaTeekkari or SimoSahkari
        print "User: "+user

        jsonstring ='{"geometry": {"type": "Point", "coordinates": [23.643239226767022, 61.519112683582854]}, "id": ' \
                    '"WFS_KATUVALO.405172", "type": "Feature", "properties": {"NIMI": "XPWR_6769212", ' \
                    '"LAMPPU_TYYPPI_KOODI": "100340", "TYYPPI_KOODI": "105007", "KATUVALO_ID": 405172, ' \
                    '"LAMPPU_TYYPPI": "ST 100 (SIEMENS)", "metadata": {"status": "teststatus", "modifier": ' \
                    '"HeliHumanisti", "modified": 1415705418}}, "geometry_name": "GEOLOC"}'

        self.con.request("PUT", "/locationdata/api/Streetlights/WFS_KATUVALO.405172", jsonstring,
                         headers={"LBD_LOGIN_HEADER": user,
                                  "Content-type": "application/json"})
        response = self.con.getresponse()
        print "(PUT) Response status code: "+str(response.status)
        self.assertEqual(response.status, 200)

        self.con.request("GET", "/locationdata/api/Streetlights/WFS_KATUVALO.405172",
                         headers={"LBD_LOGIN_HEADER": user})
        response = self.con.getresponse()
        response_text = response.read()
        print "(GET) Response status code: "+str(response.status)
        print "(GET) Response content: "+response_text
        contentjson = json.loads(response_text)
        self.assertEqual(contentjson["properties"]["metadata"]["modifier"], user)
        print "Test passed!"

    def test_put_valid_request_change_status(self):
        """
        Prerequisites: Metadata status in database should be different than the one in jsonstring.
        If not, this test is useless.
        """
        print "Running test: valid put request, change status"

        user = "HeliHumanisti"
        status = "FOOBAAAAAAR!!!"
        print "User: "+user
        print "Status: "+status
        tempstring ='{"geometry": {"type": "Point", "coordinates": [23.643239226767022, 61.519112683582854]}, "id": ' \
                    '"WFS_KATUVALO.405172", "type": "Feature", "properties": {"NIMI": "XPWR_6769212", ' \
                    '"LAMPPU_TYYPPI_KOODI": "100340", "TYYPPI_KOODI": "105007", "KATUVALO_ID": 405172, ' \
                    '"LAMPPU_TYYPPI": "ST 100 (SIEMENS)", "metadata": {"status": "teststatus", "modifier": ' \
                    '"HeliHumanisti", "modified": 1415705418}}, "geometry_name": "GEOLOC"}'

        jsonobject = json.loads(tempstring)
        jsonobject["properties"]["metadata"]["status"] = status
        jsonstring = json.dumps(jsonobject)

        self.con.request("PUT", "/locationdata/api/Streetlights/WFS_KATUVALO.405172", jsonstring,
                         headers={"LBD_LOGIN_HEADER": user,
                                  "Content-type": "application/json"})
        response = self.con.getresponse()
        print "(PUT) Response status code: "+str(response.status)
        self.assertEqual(response.status, 200)

        self.con.request("GET", "/locationdata/api/Streetlights/WFS_KATUVALO.405172",
                         headers={"LBD_LOGIN_HEADER": user})
        response = self.con.getresponse()
        response_text = response.read()
        print "(GET) Response status code: "+str(response.status)
        print "(GET) Response content: "+response_text
        contentjson = json.loads(response_text)
        self.assertEqual(contentjson["properties"]["metadata"]["status"], status)
        print "Test passed!"

    def test_put_missing_id_field_check_changes(self):
        print "Running test: id field missing, check changes"
        user = "TiinaTeekkari"

        self.con.request("GET", "/locationdata/api/Streetlights/WFS_KATUVALO.405172",
                         headers={"LBD_LOGIN_HEADER": user})
        response = self.con.getresponse()
        originaljson = json.loads(response.read())
        print "Original JSON: "+str(originaljson)
        jsonstring ='{"geometry": {"type": "Point", "coordinates": [23.643239226767022, 61.519112683582854]}, ' \
                    '"type": "Feature", "properties": {"NIMI": "XPWR_6769212", ' \
                    '"LAMPPU_TYYPPI_KOODI": "100340", "TYYPPI_KOODI": "105007", "KATUVALO_ID": 405172, ' \
                    '"LAMPPU_TYYPPI": "ST 100 (SIEMENS)", "metadata": {"status": "teststatus", "modifier": ' \
                    '"HeliHumanisti", "modified": 1415705418}}, "geometry_name": "GEOLOC"}'

        self.con.request("PUT", "/locationdata/api/Streetlights/WFS_KATUVALO.405172", jsonstring,
                         headers={"LBD_LOGIN_HEADER": user,
                                  "Content-type": "application/json"})
        response = self.con.getresponse()

        print "(PUT) Response status code: "+str(response.status)

        self.con.request("GET", "/locationdata/api/Streetlights/WFS_KATUVALO.405172",
                         headers={"LBD_LOGIN_HEADER": user})
        response = self.con.getresponse()
        afterjson = json.loads(response.read())
        print "After PUT JSON: "+str(afterjson)

        self.assertEqual(cmp(originaljson, afterjson), 0)
        print "Test passed!"

    def test_put_missing_id_field_check_response(self):
        print "Running test: id field missing, check response"
        user = "TiinaTeekkari"

        jsonstring ='{"geometry": {"type": "Point", "coordinates": [23.643239226767022, 61.519112683582854]}, ' \
                    '"type": "Feature", "properties": {"NIMI": "XPWR_6769212", ' \
                    '"LAMPPU_TYYPPI_KOODI": "100340", "TYYPPI_KOODI": "105007", "KATUVALO_ID": 405172, ' \
                    '"LAMPPU_TYYPPI": "ST 100 (SIEMENS)", "metadata": {"status": "teststatus", "modifier": ' \
                    '"HeliHumanisti", "modified": 1415705418}}, "geometry_name": "GEOLOC"}'

        self.con.request("PUT", "/locationdata/api/Streetlights/WFS_KATUVALO.405172", jsonstring,
                         headers={"LBD_LOGIN_HEADER": user,
                                  "Content-type": "application/json"})
        response = self.con.getresponse()

        print "(PUT) Response status code: "+str(response.status)

        self.assertEqual(response.status, 400)
        print "Test passed!"

    def test_put_missing_status_field_check_response(self):
        print "Running test: status field missing, check response"
        user = "TiinaTeekkari"

        jsonstring ='{"geometry": {"type": "Point", "coordinates": [23.643239226767022, 61.519112683582854]}, "id": ' \
                    '"WFS_KATUVALO.405172", "type": "Feature", "properties": {"NIMI": "XPWR_6769212", ' \
                    '"LAMPPU_TYYPPI_KOODI": "100340", "TYYPPI_KOODI": "105007", "KATUVALO_ID": 405172, ' \
                    '"LAMPPU_TYYPPI": "ST 100 (SIEMENS)", "metadata": {"modifier": ' \
                    '"HeliHumanisti", "modified": 1415705418}}, "geometry_name": "GEOLOC"}'

        self.con.request("PUT", "/locationdata/api/Streetlights/WFS_KATUVALO.405172", jsonstring,
                         headers={"LBD_LOGIN_HEADER": user,
                                  "Content-type": "application/json"})
        response = self.con.getresponse()

        print "(PUT) Response status code: "+str(response.status)

        self.assertEqual(response.status, 400)
        print "Test passed!"

    def test_put_empty_json(self):
        print "Running test: empty json"
        user = "TiinaTeekkari"

        jsonstring = '{}'

        self.con.request("PUT", "/locationdata/api/Streetlights/WFS_KATUVALO.405172", jsonstring,
                         headers={"LBD_LOGIN_HEADER": user,
                                  "Content-type": "application/json"})
        response = self.con.getresponse()
        print "(PUT) Response status code: "+str(response.status)

        self.assertEqual(response.status, 400)
        print "Test passed!"

    def test_put_empty_json_string(self):
        print "Running test: empty json"
        user = "TiinaTeekkari"

        jsonstring = ''

        self.con.request("PUT", "/locationdata/api/Streetlights/WFS_KATUVALO.405172", jsonstring,
                         headers={"LBD_LOGIN_HEADER": user,
                                  "Content-type": "application/json"})
        response = self.con.getresponse()

        print "(PUT) Response status code: "+str(response.status)

        self.assertEqual(response.status, 400)
        print "Test passed!"

    def test_no_content(self):
        print "Running test: empty json"
        user = "TiinaTeekkari"

        self.con.request("PUT", "/locationdata/api/Streetlights/WFS_KATUVALO.405172",
                         headers={"LBD_LOGIN_HEADER": user,
                                  "Content-type": "application/json"})
        response = self.con.getresponse()

        print "(PUT) Response status code: "+str(response.status)

        self.assertEqual(response.status, 400)
        print "Test passed!"

    def test_put_valid_data_invalid_resource(self):
        """
        Prerequisites: Metadata modifier in database should be different than the one in LBD_LOGIN_HEADER.
        If not, this test is useless.
        """
        print "Running test: valid put request, nonexistent resource"
        user = "TiinaTeekkari"

        jsonstring ='{"geometry": {"type": "Point", "coordinates": [23.643239226767022, 61.519112683582854]}, "id": ' \
                    '"WFS_KATUVALO.405172", "type": "Feature", "properties": {"NIMI": "XPWR_6769212", ' \
                    '"LAMPPU_TYYPPI_KOODI": "100340", "TYYPPI_KOODI": "105007", "KATUVALO_ID": 405172, ' \
                    '"LAMPPU_TYYPPI": "ST 100 (SIEMENS)", "metadata": {"status": "teststatus", "modifier": ' \
                    '"HeliHumanisti", "modified": 1415705418}}, "geometry_name": "GEOLOC"}'

        self.con.request("PUT", "/locationdata/api/Streetlights/WFS_KATUVALO.995172", jsonstring,
                         headers={"LBD_LOGIN_HEADER": user,
                                  "Content-type": "application/json"})
        response = self.con.getresponse()

        print "(PUT) Response status code: "+str(response.status)
        self.assertEqual(response.status, 404)
        print "Test passed!"

    def test_put_valid_data_id_contradiction(self):
        print "Running test: valid data, contradiction in resource id"
        user = "TiinaTeekkari"

        jsonstring ='{"geometry": {"type": "Point", "coordinates": [23.643239226767022, 61.519112683582854]}, "id": ' \
                    '"WFS_KATUVALO.405172", "type": "Feature", "properties": {"NIMI": "XPWR_6769212", ' \
                    '"LAMPPU_TYYPPI_KOODI": "100340", "TYYPPI_KOODI": "105007", "KATUVALO_ID": 405172, ' \
                    '"LAMPPU_TYYPPI": "ST 100 (SIEMENS)", "metadata": {"status": "teststatus", "modifier": ' \
                    '"HeliHumanisti", "modified": 1415705418}}, "geometry_name": "GEOLOC"}'

        self.con.request("PUT", "/locationdata/api/Streetlights/WFS_KATUVALO.405171", jsonstring,
                         headers={"LBD_LOGIN_HEADER": user,
                                  "Content-type": "application/json"})
        response = self.con.getresponse()

        print "(PUT) Response status code: "+str(response.status)
        self.assertEqual(response.status, 400)
        print "Test passed!"


class TestSingleResourceDelete(TestCase):
    """
    Test class for single resource DELETE method tests

    Tests assume that basic PUT method works.
    """
    def __init__(self, *args, **kwargs):
        self.con = httpconn
        super(TestSingleResourceDelete, self).__init__(*args, **kwargs)

    def setUp(self):
        print "Preparing system for new test..."
        user = "TiinaTeekkari"

        jsonstring ='{"geometry": {"type": "Point", "coordinates": [23.643239226767022, 61.519112683582854]}, "id": ' \
                    '"WFS_KATUVALO.405172", "type": "Feature", "properties": {"NIMI": "XPWR_6769212", ' \
                    '"LAMPPU_TYYPPI_KOODI": "100340", "TYYPPI_KOODI": "105007", "KATUVALO_ID": 405172, ' \
                    '"LAMPPU_TYYPPI": "ST 100 (SIEMENS)", "metadata": {"status": "teststatus", "modifier": ' \
                    '"HeliHumanisti", "modified": 1415705418}}, "geometry_name": "GEOLOC"}'

        self.con.request("PUT", "/locationdata/api/Streetlights/WFS_KATUVALO.405172", jsonstring,
                         headers={"LBD_LOGIN_HEADER": user,
                                  "Content-type": "application/json"})
        response = self.con.getresponse()
        self.assertEqual(response.status, 200, "Prepare failed...")
        print "System prepared!"

    def test_delete_valid(self):
        print "Running test: valid data, contradiction in resource id"
        user = "TiinaTeekkari"

        self.con.request("DELETE", "/locationdata/api/Streetlights/WFS_KATUVALO.405172",
                         headers={"LBD_LOGIN_HEADER": user})
        response = self.con.getresponse()

        print "(DELETE) Response status code: "+str(response.status)
        self.assertEqual(response.status, 200)

        self.con.request("GET", "/locationdata/api/Streetlights/WFS_KATUVALO.405172",
                         headers={"LBD_LOGIN_HEADER": user})
        response = self.con.getresponse()
        print "(GET) Response status code: "+str(response.status)
        self.assertEqual(response.status, 200)
        responsejson = json.loads(response.read())
        with self.assertRaises(KeyError):
            print responsejson["properties"]["metadata"]
        print "Test passed!"

    def test_delete_invalid_resource(self):
        print "Running test: valid data, invalid resource"
        user = "TiinaTeekkari"

        self.con.request("DELETE", "/locationdata/api/Streetlights/WFS_KATUÖÄÖÄÅ.999172",
                         headers={"LBD_LOGIN_HEADER": user})
        response = self.con.getresponse()

        print "(DELETE) Response status code: "+str(response.status)
        self.assertEqual(response.status, 404)

        print "Test passed!"

    def test_delete_invalid_collection(self):
        print "Running test: valid data, invalid collection"
        user = "TiinaTeekkari"

        self.con.request("DELETE", "/locationdata/api/FOOBARISTA/WFS_KATUVALO.405172",
                         headers={"LBD_LOGIN_HEADER": user})
        response = self.con.getresponse()

        print "(DELETE) Response status code: "+str(response.status)
        self.assertEqual(response.status, 404)

        print "Test passed!"

    def test_delete_twice(self):
        print "Running test: deleting item twice"
        user = "TiinaTeekkari"

        self.con.request("DELETE", "/locationdata/api/FOOBARISTA/WFS_KATUVALO.405172",
                         headers={"LBD_LOGIN_HEADER": user})
        response = self.con.getresponse()

        print "(DELETE) Second response status code: "+str(response.status)
        self.assertEqual(response.status, 404)

        self.con.request("DELETE", "/locationdata/api/FOOBARISTA/WFS_KATUVALO.405172",
                         headers={"LBD_LOGIN_HEADER": user})
        response = self.con.getresponse()

        print "(DELETE) Second response status code: "+str(response.status)
        self.assertEqual(response.status, 404)

        print "Test passed!"