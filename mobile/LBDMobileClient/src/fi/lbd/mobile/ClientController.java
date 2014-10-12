package fi.lbd.mobile;

import android.os.Bundle;

public interface ClientController {
	public enum ViewType {
		Map
	}
	
	void changeView(ViewType view, Bundle args);
}
