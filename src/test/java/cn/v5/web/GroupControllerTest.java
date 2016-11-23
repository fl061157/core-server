package cn.v5.web;


import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.junit.Test;

import java.io.IOException;


/**
 * Created by hi on 14-3-8.
 */
public class GroupControllerTest {

    static String requesturl = "http://192.168.31.110:9101";
    static String sessionId = "cb64c020a8ed11e3a6e463fbf5d6fe5b";
    static String userId= "80e08ce0a8df11e38b7c63fbf5d6fe5b";
    public void create() throws IOException {

        String  url = requesturl + "/api/group/create";
        HttpClient client = new HttpClient();
        PostMethod post = new PostMethod(url);

        NameValuePair[] postData = new NameValuePair[2];
        postData[0] = new NameValuePair("name", "天下第一群");
        postData[1] = new NameValuePair("member", "81cf1520a8eb11e3882863fbf5d6fe5b,9ea391a0a8df11e38b7c63fbf5d6fe5b,008b20c0a8e211e38b7c63fbf5d6fe5b");

        post.setRequestHeader("client-session", sessionId);
        post.setRequestBody(postData);

        client.executeMethod(post);

        String result = new String(post.getResponseBodyAsString());
        System.out.println(result);

    }


    public void update() throws IOException {

        String  url = requesturl + "/api/group/update";
        HttpClient client = new HttpClient();
        PostMethod post = new PostMethod(url);

        NameValuePair[] postData = new NameValuePair[2];
        postData[0] = new NameValuePair("groupId", "375e1590a8fa11e3a6cc5d33f25fe4e7");
        postData[1] = new NameValuePair("name", "first group");

        post.setRequestHeader("client-session", sessionId);
        post.setRequestBody(postData);

        client.executeMethod(post);

        String result = new String(post.getResponseBodyAsString());
        System.out.println(result);

    }


    public void get() throws IOException {

        String  url = requesturl + "/api/group/get";

        HttpClient client = new HttpClient();
        GetMethod method = new GetMethod(url);
        method.setQueryString(new NameValuePair[]{
                new NameValuePair("groupId", "375e1590a8fa11e3a6cc5d33f25fe4e7")
        });

        method.setRequestHeader("client-session", sessionId);
        client.executeMethod(method);

        String result = new String(method.getResponseBodyAsString());
        System.out.println(result);

    }



    public void invite() throws IOException {

        String  url = requesturl + "/api/group/invite";

        HttpClient client = new HttpClient();

        PostMethod post = new PostMethod(url);

        NameValuePair[] postData = new NameValuePair[2];
        postData[0] = new NameValuePair("groupId", "375e1590a8fa11e3a6cc5d33f25fe4e7");
        postData[1] = new NameValuePair("member", "e631bec0a8df11e38b7c63fbf5d6fe5b");


        post.setRequestHeader("client-session", sessionId);
        post.setRequestBody(postData);

        client.executeMethod(post);

        String result = new String(post.getResponseBodyAsString());
        System.out.println(result);

    }

    public void remove() throws IOException {

        String  url = requesturl + "/api/group/remove";

        HttpClient client = new HttpClient();

        PostMethod post = new PostMethod(url);

        NameValuePair[] postData = new NameValuePair[2];
        postData[0] = new NameValuePair("groupId", "375e1590a8fa11e3a6cc5d33f25fe4e7");
        postData[1] = new NameValuePair("member", "80e08ce0a8df11e38b7c63fbf5d6fe5b");


        post.setRequestHeader("client-session", sessionId);
        post.setRequestBody(postData);

        client.executeMethod(post);

        String result = new String(post.getResponseBodyAsString());
        System.out.println(result);

    }



}
