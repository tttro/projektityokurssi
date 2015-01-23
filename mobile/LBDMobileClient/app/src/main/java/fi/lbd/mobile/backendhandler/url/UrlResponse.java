package fi.lbd.mobile.backendhandler.url;


import java.util.HashMap;
import java.util.Map;

/**
 * Response object from URLReader.
 *
 * Created by Tommi.
 */
public class UrlResponse {
    public enum ResponseStatus {
        STATUS_200(200, "OK"),
        STATUS_400(400, "Bad Request"),
        STATUS_401(401, "Unauthorized"),
        STATUS_403(403, "Forbidden"),
        STATUS_404(404, "Not found"),
        STATUS_405(405, "Method not allowed"),
        STATUS_418(418, "I'm a teapot"),
        STATUS_500(500, "Internal server error"),
        STATUS_NOT_FOUND(-1, "Could not find status.");

        private final static Map<Integer, ResponseStatus> statuses = new HashMap<Integer, ResponseStatus>();
        static {
            for (ResponseStatus status : ResponseStatus.values()) {
                statuses.put(status.getCode(), status);
            }
        }

        private final int code;
        private final String meaning;

        private ResponseStatus(int code, String meaning) {
            this.code = code;
            this.meaning = meaning;
        }

        public int getCode() {
            return this.code;
        }

        public String getMeaning() {
            return this.meaning;
        }

        public static ResponseStatus get(int statusId) {
            ResponseStatus status = statuses.get(statusId);
            return (status == null) ? STATUS_NOT_FOUND : status;
        }
    }

    private final String contents;
    private final ResponseStatus status;
    private final String statusReason;

    /**
     * Response object from URLReader.
     *
     * @param contents  Contents of the url.
     * @param status    Returned status.
     * @param statusReason  Returned reason for the status.
     */
    public UrlResponse(String contents, ResponseStatus status, String statusReason) {
        this.contents = contents;
        this.status = status;
        this.statusReason = statusReason;
    }

    public String getContents() {
        return contents;
    }

    public ResponseStatus getStatus() {
        return status;
    }

    public String getStatusReason() {
        return statusReason;
    }

    public String toString() {
        return "ID("+ status.getCode() +"): "+ status.getMeaning() + ", reason: "+ this.getStatusReason();
    }
}
