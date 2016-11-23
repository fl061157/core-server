package cn.v5.web;


import com.amazonaws.auth.PropertiesCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.*;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.TransferManagerConfiguration;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;


/**
 * Created by hi on 14-3-8.
 */
public class UserControllerTest {

    static String requesturl = "http://us.v5.cn";

    static String sessionId = "958bfd9c31464fe5b2907e846b48d16a";
    static String userId= "f8b72ef0ba0e11e3931763fbf5d6fe5b";

    public void bindDevice() throws IOException {

        String  url = requesturl + "/api/user/bind/device";
        HttpClient client = new HttpClient();
        PostMethod post = new PostMethod(url);

        NameValuePair[] postData = new NameValuePair[3];
        postData[1] = new NameValuePair("app_id", "0");
        postData[2] = new NameValuePair("device_type", "1");
        //for(long mobile=18800000000l;mobile<=18800000260l;mobile++){
            postData[0] = new NameValuePair("mobile", "19300000002");
            post.setRequestBody(postData);
            client.executeMethod(post);
            String result = post.getResponseBodyAsString();
            System.out.println(result);
        //}

//        String result = post.getResponseBodyAsString();
//        System.out.println(result);

    }

    public void user() throws IOException {
        String  url = requesturl + "/api/user";

        HttpClient client = new HttpClient();
        GetMethod method = new GetMethod(url);
        method.setQueryString(new NameValuePair[] {
                new NameValuePair("id",userId)
        });

        method.setRequestHeader("client-session", sessionId);
        client.executeMethod(method);

        String result = method.getResponseBodyAsString();
        System.out.println(result);
    }

    public void userByMo() throws IOException {
        String  url = requesturl + "/api/user/mobile";

        HttpClient client = new HttpClient();
        GetMethod method = new GetMethod(url);
        method.setQueryString(new NameValuePair[] {
                new NameValuePair("mobile","18013301800")
        });

        method.setRequestHeader("client-session", sessionId);
        client.executeMethod(method);

        String result = method.getResponseBodyAsString();
        System.out.println(result);
    }

    public void authcode() throws IOException {
        String  url = requesturl + "/api/phone/authcode";

        HttpClient client = new HttpClient();
        GetMethod method = new GetMethod(url);
        long  currentTime = System.currentTimeMillis();
        String mobile = "18013301800";
        String countrycode = "0086";
        String authkey = DigestUtils.md5Hex("dudu_online.loulian.cn"+mobile+currentTime);
        method.setQueryString(new NameValuePair[] {
                new NameValuePair("countrycode",countrycode),
                new NameValuePair("mobile",mobile),
                new NameValuePair("currentTime",currentTime+""),
                new NameValuePair("authkey",authkey),
                new NameValuePair("real_send","yes"),
        });

        method.setRequestHeader("client-session", sessionId);
        client.executeMethod(method);

        String result = new String(method.getResponseBodyAsString());
        System.out.println(result);
    }

    public void contacts() throws IOException {
        String  url = requesturl + "/api/contacts";

        HttpClient client = new HttpClient();
        GetMethod method = new GetMethod(url);
        method.setRequestHeader("client-session", sessionId);
        client.executeMethod(method);

        String result = new String(method.getResponseBodyAsString());
        System.out.println(result);
    }


    public void addConversation() throws IOException {
        String  url = requesturl + "/api/user/conversation/add";

        HttpClient client = new HttpClient();
        PostMethod post = new PostMethod(url);

        NameValuePair[] postData = new NameValuePair[2];
        postData[0] = new NameValuePair("entity_id", "463365f0ab4a11e39ae763fbf5d6fe5b");
        postData[1] = new NameValuePair("type", "4"); // 1:grey 2:black 4:top

        post.setRequestHeader("client-session", sessionId);
        post.setRequestBody(postData);

        client.executeMethod(post);

        String result = new String(post.getResponseBodyAsString());
        System.out.println(result);
    }


    public void removeConversation() throws IOException {
        String  url = requesturl + "/api/user/conversation/remove";

        HttpClient client = new HttpClient();
        PostMethod post = new PostMethod(url);

        NameValuePair[] postData = new NameValuePair[2];
        postData[0] = new NameValuePair("entity_id", "2f740ed0abf111e3a91b63fbf5d6fe5b");
        postData[1] = new NameValuePair("type", "4"); // 1:grey 2:black 4:top

        post.setRequestHeader("client-session", sessionId);
        post.setRequestBody(postData);

        client.executeMethod(post);

        String result = new String(post.getResponseBodyAsString());
        System.out.println(result);
    }


