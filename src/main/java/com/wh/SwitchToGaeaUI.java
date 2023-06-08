package com.wh;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.util.NlsActions;
import com.wh.tools.GaeaSwitchIdea;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.io.File;

public class SwitchToGaeaUI extends AnAction {
    public SwitchToGaeaUI(@Nullable @NlsActions.ActionText String text, @Nullable @NlsActions.ActionDescription String description, @Nullable Icon icon) {
        super(text, description, icon);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        File file = GaeaSwitchIdea.getFile(e);
        GaeaSwitchIdea.notifyGaea(GaeaSwitchIdea.SwitchTo_Gaea, file.getAbsolutePath());
    }
}
