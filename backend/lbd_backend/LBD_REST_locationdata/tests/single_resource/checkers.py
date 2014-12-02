# -*- coding: UTF-8 -*-
import httplib
import json
from lbd_backend.LBD_REST_locationdata.tests.test_settings import lbdheader, url, port
from lbd_backend.utils import geo_json_scheme_validation

__author__ = 'Aki Mäkinen'

_base = "/locationdata/api/"

def _status_tester(collection, resource, con, httpmethod, content=None):
    print "Running test: " + _status_tester.__name__

    if collection[1] == resource[1]:
        expected_status = collection[1]
    elif collection[1] == 200:
        expected_status = resource[1]
    else:
        expected_status = collection[1]

    resource_uri = _base + collection[0] + "/" + resource[0]

    print "Using URI: "+resource_uri
    print "Method: " + httpmethod
    if httpmethod == ("GET" or "DELETE"):
        con.request(httpmethod, resource_uri, headers=lbdheader)
    else:
        con.request(httpmethod, resource_uri, content, headers=lbdheader)

    response = con.getresponse()
    print "Expected status code: " + str(expected_status)
    print "Response status code: " + str(response.status)
    response.read() # Because it needs to be read
    assert response.status == expected_status, "Wrong status!"

    print "Test passed!"

# test_only_collection should be set as true when testing urls returning a feature collection.
# If it is set true in these situations, the behaviour of checker is undefined.
def _content_tester(collection, resource, con, httpmethod, content=None, test_only_collection=False, allow_error_content=True):
    print "Running test..."

    if httpmethod == "GET":
        expected_result = (collection[1] and resource[1])
    else:
        expected_result = False

    resource_uri = _base + collection[0] + "/"

    if not test_only_collection:
        resource_uri = resource_uri + resource[0]

    print "Using URI: "+resource_uri
    print "Method: " + httpmethod

    if httpmethod == ("GET" or "DELETE"):
        con.request(httpmethod, resource_uri, headers=lbdheader)
    else:
        con.request(httpmethod, resource_uri, content, headers=lbdheader)

    response = con.getresponse()

    print "Response status code: " + str(response.status)
    resp_body = response.read() # Because it needs to be read

    if httpmethod == "GET":
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
        # Otherwise there shouldn't be any content in the response body.
        # We do expect at this point that GET returns valid GeoJSON. If not, these tests are moot.
        print "Response body: "+resp_body
        if expected_result == False and not allow_error_content:
                stripped = resp_body.strip()
                if stripped:
                    assert False, "Response body not empty!"

        if response.status == 200:
            con.request("GET", resource_uri, headers=lbdheader)
            response2 = con.getresponse()
            r2_status = response2.status
            r2_body = response2.read()

            assert r2_status == 200, "Checkup response went wrong. Status returned: " + str(r2_status)

            sent_content_json = json.loads(content)
            r2_json = json.loads(r2_body)
            if httpmethod == "POST":
                try:
                    assert sent_content_json["properties"]["metadata"]["status"] == r2_json["properties"]["metadata"]["status"]
                except KeyError as e:
                    assert False, e.message

            elif httpmethod == "DELETE":
                if not test_only_collection:
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

            elif httpmethod == "PUT":
                if not test_only_collection:
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