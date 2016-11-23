package cn.v5.file.amazon;

import cn.v5.file.AbstractFileStore;
import cn.v5.file.FileInfo;
import cn.v5.localentity.PersistentFile;
import cn.v5.util.LoggerFactory;
import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.ClasspathPropertiesFileCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.*;
import com.google.common.collect.Lists;
import net.coobird.thumbnailator.Thumbnails;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.sanselan.Sanselan;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StreamUtils;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

public class AmazomS3FileStoreImpl extends AbstractFileStore {
    private static final Logger log = LoggerFactory.getLogger(AmazomS3FileStoreImpl.class);
    private AmazonS3 amazon;

    @Value("${s3.region}")
    private String regionStr;

    @Override
    protected void afterPropertiesSetForSubClass() throws Exception {
        init();
    }

    private ClientConfiguration configuration;

    private void init() {

        //amazon.setRegion(Region.getRegion(Regions.US_WEST_1));
        ClasspathPropertiesFileCredentialsProvider classpathPropertiesFileCredentialsProvider = null;
        if (regionStr.equals("CN_NORTH_1")) {
            classpathPropertiesFileCredentialsProvider = new ClasspathPropertiesFileCredentialsProvider("AwsCnCredentials.properties");
        } else {
            classpathPropertiesFileCredentialsProvider = new ClasspathPropertiesFileCredentialsProvider();
        }

        if (null != configuration) {
            amazon = new AmazonS3Client(classpathPropertiesFileCredentialsProvider, configuration);
        } else {
            amazon = new AmazonS3Client(classpathPropertiesFileCredentialsProvider);
        }

        try {
            setRegion(amazon, regionStr);
        } catch (Exception e) {
            throw new RuntimeException("nknow amazon datacenter:" + regionStr);
        }
    }

    @Override
    protected FileInfo createFileInfo(String key) {
        FileInfo info = new FileInfo();
        info.setAccessKey(key);
        info.setBucketName(bucketName);
        //info.setRegion("US_WEST_1");
        info.setRegion(regionStr);
        info.setStoreService("amazon s3");

        return info;
    }

    @Override
    protected FileInfo createFileInfo(String key, String bucket, Boolean isPub) {
        FileInfo info = new FileInfo();
        info.setAccessKey(key);
        info.setBucketName(bucket);
        //info.setRegion("US_WEST_1");
        info.setRegion(regionStr);
        info.setPub(isPub);
        info.setStoreService("amazon s3");
        return info;
    }

    @Override
    public String storeFileToCloud(String key, InputStream inputStream, String fileName, long size) throws Exception {
        return this.storeFileToCloud(key, inputStream, fileName, size, false, bucketName);
    }

    @Override
    public String storeFileToCloud(String key, InputStream inputStream, String fileName, long size, boolean isPub, String bucket)
            throws Exception {
        log.debug("store pub {} file {} to s3 bucket {}", isPub, key, bucket);
        try {
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentEncoding("UTF-8");
            metadata.setContentDisposition(fileName);
            metadata.setContentLength(size);
            Date date = new Date();
            metadata.setLastModified(date);
            int index = fileName.lastIndexOf(".");
            String ext = index > -1 ? fileName.substring(index) : "";
            String contentType = conTypeMap.get(ext) == null ? null : conTypeMap.get(ext).toString();
            if (!StringUtils.isBlank(contentType)) {
                metadata.setContentType(contentType);
            } else {
                metadata.setContentType("application/octet-stream; charset=UTF-8");
            }
            metadata.setExpirationTime(DateTime.now().plusMonths(expireTime).toDate());
            PutObjectRequest por = new PutObjectRequest(bucket, key, inputStream, metadata);
            //default非公开的,这里设为公开，私密文件使用storePubFileToCloud的url
            if (isPub) {
                por.setCannedAcl(CannedAccessControlList.PublicRead);
            }
            PutObjectResult result = amazon.putObject(por);
            // 返回文件的MD5值。
            return result.getETag();
        } catch (AmazonServiceException e) {
            log.error("Amazon Service Exception with Key" + key, e);
            throw e;
        } catch (AmazonClientException e) {
            log.error("Amazon Client Exception with Key" + key, e);
            throw e;
        } finally {
            IOUtils.closeQuietly(inputStream);
        }
    }


