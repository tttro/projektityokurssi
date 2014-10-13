from django.conf.urls import patterns, include, url
from lbd_backend.LBD_REST_locationdata.views import single_resource, collection, collection_near, getjson

from django.contrib import admin
admin.autodiscover()

urlpatterns = patterns('',
    # Examples:
    # url(r'^$', 'lbd_backend.views.home', name='home'),
    # url(r'^blog/', include('blog.urls')),

    #url(r'^admin/', include(admin.site.urls)),
    url(r'^$', getjson),
    #url(r'^changejson$', changejson),
    # url(r'^api/(?P<collection>\w+)$', changejson),
    # url(r'^api/(?P<collection>\w+)/(?P<resource>\w+)$', resttest),

    # REST API
    url(r'^locationdata/api/(?P<collection>\w+)$', collection),
    url(r'^locationdata/api/(?P<collection>\w+)/(?P<resource>(\w|\.)+)$', single_resource),
    url(r'^locationdata/api/(?P<collection>\w+)/near/$', collection_near),
    # url(r'^messages/api/(?P<collection>\w+)$', stub),
    # url(r'^messages/api/(?P<collection>\w+)/(?P<message>\w+)$', stub),


)
