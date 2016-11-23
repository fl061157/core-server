package cn.v5.file;

import cn.v5.localentity.PersistentFile;

import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;

public interface FileStoreInterface {

	FileInfo storeFile(InputStream inputStream, String fileName, long size) throws Exception;
    FileInfo storeFile(InputStream inputStream, String fileName, long size, boolean isPub, String bucket) throws Exception;

    FileInfo storeAvatar(InputStream inputStream, String fileName, long size) throws Exception;
    /**
     * 存储公共可见的文件
     * @throws Exception
     */
    void fetchFile(HttpServletResponse res, String fileId, PersistentFile fileinfo, String range, String modified, String eTag, boolean isHead) throws Exception;

    /**
     * 获取已经存储的文件大小
     * @param key
     * @return 文件的字节数
     * @throws Exception
     */
    long getStoreSize(String key) throws Exception;
    long getStoreSize(String key, String bucket) throws Exception;

    String genUri(String key, String patchUrl);

}
