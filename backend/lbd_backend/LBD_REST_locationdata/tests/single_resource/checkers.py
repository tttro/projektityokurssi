# -*- coding: UTF-8 -*-
import httplib
import json
from lbd_backend.LBD_REST_locationdata.tests.test_settings import lbdheader, url, port
from lbd_backend.utils import geo_json_scheme_validation

__author__ = 'Aki Mäkinen'

_base = "/locationdata/api/"

class Checker(object):
    con = None
    httpmethod = None
    content = None
    test_only_collection = False
    allow_error_content = True

    def _status_tester(self, collection, resource):
        print "Running test: " + self._status_tester.__name__

        if collection[1] == resource[1]:
            expected_status = collection[1]
        elif collection[1] == 200:
            expected_status = resource[1]
        else:
            expected_status = collection[1]

        resource_uri = _base + collection[0] + "/" + resource[0]

        print "Using URI: "+resource_uri
        print "Method: " + self.httpmethod
        if self.httpmethod == ("GET" or "DELETE"):
            self.con.request(self.httpmethod, resource_uri, headers=lbdheader)
        else:
            self.con.request(self.httpmethod, resource_uri, self.content, headers=lbdheader)

        response = self.con.getresponse()
        print "Expected status code: " + str(expected_status)
        print "Response status code: " + str(response.status)
        response.read() # Because it needs to be read
        assert response.status == expected_status, "Wrong status!"

        print "Test passed!"

    # test_only_collection should be set as true when testing urls returning a feature collection.
    # If it is set true in these situations, the behaviour of checker is undefined.
    def _content_tester(self, collection, resource):
        print "Running test..."

        if self.httpmethod == "GET":
            expected_result = (collection[1] and resource[1])
        else:
            expected_result = False

        resource_uri = _base + collection[0] + "/"

        if not self.test_only_collection:
            resource_uri = resource_uri + resource[0]

        print "Using URI: "+resource_uri
        print "Method: " + self.httpmethod

        if self.httpmethod == ("GET" or "DELETE"):
            self.con.request(self.httpmethod, resource_uri, headers=lbdheader)
        else:
            self.con.request(self.httpmethod, resource_uri, self.content, headers=lbdheader)

        response = self.con.getresponse()

        print "Response status code: " + str(response.status)
        resp_body = response.read() # Because it needs to be read

        if self.httpmethod == "GET":
            # If the method is get, we want to know if the response is valid geojson
            if response.status == 200:
                try:
                    resp_json = json.loads(resp_body)
                except (KeyError, ValueError) as e:
                    assert False, e.message
                assert geo_json_scheme_validation(resp_json) == expected_result
            elif response.status == 404:
                pass
            else:
                if resp_body.strip():
                    print "Content: " + resp_body
                    assert False, "Received content with status code other than 200"
        else:
            # Otherwise there shouldn't be anyself.content in the response body.
            # We do expect at this point that GET returns valid GeoJSON. If not, these tests are moot.
            print "Response body: "+resp_body
            if expected_result == False and not self.allow_error_content:
                    stripped = resp_body.strip()
                    if stripped:
                        assert False, "Response body not empty!"

            if response.status == 200:
                self.con.request("GET", resource_uri, headers=lbdheader)
                response2 = self.con.getresponse()
                r2_status = response2.status
                r2_body = response2.read()

                assert r2_status == 200, "Checkup response went wrong. Status returned: " + str(r2_status)

                sent_content_json = json.loads(self.content)
                r2_json = json.loads(r2_body)
                if self.httpmethod == "POST":
                    self.con.request("GET", _base + collection[0] + "/" +sent_content_json["id"], headers=lbdheader)
                    response2 = self.con.getresponse()
                    r2_status = response2.status
                    r2_body = response2.read()

                    if r2_status == 404:
                        assert False, "Resource not found. Resouce id: " + sent_content_json["id"]
                    else:
                        assert r2_status == 200, "Response status: "+str(r2_status)

                    try:
                        assert sent_content_json["properties"]["metadata"]["status"] == r2_json["properties"]["metadata"]["status"]
                    except KeyError as e:
                        assert False, e.message

                elif self.httpmethod == "DELETE":
                    if not self.test_only_collection:
                        try:
                            t = r2_json["properties"]["metadata"]
                            assert False, "Metadata found after deletion"
                        except KeyError:
                            print "Test passed!"
                    else:
                        for item in r2_json:
                            try:
                                t = r2_json["properties"]["metadata"]
                                assert False, "Metadata found after deletion"
                            except KeyError:
                                pass

                elif self.httpmethod == "PUT":
                    if not self.test_only_collection:
                        try:
                            assert sent_content_json["properties"]["metadata"]["status"] == r2_json["properties"]["metadata"]["status"]
                        except KeyError as e:
                            assert False, e.message
                    else:
                        try:
                            for second in sent_content_json["features"]:
                                for item in r2_json["features"]:
                                    if second["id"] == item["id"]:
                                        assert second["properties"]["metadata"]["status"] == item["properties"]["metadata"]["status"]
                                        break
                                    else:
                                        try:
                                            t = second["properties"]["metadata"]
                                            assert False, "Metadata found when it shouldn't exist"
                                        except KeyError as e:
                                            pass

                        except KeyError as e:
                            assert False, e.message

        print "Test passed!"