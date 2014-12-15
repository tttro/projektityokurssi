package fi.lbd.mobile.adapters.test;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.shadows.ShadowLog;
import org.robolectric.Robolectric;

import java.util.HashMap;
import java.util.Map;

import fi.lbd.mobile.CustomRobolectricTestRunner;
import fi.lbd.mobile.R;
import fi.lbd.mobile.adapters.ListDetailsAdapter;
import fi.lbd.mobile.location.ImmutablePointLocation;
import fi.lbd.mobile.mapobjects.ImmutableMapObject;
import fi.lbd.mobile.mapobjects.MapObject;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


/**
 * Created by Ossi.
 */

@RunWith(CustomRobolectricTestRunner.class)
public class ListDetailsAdapterTest {

    @Mock ImmutablePointLocation location;
    @Mock Activity mockActivity = mock(Activity.class);

    Activity activity;
    LayoutInflater inflater;
    Map<String, String> propertiesTest1;
    Map<String, String> metadataTest1;
    MapObject mapObjectTest1;
    ListDetailsAdapter adapterTest1;
    ListDetailsAdapter adapterTest2;
    ListDetailsAdapter adapterTest3;
    ListDetailsAdapter adapterTest4;

    @Before
    public void setUp() throws Exception {
        ShadowLog.stream = System.out;
        MockitoAnnotations.initMocks(this);

        propertiesTest1 = new HashMap<>();
        propertiesTest1.put("propertyp0", "valuep0");
        propertiesTest1.put("propertyp1", "valuep1");
        metadataTest1 = new HashMap<>();
        metadataTest1.put("propertym0", "valuem0");
        metadataTest1.put("propertym1", "valuem1");

        activity = Robolectric.buildActivity(Activity.class).create().get();

        mapObjectTest1 = new ImmutableMapObject(false, "1", location, propertiesTest1, metadataTest1);

        adapterTest1 = new ListDetailsAdapter(mockActivity, mapObjectTest1, propertiesTest1.size(),
                1,metadataTest1.size());
        adapterTest2 = new ListDetailsAdapter(mockActivity, mapObjectTest1, propertiesTest1.size()+1,
                1,metadataTest1.size()+1);
        adapterTest3 = new ListDetailsAdapter(mockActivity, mapObjectTest1, propertiesTest1.size(),
                0,metadataTest1.size()-1);
        adapterTest4 = new ListDetailsAdapter(mockActivity, mapObjectTest1, 0,0,0);
    }

    @Test
    public void testConstructor() {
        final String testName = "testConstructor";
        System.out.println("_____________________________________________________________________");
        System.out.println("TESTING: "+testName);
        System.out.println("---------------------------------------------------------------------");

        assertThat(adapterTest1.getCount() == 5);
        assertThat(adapterTest2.getCount() == 5);
        assertThat(adapterTest3.getCount() == 3);
        assertThat(adapterTest4.getCount() == 0);

        ListDetailsAdapter adapterTest5 = new ListDetailsAdapter(mockActivity, mapObjectTest1,
                0,100,metadataTest1.size()-1);
        ListDetailsAdapter adapterTest6 = new ListDetailsAdapter(mockActivity, mapObjectTest1,
                propertiesTest1.size()-1,-100,metadataTest1.size()-2);

        assertThat(adapterTest5.getCount() == 2);
        assertThat(adapterTest6.getCount() == 1);
    }

