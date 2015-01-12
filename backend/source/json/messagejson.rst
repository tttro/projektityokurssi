.. _messagejson:

Message
-------

The JSON for messages has been influenced by GeoJSON (a little bit). This is visible when comparing how single and multiple
messages are relayed. A single message has type "Message" while multiple messages are sent in a JSON "envelope" with "MessageCollection"
as type.

An example containing both single message and a collection of messages::

    {
        "messages": [
            {
                "category": "Streetlights",
                "attachments": [
                    {
                        "category": "Streetlights",
                        "id": "WFS_KATUVALO.405172"
                    }
                ],
                "type": "Message",
                "topic": "Testi",
                "messageread": true,
                "message": "Tämä on testiviesti",
                "recipient": "tiina@teekkari.com",
                "id": 10,
                "sender": "tiina@teekkari.com"
            },
            {
                "category": "Streetlights",
                "attachments": [
                    {
                        "category": "Streetlights",
                        "id": "WFS_KATUVALO.405172"
                    }
                ],
                "timestamp": 1420786021,
                "topic": "Testi",
                "messageread": true,
                "type": "Message",
                "message": "Tämä on testiviesti",
                "recipient": "tiina@teekkari.com",
                "id": 11,
                "sender": "tiina@teekkari.com"
            }
        ],
        "type": "MessageCollection",
        "totalMessages": 2
    }

Message collection has three elements:

============= ======== ========== =================================================================
Field's name  Required Value type Notes
============= ======== ========== =================================================================
type          True     String     Always "MessageCollection".
messages      True     List       List of messages. Format below.
totalMessages True     Integer    Amount of messages in the collection.
============= ======== ========== =================================================================

Format for a single message is following:

============ ======== ========== ==============================================================
Field's name Required Value type Notes
============ ======== ========== ==============================================================
type         True     String     Always "Message"
topic        True     String     Topic of the message
category     False    String     Category of the message. Name of a location data collection.
id           True     Integer    Message id.
sender       True     String     Tells who sent the message.
recipient    True     String     Tells who is the recipient.
message      True     String     Content of the message.
timestamp    True     Integer    Unix timestamp (in seconds). Tells when the message was sent.
attachments  False    List       List of attachments. Format described below.
============ ======== ========== ==============================================================

.. note::
   If the a category is specified for a message, there must exist a location data collection with that name.

If attachment element is added to a message, message category becomes a required field. For flexibility, the message category
and the attachment category can be different from each other.

Attachment elements are JSON documents with two fields:

=========== ======== ========== ================================================================================
Fieldname   Required Value type Notes
=========== ======== ========== ================================================================================
category    True     String     Category of the attachment. Name of a location data collection.
id          True     String     Id of the attached object. Must exist in the specified location data collection.
=========== ======== ========== ================================================================================


