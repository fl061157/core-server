package cn.v5.util;

import com.amazonaws.util.json.JSONObject;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.InputStreamBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.springframework.util.DigestUtils;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Created by luyanliang on 2015/6/16.
 */
public class UploadUtil {
    private static final Logger log = org.slf4j.LoggerFactory.getLogger(UploadUtil.class);

    public static String upload(String url, InputStream is, String fileName, Map<String, String> headers, Map<String, String> params) throws ClientProtocolException, IOException {
        CloseableHttpClient httpClient = HttpClients.createDefault();

        HttpPost httpPost = new HttpPost(url);

        if(headers != null) {
            for (Map.Entry<String, String> headEntry : headers.entrySet()) {
                httpPost.setHeader(headEntry.getKey(), headEntry.getValue());
            }
        }

        MultipartEntity mutiEntity = new MultipartEntity();

        if(params != null) {
            for (Map.Entry<String, String> paramEntry : params.entrySet()) {
                mutiEntity.addPart(paramEntry.getKey(), new StringBody(paramEntry.getValue(), Charset.forName("utf-8")));
            }
        }

        InputStreamBody fileBody = new InputStreamBody(is, fileName);
        mutiEntity.addPart("file", fileBody);

        httpPost.setEntity(mutiEntity);
        HttpResponse httpResponse = httpClient.execute(httpPost);
        HttpEntity httpEntity = httpResponse.getEntity();

        String content = EntityUtils.toString(httpEntity);
        return content;
    }

    public static void main(String args[]) throws Exception {
        FileInputStream fis = new FileInputStream("c:\\1.jpg");
        String authKey = "34rfdfasdo0aifejak1";
        String url = "http://115.29.250.113:8080/api/user/avatar/upload";

        Map<String, String> headers = new HashMap<>();
        headers.put("region-code", "0086");

        Map<String, String> params = new HashMap<>();
        String randomId = UUID.randomUUID().toString();
        params.put("randomId", randomId);
        params.put("auth", DigestUtils.md5DigestAsHex(String.format("%s%s", randomId, authKey).getBytes()));

        String result = upload(url, fis, "1.jpg", headers, params);
        if (!result.isEmpty()) {
            JSONObject dataJson = new JSONObject(result);
            String key = (String) dataJson.get("file_id");
            System.out.println(key);
        } else {
            throw new Exception("fileserver result is empty");
        }
    }
}