    @Override
    public long getStoreSize(String key) throws Exception {
        long size = 0;
        try {
            ObjectMetadata metadata = amazon.getObjectMetadata(bucketName, key);
            size = metadata.getContentLength();
        } catch (AmazonServiceException e) {
            if (e.getStatusCode() != 404)  // 404没找到返回0
                throw e;
        }
        return size;
    }

    @Override
    public long getStoreSize(String key, String bucket) throws Exception {
        long size = 0;
        try {
            ObjectMetadata metadata = amazon.getObjectMetadata(bucket, key);
            size = metadata.getContentLength();
        } catch (AmazonServiceException e) {
            if (e.getStatusCode() != 404)  // 404没找到返回0
                throw e;
        }
        return size;
    }


    @Override
    public void fetchFile(HttpServletResponse res, String fileId, PersistentFile fileinfo, String range, String modified, String eTag, boolean isHead) throws Exception {
        log.debug("aws readFile here start...");
        S3Object object = null;
        try {
            String bucket = fileinfo.getBucketname();
            GetObjectRequest request = new GetObjectRequest(bucket, fileId);
            Long start = 0l;
            Long end = getStoreSize(fileId, bucket) - 1l;
            String startStr;
            if (!StringUtils.isBlank(range) && range.startsWith(RANGE_PREFIX)) {
                int minus = range.indexOf(RANGE_FRAGMENT);
                if (minus > -1) {
                    startStr = range.substring(RANGE_PREFIX.length(), minus);
                    try {
                        start = Long.parseLong(startStr);
                        end = range.length() == minus + 1 ? getStoreSize(fileId, bucket) - 1 : Long.parseLong(range.substring(minus + 1));
                    } catch (NumberFormatException ignored) {
                        log.error(ignored.toString());
                        throw ignored;
                    }
                }
            }
            request.setRange(start, end);
            if (!StringUtils.isBlank(modified)) {
                request.setModifiedSinceConstraint(df.parseDateTime(modified).toDate());
            }
            if (!StringUtils.isBlank(eTag)) {
                request.setNonmatchingETagConstraints(Lists.newArrayList(eTag));
            }
            try {
                object = amazon.getObject(request);
            } catch (Exception e) {
                if (fileId.split("!").length == 1) {
                    log.error(String.format("File Not Found!with bucket: %s; fileId: %s", bucket, fileId), e);
                    res.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    return;
                } else {
                    Boolean isPub = fileinfo.getPub();
                    if (isPub == null) {
                        isPub = false;
                    }
                    try {
                        object = thumbImage(request, isPub);
                    } catch (Exception e1) {
                        log.error("thumbImage Exception,key" + request.getKey(), e1);
                        throw e1;
                    }
                }
            }
            if (object == null) {
                //Not Modified
                res.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
                return;
            }
            Date expiresDate = object.getObjectMetadata().getExpirationTime();
            if (expiresDate != null) {
                res.setDateHeader("Expires", expiresDate.getTime());
            } else {
                res.setDateHeader("Expires", DateTime.now().plusMonths(expireTime).getMillis());
            }
            res.setHeader("Content-Type", object.getObjectMetadata().getContentType());
            Date modifiedDate = object.getObjectMetadata().getLastModified();
            if (modifiedDate != null) {
                res.setDateHeader("Last-Modified", modifiedDate.getTime());
            }
            res.setHeader("ETag", object.getObjectMetadata().getETag());
            if (StringUtils.isBlank(range)) {
                res.addHeader("Content-Length", String.valueOf(object.getObjectMetadata().getContentLength()));
            } else {
                res.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);
                res.addHeader("Content-Length", String.valueOf(end - start + 1));
                res.addHeader("Content-Range", "bytes " + String.valueOf(start) + "-" + String.valueOf(end) + "/" + object.getObjectMetadata().getInstanceLength());
            }
            res.setHeader("Accept-Ranges", "bytes");
            res.setHeader("Cache-Control", "max-age=2592000");
            if (!isHead) {
                StreamUtils.copy(object.getObjectContent(), res.getOutputStream());
            }
        } catch (Exception e) {
            log.error(String.format("fails to fetch file.fileId:%s", fileId), e);
            throw e;
        } finally {
            if (object != null) {
                object.close();
            }
        }
    }

    private S3Object thumbImage(GetObjectRequest request, Boolean isPub)
            throws Exception {
        S3Object object = null;
        String fileId = request.getKey();
        String buckname = request.getBucketName();
        int max = 0;
        try {
            max = Integer.parseInt(fileId.split("!")[1]);
        } catch (Exception e1) {
            throw new Exception("非法的图片大小");
        }
        String src = fileId.split("!")[0];
        BufferedImage read = null;
        try {
            request.setKey(src);
            object = amazon.getObject(request);
            read = ImageIO.read(object.getObjectContent());
        } catch (IOException e) {
            log.debug("fail to shrink image , change image tool. img key:" + request.getKey(), e);
            S3Object object1 = amazon.getObject(request);
            try{
                read = Sanselan.getBufferedImage(object1.getObjectContent());
            } catch (IOException e1) {
                log.error("fail to shrink image ,return original image. img key:" + request.getKey(), e1);
                return amazon.getObject(request);
            }finally {
                if (null != object1) {
                    object1.close();
                }
            }
        } catch (AmazonServiceException e) {
            log.error("aws serviceException", e);
            throw e;
        } catch (AmazonClientException e) {
            log.error("aws clientException", e);
            throw e;
        } finally {
            if (null != object) {
                object.close();
            }
        }
        if (read == null) {
            throw new Exception("该文件不是图片");
        } else {
            int source_width = read.getWidth();
            int source_height = read.getHeight();
            int target_width = 0;
            int target_height = 0;

            if (source_width > source_height) {
                target_width = max;
                target_height = (max * source_height) / source_width;
            } else {
                target_height = max;
                target_width = (max * source_width) / source_height;
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            Thumbnails.of(read).size(target_width, target_height).outputFormat("jpg").outputQuality(0.8f).toOutputStream(out);

            ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
            storeFileToCloud(fileId, in, object.getObjectMetadata().getContentDisposition(), in.available(), isPub, buckname);
            request.setKey(fileId);
            try {
                object = amazon.getObject(request);
            } catch (AmazonServiceException e) {
                log.error("Amazon Service Exception with Key" + request.getKey(), e);
                throw e;
            } catch (AmazonClientException e) {
                log.error("Amazon Client Exception with Key" + request.getKey(), e);
                throw e;
            }
        }
        return object;
    }

    /**
     * 配置amazon s3 所属的数据中心
     *
     * @param amazon2
     * @param region
     * @throws Exception 无法找到指定的数据中心
     * @author xiechanglei
     * @date 2014-3-11 下午2:42:03
     */
    @SuppressWarnings("unused")
    private void setRegion(AmazonS3 amazon2, String region) throws Exception {
        if ("GovCloud".equals(region)) {
            amazon.setRegion(Region.getRegion(Regions.US_WEST_1));
        } else if ("US_EAST_1".equals(region)) {
            amazon.setRegion(Region.getRegion(Regions.US_EAST_1));
        } else if ("US_WEST_1".equals(region)) {
            amazon.setRegion(Region.getRegion(Regions.US_WEST_1));
        } else if ("US_WEST_2".equals(region)) {
            amazon.setRegion(Region.getRegion(Regions.US_WEST_2));
        } else if ("EU_WEST_1".equals(region)) {
            amazon.setRegion(Region.getRegion(Regions.EU_WEST_1));
        } else if ("AP_SOUTHEAST_1".equals(region)) {
            amazon.setRegion(Region.getRegion(Regions.AP_SOUTHEAST_1));
        } else if ("AP_SOUTHEAST_2".equals(region)) {
            amazon.setRegion(Region.getRegion(Regions.AP_SOUTHEAST_2));
        } else if ("AP_NORTHEAST_1".equals(region)) {
            amazon.setRegion(Region.getRegion(Regions.AP_NORTHEAST_1));
        } else if ("SA_EAST_1".equals(region)) {
            amazon.setRegion(Region.getRegion(Regions.SA_EAST_1));
        } else if ("CN_NORTH_1".equals(region)) {
            amazon.setRegion(Region.getRegion(Regions.CN_NORTH_1));
        } else {
            throw new Exception("unknow amazon datacenter:" + region);
        }
    }

    public void setConfiguration(ClientConfiguration configuration) {
        this.configuration = configuration;
    }
}
