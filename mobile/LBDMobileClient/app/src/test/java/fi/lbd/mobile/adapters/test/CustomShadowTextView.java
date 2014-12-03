package fi.lbd.mobile.adapters.test;

/**
 * Created by Ossi on 3.12.2014.
 */

import android.widget.TextView;

import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.shadows.ShadowTextView;

public class CustomShadowTextView extends ShadowTextView {

    private CharSequence text = "";

    @Implementation
    public void setText( CharSequence text ) {
        this.text = text;
    }
    @Implementation
    public CharSequence getText(){
        return this.text;
    }
}