    public void addContact() throws IOException {
        String  url = requesturl + "/api/contact/add";

        HttpClient client = new HttpClient();
        PostMethod post = new PostMethod(url);
        String key ="18013301805";
        NameValuePair[] postData = new NameValuePair[2];
        postData[0] = new NameValuePair("key", key);
        postData[1] = new NameValuePair("app_id", "0");

        post.setRequestHeader("client-session", sessionId);
        post.setRequestBody(postData);

        client.executeMethod(post);

        String result = new String(post.getResponseBodyAsString());
        System.out.println(result);
    }

    public void test() throws IOException {
        String mobile = "008618013301800";
        System.out.println(mobile.substring(0,4));
        System.out.println(mobile.substring(4));
    }

    public void delContact() throws IOException {
        String  url = requesturl + "/api/contact/del";

        HttpClient client = new HttpClient();
        PostMethod post = new PostMethod(url);
        String name ="00dfb1c0a8d411e39d7163fbf5d6fe5b";
        NameValuePair[] postData = new NameValuePair[2];
        postData[0] = new NameValuePair("name", name);
        postData[1] = new NameValuePair("app_id", "0");

        post.setRequestHeader("client-session", sessionId);
        post.setRequestBody(postData);

        client.executeMethod(post);

        String result = new String(post.getResponseBodyAsString());
        System.out.println(result);
    }


    public void disturb() throws IOException {
        String  url = requesturl + "/api/user/disturb";

        HttpClient client = new HttpClient();
        PostMethod post = new PostMethod(url);

        NameValuePair[] postData = new NameValuePair[2];
        postData[0] = new NameValuePair("disable", "yes");
        postData[1] = new NameValuePair("time", "10:00-02:20");

        post.setRequestHeader("client-session", sessionId);
        post.setRequestBody(postData);

        client.executeMethod(post);

        String result = new String(post.getResponseBodyAsString());
        System.out.println(result);
    }

    public void uploadContacts() throws IOException {
        String  url = requesturl + "/api/contacts/upload";

        HttpClient client = new HttpClient();
        PostMethod post = new PostMethod(url);

        NameValuePair[] postData = new NameValuePair[2];
        postData[0] = new NameValuePair("phone", "13337723775,18013301850");
        postData[1] = new NameValuePair("app_id", "0");

        post.setRequestHeader("client-session", sessionId);
        post.setRequestBody(postData);

        client.executeMethod(post);

        String result = new String(post.getResponseBodyAsString());
        System.out.println(result);
    }



    public void unread() throws IOException {
        String  url = requesturl + "/api/user/message/unread";

        HttpClient client = new HttpClient();
        PostMethod post = new PostMethod(url);

        NameValuePair[] postData = new NameValuePair[1];
        postData[0] = new NameValuePair("unread", "10");
//        postData[1] = new NameValuePair("time", "10:00-02:20");

        post.setRequestHeader("client-session", sessionId);
        post.setRequestBody(postData);

        client.executeMethod(post);

        String result = new String(post.getResponseBodyAsString());
        System.out.println(result);
    }

    public void fileResume() throws Exception {
        ConcurrentHashMap map;
        //final String filePath = "d:/v2.mp4";                                                                                                    1
        final String filePath = "d:/v3.txt";
        String fileId = "00000001-05c6-40c7-886e-cd343ccbea32";
        final long size = (new File(filePath)).length();

        HttpClient client = new HttpClient();
        PostMethod post = new PostMethod("http://localhost:8080/api/file/resume");

        post.setRequestHeader("client-session", sessionId);
        post.setRequestHeader("fileId", fileId);
        post.setRequestHeader("totalLen", size + "");
        post.setRequestEntity(new RequestEntity() {
            @Override
            public boolean isRepeatable() {
                return false;
            }

            @Override
            public void writeRequest(OutputStream out) throws IOException {
                RandomAccessFile file = new RandomAccessFile(filePath, "r");
                //byte[] buf = new byte[(int)(size/2)];
                //file.seek((int)(size/2));
                byte[] buf = new byte[(int)size];
                file.read(buf);
                IOUtils.write(buf, out);
                /*try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }*/
                //throw new IOException("");
            }

            @Override
            public long getContentLength() {
                return size;
            }

            @Override
            public String getContentType() {
                return null;
            }
        });

        client.executeMethod(post);

        System.out.println(new String(post.getResponseBody()));
    }

