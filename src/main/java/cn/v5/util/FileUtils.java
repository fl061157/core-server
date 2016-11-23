package cn.v5.util;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import cn.v5.util.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;

/**
 * User: qgan(qgan@v5.cn)
 * Date: 14-5-8 上午9:12
 */
@Service
public class FileUtils implements InitializingBean {
    private static final Logger log = LoggerFactory.getLogger(FileUtils.class);
    public static File TMP_DIR;

    public static long ERR_SIZE = -1;

    @Value("${core.server.tmp.dir}")
    private String tmpDir;

    /**
     * Invoked by a BeanFactory after it has set all bean properties supplied
     * (and satisfied BeanFactoryAware and ApplicationContextAware).
     * <p>This method allows the bean instance to perform initialization only
     * possible when all bean properties have been set and to throw an
     * exception in the event of misconfiguration.
     *
     * @throws Exception in the event of misconfiguration (such
     *                   as failure to set an essential property) or if initialization fails.
     */
    @Override
    public void afterPropertiesSet() throws Exception {
       init();
    }

    public  void init() {
        try {
            String path = tmpDir;
            TMP_DIR = new File(path);
            if (!TMP_DIR.exists())
                TMP_DIR.mkdir();
        } catch (Exception e) {
            log.error(String.format("fails to create temp dir.%s",tmpDir),e);
        }
    }

    /**
     * 保存输入流到临时文件，返回保存成功的字节大小，返回-1表示文件没有创建成功
     *
     * @param inputStream
     * @param tmpFileName
     * @return
     */
    public long savaTmpFile(InputStream inputStream, String tmpFileName) {
        OutputStream out = null;
        File tmpFile = null;
        try {
            tmpFile = new File(TMP_DIR.getAbsolutePath() + File.separator + tmpFileName);
            boolean success = false;
            try {
                success = tmpFile.createNewFile();
            } catch (IOException e) {
                log.error(e.getMessage(), e);
            }
            if (!success) {
                log.warn("create tmp file error!");
                return ERR_SIZE;
            }
            out = new FileOutputStream(tmpFile);
            IOUtils.copy(inputStream, out);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        } finally {
            IOUtils.closeQuietly(inputStream);
            IOUtils.closeQuietly(out);
        }
        return null != tmpFile?tmpFile.length():-1;
    }

    public InputStream getTmpInputStream(String tmpFileName) throws FileNotFoundException {
        File tmpFile = new File(TMP_DIR.getAbsolutePath() + File.separator + tmpFileName);
        return new FileInputStream(tmpFile);
    }

    public File getTmpFile(String tmpFileName) {
        return new File(TMP_DIR.getAbsolutePath() + File.separator + tmpFileName);
    }

    public void delTmpFile(String tmpFileName) {
        boolean res = org.apache.commons.io.FileUtils.deleteQuietly(new File(TMP_DIR.getAbsolutePath() + File.separator + tmpFileName));
        log.debug("delete temp file {} res is {}", tmpFileName, res);
    }

    public long append(String src, String dst) throws IOException {
        File dstFile = new File(TMP_DIR.getAbsolutePath() + File.separator + dst);
        File srcFile = new File(TMP_DIR.getAbsolutePath() + File.separator + src);

        OutputStream out = null;
        try {
            out = new FileOutputStream(dstFile, true);
            return org.apache.commons.io.FileUtils.copyFile(srcFile, out);
        } catch (IOException e) {
            throw e;
        } finally {
            IOUtils.closeQuietly(out);
        }
    }

    public void write(InputStream input, OutputStream os, Long range) throws IOException {
        byte[] b = new byte[2048];
        try {
            if (range != null && range != 0)
                input.skip(range);
            while (true) {
                int read = input.read(b);
                if (read == -1) {
                    break;
                } else {
                    os.write(b, 0, read);
                }
            }

            os.flush();
        } catch (IOException e) {
            throw e;
        } finally {
            IOUtils.closeQuietly(os);
            IOUtils.closeQuietly(input);
        }
    }


}
