# -*- coding: UTF-8 -*-
from functools import partial, update_wrapper
import httplib
import itertools

from nose import with_setup

from lbd_backend.LBD_REST_locationdata.tests.single_resource.checkers import _status_tester, _content_tester
from lbd_backend.LBD_REST_locationdata.tests.test_settings import url, port, lbdheader


__author__ = 'Aki Mäkinen'

con = httplib.HTTPConnection(url, port)
_base = "/locationdata/api/"
_method = None
_content = '{"geometry": {"type": "Point", "coordinates": [23.643239226767022, 61.519112683582854]}, "id": ' \
           '"WFS_KATUVALO.405172", "type": "Feature", "properties": {"NIMI": "XPWR_6769212", ' \
           '"LAMPPU_TYYPPI_KOODI": "100340", "TYYPPI_KOODI": "105007", "KATUVALO_ID": 405172, ' \
           '"LAMPPU_TYYPPI": "ST 100 (SIEMENS)", "metadata": {"status": "teststatus", "modifier": ' \
           '"HeliHumanisti", "modified": 1415705418}}, "geometry_name": "GEOLOC"}'

_collectionstatuses = [("Streetlights",200),
                     ("Streetlighstfa",404),
                     (" ",404),
                     ("", 404),
                     ("äöääöäö", 404),
                     ("///\\\///",404),
                     ("Streetlights{;};", 404)]

_resourcestatuses = [("WFS_KATUVALO.405171",200),
                   ("WFS_KATUVALO.999171",404),
                   ("ÄÖÄÖÄÅÅÅ",404),
                   ("/foobar",404),
                   ("/))#(/¤)(#/¤",404),
                   ("WFS_KATUVALO.405171},{",404)
                   ]
_statuscombinations = list(itertools.product(_collectionstatuses, _resourcestatuses))

# For validating the possible content

_collectionvalues = [("Streetlights",True),
                     ("Streetlighstfa",False),
                     (" ",False),
                     ("", False),
                     ("äöääöäö", False),
                     ("///\\\///",False),
                     ("Streetlights{;};", False)]

_resourcevalues = [("WFS_KATUVALO.405171",True),
                   ("WFS_KATUVALO.999171",False),
                   ("ÄÖÄÖÄÅÅÅ",False),
                   ("/foobar",False),
                   ("/))#(/¤)(#/¤",False),
                   ("WFS_KATUVALO.405171},{",False)]

_valuecombinations = list(itertools.product(_collectionvalues, _resourcevalues))

def setup_get():
    global con
    _con = httplib.HTTPConnection(url, port)

def teardown_get():
    global _method
    _method = None

def teardown_dummy():
    pass

def setup_delete_single():
    print "Setup delete (single)"
    global con
    _con = httplib.HTTPConnection(url, port)
    _con.request("POST", "locationdata/api/Streetligths/WFS_KATUVALO.405171", _content, headers=lbdheader)
    setupresp = _con.getresponse()
    setupresp.read()
    print "Metadata post status: "+str(setupresp.status)
    assert setupresp.status == 200, "Setting up delete (single) went wrong!"
    _con = httplib.HTTPConnection(url, port)

def setup_delete_collection():
    print "Setup delete (collection)"
    global con
    _con = httplib.HTTPConnection(url, port)
    _con.request("GET", "/locationdata/api/SL/testing/popmeta", headers={"LBD_LOGIN_HEADER": "VakaVanhaVainamoinen"})
    setupresp = _con.getresponse()
    setupresp.read()
    print "Populate meta status: "+str(setupresp.status)
    assert setupresp.status == 200, "Setting up delete went wrong!"
    _con = httplib.HTTPConnection(url, port)

def empty_metadata():
    print "Clearing metadata"
    global con
    _con = httplib.HTTPConnection(url, port)
    _con.request("GET", "/locationdata/api/SL/testing/dropmeta", headers={"LBD_LOGIN_HEADER": "VakaVanhaVainamoinen"})
    setupresp = _con.getresponse()
    setupresp.read()
    print "Clear meta status: "+str(setupresp.status)
    assert setupresp.status == 200, "Clearing metadata went wrong!"
    _con = httplib.HTTPConnection(url, port)








