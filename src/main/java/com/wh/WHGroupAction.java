package com.wh;

import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.wh.tools.GaeaSwitchIdea;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

public class WHGroupAction extends ActionGroup {

    protected String getString(String title) {
        String charset = "gbk";
        try {
            return new String(title.getBytes(charset), charset);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    @NotNull
    @Override
    public AnAction[] getChildren(AnActionEvent event) {
        List<AnAction> actions = new ArrayList<>();
        if (!GaeaSwitchIdea.isLock(event))
            actions.add(new LockAction(
                    getString("Lock"),
                    getString("exclusive lock this file, not edit by other user!"),
                    new ImageIcon(getClass().getResource("/image/lock.png")))
            );
        else
            actions.add(new UnlockAction(
                    getString("Unlock"),
                    "unlock this file, edit by other user!",
                    new ImageIcon(getClass().getResource("/image/unlock.png"))));

        if (GaeaSwitchIdea.hasLock())
            actions.add(new UnlockAction(
                    getString("Unlocks"),
                    getString("unlock all locked file!"),
                    new ImageIcon(getClass().getResource("/image/unlock.png"))));

        if (GaeaSwitchIdea.getFile(event) != null) {
            actions.add(new SwitchToGaeaUI(
                    getString("SwitchToGaea"),
                    getString("switch to ui of gaea!"),
                    new ImageIcon(getClass().getResource("/image/switchtogaea.png"))));

            actions.add(new PostCodeToGaea(
                    getString("SaveToGaea"),
                    getString("save code to gaea!"),
                    new ImageIcon(getClass().getResource("/image/savetogaea.png"))));
        }
        return actions.toArray(new AnAction[actions.size()]);
    }
}
