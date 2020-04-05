import tool.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Objects;

import static tool.AES.DEFAULT_PWD;

public class JiaguMain {

    private static final String APK_FILE_TEMP_DIR = "source/apk/temp";
    private static  String APK_FILE_DIR = null;
    private static  String AAR_FILE_DIR = null;
    private static final String AAR_FILE_TEMP_DIR = "/source/aar/temp";
    private static String PROPATH = null;
    public static void main(String[] args) {
        try {
            File proFile = new File("");
            PROPATH = proFile.getCanonicalPath();
            APK_FILE_DIR = PROPATH + File.separator + "source/apk";
            AAR_FILE_DIR = PROPATH + File.separator + "source/aar";

            File apkTempFileDir = new File(PROPATH + File.separator +  APK_FILE_TEMP_DIR);
            File aarTempFileDir = new File(PROPATH + AAR_FILE_TEMP_DIR);
            deleteFile(apkTempFileDir, aarTempFileDir);

            AES.init(DEFAULT_PWD);
            System.out.println("apkFileDir path:" + apkTempFileDir);
            File newApkFile = new File(PROPATH + File.separator + APK_FILE_TEMP_DIR);
            //对原始apk的dex文件进行AES加密，并且重命名
            aesOriginApk(newApkFile);
            //对aar文件进行dex文件转换

            File aarFile = new File(AAR_FILE_DIR + File.separator + "mylibrary-debug.aar");
            if(aarFile.exists()){
                File dexFile = Dx.jar2Dex(aarFile);
                if(!dexFile.exists()){
                    System.out.println("dex file no exit xxxxxxxxxxxxxx");
                    return;
                }

                File newDexFile = new File(apkTempFileDir.getPath() + File.separator + "classes.dex");
                if(!newDexFile.exists()){
                    newDexFile.createNewFile();
                }

                FileOutputStream dexStream = new FileOutputStream(newDexFile);

                byte[] aarBytes = Utils.getBytes(dexFile);

                dexStream.write(aarBytes);
                dexStream.flush();
                dexStream.close();

                File unsignApkFile = new File(PROPATH + File.separator + "apk-unsign.apk");
                if(!unsignApkFile.exists()){
                    unsignApkFile.createNewFile();
                }

                System.out.println("===========zip================");
                System.out.println("apk temp file path:" + apkTempFileDir.getPath());
                //将apk/temp目录下的文件进行打包压缩
                Zip.zip(apkTempFileDir, unsignApkFile);
                //签名
                Signature.signature(unsignApkFile, new File(PROPATH + File.separator + "apk-signed.apk"));

            }else {
                System.out.println("aar file no exit");
            }


        }  catch (Exception e) {
            e.printStackTrace();
        }
    }


    private static void aesOriginApk(File newApkFile) throws Exception{

        if(!newApkFile.exists()){
            newApkFile.mkdirs();
        }

        System.out.println("origin apk file path:" + APK_FILE_DIR);
        File originApkFile = new File(APK_FILE_DIR + File.separator+  "app-debug.apk");
        if(!originApkFile.exists()){
            System.out.println("========origin apk file no exit path:" + originApkFile.getPath());
            return;
        }

        AES.encryptAPKFile(originApkFile, newApkFile);

        if(newApkFile.isDirectory()){
            for(File newApkDirFile: Objects.requireNonNull(newApkFile.listFiles())){
                if(newApkDirFile.isFile()){
                    if(newApkDirFile.getName().endsWith(".dex")){
                        String dexName = newApkDirFile.getName();
                        int cursor = dexName.indexOf(".dex");
                        String reName = newApkDirFile.getParent() + File.separator + dexName.substring(0, cursor) + "_" + ".dex";
                        System.out.println("reName value:" + reName);
                        newApkDirFile.renameTo(new File(reName));
                    }
                }
            }
        }
    }

    private static void deleteFile(File apkFileDir, File aarFileDir){
        if(apkFileDir.exists()){
            for(File apkFile: Objects.requireNonNull(apkFileDir.listFiles())){
                if(apkFile.isFile()){
                    apkFile.delete();
                }
            }
        }else {
            apkFileDir.mkdirs();
        }
        if(aarFileDir.exists()){
            for(File aarFile: Objects.requireNonNull(aarFileDir.listFiles())){
                if(aarFile.isFile()){
                    aarFile.delete();
                }
            }
        }else {
            aarFileDir.mkdirs();
        }

    }

}
