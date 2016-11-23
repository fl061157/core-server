package cn.v5.file.aliyun;

import cn.v5.file.AbstractFileStore;
import cn.v5.file.FileInfo;
import cn.v5.localentity.PersistentFile;
import cn.v5.util.LoggerFactory;
import com.aliyun.openservices.ClientConfiguration;
import com.aliyun.openservices.ClientException;
import com.aliyun.openservices.oss.OSSClient;
import com.aliyun.openservices.oss.OSSException;
import com.aliyun.openservices.oss.model.GetObjectRequest;
import com.aliyun.openservices.oss.model.OSSObject;
import com.aliyun.openservices.oss.model.ObjectMetadata;
import com.aliyun.openservices.oss.model.PutObjectResult;
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

/**
 * User: qgan(qgan@v5.cn)
 * Date: 14-5-7 上午9:32
 */
public class AliyunOSSFileStoreImpl extends AbstractFileStore {

    private static final Logger log = LoggerFactory.getLogger(AliyunOSSFileStoreImpl.class);
    private static final String ERROR_CODE_NOT_FOUND = "NoSuchKey";

    @Value("${accessId}")
    private String access_id;

    @Value("${accessKey}")
    private String access_key;

    @Value("${accessEndpoint}")
    private String oss_endpoint;

    private OSSClient client;

    @Override
    protected void afterPropertiesSetForSubClass() throws Exception {
        ClientConfiguration config = new ClientConfiguration();
        client = new OSSClient(oss_endpoint, access_id, access_key, config);
    }

    @Override
    public long getStoreSize(String key) throws Exception {
        return getStoreSize(key, bucketName);
    }

    @Override
    public long getStoreSize(String key, String bucket) throws Exception {
        long size = 0;
        try {
            ObjectMetadata metadata = client.getObjectMetadata(bucket, key);
            size = metadata.getContentLength();
        } catch (OSSException e) {
            if (!e.getErrorCode().equals(ERROR_CODE_NOT_FOUND))
                throw e;
        }
        return size;
    }

    @Override
    public void fetchFile(HttpServletResponse res, String fileId, PersistentFile fileinfo, String range, String modified, String eTag, boolean isHead) throws Exception {
        log.debug("aliyun readFile here start...");
        OSSObject object = null;
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
                log.debug("set modified {},{}", modified, df.parseDateTime(modified).toDate());
                request.setModifiedSinceConstraint(df.parseDateTime(modified).toDate());
            }
            if (!StringUtils.isBlank(eTag)) {
                log.debug("set eTag {}", eTag);
                request.setNonmatchingETagConstraints(Lists.newArrayList(eTag));
            }
            try {
                object = client.getObject(request);
            } catch (Exception e) {
                if (fileId.split("!").length == 1) {
                    log.error(String.format("File Not Found!with bucket: %s; fileId: %s", bucket, fileId), e);
                    throw e;
                } else {
                    Boolean isPub = fileinfo.getPub();
                    if (isPub == null) {
                        isPub = false;
                    }
                    try {
                        object = thumbImage(request, isPub);
                    } catch (Exception e2) {
                        throw e2;
                    }
                }
            }
            if (object == null) {
                //Not Modified
                res.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
                return;
            }
//            Date expiresDate = object.getObjectMetadata().getExpirationTime();
//            if (expiresDate != null) {
//                log.debug("get expiresDate,{}",expiresDate);
//                res.setDateHeader("Expires", expiresDate.getTime());
//            }
            res.setDateHeader("Expires", DateTime.now().plusMonths(expireTime).getMillis());
            res.setHeader("Content-Type", object.getObjectMetadata().getContentType());
//            Date modifiedDate = object.getObjectMetadata().getLastModified();
//            if (modifiedDate != null) {
//                log.debug("get modifiedDate,{}",modifiedDate);
//                res.setDateHeader("Last-Modified", modifiedDate.getTime());
//            }
            res.setHeader("ETag", object.getObjectMetadata().getETag());
            if (StringUtils.isBlank(range)) {
                res.addHeader("Content-Length", String.valueOf(object.getObjectMetadata().getContentLength()));
            } else {
                res.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);
                res.addHeader("Content-Length", String.valueOf(end - start + 1));
                res.addHeader("Content-Range", "bytes " + String.valueOf(start) + "-" + String.valueOf(end) + "/" + String.valueOf(getStoreSize(fileId, bucket)));
            }
            if (!isHead) {
                StreamUtils.copy(object.getObjectContent(), res.getOutputStream());
            }
        } catch (Exception e) {
            log.error(String.format("fails to fetch file.fileId:%s", fileId), e);
            throw e;
        } finally {
            if (object != null && object.getObjectContent() != null) {
                object.getObjectContent().close();
            }
        }
    }

    /**
     * 返回文件MD5值。
     */
    @Override
    protected String storeFileToCloud(String key, InputStream inputStream, String fileName, long size) throws Exception {
        return storeFileToCloud(key, inputStream, fileName, size, true, bucketName);
    }

    /**
     * 返回文件MD5值。
     */
    @Override
    protected String storeFileToCloud(String key, InputStream inputStream, String fileName, long size, boolean isPub, String bucket) throws Exception {
        log.debug("store pub {} file {} to s3 bucket {}", isPub, key, bucket);
        try {
            fileName = fileName == null ? "" : fileName;
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
            PutObjectResult result = client.putObject(bucket, key, inputStream, metadata);
            // 返回文件的MD5值。
            return result.getETag();
        } catch (Exception e) {
            log.error("store file Exception with Key" + key, e);
            throw e;
        } finally {
            IOUtils.closeQuietly(inputStream);
        }
    }

    @Override
    protected FileInfo createFileInfo(String key) {
        FileInfo info = new FileInfo();
        info.setAccessKey(key);
        info.setBucketName(bucketName);
        info.setRegion("CN_EAST_1");
        info.setStoreService("aliyun oss");
        return info;
    }

    @Override
    protected FileInfo createFileInfo(String key, String bucket, Boolean isPub) {
        FileInfo info = new FileInfo();
        info.setAccessKey(key);
        info.setBucketName(bucket);
        info.setRegion("CN_EAST_1");
        info.setStoreService("aliyun oss");
        return info;
    }


    private OSSObject thumbImage(GetObjectRequest request, Boolean isPub)
            throws Exception {
        OSSObject object = null;
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
            object = client.getObject(request);
            read = ImageIO.read(object.getObjectContent());
        } catch (IOException e) {
            log.debug("fail to shrink image , change image tool. img key:" + request.getKey(), e);
            OSSObject object1 = client.getObject(request);
            try {
                read = Sanselan.getBufferedImage(object1.getObjectContent());
            } catch (IOException e1) {
                log.error("fail to shrink image ,return original image. img key:" + request.getKey(), e1);
                return client.getObject(request);
            }finally {
                if(object1.getObjectContent()!=null){
                    object1.getObjectContent().close();
                }
            }
        } catch (OSSException e) {
            log.error("oss exception with Key" + request.getKey(), e);
            throw e;
        } catch (ClientException e) {
            log.error("client exception", e);
            throw e;
        } finally {
            if (object != null && object.getObjectContent() != null) {
                object.getObjectContent().close();
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
                object = client.getObject(request);
            } catch (OSSException e) {
                log.error("oss exception with Key" + request.getKey(), e);
            } catch (ClientException e) {
                log.error("client exception with Key" + request.getKey(), e);
            }
        }
        return object;
    }
}
