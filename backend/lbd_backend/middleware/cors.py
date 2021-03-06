# -*- coding: UTF-8 -*-
__author__ = 'Aki Mäkinen'
from django.http import HttpResponse

_cors_dict = {
    "Access-Control-Allow-Origin": "*",
    "Access-Control-Allow-Credentials": "false",
    "Access-Control-Allow-Headers": "LBD_LOGIN_HEADER, LBD_OAUTH_ID, Authorization, Content-Type, Accept",
    "Access-Control-Allow-Methods": "GET, PUT, POST, DELETE, OPTIONS"
}

class CorsMiddleware(object):

    def process_request(self, request):
        if request.method == "OPTIONS" and "HTTP_ACCESS_CONTROL_REQUEST_METHOD" in request.META:
            return HttpResponse()
        else:
            return None

    def process_response(self, request, response):
        if isinstance(response, HttpResponse):
            for header, value in _cors_dict.iteritems():
                response[header] = value

        return response

