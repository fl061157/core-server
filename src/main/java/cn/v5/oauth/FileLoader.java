package cn.v5.oauth;

import cn.v5.util.LoggerFactory;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.eclipse.jetty.http.HttpStatus;
import org.slf4j.Logger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by fangliang on 16/5/10.
 */

public class FileLoader {

    private static Logger LOGGER = LoggerFactory.getLogger(FileLoader.class);

    private static File TMP_ILE = new File("/tmp");

    public static File load(String url, String destFileName) throws IOException {
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("[Oauth2] FileLoader url:{} , destFileName:{}", url, destFileName);
        }
        CloseableHttpClient httpclient = null;
        try {
            httpclient = HttpClients.createDefault();
            HttpGet httpGet = new HttpGet(url);
            HttpResponse response = httpclient.execute(httpGet);

            StatusLine statusLine = response.getStatusLine();

            int code = statusLine.getStatusCode();
            if (code != HttpStatus.OK_200) {
                LOGGER.error("[FileLoader] failure reason:{} , code:{}  ", statusLine.getReasonPhrase(), code);
                throw new IOException("Http request error code:" + code);
            }
            HttpEntity entity = response.getEntity();
            InputStream inputStream = entity.getContent();
            File file = new File(TMP_ILE, destFileName);
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            int pos;
            byte[] tmp = new byte[1024];
            while ((pos = inputStream.read(tmp)) != -1) {
                fileOutputStream.write(tmp, 0, pos);
            }
            fileOutputStream.flush();
            fileOutputStream.close();
            inputStream.close();

            return file;
        } finally {
            if (httpclient != null) {
                httpclient.close();
            }
        }
    }

}
