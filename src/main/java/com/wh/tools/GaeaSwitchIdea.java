package com.wh.tools;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.wh.ProcessMsg;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class GaeaSwitchIdea {
    public static final int SwitchTo_Gaea = 1;
    public static final int PostCodeTo_Gaea = 2;
    public static final int LockFile = 3;
    public static final int UnlockFile = 4;

    static final Map<File, RandomAccessFile> lockFiles = new ConcurrentHashMap<>();

    public static void notifyGaea(int command, String msg) {
        try {
            new ProcessMsg("GAEA-PROCESS-SHARE").write(command, msg, null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    public static VirtualFile getVirtualFile(AnActionEvent e) {
        return e.getData(PlatformDataKeys.VIRTUAL_FILE);
    }

    public static File getFile(VirtualFile virtualFile) {
        if (virtualFile == null)
            return null;
        return VfsUtilCore.virtualToIoFile(virtualFile);
    }

    public static File getFile(AnActionEvent e) {
        VirtualFile virtualFile = getVirtualFile(e);
        File file = getFile(virtualFile);
        return file;
    }

    public static RandomAccessFile getRandomAccessFile(VirtualFile virtualFile) {
        if (virtualFile == null)
            return null;

        File file = VfsUtilCore.virtualToIoFile(virtualFile);
        return getRandomAccessFile(file);
    }

    public static RandomAccessFile getRandomAccessFile(File file) {
        if (file == null)
            return null;

        return lockFiles.get(file);
    }

    public VirtualFile toVirtualFile(File file){
        return LocalFileSystem.getInstance().findFileByIoFile(file);
    }

    public static boolean hasLock(){
        return lockFiles.size() > 0;
    }

    public static boolean isLock(AnActionEvent e){
        File file = getFile(e);
        if (file == null)
            return false;

        return lockFiles.containsKey(file);
    }

    public static void lock(AnActionEvent e) {
        lock(getVirtualFile(e));
    }

    public static void lock(VirtualFile virtualFile) {
        if (virtualFile == null) {
            MsgHelper.warn("file is empty!", "lock");
            return;
        }
        notifyGaea(LockFile, getFile(virtualFile).getAbsolutePath());
    }

    public static Editor getEditor(AnActionEvent event){
        return event.getData(CommonDataKeys.EDITOR);
    }

    public static Project getCurrentProject(){
        Project[] projects = ProjectManager.getInstance().getOpenProjects();
        if (projects == null || projects.length == 0)
            return null;

        return projects[projects.length - 1];
    }

    public boolean isProjectFile(File file) {
        ProjectFileIndex projectFileIndex =
                ProjectRootManager.getInstance(getCurrentProject()).getFileIndex();
        return projectFileIndex.isInSource(toVirtualFile(file));
    }

    public static void lock(File localFile, File file, boolean hint) {
        if (file == null)
            return;

        if (lockFiles.containsKey(file))
            return;

        synchronized (lockFiles) {
            RandomAccessFile lockFile = null;
            try {
                lockFile = new RandomAccessFile(file, "rw");
                if (lockFile.getChannel().tryLock() == null)
                    throw new IOException("other user locked!");
                lockFiles.put(localFile, lockFile);
                if (hint)
                    MsgHelper.msg("lock is ok!", "lock");
            } catch (Exception ex) {
                if (lockFile != null) {
                    try {
                        lockFile.close();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
                MsgHelper.error("file is locked by other user!", "lock");
            }
        }

    }

    public static void unlock(AnActionEvent e) {
        unlock(getVirtualFile(e), true);
    }

    public static boolean unlock(VirtualFile virtualFile, boolean hint) {
        return unlock(getFile(virtualFile), hint);
    }

    public static boolean unlock(File file, boolean hint) {
        RandomAccessFile lockFile = lockFiles.remove(file);
        if (lockFile != null) {
            try {
                lockFile.close();
                if (hint)
                    MsgHelper.msg("file unlock is ok!", "unlock");
                return true;
            } catch (IOException e) {
                MsgHelper.error(e.toString(), "unlock");
            }
        }

        return false;
    }

    public static void unlocks() {
        synchronized (lockFiles) {
            for (File file : lockFiles.keySet()) {
                unlock(file, false);
            }
            lockFiles.clear();
            MsgHelper.msg("lock all is ok!", "unlock");
        }
    }

    public PsiFile getPSIFile(AnActionEvent e) {
        PsiFile psiFile = e.getData(CommonDataKeys.PSI_FILE);
        return psiFile;
    }
}
