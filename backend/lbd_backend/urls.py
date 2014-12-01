# -*- coding: utf-8 -*-
"""
.. module:: lbd_backend.urls
    :platform: Unix, Windows
    :synopsis: This module handles http requests related to location data.
"""
__author__ = 'Aki Mäkinen'


from django.conf.urls import patterns, url
from lbd_backend.LBD_REST_locationdata.views import single_resource, collection, \
    collection_near, collection_inarea, testing_view_popmeta, testing_view_dropmeta, search_from_rest
from django.contrib import admin
from django.conf import settings

admin.autodiscover()

urlpatterns = patterns('',
    # REST API
    url(r'^locationdata/api/(?P<collection>\w+)/$', collection),
    url(r'^locationdata/api/(?P<collection>\w+)/(?P<resource>(\w|\.)+)$', single_resource),
    url(r'^locationdata/api/(?P<collection>\w+)/near/$', collection_near),
    url(r'^locationdata/api/(?P<collection>\w+)/inarea/$', collection_inarea),
    url(r'^locationdata/api/(?P<collection>\w+)/search/$', search_from_rest),

    # url(r'^messages/api/(?P<collection>\w+)$', stub),
    # url(r'^messages/api/(?P<collection>\w+)/(?P<message>\w+)$', stub),
)

if settings.TESTING_URLS:
    testpatterns = patterns('',
        url(r'^locationdata/api/SL/testing/popmeta$', testing_view_popmeta),
        url(r'^locationdata/api/SL/testing/dropmeta$', testing_view_dropmeta),
    )

    urlpatterns += testpatterns

