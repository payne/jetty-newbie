package de.afarber.fcmnotregistered;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.Result;
import org.eclipse.jetty.client.util.BufferingResponseListener;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.util.ajax.JSON;

public class Main {
    private static final String FCM_URL            = "https://fcm.googleapis.com/fcm/send";
    private static final String FCM_KEY            = "key=REPLACE_BY_YOUR_KEY";
    private static final String FCM_RESULTS        = "results";
    private static final String FCM_ERROR          = "error";
    private static final String FCM_NOT_REGISTERED = "NotRegistered";
    
    private static final String EXAMPLE_RESPONSE_1 =  "";

    private static final Map<String, Object> REQUEST      = new HashMap<>();
    private static final Map<String, Object> NOTIFICATION = new HashMap<>();
    private static final Map<String, Object> DATA         = new HashMap<>();
    
    static {
        REQUEST.put("to", "APA91bHun4MxP5egoKMwt2KZFBaFUH-1RYqx...");
        REQUEST.put("notification", NOTIFICATION);
        REQUEST.put("data", DATA);
        NOTIFICATION.put("body", "great match!");
        NOTIFICATION.put("title", "Portugal vs. Denmark");
        NOTIFICATION.put("icon", "myicon");
        DATA.put("Nick", "Mario");
        DATA.put("Room", "PortugalVSDenmark");
    }

    private static final HttpClient sHttpClient = new HttpClient();
    private static final BufferingResponseListener sFcmListener = new BufferingResponseListener() {
        @Override
        public void onComplete(Result result) {
            if (!result.isSucceeded()) {
                System.err.println(result.getFailure());
                return;
            }
            
            String body = getContentAsString(StandardCharsets.UTF_8);
            System.out.printf("onContent: %s\n", body);
            Map<String, Object> resp = (Map<String, Object>) JSON.parse(body);
            
            try {
                Object[] results = (Object[]) resp.get(FCM_RESULTS);
                Map map = (Map) results[0];
                String error = (String) map.get(FCM_ERROR);
                if (FCM_NOT_REGISTERED.equals(error)) {
                    // TODO delete FCM token from the database
                }
            } catch (Exception ex) {
                System.err.println(ex);
            }
        }
    };

    public static void main(String[] args) throws Exception {
        sHttpClient.start();
        sHttpClient.POST(FCM_URL)
            .header(HttpHeader.AUTHORIZATION, FCM_KEY)
            .header(HttpHeader.CONTENT_TYPE, "application/json")
            .content(new StringContentProvider(JSON.toString(REQUEST)))
            .send(sFcmListener);
    }
}