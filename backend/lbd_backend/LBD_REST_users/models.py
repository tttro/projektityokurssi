from mongoengine import *

class User(DynamicDocument):
    user_id = StringField(required=True, unique=True)
    email = EmailField(required=True, unique=True)
    first_name = StringField()
    last_name = StringField()