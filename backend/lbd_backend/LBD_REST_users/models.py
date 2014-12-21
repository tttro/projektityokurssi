from mongoengine import *

class User(DynamicDocument):
    user_id = StringField(required=True)
    email = EmailField(required=True)
    first_name = StringField()
    last_name = StringField()