package cn.v5.util;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.slf4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

public class HttpPost {
    private static final Logger log = org.slf4j.LoggerFactory.getLogger(HttpPost.class);
    private static int connectTimeOut = 5000;
    private static int readTimeOut = 10000;
    private static String requestEncoding = "UTF-8";

    public static int getConnectTimeOut() {
        return connectTimeOut;
    }

    public static void setConnectTimeOut(int connectTimeOut) {
        HttpPost.connectTimeOut = connectTimeOut;
    }

    public static int getReadTimeOut() {
        return readTimeOut;
    }

    public static void setReadTimeOut(int readTimeOut) {
        HttpPost.readTimeOut = readTimeOut;
    }

    public static String getRequestEncoding() {
        return requestEncoding;
    }

    public static void setRequestEncoding(String requestEncoding) {
        HttpPost.requestEncoding = requestEncoding;
    }

    public static String doGet(String requrl, Map<String, ?> parameters) {
        HttpClient httpClient = new HttpClient();
        GetMethod getMethod = new GetMethod(requrl);
        StringBuilder sb = new StringBuilder();
        if (!MapUtils.isEmpty(parameters)) {
            for (String key : parameters.keySet()) {
                sb.append(key).append("=").append(parameters.get(key)).append("&");
            }
            getMethod.setQueryString(org.yaml.snakeyaml.util.UriEncoder.encode(sb.toString()));
            if (log.isDebugEnabled()) {
                log.debug("[doGet],queryString:{}", getMethod.getQueryString());
            }

        }
        String res = "";
        try {
            int statusCode = httpClient.executeMethod(getMethod);
            if (statusCode == HttpStatus.SC_OK) {
                res = new String(getMethod.getResponseBody());
            }
            if (log.isDebugEnabled()) {
                log.debug("[HttpPost],doGet:statusCode:{}", statusCode);
            }
        } catch (IOException e) {
            log.error("[doGet],e:{}", e);
            log.error(e.getMessage(), e);
        } finally {
            getMethod.releaseConnection();
        }

        return res;
    }

    public static String doGet(String requrl, Map<?, ?> parameters, String recvEndcoding) {
        HttpURLConnection url_con = null;
        String responseContent = "";
        String vchartset = (recvEndcoding == null) ? HttpPost.requestEncoding : recvEndcoding;
        try {
            StringBuffer params = new StringBuffer();
            for (Iterator<?> iter = parameters.entrySet().iterator(); iter.hasNext(); ) {
                Entry<?, ?> element = (Entry<?, ?>) iter.next();
                params.append(element.getKey().toString());
                params.append("=");
                params.append(URLEncoder.encode(element.getValue().toString(), vchartset));
                params.append("&");
            }
            if (params.length() > 0) {
                params = params.deleteCharAt(params.length() - 1);
            }


            requrl = requrl + "?" + params;
            URL url = new URL(requrl);
            url_con = (HttpURLConnection) url.openConnection();
            url_con.setRequestMethod("GET");
            System.setProperty("sun.net.client.defaultConnectTimeout", String.valueOf(HttpPost.connectTimeOut));
            System.setProperty("sun.net.client.defaultReadTimeout", String.valueOf(HttpPost.readTimeOut));
            url_con.setDoOutput(true);//
            //byte[] b=params.toString().getBytes();
            //url_con.getOutputStream().write(b, 0,b.length);
            url_con.getOutputStream().flush();
            url_con.getOutputStream().close();
            BufferedReader in = new BufferedReader(new InputStreamReader(url_con.getInputStream(), vchartset));
            String buf = null;
            while ((buf = in.readLine()) != null) {
                responseContent += buf;
            }

            int code = url_con.getResponseCode();
            if (code != 200) {
                responseContent = "ERROR" + code;
            }
        } catch (Exception e) {
            log.error("网络故障:" + e.toString(), e);
        } finally {
            if (url_con != null) {
                url_con.disconnect();
            }
        }

        return responseContent;

    }


    public static String doGet(String reqUrl, String recvEncoding) {
        HttpURLConnection url_con = null;
        String responseContent = "";
        String vchartset = recvEncoding == null ? HttpPost.requestEncoding : recvEncoding;
        try {
            StringBuffer params = new StringBuffer();
            String queryUrl = reqUrl;
            int paramIndex = reqUrl.indexOf("?");

            if (paramIndex > 0) {
                queryUrl = reqUrl.substring(0, paramIndex);
                String parameters = reqUrl.substring(paramIndex + 1, reqUrl.length());
                String[] paramArray = parameters.split("&");
                for (int i = 0; i < paramArray.length; i++) {
                    String string = paramArray[i];
                    int index = string.indexOf("=");
                    if (index > 0) {
                        String parameter = string.substring(0, index);
                        String value = string.substring(index + 1, string.length());
                        params.append(parameter);
                        params.append("=");
                        params.append(URLEncoder.encode(value, vchartset));
                        params.append("&");
                    }
                }

                params = params.deleteCharAt(params.length() - 1);
            }

            //logger.info("queryUrl:"+queryUrl);

            URL url = new URL(queryUrl);
            url_con = (HttpURLConnection) url.openConnection();
            url_con.setRequestMethod("GET");
            System.setProperty("sun.net.client.defaultConnectTimeout", String.valueOf(HttpPost.connectTimeOut));
            System.setProperty("sun.net.client.defaultReadTimeout", String.valueOf(HttpPost.readTimeOut));
            url_con.setDoOutput(true);
            byte[] b = params.toString().getBytes();
            url_con.getOutputStream().write(b, 0, b.length);
            url_con.getOutputStream().flush();
            url_con.getOutputStream().close();

            BufferedReader in = new BufferedReader(new InputStreamReader(url_con.getInputStream(), vchartset));
            String buf = null;
            while ((buf = in.readLine()) != null) {
                responseContent += buf;
            }


            int code = url_con.getResponseCode();
            if (code != 200) {
                responseContent = "ERROR" + code;
            }
        } catch (Exception e) {
            log.error("网络故障:" + e.toString());
        } finally {
            if (url_con != null) {
                url_con.disconnect();
            }
        }

        return responseContent;

    }