    public void getFileSize() throws Exception {
        //final String filePath = "d:/v2.mp4";
        String filePath = "d:/v3.txt";
        String fileId = "00000009-05c6-40c7-886e-cd343ccbea32";
        final long size = (new File(filePath)).length();

        HttpClient client = new HttpClient();
        GetMethod get = new GetMethod("http://localhost:8080/api/file/size");

        get.setRequestHeader("client-session", sessionId);
        get.setRequestHeader("fileId", fileId);
        get.setRequestHeader("totalLen", size + "");

        client.executeMethod(get);
        System.out.println(new String(get.getResponseBody()));
        System.out.println(get.getResponseHeader("Content-Size"));
        System.out.println("temp file size is " + new File("E:\\opt\\faceshow\\data\\resume_tmp\\" + fileId).length());
    }

    public void downloadFile() throws Exception {
        HttpClient client = new HttpClient();
        String fileId = "00000001-05c6-40c7-886e-cd343ccbea32";
        //String fileId = "56ba440e-201b-493b-9e74-f2cffa117846";
        GetMethod get = new GetMethod("http://localhost:8080/api/file/download/" + fileId);

        get.setRequestHeader("client-session", sessionId);

        client.executeMethod(get);
        System.out.println(new String(get.getResponseBody()));
    }

    public static void multipartsUpload() throws Exception {
        String existingBucketName = "handwin1";
        String filePath = "d:/v3.txt";
        String keyName = "00000000c-d507-4d2f-8e8f-c8713dd01854";
        AmazonS3 s3Client = new AmazonS3Client(new PropertiesCredentials(UserControllerTest.class.getResourceAsStream( "/AwsCredentials.properties")));

        // Set part size to 5 MB.
        long partSize = 5 * 1024;

        TransferManager tm = new TransferManager(s3Client);
        TransferManagerConfiguration tmc = new TransferManagerConfiguration();
        tmc.setMinimumUploadPartSize(partSize);
        tm.setConfiguration(tmc);

        // Create a list of UploadPartResponse objects. You get one of these for
        // each part upload.
        List<PartETag> partETags = new ArrayList<PartETag>();
        // Step 1: Initialize.
        InitiateMultipartUploadRequest initRequest = new InitiateMultipartUploadRequest( existingBucketName, keyName);
        InitiateMultipartUploadResult initResponse = s3Client.initiateMultipartUpload(initRequest);
        System.out.println(initResponse.getUploadId());

        File file = new File(filePath);
        long contentLength = file.length();

        try {
            // Step 2: Upload parts.
            long filePosition = 0;
            for (int i = 1; filePosition < contentLength; i++) {
                // Last part can be less than 5 MB. Adjust part size.
                partSize = Math.min(partSize, (contentLength - filePosition));
                // Create request to upload a part.
                UploadPartRequest uploadRequest = new UploadPartRequest().withBucketName(existingBucketName).withKey(keyName)
                        .withUploadId(initResponse.getUploadId()).withPartNumber(i).withFileOffset(filePosition)
                        .withFile(file).withPartSize(partSize);
                // Upload part and add response to our list.
                PartETag tag = s3Client.uploadPart(uploadRequest).getPartETag();
                partETags.add(tag);
                System.out.println(tag.getPartNumber() + ":" + tag.getETag());
                filePosition += partSize;
            }
            // Step 3: complete.
            CompleteMultipartUploadRequest compRequest = new CompleteMultipartUploadRequest(existingBucketName, keyName, initResponse.getUploadId(), partETags);

            s3Client.completeMultipartUpload(compRequest);
            System.out.println("complete!");
        } catch (Exception e) {
            e.printStackTrace();
            s3Client.abortMultipartUpload(new AbortMultipartUploadRequest(existingBucketName, keyName, initResponse.getUploadId()));
            System.out.println("abort!");
        }
    }

    public static void main(String[] args) throws Exception {

        System.out.println("/api/file/resume".endsWith("/api/file/resume"));
    }

}
