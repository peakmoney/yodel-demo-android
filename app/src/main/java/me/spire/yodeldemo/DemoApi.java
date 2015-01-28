package me.spire.yodeldemo;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.http.Body;
import retrofit.http.POST;

public class DemoApi {

    private RestAdapter mRestAdapter;
    private DemoService mDemoService;

    public DemoApi() {
        mRestAdapter = new RestAdapter.Builder()
                .setEndpoint("http://10.0.1.22:3000")
                .build();

        mDemoService = mRestAdapter.create(DemoService.class);
    }

    public DemoService getService() {
        return mDemoService;
    }

    public static class Device {
        private Integer user_id;
        private String token;
        private String platform = "android";

        public Device(int userId, String token) {
            this.user_id = userId;
            this.token = token;
        }

        public Integer getUserId() {
            return user_id;
        }

        public String getToken() {
            return token;
        }

        public void setToken(String token) {
            this.token = token;
        }

        public String getPlatform() {
            return platform;
        }
    }

    public static class Message {
        private Integer user_id;
        private String message;
        private String payload;

        public Message(int userId, String message, String payload) {
            this.user_id = userId;
            this.message = message;
            this.payload = payload;
        }
    }

    public static class StatusResponse {
        private String status;

        public String getStatus() {
            return status;
        }
    }

    public interface DemoService {
        @POST("/subscribe")
        public void subscribe(@Body Device device, Callback<StatusResponse> cb);

        @POST("/unsubscribe")
        public void unsubscribe(@Body Device device, Callback<StatusResponse> cb);

        @POST("/notify")
        public void notify(@Body Message message, Callback<StatusResponse> cb);
    }

}