    public static String doPost(String reqUrl, Map<String, String> parameters, Map<String, String> headerParameters, String recvEncoding) {
        HttpURLConnection url_con = null;
        String responseContent = "";
        String vchartset = recvEncoding == null ? HttpPost.requestEncoding : recvEncoding;
        try {
            StringBuffer params = new StringBuffer();
            for (Iterator<?> iter = parameters.entrySet().iterator(); iter.hasNext(); ) {
                Entry<?, ?> element = (Entry<?, ?>) iter.next();
                params.append(element.getKey().toString());
                params.append("=");
                params.append(URLEncoder.encode(element.getValue().toString(), vchartset));
                params.append("&");
            }

            if (params.length() > 0) {
                params = params.deleteCharAt(params.length() - 1);
            }
            //logger.info("params:"+params);

            URL url = new URL(reqUrl);
            url_con = (HttpURLConnection) url.openConnection();
            url_con.setRequestMethod("POST");
            url_con.setConnectTimeout(HttpPost.connectTimeOut);
            url_con.setReadTimeout(HttpPost.readTimeOut);

            //设置请求头
            if (headerParameters != null) {
                Iterator<Entry<String, String>> iter = headerParameters.entrySet().iterator();
                while (iter.hasNext()) {
                    Entry<String, String> next = iter.next();
                    url_con.setRequestProperty(next.getKey(), next.getValue());
                }
            }

            url_con.setDoOutput(true);

            byte[] b = params.toString().getBytes();
            url_con.getOutputStream().write(b, 0, b.length);
            url_con.getOutputStream().flush();
            url_con.getOutputStream().close();

            BufferedReader in = new BufferedReader(new InputStreamReader(url_con.getInputStream(), vchartset));
            String buf = null;
            while ((buf = in.readLine()) != null) {
                responseContent += buf;
            }

            int code = url_con.getResponseCode();
            if (code != 200) {
                responseContent = "ERROR" + code;
            }

        } catch (IOException e) {
            log.error("网络故障:" + e.toString());
        } finally {
            if (url_con != null) {
                url_con.disconnect();
            }
        }

        return responseContent;
    }

    public static String doPost(String reqUrl, Map<String, String> parameters, String recvEncoding) {
        HttpURLConnection url_con = null;
        String responseContent = "";
        String vchartset = recvEncoding == null ? HttpPost.requestEncoding : recvEncoding;
        try {
            StringBuffer params = new StringBuffer();
            for (Iterator<?> iter = parameters.entrySet().iterator(); iter.hasNext(); ) {
                Entry<?, ?> element = (Entry<?, ?>) iter.next();
                params.append(element.getKey().toString());
                params.append("=");
                params.append(URLEncoder.encode(element.getValue().toString(), vchartset));
                params.append("&");
            }

            if (params.length() > 0) {
                params = params.deleteCharAt(params.length() - 1);
            }
            //logger.info("params:"+params);

            URL url = new URL(reqUrl);
            url_con = (HttpURLConnection) url.openConnection();
            url_con.setRequestMethod("POST");
            url_con.setConnectTimeout(HttpPost.connectTimeOut);
            url_con.setReadTimeout(HttpPost.readTimeOut);
            url_con.setDoOutput(true);
            byte[] b = params.toString().getBytes();
            url_con.getOutputStream().write(b, 0, b.length);
            url_con.getOutputStream().flush();
            url_con.getOutputStream().close();

            BufferedReader in = new BufferedReader(new InputStreamReader(url_con.getInputStream(), vchartset));
            String buf = null;
            while ((buf = in.readLine()) != null) {
                responseContent += buf;
            }

            int code = url_con.getResponseCode();
            if (code != 200) {
                responseContent = "ERROR" + code;
            }

        } catch (IOException e) {
            log.error("网络故障:" + e.toString(),e);
        } finally {
            if (url_con != null) {
                url_con.disconnect();
            }
        }

        return responseContent;
    }


    public static void main(String[] args) throws UnsupportedEncodingException {
        Map<String, String> map = new HashMap<String, String>();
        map.put("username", "NFTB700093");//此处填写用户账号
        map.put("scode", "268990");//此处填写用户密码
        map.put("mobile", "18013301800");//此处填写发送号码
        map.put("tempid", "MB-2013110603");//此处填写发送号码

        map.put("content", "@ma@=1234");//此处填写短信内容
        String temp = HttpPost.doPost("http://mssms.cn:8000/msm/sdk/http/sendsms.jsp", map, "GBK");
        System.out.println("值:" + temp);//此处为短信发送的返回值
    }
}
