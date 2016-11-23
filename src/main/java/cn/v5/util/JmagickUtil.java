package cn.v5.util;




import org.slf4j.Logger;
import cn.v5.util.LoggerFactory;

import java.util.Properties;

/**
 * Created by IntelliJ IDEA.
 * User: SUNCHJ
 * Date: 12-8-16
 * Time: 下午6:18
 *
 */
public class JmagickUtil {
    private static final Logger log = LoggerFactory.getLogger(JmagickUtil.class);

/**
 * 合并图片
 * @param comePathFile 源图片路径
 * @param toPathFile   图片路径名称
 * @param resize  图片的大小
 * @return
 */
 public static  void convert(String comePathFile,String toPathFile,String resize){

    /*  图片命令格式:
     * convert  -resize 215X53 logo_b.png logo_a.png
     */
    String str = "convert  -resize '"+resize+"' "+comePathFile+" "+toPathFile+" ";
    if(resize == null){
         str = "convert "+comePathFile+" "+toPathFile+" ";
    }
     final String finalStr = str;

     execWindowsOrLinux(finalStr);

 }

    /**
     * 生成九宫格图片
     * @param comePathFile
     * @param picName
     */
    public static  void ninePic(String comePathFile,String picName,String thumbPic){
        if(comePathFile == null ) return;
        String str = "convert -size 308x308 -quality 20  -strip xc:#d9d9d9  "+comePathFile+" "+picName+"; cp -rf "+picName+" "+thumbPic+";";
        //-resize 50%
        log.debug("convert :"+str);
        execWindowsOrLinux(str);
    }
  /**
  * 获取操作系统名称
  * @return
  */
     public static String getOS(){
         Properties pros = System.getProperties();
         String os = (String) pros.get("os.name");
         log.debug(os);
         return os;
     }

    public static void main(String args[]){
        //JmagickUtil.convert("D:\\logo_a.png","D:\\logo_c.png","215X53");
        JmagickUtil.convert("D:\\logo_a.png","D:\\logo_g.png","215X53");
    }


  /**
   * 根据操作系统类型来执行调用系统命令
   * @param command
   */
  public static  void  execWindowsOrLinux(String command) {
    Process proc = null;
    log.debug("命令为："+command);
     //String[] cmd = {"cmd.exe","/c",comman};
    String osName = getOS();
    try {
          if(osName.startsWith("Windows")){//windows下调用系统命令
                   String[] cmdWindows = {"cmd.exe","/c",command};
             proc = Runtime.getRuntime().exec(cmdWindows);
            }else if(osName.startsWith("Linux")){//Linux下调用系统命令
                 String[] cmdLinux = {"/bin/sh","-c",command};
                  proc = Runtime.getRuntime().exec(cmdLinux);
           }else if(osName.startsWith("Unix")){
               String[] cmdUnix = {command};
                 proc = Runtime.getRuntime().exec(cmdUnix);
         }
        int result = proc.waitFor();
        log.debug("Process result:" + result);
     }
     catch (Exception e)
     {
             e.printStackTrace();
     }
  }

}
