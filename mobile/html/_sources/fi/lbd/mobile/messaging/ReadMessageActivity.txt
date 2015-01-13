.. java:import:: android.app Activity

.. java:import:: android.content Context

.. java:import:: android.content Intent

.. java:import:: android.os Bundle

.. java:import:: android.view View

.. java:import:: android.widget TextView

.. java:import:: android.widget Toast

.. java:import:: com.squareup.otto Subscribe

.. java:import:: fi.lbd.mobile ActiveActivitiesTracker

.. java:import:: fi.lbd.mobile R

.. java:import:: fi.lbd.mobile.events BusHandler

.. java:import:: fi.lbd.mobile.events RequestFailedEvent

.. java:import:: fi.lbd.mobile.messaging.events DeleteMessageEvent

.. java:import:: fi.lbd.mobile.messaging.events DeleteMessageSucceededEvent

.. java:import:: fi.lbd.mobile.messaging.events RequestUserMessagesEvent

.. java:import:: fi.lbd.mobile.messaging.messageobjects StringMessageObject

ReadMessageActivity
===================

.. java:package:: fi.lbd.mobile.messaging
   :noindex:

.. java:type:: public class ReadMessageActivity extends Activity

Methods
-------
onCreate
^^^^^^^^

.. java:method:: @Override protected void onCreate(Bundle savedInstanceState)
   :outertype: ReadMessageActivity

onDeleteClick
^^^^^^^^^^^^^

.. java:method:: public void onDeleteClick(View view)
   :outertype: ReadMessageActivity

onEvent
^^^^^^^

.. java:method:: @Subscribe public void onEvent(DeleteMessageSucceededEvent event)
   :outertype: ReadMessageActivity

onEvent
^^^^^^^

.. java:method:: @Subscribe public void onEvent(RequestFailedEvent event)
   :outertype: ReadMessageActivity

onPause
^^^^^^^

.. java:method:: @Override public void onPause()
   :outertype: ReadMessageActivity

onReplyClick
^^^^^^^^^^^^

.. java:method:: public void onReplyClick(View view)
   :outertype: ReadMessageActivity

onResume
^^^^^^^^

.. java:method:: @Override public void onResume()
   :outertype: ReadMessageActivity

onStart
^^^^^^^

.. java:method:: @Override public void onStart()
   :outertype: ReadMessageActivity

onStop
^^^^^^

.. java:method:: @Override public void onStop()
   :outertype: ReadMessageActivity

