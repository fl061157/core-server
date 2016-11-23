package cn.v5.file.local;

import cn.v5.file.FileInfo;
import cn.v5.file.FileStoreInterface;
import cn.v5.localentity.PersistentFile;
import cn.v5.util.LoggerFactory;
import net.coobird.thumbnailator.Thumbnails;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;

import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;
import java.util.UUID;

/**
 * @author DecayParadise 文件存储的本地实现 将文件暂存在本地
 */
public class LocalFileStoreImpl implements FileStoreInterface {

    private static final Logger log = LoggerFactory.getLogger(LocalFileStoreImpl.class);

    @Value("${file.storage.path}")
    private String fileStorePath;

    @Value("${avatar.storage.path}")
    private String avatarStoragePath;

    /**
	 * 保存文件
	 */
	@Override
	public FileInfo storeFile(InputStream inputStream, String fileName,long size)
			throws Exception {

        log.debug("fileStorePath:"+fileStorePath);
        if(fileStorePath == null){
            fileStorePath = "/opt/faceshow/data/file/";
        }
		return persitFile(inputStream, fileName, fileStorePath);
	}

    @Override
    public FileInfo storeFile(InputStream inputStream, String fileName, long size, boolean isPub, String bucket) throws Exception {
        return null;
    }


    @Override
    public String genUri(String key, String patchUrl) {
        return null;
    }

    @Override
    public long getStoreSize(String key) throws Exception {
        throw new UnsupportedOperationException();
    }

    @Override
    public long getStoreSize(String key, String bucket) throws Exception {
        return 0;
    }

    @Override
    public void fetchFile(HttpServletResponse res, String fileId, PersistentFile fileinfo, String range, String modified, String eTag, boolean isHead) throws Exception {

    }


    private void transferTo(File file, long position, long count,
                                  WritableByteChannel target) throws IOException {
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(file);
            fis.getChannel().transferTo(position, count, target);
        } catch (IOException e) {
            throw e;
        } finally {
            IOUtils.closeQuietly(fis);
        }
    }

	@SuppressWarnings("resource")
	private void readFile(HttpServletResponse res, File obsertFile, FileInputStream input, ServletOutputStream os, Long range) throws Exception {
         long len = obsertFile.length() - range;
         os = res.getOutputStream();
         res.reset();
         res.setHeader("Content-Disposition", "attachment; filename="
                 + obsertFile.getName());
         res.setContentType("application/octet-stream; charset=UTF-8");
         res.setHeader("Content-Length", String.valueOf(len));

         transferTo(obsertFile, range, len, Channels.newChannel(os));
        /*try {
            input = new FileInputStream(obsertFile);

            byte[] b = new byte[4096];
            while (true) {
                int read = input.read(b);
                if (read == -1) {
                    break;
                } else {
                    os.write(b, 0, read);
                    os.flush();
                }
            }
            os.flush();
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        } finally {
            if (os != null) {
                os.close();
            }
            if (input != null) {
                input.close();
            }
        }*/
	}

	/*
	 * 保存头像
	 */
	@Override
	public FileInfo storeAvatar(InputStream inputStream, String fileName,long size)
			throws Exception {
		String fileStorePath = avatarStoragePath;
        log.debug("fileStorePath:"+fileStorePath);
        if(fileStorePath == null){
            fileStorePath = "/opt/faceshow/data/file/";
        }
		return persitFile(inputStream, fileName, fileStorePath);
	}



    private FileInfo persitFile(InputStream inputStream, String fileName,
			String fileStorePath) throws Exception {
		FileOutputStream fou = null;
		try {
			createBucket(fileStorePath);
            int lastIndex = fileName.lastIndexOf(".") ;
            String ext =  lastIndex != -1 ? fileName.substring( lastIndex ) : fileName ;
			String randomStr = createPackage(fileStorePath); // 创建文件夹
			File obsertFile = new File(fileStorePath + randomStr + File.separator + "default"+ext);// 保存文件
			if(!obsertFile.exists()){
                obsertFile.createNewFile();
            }

			fou = new FileOutputStream(obsertFile);
			byte[] temp = new byte[4096];
			while (true) {
				int read = inputStream.read(temp);
				if (read == -1) {
					break;
				} else {
					fou.write(temp, 0, read);
					fou.flush();
				}
			}
			FileInfo info = new FileInfo();
			info.setAccessKey(randomStr);
			info.setBucketName(fileStorePath);
			info.setStoreService("localfile");
            info.setExt(ext);

			return info;
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		} finally {
			if (inputStream != null) {
				inputStream.close();
			}
			if (fou != null) {
				fou.close();
			}
		}
	}

	private String createPackage(String fileStorePath) {
		String randomStr = UUID.randomUUID().toString();
		File packageFile = new File(fileStorePath + randomStr);
		packageFile.mkdir();
		return randomStr;
	}

	private void createBucket(String fileStorePath) {

		File path = new File(fileStorePath);
		if (!path.exists()) {
			path.mkdirs();
		}
	}


    private File thumbImage(String fileId, PersistentFile fileinfo)
            throws Exception {

        int max = 0;
        try {
            max = Integer.parseInt(fileId.split("!")[1]);
        } catch (Exception e1) {
            throw new Exception("非法的图片大小");
        }

        String buckname = fileinfo.getBucketname();
        String src = fileId.split("!")[0];


        String newfileName = buckname + src + File.separator + "default_" + max + fileinfo.getExt();
        log.debug("newfileName="+newfileName);

        File newfile = new File(newfileName);
        if(newfile.exists()){
            return newfile;
        }


        String fileName = buckname + src + File.separator + "default" + fileinfo.getExt();
        log.debug("fileName="+fileName);

        File file = new File(fileName);

        BufferedImage read = null;
        try {
            read = ImageIO.read(file);
        } catch (IOException e) {
            log.error("image format is invalid.", e);
            return file;
        }
        if(read==null){
            throw new Exception("该文件不是图片");
        }else{
            int source_width = read.getWidth();
            int source_height = read.getHeight();
            int target_width = 0;
            int target_height = 0;

            if(source_width>source_height){
                target_width = max;
                target_height = (max * source_height)/ source_width;
            }else{
                target_height = max;
                target_width = (max * source_width)/source_height;
            }

            Thumbnails.of(read).size(target_width, target_height).toFile(newfile);
            return newfile;
        }

    }


}