@with_setup(setup_get, teardown_get)
def test_generator_get_status():
    i = 1
    for col, res in _statuscombinations:
        global _method
        _method = "GET"
        call = partial(_status_tester)
        update_wrapper(call, _status_tester)
        call.description = "Testing response statuses with different collection and resource values"
        test_generator_get_status.__name__ = "GetStatusTest#"+str(i)
        yield _status_tester, col, res
        i += 1


@with_setup(setup_get, teardown_get)
def test_generator_get_content():
    i = 1
    for col, res in _valuecombinations:
        global _method
        _method = "GET"
        call = partial(_content_tester)
        update_wrapper(call, _content_tester)
        call.description = "Testing response statuses with different collection and resource values"
        test_generator_get_content.__name__ = "GetContentTest#"+str(i)
        yield _content_tester, col, res
        i += 1

@with_setup(empty_metadata)
def test_generator_post_status():
    i = 1
    for col, res in _statuscombinations:
        global _method
        _method = "POST"
        call = partial(_status_tester)
        update_wrapper(call, _status_tester)
        call.description = "Testing response statuses with different collection and resource values"
        test_generator_post_status.__name__ = "PostStatusTest#"+str(i)
        yield _status_tester, col, res
        i += 1


@with_setup(empty_metadata)
def test_generator_post_content():
    i = 1
    for col, res in _valuecombinations:
        global _method
        _method = "POST"
        call = partial(_content_tester)
        update_wrapper(call, _content_tester)
        call.description = "Testing response statuses with different collection and resource values"
        test_generator_post_content.__name__ = "PostContentTest#"+str(i)
        yield _content_tester, col, res
        i += 1


@with_setup(setup_delete_single)
def test_generator_delete_status():
    i = 1
    for col, res in _statuscombinations:
        global _method
        _method = "DELETE"
        call = partial(_status_tester)
        update_wrapper(call, _status_tester)
        call.description = "Testing response statuses with different collection and resource values"
        test_generator_delete_status.__name__ = "DeleteStatusTest#"+str(i)
        yield _status_tester, col, res
        i += 1


@with_setup(setup_delete_single)
def test_generator_delete_content():
    i = 1
    for col, res in _valuecombinations:
        global _method
        _method = "DELETE"
        call = partial(_content_tester)
        update_wrapper(call, _content_tester)
        call.description = "Testing response statuses with different collection and resource values"
        test_generator_delete_content.__name__ = "DeleteContentTest#"+str(i)
        yield _content_tester, col, res
        i += 1


@with_setup(setup_delete_single)
def test_generator_delete_status():
    i = 1
    for col, res in _statuscombinations:
        global _method
        _method = "DELETE"
        call = partial(_status_tester)
        update_wrapper(call, _status_tester)
        call.description = "Testing response statuses with different collection and resource values"
        test_generator_delete_status.__name__ = "DeleteStatusTest#"+str(i)
        yield _status_tester, col, res
        i += 1


@with_setup(setup_delete_single)
def test_generator_delete_content():
    i = 1
    for col, res in _valuecombinations:
        global _method
        _method = "DELETE"
        call = partial(_content_tester)
        update_wrapper(call, _content_tester)
        call.description = "Testing response statuses with different collection and resource values"
        test_generator_delete_content.__name__ = "DeleteContentTest#"+str(i)
        yield _content_tester, col, res
        i += 1
