from functools import wraps
from django.http import HttpResponse
from RESThandlers.HandlerInterface.Factory import HandlerFactory

# TODO: PLACEHOLDER NAME
def restifier(func):
    @wraps(func)
    def wrapper(request, *args, **kwargs):
        if "collection" in kwargs:
            kwargs["handlerinterface"] = HandlerFactory(kwargs["collection"]).create()
        else:
            return HttpResponse(status=404)
        return func(request, *args, **kwargs)
    return wrapper