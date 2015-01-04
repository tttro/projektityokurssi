import json
from django.http import HttpResponse
from django.shortcuts import render

# Create your views here.
from django.views.decorators.http import require_http_methods
from lbd_backend.LBD_REST_locationdata.decorators import this_is_a_login_wrapper_dummy
from lbd_backend.LBD_REST_users.models import User
from lbd_backend.utils import s_codes


@this_is_a_login_wrapper_dummy
@require_http_methods(["GET"])
def list_users(request, *args, **kwargs):
    items = User.objects.as_pymongo()
    temp = list()
    for item in items:
        item.pop("_id")
        item.pop("user_id")
        temp.append(item)
    return HttpResponse(content=json.dumps(temp), status=s_codes["OK"], content_type="application/json")