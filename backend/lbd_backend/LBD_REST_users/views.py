import json

from django.http import HttpResponse
from django.shortcuts import render_to_response


# Create your views here.
from django.views.decorators.http import require_http_methods
import httplib2
import mongoengine
from lbd_backend.LBD_REST_locationdata.decorators import lbd_require_login
from lbd_backend.LBD_REST_users.models import User
from lbd_backend.utils import s_codes


@lbd_require_login
@require_http_methods(["GET"])
def list_users(request, *args, **kwargs):
    items = User.objects.as_pymongo()
    temp = list()
    for item in items:
        item.pop("_id")
        item.pop("user_id")
        temp.append(item)
    return HttpResponse(content=json.dumps(temp), status=s_codes["OK"], content_type="application/json; charset=utf-8")

### SIMPLE REGISTERATION. DO NOT USE IN PRODUCTION!

@require_http_methods(["GET"])
def user_exists(request, *args, **kwargs):
    return render_to_response('registered_user_test.html')


@require_http_methods(["POST"])
def add_user(request, *args, **kwargs):
    access_token = request.META["HTTP_LBD_LOGIN_HEADER"]

    userid = request.META["HTTP_LBD_OAUTH_ID"]
    url = ('https://www.googleapis.com/oauth2/v1/tokeninfo?access_token=%s' % access_token)
    h = httplib2.Http()
    resp, content = h.request(url, 'GET')
    try:
        res = json.loads(content)
    except ValueError:
        return HttpResponse(status=s_codes["INTERNALERROR"])

    # If there was an error in the access token info, abort.
    try:
        rstatus = int(resp.get('status'))
    except ValueError:
        return HttpResponse(status=s_codes["INTERNALERROR"])

    if rstatus != 200:
        print "STATUS: " + str(resp.get('status'))
        print "CONTENT: "+ content
        return HttpResponse(status=s_codes["BAD"])

    if userid == res["user_id"]:
        print "User matches"
    else:
        return HttpResponse(content='{"message": "Error. ID mismatch."}', status=s_codes["BAD"],
                            content_type="application/json; charset=utf-8")
    try:
        result = json.loads(h.request('https://www.googleapis.com/oauth2/v2/userinfo',
                                      headers={"Authorization": "Bearer "+ request.META["HTTP_LBD_LOGIN_HEADER"]})[1])
    except ValueError:
        return HttpResponse(status=s_codes["INTERNALERROR"])

    try:
        user = User()
        user.user_id = request.META["HTTP_LBD_OAUTH_ID"]
        user.email = result['email']
        user.first_name = result["given_name"]
        user.last_name = result["family_name"]
        user.save()
        return HttpResponse(status=s_codes["OK"])
    except mongoengine.NotUniqueError:
        return HttpResponse(status=s_codes["BAD"])
    except Exception as e:
        template = "An exception of type {0} occured. Arguments:\n{1!r}"
        print template.format(type(e).__name__, e.args)
        return HttpResponse(status=s_codes["INTERNALERROR"])


def index(request, *args, **kwargs):
    return render_to_response('google_auth.html')