# class TestSingleResourceGet(TestCase):
#     """
#     Tests for function single_resource GET
#
#     Tests assume that in the database exists Streetlights with id 405171 and 405172.
#     The first one is expected not to have meta data attached to it while the latter one
#     is expected to have meta data.
#     """
#     def test_get_valid_request(self):
#         print "Running test: valid get request"
#         self.con.request("GET", "/locationdata/api/Streetlights/WFS_KATUVALO.405171",
#                          headers={"LBD_LOGIN_HEADER": "TiinaTeekkari"})
#         response = self.con.getresponse()
#         print "Response status code: "+str(response.status)
#         print "Response content: "+response.read()
#         self.assertEqual(response.status, 200)
#         print "Test passed!"
#
#     def test_get_resource_does_not_exist(self):
#         print "Running test: resource does not exist"
#         self.con.request("GET", "/locationdata/api/Streetlights/WFS_KATUVALO.995171",
#                          headers={"LBD_LOGIN_HEADER": "TiinaTeekkari"})
#         response = self.con.getresponse()
#         print "Response status code: "+str(response.status)
#         print "Response content: "+response.read()
#         self.assertEqual(response.status, 404)
#         print "Test passed!"
#
#     # TODO: Move to login decorator tests
#     def test_get_no_login_header(self):
#         print "Running test: no login header"
#         self.con.request("GET", "/locationdata/api/Streetlights/WFS_KATUVALO.405171")
#         response = self.con.getresponse()
#         print "Response status code: "+str(response.status)
#         print "Response content: "+response.read()
#         self.assertEqual(response.status, 400)
#         print "Test passed!"
#
#     def test_get_collection_does_not_exist(self):
#         print "Running test: collection does not exist"
#         self.con.request("GET", "/locationdata/api/FOOBARISTA/WFS_KATUVALO.405171",
#                          headers={"LBD_LOGIN_HEADER": "TiinaTeekkari"})
#         response = self.con.getresponse()
#         print "Response status code: "+str(response.status)
#         print "Response content: "+response.read()
#         self.assertEqual(response.status, 404)
#         print "Test passed!"
#
#     def test_get_valid_request_content_check_no_meta(self):
#         print "Running test: Check that content is valid GeoJSON, no metadata"
#         self.con.request("GET", "/locationdata/api/Streetlights/WFS_KATUVALO.405171",
#                          headers={"LBD_LOGIN_HEADER": "TiinaTeekkari"})
#         response = self.con.getresponse()
#         response_text = response.read()
#         print "Response status code: "+str(response.status)
#         print "Response content: "+response_text
#         content_json = json.loads(response_text)
#         self.assertTrue(geo_json_scheme_validation(content_json))
#         print "Test passed!"
#
#     def test_get_valid_request_content_check_with_meta(self):
#         print "Running test: Check that content is valid GeoJSON, with metadata"
#         self.con.request("GET", "/locationdata/api/Streetlights/WFS_KATUVALO.405172",
#                          headers={"LBD_LOGIN_HEADER": "TiinaTeekkari"})
#         response = self.con.getresponse()
#         response_text = response.read()
#         print "Response status code: "+str(response.status)
#         print "Response content: "+response_text
#         content_json = json.loads(response_text)
#         self.assertTrue(geo_json_scheme_validation(content_json))
#         print "Test passed!"
#
#
# class TestSingleResourcePut(TestCase):
#     """
#     Tests for function single_resource PUT
#
#     Tests assume that in the database exists Streetlights with id 405171 and 405172.
#     The first one is expected not to have meta data attached to it while the latter one
#     is expected to have meta data.
#
#     test_put_valid_request_* tests are the most important ones. They test that the REST handles a valid request correctly.
#
#     """
#     def setUp(self):
#         self.con = httplib.HTTPConnection(url, port)
#
#     def test_put_valid_request_change_user(self):
#         """
#         Prerequisites: Metadata modifier in database should be different than the one in LBD_LOGIN_HEADER.
#         If not, this test is useless.
#         """
#         print "Running test: valid put request, change user"
#
#         user = "SimoSahkari"  # TiinaTeekkari or SimoSahkari
#         print "User: "+user
#
#         jsonstring ='{"geometry": {"type": "Point", "coordinates": [23.643239226767022, 61.519112683582854]}, "id": ' \
#                     '"WFS_KATUVALO.405172", "type": "Feature", "properties": {"NIMI": "XPWR_6769212", ' \
#                     '"LAMPPU_TYYPPI_KOODI": "100340", "TYYPPI_KOODI": "105007", "KATUVALO_ID": 405172, ' \
#                     '"LAMPPU_TYYPPI": "ST 100 (SIEMENS)", "metadata": {"status": "teststatus", "modifier": ' \
#                     '"HeliHumanisti", "modified": 1415705418}}, "geometry_name": "GEOLOC"}'
#
#         self.con.request("PUT", "/locationdata/api/Streetlights/WFS_KATUVALO.405172", jsonstring,
#                          headers={"LBD_LOGIN_HEADER": user,
#                                   "Content-type": "application/json"})
#         response = self.con.getresponse()
#         print "(PUT) Response status code: "+str(response.status)
#         self.assertEqual(response.status, 200)
#
#         self.con = httplib.HTTPConnection(url, port)
#         self.con.request("GET", "/locationdata/api/Streetlights/WFS_KATUVALO.405172",
#                          headers={"LBD_LOGIN_HEADER": user})
#         response = self.con.getresponse()
#         response_text = response.read()
#
#         print "(GET) Response status code: "+str(response.status)
#         print "(GET) Response content: "+response_text
#         contentjson = json.loads(response_text)
#         self.assertEqual(contentjson["properties"]["metadata"]["modifier"], user)
#         print "Test passed!"
#
#     def test_put_valid_request_change_status(self):
#         """
#         Prerequisites: Metadata status in database should be different than the one in jsonstring.
#         If not, this test is useless.
#         """
#         print "Running test: valid put request, change status"
#
#         user = "HeliHumanisti"
#         status = "FOOBAAAAAAR!!!"
#         print "User: "+user
#         print "Status: "+status
#         tempstring ='{"geometry": {"type": "Point", "coordinates": [23.643239226767022, 61.519112683582854]}, "id": ' \
#                     '"WFS_KATUVALO.405172", "type": "Feature", "properties": {"NIMI": "XPWR_6769212", ' \
#                     '"LAMPPU_TYYPPI_KOODI": "100340", "TYYPPI_KOODI": "105007", "KATUVALO_ID": 405172, ' \
#                     '"LAMPPU_TYYPPI": "ST 100 (SIEMENS)", "metadata": {"status": "teststatus", "modifier": ' \
#                     '"HeliHumanisti", "modified": 1415705418}}, "geometry_name": "GEOLOC"}'
#
#         jsonobject = json.loads(tempstring)
#         jsonobject["properties"]["metadata"]["status"] = status
#         jsonstring = json.dumps(jsonobject)
#
#         self.con.request("PUT", "/locationdata/api/Streetlights/WFS_KATUVALO.405172", jsonstring,
#                          headers={"LBD_LOGIN_HEADER": user,
#                                   "Content-type": "application/json"})
#         response = self.con.getresponse()
#         print "(PUT) Response status code: "+str(response.status)
#         self.assertEqual(response.status, 200)
#
#         self.con = httplib.HTTPConnection(url, port)
#         self.con.request("GET", "/locationdata/api/Streetlights/WFS_KATUVALO.405172",
#                          headers={"LBD_LOGIN_HEADER": user})
#         response = self.con.getresponse()
#         response_text = response.read()
#         print "(GET) Response status code: "+str(response.status)
#         print "(GET) Response content: "+response_text
#         contentjson = json.loads(response_text)
#         self.assertEqual(contentjson["properties"]["metadata"]["status"], status)
#         print "Test passed!"
#
#     def test_put_missing_id_field_check_changes(self):
#         print "Running test: id field missing, check changes"
#         user = "TiinaTeekkari"
#
#         self.con.request("GET", "/locationdata/api/Streetlights/WFS_KATUVALO.405172",
#                          headers={"LBD_LOGIN_HEADER": user})
#         response = self.con.getresponse()
#         originaljson = json.loads(response.read())
#         print "Original JSON: "+str(originaljson)
#         jsonstring ='{"geometry": {"type": "Point", "coordinates": [23.643239226767022, 61.519112683582854]}, ' \
#                     '"type": "Feature", "properties": {"NIMI": "XPWR_6769212", ' \
#                     '"LAMPPU_TYYPPI_KOODI": "100340", "TYYPPI_KOODI": "105007", "KATUVALO_ID": 405172, ' \
#                     '"LAMPPU_TYYPPI": "ST 100 (SIEMENS)", "metadata": {"status": "teststatus", "modifier": ' \
#                     '"HeliHumanisti", "modified": 1415705418}}, "geometry_name": "GEOLOC"}'
#
#         self.con.request("PUT", "/locationdata/api/Streetlights/WFS_KATUVALO.405172", jsonstring,
#                          headers={"LBD_LOGIN_HEADER": user,
#                                   "Content-type": "application/json"})
#         response = self.con.getresponse()
#
#         print "(PUT) Response status code: "+str(response.status)
#
#         self.con = httplib.HTTPConnection(url, port)
#         self.con.request("GET", "/locationdata/api/Streetlights/WFS_KATUVALO.405172",
#                          headers={"LBD_LOGIN_HEADER": user})
#         response = self.con.getresponse()
#         afterjson = json.loads(response.read())
#         print "After PUT JSON: "+str(afterjson)
#
#         self.assertEqual(cmp(originaljson, afterjson), 0)
#         print "Test passed!"
#
#     def test_put_missing_id_field_check_response(self):
#         print "Running test: id field missing, check response"
#         user = "TiinaTeekkari"
#
#         jsonstring ='{"geometry": {"type": "Point", "coordinates": [23.643239226767022, 61.519112683582854]}, ' \
#                     '"type": "Feature", "properties": {"NIMI": "XPWR_6769212", ' \
#                     '"LAMPPU_TYYPPI_KOODI": "100340", "TYYPPI_KOODI": "105007", "KATUVALO_ID": 405172, ' \
#                     '"LAMPPU_TYYPPI": "ST 100 (SIEMENS)", "metadata": {"status": "teststatus", "modifier": ' \
#                     '"HeliHumanisti", "modified": 1415705418}}, "geometry_name": "GEOLOC"}'
#
#         self.con.request("PUT", "/locationdata/api/Streetlights/WFS_KATUVALO.405172", jsonstring,
#                          headers={"LBD_LOGIN_HEADER": user,
#                                   "Content-type": "application/json"})
#         response = self.con.getresponse()
#
#         print "(PUT) Response status code: "+str(response.status)
#
#         self.assertEqual(response.status, 400)
#         print "Test passed!"
#
#     def test_put_missing_status_field_check_response(self):
#         print "Running test: status field missing, check response"
#         user = "TiinaTeekkari"
#
#         jsonstring ='{"geometry": {"type": "Point", "coordinates": [23.643239226767022, 61.519112683582854]}, "id": ' \
#                     '"WFS_KATUVALO.405172", "type": "Feature", "properties": {"NIMI": "XPWR_6769212", ' \
#                     '"LAMPPU_TYYPPI_KOODI": "100340", "TYYPPI_KOODI": "105007", "KATUVALO_ID": 405172, ' \
#                     '"LAMPPU_TYYPPI": "ST 100 (SIEMENS)", "metadata": {"modifier": ' \
#                     '"HeliHumanisti", "modified": 1415705418}}, "geometry_name": "GEOLOC"}'
#
#         self.con.request("PUT", "/locationdata/api/Streetlights/WFS_KATUVALO.405172", jsonstring,
#                          headers={"LBD_LOGIN_HEADER": user,
#                                   "Content-type": "application/json"})
#         response = self.con.getresponse()
#
#         print "(PUT) Response status code: "+str(response.status)
#
#         self.assertEqual(response.status, 400)
#         print "Test passed!"
#
#     def test_put_empty_json(self):
#         print "Running test: empty json"
#         user = "TiinaTeekkari"
#
#         jsonstring = '{}'
#
#         self.con.request("PUT", "/locationdata/api/Streetlights/WFS_KATUVALO.405172", jsonstring,
#                          headers={"LBD_LOGIN_HEADER": user,
#                                   "Content-type": "application/json"})
#         response = self.con.getresponse()
#         print "(PUT) Response status code: "+str(response.status)
#
#         self.assertEqual(response.status, 400)
#         print "Test passed!"
#
#     def test_put_empty_json_string(self):
#         print "Running test: empty json"
#         user = "TiinaTeekkari"
#
#         jsonstring = ''
#
#         self.con.request("PUT", "/locationdata/api/Streetlights/WFS_KATUVALO.405172", jsonstring,
#                          headers={"LBD_LOGIN_HEADER": user,
#                                   "Content-type": "application/json"})
#         response = self.con.getresponse()
#
#         print "(PUT) Response status code: "+str(response.status)
#
#         self.assertEqual(response.status, 400)
#         print "Test passed!"
#
#     def test_no_content(self):
#         print "Running test: empty json"
#         user = "TiinaTeekkari"
#
#         self.con.request("PUT", "/locationdata/api/Streetlights/WFS_KATUVALO.405172",
#                          headers={"LBD_LOGIN_HEADER": user,
#                                   "Content-type": "application/json"})
#         response = self.con.getresponse()
#
#         print "(PUT) Response status code: "+str(response.status)
#
#         self.assertEqual(response.status, 400)
#         print "Test passed!"
#
#     def test_put_valid_data_invalid_resource(self):
#         """
#         Prerequisites: Metadata modifier in database should be different than the one in LBD_LOGIN_HEADER.
#         If not, this test is useless.
#         """
#         print "Running test: valid put request, nonexistent resource"
#         user = "TiinaTeekkari"
#
#         jsonstring ='{"geometry": {"type": "Point", "coordinates": [23.643239226767022, 61.519112683582854]}, "id": ' \
#                     '"WFS_KATUVALO.405172", "type": "Feature", "properties": {"NIMI": "XPWR_6769212", ' \
#                     '"LAMPPU_TYYPPI_KOODI": "100340", "TYYPPI_KOODI": "105007", "KATUVALO_ID": 405172, ' \
#                     '"LAMPPU_TYYPPI": "ST 100 (SIEMENS)", "metadata": {"status": "teststatus", "modifier": ' \
#                     '"HeliHumanisti", "modified": 1415705418}}, "geometry_name": "GEOLOC"}'
#
#         self.con.request("PUT", "/locationdata/api/Streetlights/WFS_KATUVALO.995172", jsonstring,
#                          headers={"LBD_LOGIN_HEADER": user,
#                                   "Content-type": "application/json"})
#         response = self.con.getresponse()
#
#         print "(PUT) Response status code: "+str(response.status)
#         self.assertEqual(response.status, 404)
#         print "Test passed!"
#
#     def test_put_valid_data_id_contradiction(self):
#         print "Running test: valid data, contradiction in resource id"
#         user = "TiinaTeekkari"
#
#         jsonstring ='{"geometry": {"type": "Point", "coordinates": [23.643239226767022, 61.519112683582854]}, "id": ' \
#                     '"WFS_KATUVALO.405172", "type": "Feature", "properties": {"NIMI": "XPWR_6769212", ' \
#                     '"LAMPPU_TYYPPI_KOODI": "100340", "TYYPPI_KOODI": "105007", "KATUVALO_ID": 405172, ' \
#                     '"LAMPPU_TYYPPI": "ST 100 (SIEMENS)", "metadata": {"status": "teststatus", "modifier": ' \
#                     '"HeliHumanisti", "modified": 1415705418}}, "geometry_name": "GEOLOC"}'
#
#         self.con.request("PUT", "/locationdata/api/Streetlights/WFS_KATUVALO.405171", jsonstring,
#                          headers={"LBD_LOGIN_HEADER": user,
#                                   "Content-type": "application/json"})
#         response = self.con.getresponse()
#
#         print "(PUT) Response status code: "+str(response.status)
#         self.assertEqual(response.status, 400)
#         print "Test passed!"
#
#
# class TestSingleResourceDelete(TestCase):
#     """
#     Test class for single resource DELETE method tests
#
#     Tests assume that basic PUT method works.
#     """
#
#     def setUp(self):
#         self.con = httplib.HTTPConnection(url, port)
#         print "Preparing system for new test..."
#         user = "TiinaTeekkari"
#
#         jsonstring ='{"geometry": {"type": "Point", "coordinates": [23.643239226767022, 61.519112683582854]}, "id": ' \
#                     '"WFS_KATUVALO.405172", "type": "Feature", "properties": {"NIMI": "XPWR_6769212", ' \
#                     '"LAMPPU_TYYPPI_KOODI": "100340", "TYYPPI_KOODI": "105007", "KATUVALO_ID": 405172, ' \
#                     '"LAMPPU_TYYPPI": "ST 100 (SIEMENS)", "metadata": {"status": "teststatus", "modifier": ' \
#                     '"HeliHumanisti", "modified": 1415705418}}, "geometry_name": "GEOLOC"}'
#
#         self.con.request("PUT", "/locationdata/api/Streetlights/WFS_KATUVALO.405172", jsonstring,
#                          headers={"LBD_LOGIN_HEADER": user,
#                                   "Content-type": "application/json"})
#         response = self.con.getresponse()
#         self.assertEqual(response.status, 200, "Prepare failed...")
#
#         print "Reinitializing connection..."
#         self.con = httplib.HTTPConnection(url, port)
#
#         print "System prepared!"
#
#     def test_delete_valid(self):
#         print "Running test: valid data"
#         user = "TiinaTeekkari"
#
#         self.con.request("DELETE", "/locationdata/api/Streetlights/WFS_KATUVALO.405172",
#                          headers={"LBD_LOGIN_HEADER": user})
#         response = self.con.getresponse()
#
#         print "(DELETE) Response status code: "+str(response.status)
#         self.assertEqual(response.status, 200)
#
#         self.con = httplib.HTTPConnection(url, port)
#         self.con.request("GET", "/locationdata/api/Streetlights/WFS_KATUVALO.405172",
#                          headers={"LBD_LOGIN_HEADER": user})
#         response = self.con.getresponse()
#
#         print "(GET) Response status code: "+str(response.status)
#
#         self.assertEqual(response.status, 200)
#
#         responsejson = json.loads(response.read())
#         with self.assertRaises(KeyError):
#             print responsejson["properties"]["metadata"]
#         print "Test passed!"
#
#     def test_delete_invalid_resource(self):
#         print "Running test: valid data, invalid resource"
#         user = "TiinaTeekkari"
#
#         self.con.request("DELETE", "/locationdata/api/Streetlights/WFS_KATUÖÄÖÄÅ.999172",
#                          headers={"LBD_LOGIN_HEADER": user})
#         response = self.con.getresponse()
#
#         print "(DELETE) Response status code: "+str(response.status)
#         self.assertEqual(response.status, 404)
#
#         print "Test passed!"
#
#     def test_delete_invalid_collection(self):
#         print "Running test: valid data, invalid collection"
#         user = "TiinaTeekkari"
#
#         self.con.request("DELETE", "/locationdata/api/FOOBARISTA/WFS_KATUVALO.405172",
#                          headers={"LBD_LOGIN_HEADER": user})
#         response = self.con.getresponse()
#
#         print "(DELETE) Response status code: "+str(response.status)
#         self.assertEqual(response.status, 404)
#
#         print "Test passed!"
#
#     def test_delete_twice(self):
#         print "Running test: deleting item twice"
#         user = "TiinaTeekkari"
#
#         self.con.request("DELETE", "/locationdata/api/FOOBARISTA/WFS_KATUVALO.405172",
#                          headers={"LBD_LOGIN_HEADER": user})
#         response = self.con.getresponse()
#
#         print "(DELETE) Second response status code: "+str(response.status)
#         self.assertEqual(response.status, 404)
#
#         self.con = httplib.HTTPConnection(url, port)
#         self.con.request("DELETE", "/locationdata/api/FOOBARISTA/WFS_KATUVALO.405172",
#                          headers={"LBD_LOGIN_HEADER": user})
#         response = self.con.getresponse()
#
#         print "(DELETE) Second response status code: "+str(response.status)
#         self.assertEqual(response.status, 404)
#
#         print "Test passed!"
#
#
# class TestSingleResourcePost(TestCase):
#     def setUp(self):
#         self.con = httplib.HTTPConnection(url, port)
#         self.headers = lbdheader
#
#     def test_post_method(self):
#         print "Running test: test POST method, valid collection and resource"
#         jsonstring ='{"geometry": {"type": "Point", "coordinates": [23.643239226767022, 61.519112683582854]}, "id": ' \
#                     '"WFS_KATUVALO.405172", "type": "Feature", "properties": {"NIMI": "XPWR_6769212", ' \
#                     '"LAMPPU_TYYPPI_KOODI": "100340", "TYYPPI_KOODI": "105007", "KATUVALO_ID": 405172, ' \
#                     '"LAMPPU_TYYPPI": "ST 100 (SIEMENS)", "metadata": {"status": "teststatus", "modifier": ' \
#                     '"HeliHumanisti", "modified": 1415705418}}, "geometry_name": "GEOLOC"}'
#
#         self.con.request("POST", "/locationdata/api/Streetlights/WFS_KATUVALO.405172", jsonstring,
#                          headers=self.headers)
#         response = self.con.getresponse()
#
#         print "(POST) Response status code: "+str(response.status)
#         self.assertEqual(response.status, 405)
#
#         print "Test passed!"
#
#     def test_post_method_invalid_resource(self):
#         print "Running test: test POST method, valid collection, invalid resource"
#         jsonstring ='{"geometry": {"type": "Point", "coordinates": [23.643239226767022, 61.519112683582854]}, "id": ' \
#                     '"WFS_KATUVALO.405172", "type": "Feature", "properties": {"NIMI": "XPWR_6769212", ' \
#                     '"LAMPPU_TYYPPI_KOODI": "100340", "TYYPPI_KOODI": "105007", "KATUVALO_ID": 405172, ' \
#                     '"LAMPPU_TYYPPI": "ST 100 (SIEMENS)", "metadata": {"status": "teststatus", "modifier": ' \
#                     '"HeliHumanisti", "modified": 1415705418}}, "geometry_name": "GEOLOC"}'
#
#         self.con.request("POST", "/locationdata/api/Streetlights/WFS_KATUVALO.999999", jsonstring,
#                          headers=self.headers)
#         response = self.con.getresponse()
#
#         print "POST Response status code: "+str(response.status)
#         self.assertEqual(response.status, 405)
#
#         print "Test passed!"
#
#     def test_post_method_invalid_collection(self):
#         print "Running test: test POST method, valid collection, invalid resource"
#         jsonstring ='{"geometry": {"type": "Point", "coordinates": [23.643239226767022, 61.519112683582854]}, "id": ' \
#                     '"WFS_KATUVALO.405172", "type": "Feature", "properties": {"NIMI": "XPWR_6769212", ' \
#                     '"LAMPPU_TYYPPI_KOODI": "100340", "TYYPPI_KOODI": "105007", "KATUVALO_ID": 405172, ' \
#                     '"LAMPPU_TYYPPI": "ST 100 (SIEMENS)", "metadata": {"status": "teststatus", "modifier": ' \
#                     '"HeliHumanisti", "modified": 1415705418}}, "geometry_name": "GEOLOC"}'
#
#         self.con.request("POST", "/locationdata/api/FOOBARISTA/WFS_KATUVALO.405172", jsonstring,
#                          headers=self.headers)
#         response = self.con.getresponse()
#
#         print "POST Response status code: "+str(response.status)
#         self.assertEqual(response.status, 405)
#
#         print "Test passed!"