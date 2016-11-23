package cn.v5.service;

import cn.v5.entity.CurrentUser;
import cn.v5.entity.User;
import cn.v5.file.FileInfo;
import cn.v5.file.FileStoreInterface;
import cn.v5.localentity.CrashLog;
import cn.v5.localentity.PersistentFile;
import cn.v5.util.UploadUtil;
import com.alibaba.fastjson.JSON;
import com.amazonaws.util.json.JSONObject;
import info.archinnov.achilles.persistence.PersistenceManager;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 以后只保留storeAvatarToFileServer方法，其他都去掉。文件功能全部移到文件服务中。
 */
@Service
public class FileStoreService {
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Value("${uploadType.Avatar}")
    private String avatarType;
    @Value("${uploadType.Feedback}")
    private String feedbackType;
    @Value("${uploadType.Default}")
    private String defaultType;

    @Value("${cn.fileserver.url}")
    private String cnFileServerUrl;

    @Value("${us.fileserver.url}")
    private String usFileServerUrl;

    @Value("${auth.key}")
    private String authKey;

    @Inject
    private FileStoreInterface fileStoreInterface;
    @Autowired
    @Qualifier("localManager")
    private PersistenceManager manager;

    @Autowired
    private FileMD5Service fileMD5Service;

    /**
     * @param inputStream
     * @return 返回文件存储的位置 amazon s3 的位置
     * @throws Exception
     * @author xiechanglei
     * @date 2014-3-10 上午10:43:55
     */
    @Deprecated
    public FileInfo storeFile(InputStream inputStream, String fileName, long size) throws Exception {
        FileInfo info = fileStoreInterface.storeFile(inputStream, fileName, size);
        storePersistentFile(info);
        return info;
    }

    @Deprecated
    public FileInfo storeFile(InputStream inputStream, String fileName, long size, String type) throws Exception {
        FileInfo info;
        boolean isPub = type.equals(avatarType) || type.equals(feedbackType);
        info = fileStoreInterface.storeFile(inputStream, fileName, size, isPub, type);
        storePersistentFile(info);
        return info;
    }

    @Deprecated
    public String genUri(String key, String type) {
        return fileStoreInterface.genUri(key, type);
    }

    @Deprecated
    public FileInfo storeAvatar(InputStream inputStream, String fileName, long size) throws Exception {
        FileInfo info = fileStoreInterface.storeAvatar(inputStream, fileName, size);
        return info; // 头像单独处理存储本地 另行调方法
        // return storePersistentFile(info);//暂时没有安全机制，直接放回url，通过url，任何人都是可以访问的
    }

    @Deprecated
    public void storeAvatarPersistentFile(FileInfo info, String userId) {
        PersistentFile file = new PersistentFile();
        file.setId(info.getAccessKey());
        file.setDate(sdf.format(new Date()));
        file.setBucketname(info.getBucketName());
        file.setRegion(info.getRegion());
        file.setService(info.getStoreService());
        file.setExt(info.getExt());
        file.setCreator(userId);
        manager.insert(file);
    }

    @Deprecated
    private String storePersistentFile(FileInfo info) {
        PersistentFile file = new PersistentFile();
        file.setId(info.getAccessKey());
        file.setDate(sdf.format(new Date()));
        file.setBucketname(info.getBucketName());
        file.setRegion(info.getRegion());
        file.setService(info.getStoreService());
        file.setExt(info.getExt());
        User user = CurrentUser.user();
        file.setCreator(user.getId());
        manager.insert(file);
        return info.getAccessKey();
    }

    @Deprecated
    public void fetchFile(HttpServletResponse res, String fileId, String range, String modified, String eTag, boolean isHead) throws Exception {
        PersistentFile find = manager.find(PersistentFile.class, fileId.indexOf("!") == -1 ? fileId : fileId.split("!")[0]);
        if (find == null) {
            PersistentFile file = new PersistentFile();
            file.setPub(false);
            file.setBucketname("handwin1");
            fileStoreInterface.fetchFile(res, fileId, file, range, modified, eTag, isHead);
        } else {
            fileStoreInterface.fetchFile(res, fileId, find, range, modified, eTag, isHead);
        }
    }

    @Deprecated
    public FileStoreInterface getFileStoreInterface() {
        return fileStoreInterface;
    }

    @Deprecated
    public void setFileStoreInterface(FileStoreInterface fileStoreInterface) {
        this.fileStoreInterface = fileStoreInterface;
    }

    @Deprecated
    public PersistenceManager getManager() {
        return manager;
    }

    @Deprecated
    public void setManager(PersistenceManager manager) {
        this.manager = manager;
    }

    @Deprecated
    public String storeCrashLog(InputStream inputStream, String fileName, long size, String os, String device) throws Exception {
        FileInfo info = fileStoreInterface.storeFile(inputStream, fileName, size);
        CrashLog log = new CrashLog();
        log.setDate(sdf.format(new Date()));
        log.setDevice(device);
        log.setFileId(info.getAccessKey());
        log.setOs(os);
        manager.insert(log);
        return storePersistentFile(info);
    }

    // 经过文件服务器保存头像图片
    public String storeAvatarToFileServer(MultipartFile multipartFile, String userId, String regionCode) throws Exception {
        String fileServerBasicUrl = null;
        if (regionCode.equals("0086")) {
            fileServerBasicUrl = cnFileServerUrl;
        } else {
            fileServerBasicUrl = usFileServerUrl;
        }
        String url = String.format("%s/api/user/avatar/upload", fileServerBasicUrl);

        Map<String, String> headers = new HashMap<>();
        headers.put("region-code", regionCode);

        Map<String, String> params = new HashMap<>();
        if (userId != null) {
            params.put("userId", userId);
        }
        String randomId = UUID.randomUUID().toString();
        params.put("randomId", randomId);
        params.put("auth", DigestUtils.md5DigestAsHex(String.format("%s%s", randomId, authKey).getBytes()));
        String result = UploadUtil.upload(url, multipartFile.getInputStream(), multipartFile.getName(), headers, params);
        if (!result.isEmpty()) {
            JSONObject dataJson = new JSONObject(result);
            return (String) dataJson.get("file_url");
        } else {
            throw new Exception("fileserver result is empty");
        }
    }

    public String storeHeadImgToFileServer(File file, String userID, String regionCode) throws Exception {

        String fileServerURL = (regionCode != null && regionCode.equals("0086")) ? cnFileServerUrl : usFileServerUrl;
        String url = String.format("%s/api/user/avatar/upload", fileServerURL);
        Map<String, String> headers = new HashMap<>();
        headers.put("region-code", regionCode);

        Map<String, String> params = new HashMap<>();
        if (StringUtils.isNoneBlank(userID)) {
            params.put("userId", userID);
        }
        String randomId = UUID.randomUUID().toString();
        params.put("randomId", randomId);
        params.put("auth", DigestUtils.md5DigestAsHex(String.format("%s%s", randomId, authKey).getBytes()));
        FileInputStream inputStream = new FileInputStream(file);
        String result;
        try {
            result = UploadUtil.upload(url, inputStream, file.getName(), headers, params);
        } catch (IOException e) {
            throw e;
        }
        if (StringUtils.isBlank(result)) {
            throw new Exception("fileServer result is empty !");
        }
        Map<String, String> rMap = JSON.parseObject(result, Map.class); //TODO Exception
        return rMap.get("file_url");
    }


}
