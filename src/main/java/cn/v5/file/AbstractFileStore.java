package cn.v5.file;


import cn.v5.metric.FileStoreMetric;
import cn.v5.util.LoggerFactory;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;

import javax.inject.Inject;
import java.io.InputStream;
import java.util.*;

/**
 * User: qgan(qgan@v5.cn)
 * Date: 14-5-8 上午11:19
 */
public abstract class AbstractFileStore implements FileStoreInterface, InitializingBean {
    private static final Logger log = LoggerFactory.getLogger(AbstractFileStore.class);

    protected DateTimeFormatter df = DateTimeFormat.forPattern("EEE, dd MMM yyyy HH:mm:ss' GMT'").withLocale(Locale.ENGLISH);

    @Inject
    protected Properties conTypeMap;

    protected final String DEFAULT_BUCKET_NAME = "handwin1";
    protected final String RANGE_PREFIX = "bytes=";
    protected final int RANGE_FRAGMENT = '-';

    protected String bucketName = DEFAULT_BUCKET_NAME;
    protected Map<String, String> bucketMap = new HashMap<>();

    @Value("${expireTime}")
    protected int expireTime;

    @Value("${uploadType.Avatar}")
    protected String avatarType;

    @Value("${uploadType.Feedback}")
    protected String feedbackType;

    @Value("${uploadType.Default}")
    protected String defaultType;

    @Value("${bucket.avatar}")
    private String avatarBucket;

    @Value("${bucket.feedback}")
    private String feedbackBucket;

    @Value("${bucket.default}")
    private String defaultBucket;

    @Value("${cdn.url}")
    private String cdnUrl;

    @Value("${base.url}")
    private String defaultBaseUrl;

    private FileStoreMetric fileStoreMetric;

    @Override
    public void afterPropertiesSet() throws Exception {
        bucketMap.put("avatar", avatarBucket);
        bucketMap.put("feedback", feedbackBucket);
        bucketMap.put("default", defaultBucket);
        this.afterPropertiesSetForSubClass();
    }

    protected abstract void afterPropertiesSetForSubClass() throws Exception;

    public void setBucketName(String bucketName) {
        this.bucketName = bucketName;
    }

    /**
     * 将文件保存到云端
     *
     * @param key
     * @param inputStream
     * @param fileName
     * @param size
     * @return 文件md5值
     */
    protected abstract String storeFileToCloud(String key, InputStream inputStream, String fileName, long size) throws Exception;

    protected abstract String storeFileToCloud(String key, InputStream inputStream, String fileName, long size, boolean isPub, String bucket) throws Exception;


    protected abstract FileInfo createFileInfo(String key);

    protected abstract FileInfo createFileInfo(String key, String bucket, Boolean isPub);

    @Override
    public String genUri(String key, String type) {
        String baseUrl, patch;
        if (avatarType.equals(type) || feedbackType.equals(type)) {
            baseUrl = cdnUrl;
            patch = type;
        } else {
            baseUrl = defaultBaseUrl;
            patch = "download";
        }
        return String.format("%s/api/file/%s/%s", baseUrl, patch, key);
    }

    /**
     * @param inputStream 文件输入流
     * @param fileName    文件名称
     * @return 返回存储的文件的信息，包括何种服务存储的，空间名称，唯一标示等等
     */
    @Override
    public FileInfo storeFile(InputStream inputStream, String fileName, long size)
            throws Exception {
        String key = UUID.randomUUID().toString();
        FileInfo info = createFileInfo(key);
        String md5 = storeFileToCloud(key, inputStream, fileName, size);
        info.setMd5(md5);
        return info;
    }

    @Override
    public FileInfo storeFile(InputStream inputStream, String fileName, long size, boolean isPub, String type)
            throws Exception {
        String key = UUID.randomUUID().toString();
        String bucket = bucketMap.get(type);
        if (bucket == null) {
            bucket = bucketMap.get("default");
        }
        FileInfo info = createFileInfo(key, bucket, isPub);

        com.codahale.metrics.Timer.Context context = null;
        try {
            context = beforeStoreFile();
            String md5 = storeFileToCloud(key, inputStream, fileName, size, isPub, bucket);
            info.setMd5(md5);
        } catch (Exception e) {
            fileStoreError();
            throw e;
        } finally {
            afterFileStore(context);
        }

        return info;
    }

    @Override
    public FileInfo storeAvatar(InputStream inputStream, String fileName, long size) throws Exception {
        return storeFile(inputStream, fileName, size, true, avatarType);
    }

    public Properties getConTypeMap() {
        return conTypeMap;
    }

    public void setConTypeMap(Properties conTypeMap) {
        this.conTypeMap = conTypeMap;
    }

    public void setFileStoreMetric(FileStoreMetric fileStoreMetric) {
        this.fileStoreMetric = fileStoreMetric;
    }

    private com.codahale.metrics.Timer.Context beforeStoreFile() {
        com.codahale.metrics.Timer.Context result = null;
        if (null != fileStoreMetric && null != fileStoreMetric.getReqTimer()) {
            result = fileStoreMetric.getReqTimer().time();
        }
        return result;
    }

    private void fileStoreError() {
        if (null != fileStoreMetric && null != fileStoreMetric.getErrorMeter()) {
            fileStoreMetric.getErrorMeter().mark();
        }
    }

    private void afterFileStore(com.codahale.metrics.Timer.Context context) {
        if (null != context) {
            context.close();
        }
    }
}
