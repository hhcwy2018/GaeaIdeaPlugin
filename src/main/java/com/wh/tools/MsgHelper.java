package com.wh.tools;

import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.ui.Messages;

public class MsgHelper {

    public static void Message(Runnable runnable){
        Application application = ApplicationManager.getApplication();
        if (application.isDispatchThread())
            runnable.run();
        else
            application.invokeLater(new Runnable() {
                @Override
                public void run() {
                    runnable.run();
                }
            });
    }
    public static void msg(String msg, String title){
        Message(new Runnable() {
            @Override
            public void run() {
                Messages.showInfoMessage(msg, title);
            }
        });
    }

    public static void warn(String msg, String title){
        Message(new Runnable() {
            @Override
            public void run() {
                Messages.showWarningDialog(msg, title);
            }
        });
    }

    public static void error(String msg, String title){
        Message(new Runnable() {
            @Override
            public void run() {
                Messages.showErrorDialog(msg, title);
            }
        });
    }
}
