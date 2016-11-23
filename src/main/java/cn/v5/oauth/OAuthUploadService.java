package cn.v5.oauth;

import cn.v5.service.FileStoreService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;

/**
 * Created by fangliang on 16/5/10.
 */

@Service
public class OAuthUploadService {

    @Autowired
    private FileStoreService fileStoreService;

    public String upload(String url, String oAuthUnioID, String regionCode, String suffix) throws Exception {
        String destFileName = String.format("%s.%s", oAuthUnioID, suffix);
        File file = FileLoader.load(url, destFileName);
        try {
            return fileStoreService.storeHeadImgToFileServer(file, StringUtils.EMPTY, regionCode);
        } finally {
            if (file != null) {
                try {
                    file.delete() ;
                } catch (Exception e) {
                }
            }
        }
    }


}
