package com.wh.tools;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.ProjectManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public abstract class FileHelp {

    public static File getFile(String ...names){
        File file = VfsUtilCore.virtualToIoFile(ProjectManager.getInstance().getOpenProjects()[0].getWorkspaceFile());
        file = file.getParentFile().getParentFile();
        for (String name:names) {
            file = new File(file, name);
        }

        return file;
    }

    public static String removeExt(String name) {
        int index = name.indexOf(".");
        if (index != -1)
            name = name.substring(0, index);
        return name;
    }

    public static void moveFile(File sourceFile, File destFile) throws IOException {
        Path source = Paths.get(sourceFile.getAbsolutePath());
        Path dest = Paths.get(destFile.getAbsolutePath());
        Files.move(source, dest, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
    }

    public static String ChangeExt(String filename, String newext) {
        for (int i = filename.length() - 1; i >= 0; i--) {
            if (filename.charAt(i) == '.') {
                filename = filename.substring(0, i + 1);
                return filename + newext;
            }

        }
        return filename + "." + newext;
    }

    public static String GetExt(String filename) {
        for (int i = filename.length() - 1; i >= 0; i--) {
            if (filename.charAt(i) == '.') {
                String ext = filename.substring(i + 1);
                return ext;
            }

        }
        return null;
    }

    public static File getFile(File basePath, String... names) {
        if (names == null || names.length == 0)
            return basePath;

        for (String name : names) {
            if (name == null || name.isEmpty())
                continue;
            basePath = new File(basePath, name);
        }
        return basePath;
    }

    public static File GetFile(String filename, File path) {
        if (path == null)
            return null;

        File file = new File(path, filename);
        return file;
    }

    public static File createFile(String fileName) throws IOException {
        File file = new File(fileName);
        File path = file.getParentFile();
        if (path != null && !path.exists())
            if (!path.mkdirs())
                return null;
        file.createNewFile();
        return file;
    }

    public static boolean isFileExist(String fileName) {
        File file = new File(fileName);
        return file.exists();
    }

    public static boolean delFile(String fileName) {
        File file = new File(fileName);
        if (file == null || !file.exists() || file.isDirectory())
            return false;
        file.delete();
        return true;
    }

    public static boolean renameFile(String oldfileName, String newFileName) {
        File oleFile = new File(oldfileName);
        File newFile = new File(newFileName);
        return oleFile.renameTo(newFile);
    }

    public static void delDir(File dir) throws Exception {
        delDir(dir, new IDelDir() {

            @Override
            public boolean prepareDeleteFile(File file) {
                return true;
            }

            @Override
            public boolean deletedFile(File file, boolean isok) {
                return isok;
            }
        });
    }

    protected static void fireDir(File file, IDelDir iDelDir) throws Exception {
        if (iDelDir.prepareDeleteFile(file)) {
            if (!file.delete()) {
                if (!iDelDir.deletedFile(file, false))
                    throw new IOException("delete[" + file.getAbsolutePath() + "] failed!");
            } else {
                if (!iDelDir.deletedFile(file, true))
                    throw new Exception("user abort operation!");
            }
        } else {
            throw new Exception("user abort operation!");
        }
    }

    public static void delDir(File dir, IDelDir iDelDir) throws Exception {
        if (dir == null || !dir.exists() || dir.isFile()) {
            return;
        }

        File[] files = dir.listFiles();
        if (files == null)
            return;

        if (!iDelDir.prepareDeleteFile(dir)) {
            return;
        }

        for (File file : files) {
            if (file.isFile()) {
                fireDir(file, iDelDir);
            } else if (file.isDirectory()) {
                delDir(file);
            }
        }

        fireDir(dir, iDelDir);
    }

    public static void copyFileTo(File srcFile, File destFile) throws IOException {
        copyFileTo(srcFile, destFile, null);
    }

    public static void copyFileTo(File srcFile, File destFile, OverWrite overWrite) throws IOException {
        if (srcFile.isDirectory() || destFile.isDirectory())
            throw new IOException("源或者目的不是文件！");

        try {
            if (overWrite != null && !overWrite.before(srcFile, destFile) && destFile.exists())
                return;
        } catch (Exception e) {
            e.printStackTrace();
            throw new IOException(e);
        }

        try (FileInputStream fis = new FileInputStream(srcFile);
             FileOutputStream fos = new FileOutputStream(destFile);) {
            int readLen = 0;
            byte[] buf = new byte[1024];
            while ((readLen = fis.read(buf)) != -1) {
                fos.write(buf, 0, readLen);
            }
            fos.flush();
        }

        if (overWrite != null)
            try {
                overWrite.after(srcFile, destFile);
            } catch (Exception e) {
                e.printStackTrace();
                throw new IOException(e);
            }

    }

    public static File getPath(File baseDir, String... pathNames) {
        if (pathNames == null || pathNames.length == 0)
            return baseDir;

        File result = baseDir;
        for (String string : pathNames) {
            if (string == null)
                continue;

            string = string.trim();
            if (string.isEmpty())
                continue;

            result = new File(result, string);
        }

        return result;
    }

    public static boolean copyFilesTo(File srcDir, File destDir) throws IOException {
        return copyFilesTo(srcDir, destDir, (String[]) null);
    }

    public static boolean copyFilesTo(File srcDir, File destDir, String[] exts) throws IOException {
        HashMap<String, String> extHash = new HashMap<>();
        if (exts != null) {
            for (String ext : exts) {
                ext = ext.toLowerCase().trim();
                extHash.put(ext, ext);
            }
        }
        return copyFilesTo(srcDir, destDir, extHash);
    }

    public static boolean copyFilesTo(File srcDir, File destDir, CopyFilter copyFilter) throws IOException {
        return copyFilesTo(srcDir, destDir, null, copyFilter);
    }

    public static boolean copyFilesTo(File srcDir, File destDir, OverWrite overWrite)
            throws IOException {
        return copyFilesTo(srcDir, destDir, null, null, overWrite);
    }

    public static boolean copyFilesTo(File srcDir, File destDir, HashMap<String, String> exts)
            throws IOException {
        return copyFilesTo(srcDir, destDir, exts, null);
    }

    public static boolean copyFilesTo(File srcDir, File destDir, HashMap<String, String> exts,
                                      CopyFilter copyFilter) throws IOException {
        return copyFilesTo(srcDir, destDir, exts, copyFilter, null);
    }

    public static boolean copyFilesTo(File srcDir, File destDir, HashMap<String, String> exts,
                                      CopyFilter copyFilter, OverWrite overWrite)
            throws IOException {
        if (!srcDir.exists())
            return true;

        if (!srcDir.isDirectory())
            throw new IOException("[" + srcDir + "] not dir!");

        if (destDir == null) {
            throw new IOException("set dir please first!");
        }
        if (!destDir.exists())
            if (!destDir.mkdirs())
                throw new IOException("make dir[" + destDir.getAbsolutePath() + "] failed!");

        File[] srcFiles = srcDir.listFiles();
        if (srcFiles == null || srcFiles.length == 0)
            return true;

        for (File srcFile : srcFiles) {
            if (srcFile.isFile()) {
                File destFile = new File(destDir, srcFile.getName());
                if (exts != null && exts.size() > 0) {
                    String ext = GetExt(srcFile.getName());
                    ext = ext.toLowerCase().trim();
                    if (!exts.containsKey(ext))
                        continue;
                }

                if (copyFilter != null) {
                    if (copyFilter instanceof ReplaceCopyFilter) {
                        AtomicReference<File> desAtomicReference = new AtomicReference<>(destFile);
                        if (((ReplaceCopyFilter) copyFilter).filter(srcFile, desAtomicReference)) {
                            destFile = desAtomicReference.get();
                        } else {
                            continue;
                        }
                    } else if (!copyFilter.filter(srcFile, destFile))
                        continue;
                }
				copyFileTo(srcFile, destFile, overWrite);
            } else if (srcFile.isDirectory()) {
                File theDestDir = new File(destDir, srcFile.getName());
                if (!copyFilesTo(srcFile, theDestDir, exts, copyFilter, overWrite))
                    return false;
            }
        }
        return true;
    }

    public static boolean getFiles(File srcDir, List<File> result) throws IOException {
        return getFiles(srcDir, result, true);
    }

    public static boolean getFiles(File srcDir, List<File> result, boolean root) throws IOException {
        if (!srcDir.isDirectory())
            return !root;

        File[] srcFiles = srcDir.listFiles();
        for (int i = 0; i < srcFiles.length; i++) {
            if (srcFiles[i].isFile()) {
                result.add(new File(srcDir, srcFiles[i].getName()));
            } else if (srcFiles[i].isDirectory()) {
                getFiles(srcFiles[i], result, false);
            }
        }

        return true;
    }

    public static void moveFileTo(File srcFile, File destFile) throws IOException {
        copyFileTo(srcFile, destFile);
        String filename = srcFile.getAbsolutePath() + File.separator + srcFile.getName();
        if (!delFile(filename))
            throw new IOException("delete[" + filename+"] failed!");
    }

    public static boolean moveFilesTo(File srcDir, File destDir) throws Exception {
        if (!srcDir.isDirectory() || !destDir.isDirectory()) {
            return false;
        }
        File[] srcDirFiles = srcDir.listFiles();
        for (int i = 0; i < srcDirFiles.length; i++) {
            if (srcDirFiles[i].isFile()) {
                String destfilename = destDir.getPath() + "//" + srcDirFiles[i].getName();
                String srcfilename = srcDir.getPath() + "//" + srcDirFiles[i].getName();
                File oneDestFile = new File(destfilename);
                moveFileTo(srcDirFiles[i], oneDestFile);
                delFile(srcfilename);
            } else if (srcDirFiles[i].isDirectory()) {
                File oneDestFile = new File(destDir.getPath() + "//" + srcDirFiles[i].getName());
                moveFilesTo(srcDirFiles[i], oneDestFile);
                delDir(srcDirFiles[i]);
            }

        }
        return true;
    }

    public static boolean DeleteFile(File file) {
        if (!file.exists()) {
            return false;
        } else {
            if (file.isFile()) {
                file.delete();
                return true;
            } else if (file.isDirectory()) {
                File[] childFile = file.listFiles();
                if (childFile == null || childFile.length == 0) {
                    file.delete();
                    return true;
                }
                for (File f : childFile) {
                    DeleteFile(f);
                }
                file.delete();
                return true;
            } else
                return true;
        }
    }

    public static BasicFileAttributes getFileAttr(File file) throws IOException {
        Path path = file.toPath();
        BasicFileAttributeView basicview = Files.getFileAttributeView(path, BasicFileAttributeView.class,
                LinkOption.NOFOLLOW_LINKS);
        return basicview.readAttributes();
    }

    public static Date getFileCreateTime(File file) throws IOException {
        return new Date(getFileAttr(file).creationTime().toMillis());
    }

    public static Date getFileLastModifyTime(File file) {
        return new Date(file.lastModified());
    }

    public interface IDelDir {
        boolean prepareDeleteFile(File file);

        boolean deletedFile(File file, boolean isok);
    }

    public interface ReplaceCopyFilter extends CopyFilter {
        boolean filter(File srcFile, AtomicReference<File> destFile);

        default boolean filter(File srcFile, File destFile) {
            return true;
        }
    }

    public interface CopyFilter {
        boolean filter(File srcFile, File destFile);
    }

    public interface OverWrite {
        boolean before(File sourceFile, File destFile) throws Exception;

        void after(File sourceFile, File destFile) throws Exception;
    }
}