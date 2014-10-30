from functools import wraps
from django.http import HttpResponse
from RESThandlers.HandlerInterface.Exceptions import CollectionNotInstalled
from RESThandlers.HandlerInterface.Factory import HandlerFactory

# TODO: PLACEHOLDER NAME
def location_collection(func):
    @wraps(func)
    def wrapper(request, *args, **kwargs):
        if "collection" in kwargs:
            try:
                hf = HandlerFactory(kwargs["collection"])
                kwargs["handlerinterface"] = hf.create()
            except CollectionNotInstalled:
                return HttpResponse(status=404)
        else:
            return HttpResponse(status=404)
        try:
            return func(request, *args, **kwargs)
        except NotImplementedError:
            return HttpResponse(status=418)
    return wrapper