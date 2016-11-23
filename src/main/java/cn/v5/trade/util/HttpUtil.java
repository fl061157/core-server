package cn.v5.trade.util;

import okhttp3.*;
import okio.BufferedSink;

import java.io.IOException;

/**
 * Created by fangliang on 16/9/2.
 */
public class HttpUtil {

    public static String doPost(String url, String postBody) throws Exception {

        OkHttpClient client = new OkHttpClient();
        final MediaType MEDIA_TYPE_TEXT = MediaType.parse("text/plain");
        RequestBody requestBody = new RequestBody() {
            @Override
            public MediaType contentType() {
                return MEDIA_TYPE_TEXT;
            }

            @Override
            public void writeTo(BufferedSink sink) throws IOException {
                sink.writeUtf8(postBody);
            }

            @Override
            public long contentLength() throws IOException {
                return postBody.length();
            }
        };

        Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .build();

        Response response = client.newCall(request).execute(); //TODO  Update Order
        if (!response.isSuccessful()) {
            throw new IOException("Remote error : " + response);
        }

        return response.body().string();

    }


}
