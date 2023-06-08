package com.wh;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.wh.tools.GaeaSwitchIdea;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class UnlockAction extends AnAction {
    public UnlockAction(String title, String memo, ImageIcon imageIcon) {
        super(title, memo, imageIcon);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        GaeaSwitchIdea.unlock(e);
    }
}
