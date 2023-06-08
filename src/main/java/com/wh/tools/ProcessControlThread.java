package com.wh.tools;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectLocator;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.wh.ProcessMsg;

import javax.swing.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;


@Service
public final class ProcessControlThread extends Thread {

    // 指令必须大于0
    public static final int SwitchTo_Idea = 1;
    public static final int OpenEditor_Idea = 2;
    static ProcessControlThread processControlThread;

    public static final void open() {
        if (processControlThread != null)
            return;

        processControlThread = new ProcessControlThread();
        processControlThread.start();
    }

    public static void close() {
        if (processControlThread == null)
            return;
        processControlThread.interrupt();
        processControlThread = null;
    }

    public void run() {
        ProcessMsg processMsg = new ProcessMsg("IDEA-PROCESS-SHARE");
        ProcessMsg.IMsg onMsg = new ProcessMsg.IMsg() {

            protected File getCodeFile(File remoteFile) {
                File file = new File(ProjectManager.getInstance().getOpenProjects()[0].getBasePath());
                file = FileHelp.getFile(file, "gaea", "code");

                List<String> pathNames = new ArrayList<>();
                while (!remoteFile.getName().equalsIgnoreCase("code")) {
                    pathNames.add(0, remoteFile.getName());
                    remoteFile = remoteFile.getParentFile();
                }
                for (int i = 0; i < pathNames.size(); i++) {
                    file = new File(file, pathNames.get(i));
                }

                return file;
            }
            protected void switchTo_Idea(File file) throws IOException {
                final File codeFile = getCodeFile(file);
                if (codeFile.lastModified() < file.lastModified()) {
                    if (codeFile.exists())
                        if (!codeFile.delete()) {
                            throw new IOException("delete file[" + codeFile.getAbsolutePath() + "] failed!");
                        }

                    RandomAccessFile accessFile = GaeaSwitchIdea.getRandomAccessFile(codeFile);
                    if (accessFile != null) {
                        byte[] buffer = new byte[(int) accessFile.length()];
                        accessFile.seek(0);
                        int len = buffer.length;

                        try (FileOutputStream inputStream = new FileOutputStream(file);) {
                            while (len > 0) {
                                int readLen = accessFile.read(buffer);
                                len -= readLen;
                                inputStream.write(buffer, 0, readLen);
                            }
                        }

                    } else
                        FileHelp.copyFileTo(file, codeFile);

                }

                SwingUtilities.invokeLater(new Runnable() {

                    @Override
                    public void run() {
                        try {
                            openEditor(codeFile);
                            int dwProcessId = ProcessHelper.getProcessId();
                            WindowHelper.bringProcessToTop(dwProcessId);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            }

            protected void openEditor(File file) throws Exception {
                VirtualFile virtualFile = VfsUtilCore.fileToVirtualFile(file);
//                PsiFile psiFile = PsiManager.getInstance(project).findFile(virtualFile);
//                Document document = FileDocumentManager.getInstance().getDocument(virtualFile);
                ApplicationManager.getApplication().invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        Project project = ProjectLocator.getInstance().guessProjectForFile(virtualFile);
                        OpenFileDescriptor openFileDescriptor = new OpenFileDescriptor(project, virtualFile);
                        FileEditorManager.getInstance(project).openEditor(openFileDescriptor,
                                true);
                    }
                });

            }

            @Override
            public void onMsg(int command, String value) {
                try {
                    switch (command) {
                        case SwitchTo_Idea: {
                            File file = new File(value);
                            switchTo_Idea(file);
                            break;
                        }
                        case OpenEditor_Idea: {
                            File file = new File(value);
                            openEditor(file);
                            break;
                        }
                        case GaeaSwitchIdea.LockFile: {
                            String[] tmps = value.split(",");
                            GaeaSwitchIdea.lock(new File(tmps[0]), new File(tmps[1]), true);
                            break;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    MsgHelper.error(e.toString(), "gaea");
                }
            }
        };

        while (!isInterrupted())
            try {
                processMsg.read(null, onMsg);
            } catch (Exception e) {
                e.printStackTrace();
            }
    }

}
