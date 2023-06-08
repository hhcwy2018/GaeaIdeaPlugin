package com.wh;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.wh.tools.GaeaSwitchIdea;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

public class LockAction extends AnAction {
    public LockAction(String title, String memo, ImageIcon imageIcon) {
        super(title, memo, imageIcon);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
//        final Editor editor = (Editor) e.getRequiredData(CommonDataKeys.EDITOR);
//        final Project project = e.getRequiredData(CommonDataKeys.PROJECT);
//        final PsiFile psiFile = e.getRequiredData(CommonDataKeys.PSI_FILE);
//        final Document document = editor.getDocument();
//        editor.getComponent().addComponentListener(new ComponentAdapter() {
//            @Override
//            public void componentHidden(ComponentEvent e) {
//                GaeaSwitchIdea.unlock(virtualFile, false);
//            }
//        });
//        final VirtualFile virtualFile = GaeaSwitchIdea.getVirtualFile(e);

        GaeaSwitchIdea.lock(e);
    }
}
