package cn.v5.service;

import cn.v5.util.HttpPost;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Created by sunhao on 15-2-6.
 */
@Service
public class HttpService {

    public String doGet(String requrl, Map<String, ?> parameters) {
        return HttpPost.doGet(requrl, parameters);
    }

    public String doGet(String requrl, Map<?, ?> parameters, String recvEndcoding) {
        return HttpPost.doGet(requrl, parameters, recvEndcoding);
    }

    public String doGet(String reqUrl, String recvEncoding) {
        return HttpPost.doGet(reqUrl, recvEncoding);
    }

    public String doPost(String reqUrl, Map<String, String> parameters, Map<String, String> headerParameters, String recvEncoding) {
        return HttpPost.doPost(reqUrl, parameters, headerParameters, recvEncoding);
    }

    public String doPost(String reqUrl, Map<String, String> parameters, String recvEncoding) {
        return HttpPost.doPost(reqUrl, parameters, recvEncoding);
    }
}