# -*- coding: utf-8 -*-
"""
.. module:: lbd_backend.urls
    :platform: Unix, Windows
    :synopsis: This module handles http requests related to location data.
"""

__author__ = 'Aki Mäkinen'


from django.conf.urls import patterns, url
from lbd_backend.LBD_REST_locationdata.views import single_resource, collection, \
    collection_near, collection_inarea
from django.contrib import admin
admin.autodiscover()

urlpatterns = patterns('',
    # REST API
    url(r'^locationdata/api/(?P<collection>\w+)/$', collection),
    url(r'^locationdata/api/(?P<collection>\w+)/(?P<resource>(\w|\.)+)$', single_resource),
    url(r'^locationdata/api/(?P<collection>\w+)/near/$', collection_near),
    url(r'^locationdata/api/(?P<collection>\w+)/inarea/$', collection_inarea),
    # url(r'^messages/api/(?P<collection>\w+)$', stub),
    # url(r'^messages/api/(?P<collection>\w+)/(?P<message>\w+)$', stub),


)
