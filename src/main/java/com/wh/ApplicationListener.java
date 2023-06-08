package com.wh;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManagerListener;
import com.intellij.openapi.vfs.newvfs.BulkFileListener;
import com.intellij.openapi.vfs.newvfs.events.VFileEvent;
import com.wh.tools.ProcessControlThread;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ApplicationListener implements BulkFileListener, ProjectManagerListener {

    @Override
    public void before(@NotNull List<? extends @NotNull VFileEvent> events) {
//        for (VFileEvent event : events) {
//            if (event.isFromSave()){
//                if (GaeaSwitchIdea.unlock(event.getFile(), false)){
//                    needProcessor.put(event.getFile(), event.getFile());
//                }
//            }
//        }
    }

    @Override
    public void after(@NotNull List<? extends @NotNull VFileEvent> events) {
//        for (VFileEvent event : events) {
//            if (event.isFromSave()){
//                if (needProcessor.containsKey(event.getFile()))
//                    GaeaSwitchIdea.lock(event.getFile(), false);
//            }
//        }
    }

    @Override
    public void projectOpened(@NotNull Project project) {
        ProcessControlThread.open();
    }

    @Override
    public void projectClosed(@NotNull Project project) {
        ProcessControlThread.close();
    }
}
