package org.dasxunya.diploma;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import org.jetbrains.annotations.NotNull;

public class ParameterizedTestsPlugin extends AnAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
        Project project = anActionEvent.getProject();
        Messages.showMessageDialog(project, "Hello, You just clicked the IntelliJ project view popup menu action item.",
                "Project View Action", Messages.getInformationIcon());
    }

    @Override
    public boolean isDumbAware() {
        return false;
    }
}