    @Test
    public void testGetView(){
        final String testName = "testGetView";
        System.out.println("_____________________________________________________________________");
        System.out.println("TESTING: "+testName);
        System.out.println("---------------------------------------------------------------------");

        LayoutInflater layoutInflater = Robolectric.buildActivity(Activity.class).create().get().getLayoutInflater();
        LayoutInflater mockLayoutInflater = mock(LayoutInflater.class);
        ViewGroup mockViewGroup = mock(ViewGroup.class);

        LinearLayout parent = new LinearLayout(Robolectric.application);
        LinearLayout linearLayout = (LinearLayout)layoutInflater.inflate(R.layout.listview_single_row, parent);
        TextView titleText = (TextView)linearLayout.findViewById(R.id.textViewObjectId);
        TextView dataText = (TextView)linearLayout.findViewById(R.id.textViewDistance);

        when(mockActivity.getLayoutInflater()).thenReturn(mockLayoutInflater);
        when(mockLayoutInflater.inflate(R.layout.listview_double_row, mockViewGroup, false)).thenReturn(mockViewGroup);
        when(mockViewGroup.findViewById(R.id.textViewObjectId)).thenReturn(titleText);
        when(mockViewGroup.findViewById(R.id.textViewObjectLocation)).thenReturn(dataText);

        titleText.setText("Text");
        dataText.setText("Text");
        for(int i = -1; i < 7; ++i){

            // Test an illegal index in each adapter
            if(i == -1){
                adapterTest1.getView(i, null, mockViewGroup);
                assertThat(titleText.getText().equals("Text"));
                assertThat(dataText.getText().equals("Text"));
                titleText.setText("Text");
                dataText.setText("Text");

                adapterTest2.getView(i, null, mockViewGroup);
                assertThat(titleText.getText().equals("Text"));
                assertThat(dataText.getText().equals("Text"));
                titleText.setText("Text");
                dataText.setText("Text");

                adapterTest3.getView(i, null, mockViewGroup);
                assertThat(titleText.getText().equals("Text"));
                assertThat(dataText.getText().equals("Text"));
                titleText.setText("Text");
                dataText.setText("Text");

                adapterTest4.getView(i, null, mockViewGroup);
                assertThat(titleText.getText().equals("Text"));
                assertThat(dataText.getText().equals("Text"));
                titleText.setText("Text");
                dataText.setText("Text");
            }
            // Test the first two elements in each adapter
            else if(i < 2) {
                adapterTest1.getView(i, null, mockViewGroup);
                assertThat(titleText.getText().equals(String.format("propertyp%d", i)));
                assertThat(dataText.getText().equals(String.format("valuep%d", i)));
                titleText.setText("Should change");
                dataText.setText("Should change");

                adapterTest2.getView(i, null, mockViewGroup);
                assertThat(titleText.getText().equals(String.format("propertyp%d", i)));
                assertThat(dataText.getText().equals(String.format("valuep%d", i)));
                titleText.setText("Should change");
                dataText.setText("Should change");

                adapterTest3.getView(i, null, mockViewGroup);
                assertThat(titleText.getText().equals(String.format("propertyp%d", i)));
                assertThat(dataText.getText().equals(String.format("valuep%d", i)));
                titleText.setText("Should not change");
                dataText.setText("Should not change");

                adapterTest4.getView(i, null, mockViewGroup);
                assertThat(titleText.getText().equals("Should not change"));
                assertThat(dataText.getText().equals("Should not change"));
                titleText.setText("Should change");
                dataText.setText("Should change");
            }

            // Test the third element in each adapter. AdapterTest3 doesn't contain location, so
            // third element should be a metadata element.
            else if (i == 2){
                adapterTest1.getView(i, null, mockViewGroup);
                assertThat(titleText.getText().equals("LOCATION"));
                titleText.setText("Should change");
                adapterTest2.getView(i, null, mockViewGroup);
                assertThat(titleText.getText().equals("LOCATION"));
                titleText.setText("Should change");
                adapterTest3.getView(i, null, mockViewGroup);
                assertThat(titleText.getText().equals("propertym0"));
                assertThat(dataText.getText().equals("valuem0"));
                titleText.setText("Should not change");
                adapterTest4.getView(i, null, mockViewGroup);
                assertThat(titleText.getText().equals("Should not change"));
                titleText.setText("Should change");
                dataText.setText("Should change");
            }

            // Test the fourth element in each adapter
            else if (i == 3){
                adapterTest1.getView(i, null, mockViewGroup);
                assertThat(titleText.getText().equals("propertym0"));
                assertThat(dataText.getText().equals("valuem0"));
                titleText.setText("Should change");
                dataText.setText("Should change");

                adapterTest2.getView(i, null, mockViewGroup);
                assertThat(titleText.getText().equals("propertym0"));
                assertThat(dataText.getText().equals("valuem0"));
                titleText.setText("Should not change");
                dataText.setText("Should not change");

                adapterTest3.getView(i, null, mockViewGroup);
                assertThat(titleText.getText().equals("Should not change"));
                assertThat(dataText.getText().equals("Should not change"));
                titleText.setText("Should not change");
                dataText.setText("Should not change");

                adapterTest4.getView(i, null, mockViewGroup);
                assertThat(titleText.getText().equals("Should not change"));
                assertThat(dataText.getText().equals("Should not change"));
                titleText.setText("Should change");
                dataText.setText("Should change");
            }

            // Test fifth elements
            else if (i == 4){
                adapterTest1.getView(i, null, mockViewGroup);
                assertThat(titleText.getText().equals("propertym1"));
                assertThat(dataText.getText().equals("valuem1"));
                titleText.setText("Should change");
                dataText.setText("Should change");

                adapterTest2.getView(i, null, mockViewGroup);
                assertThat(titleText.getText().equals("propertym1"));
                assertThat(dataText.getText().equals("valuem1"));
                titleText.setText("Should not change");
                dataText.setText("Should not change");

                adapterTest3.getView(i, null, mockViewGroup);
                assertThat(titleText.getText().equals("Should not change"));
                assertThat(dataText.getText().equals("Should not change"));

                adapterTest4.getView(i, null, mockViewGroup);
                assertThat(titleText.getText().equals("Should not change"));
                assertThat(dataText.getText().equals("Should not change"));
                titleText.setText("Should not change");
                dataText.setText("Should not change");
            }

            else {
                adapterTest1.getView(i, null, mockViewGroup);
                assertThat(titleText.getText().equals("Should not change"));
                assertThat(dataText.getText().equals("Should not change"));
                adapterTest2.getView(i, null, mockViewGroup);
                assertThat(titleText.getText().equals("Should not change"));
                assertThat(dataText.getText().equals("Should not change"));
                adapterTest3.getView(i, null, mockViewGroup);
                assertThat(titleText.getText().equals("Should not change"));
                assertThat(dataText.getText().equals("Should not change"));
                adapterTest4.getView(i, null, mockViewGroup);
                assertThat(titleText.getText().equals("Should not change"));
                assertThat(dataText.getText().equals("Should not change"));
            }
        }

    }
}
