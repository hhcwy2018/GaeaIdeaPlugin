package com.wh;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.util.NlsActions;
import com.wh.tools.FileHelp;
import com.wh.tools.GaeaSwitchIdea;
import com.wh.tools.MsgHelper;
import jnr.posix.MsgHdr;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.io.*;

public class PostCodeToGaea extends AnAction {
    public PostCodeToGaea(@Nullable @NlsActions.ActionText String text, @Nullable @NlsActions.ActionDescription String description, @Nullable Icon icon) {
        super(text, description, icon);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        try {
            File destFile = GaeaSwitchIdea.getFile(e);
//            File destFile = new File(sourceFile.getParentFile(), sourceFile.getName() + ".copy");
//            if (destFile.exists())
//                if (!destFile.delete())
//                    throw new IOException("delete file[] failed!");
            RandomAccessFile accessFile = GaeaSwitchIdea.getRandomAccessFile(GaeaSwitchIdea.getVirtualFile(e));

            if (accessFile != null){
                try(FileInputStream inputStream = new FileInputStream(destFile);) {
                    byte[] buffer = new byte[inputStream.available()];
                    accessFile.setLength(0);
                    int len = buffer.length;
                    while (len > 0) {
                        int readLen = inputStream.read(buffer);
                        len -= readLen;
                        accessFile.write(buffer, 0, readLen);
                    }
                }
                MsgHelper.msg("save file is ok!", "save to gaea");
            }else
                MsgHelper.msg("lock this file please first!","save to gaea");

//            GaeaSwitchIdea.notifyGaea(GaeaSwitchIdea.PostCodeTo_Gaea, sourceFile.getAbsolutePath());
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}
