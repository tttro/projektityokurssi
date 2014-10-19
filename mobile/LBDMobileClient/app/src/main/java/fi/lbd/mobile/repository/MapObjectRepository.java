package fi.lbd.mobile.repository;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.List;

import fi.lbd.mobile.mapobjects.MapObject;
import fi.lbd.mobile.mapobjects.PointLocation;

/**
 * Handles the map objects. Fetches the objects from backend service if required.
 *
 * Created by tommi on 19.10.2014.
 */
public class MapObjectRepository {

	private static final String tempContents = "[{\"OD_data\": {\"geometry_name\": \"GEOLOC\", \"geometry\": {\"type\": \"Point\", \"coordinates\": [23.79485698869311, 61.50460698965098]}, \"feature_id\": \"WFS_KATUVALO.385517\", \"_id\": {\"$oid\": \"543cd02915040d2c248286f7\"}, \"type\": \"Feature\", \"properties\": {\"NIMI\": \"XPWR_3800864\", \"LAMPPU_TYYPPI_KOODI\": \"100991\", \"TYYPPI_KOODI\": \"105001\", \"KATUVALO_ID\": 385517, \"LAMPPU_TYYPPI\": \"Ei m\\u00e4\\u00e4ritelty (TUNTEMATON)\", \"TYYPPI\": \"Hg_125W ELOHOPEALAMPPU\"}}, \"MetaData\": {}, \"OD_info\": {\"identifier_field_name\": \"feature_id\"}}, {\"OD_data\": {\"geometry_name\": \"GEOLOC\", \"geometry\": {\"type\": \"Point\", \"coordinates\": [23.795944666699963, 61.50414388252429]}, \"feature_id\": \"WFS_KATUVALO.380904\", \"_id\": {\"$oid\": \"543cd04715040d2c24829509\"}, \"type\": \"Feature\", \"properties\": {\"NIMI\": \"XPWR_7933809\", \"LAMPPU_TYYPPI_KOODI\": \"100309\", \"TYYPPI_KOODI\": \"105004\", \"KATUVALO_ID\": 380904, \"LAMPPU_TYYPPI\": \"SC 50 (SITECO)\", \"TYYPPI\": \"Sp-Na_70W SUURPAINENATRIUM\"}}, \"MetaData\": {}, \"OD_info\": {\"identifier_field_name\": \"feature_id\"}}, {\"OD_data\": {\"geometry_name\": \"GEOLOC\", \"geometry\": {\"type\": \"Point\", \"coordinates\": [23.794550107729926, 61.50349638615375]}, \"feature_id\": \"WFS_KATUVALO.383914\", \"_id\": {\"$oid\": \"543cd06015040d2c2482a0b2\"}, \"type\": \"Feature\", \"properties\": {\"NIMI\": \"XPWR_3800792\", \"LAMPPU_TYYPPI_KOODI\": \"100991\", \"TYYPPI_KOODI\": \"105001\", \"KATUVALO_ID\": 383914, \"LAMPPU_TYYPPI\": \"Ei m\\u00e4\\u00e4ritelty (TUNTEMATON)\", \"TYYPPI\": \"Hg_125W ELOHOPEALAMPPU\"}}, \"MetaData\": {}, \"OD_info\": {\"identifier_field_name\": \"feature_id\"}}, {\"OD_data\": {\"geometry_name\": \"GEOLOC\", \"geometry\": {\"type\": \"Point\", \"coordinates\": [23.795338124987868, 61.504624152265954]}, \"feature_id\": \"WFS_KATUVALO.385516\", \"_id\": {\"$oid\": \"543cd07415040d2c2482aa29\"}, \"type\": \"Feature\", \"properties\": {\"NIMI\": \"XPWR_3800862\", \"LAMPPU_TYYPPI_KOODI\": \"100991\", \"TYYPPI_KOODI\": \"105001\", \"KATUVALO_ID\": 385516, \"LAMPPU_TYYPPI\": \"Ei m\\u00e4\\u00e4ritelty (TUNTEMATON)\", \"TYYPPI\": \"Hg_125W ELOHOPEALAMPPU\"}}, \"MetaData\": {}, \"OD_info\": {\"identifier_field_name\": \"feature_id\"}}]";


	public List<MapObject> getObjectsNearLocation(PointLocation location) {
		//String url = "http://api-lbdserver.rhcloud.com/locationdata/api/Streetlights/near/?latitude=23.795199257764725&longitude=61.503697166613755";
        String url = "http://api-lbdserver.rhcloud.com/locationdata/api/Streetlights/near/?latitude="+location.latitude+"&longitude="+location.longitude;

		String contents = URLReader.get(url);
		try {
			//return MapObjectParser.parseArrayOfObjects(new JSONArray(tempContents));
            return MapObjectParser.parseArrayOfObjects(new JSONArray(contents));
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        // TODO: Finally lohkoon streamien sulkemiset
        return null;
	}
	

}
