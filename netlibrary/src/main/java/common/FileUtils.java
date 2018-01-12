package common;

import android.os.Environment;

import java.io.File;

/**
 * Created by cxg on 2017/7/13.
 */

public class FileUtils {

    /**
     * 递归删除文件和文件夹
     * @param isAll
     * 			  是否删除根目录
     * @param file
     *            要删除的根目录
     */
    public static void deleteFile(File file, boolean isAll) {
        if (!file.exists()) {
            return;
        } else {
            if (file.isFile()) {
                file.delete();
                return;
            }

            if (file.isDirectory()) {
                File[] childFile = file.listFiles();
                if (childFile == null || childFile.length == 0) {
                    if(isAll)
                        file.delete();
                    return;
                }
                for (File f : childFile) {
                    deleteFile(f, true);
                }
                if(isAll)
                    file.delete();
            }
        }
    }

    /**
     * @return 程序在sdcard上的存储路径
     */
    public static String getSdcardPath() {
        String path = Environment.getExternalStorageDirectory().getAbsolutePath();
        File sdcardFile = new File(path);
        if (!sdcardFile.exists()){
            sdcardFile.mkdirs();
        }
        return path;
    }
}
