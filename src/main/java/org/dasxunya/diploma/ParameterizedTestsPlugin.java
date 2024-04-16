package org.dasxunya.diploma;

import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile; //нужен для генерации структуры файлов в тестах
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;

public class ParameterizedTestsPlugin extends AnAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
        Project project = anActionEvent.getProject();
        /*1)Проверить инстанс класса или метода был получен*/
        //сгенерить иерархию тестов

        Messages.showMessageDialog(project, "Hello, You just clicked the IntelliJ project view popup menu action item.",
                "Project View Action", Messages.getInformationIcon());
    }

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return super.getActionUpdateThread();
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        //если тыкнули на generate test template вывести это
        PsiElement data = e.getData(CommonDataKeys.PSI_ELEMENT);

        if (data != null) {
            //System.out.println(data.getContext());
            System.out.println(findMethod(data));;
        }
        super.update(e);
    }

    private static PsiMethod findMethod(PsiElement element) {
        PsiMethod method = (element instanceof PsiMethod) ? (PsiMethod) element :
                PsiTreeUtil.getParentOfType(element, PsiMethod.class);
        if (method != null && method.getContainingClass() != null) {
            return findMethod(method.getParent());
        }
        return method;
    }
    @Override
    public boolean isDumbAware() {
        return false;
    }
}


/**
 * TODO:
 * создание параметризованных тестовых методов:
 *  2) Если класс - генерировать по каждому методу класса:
 *      - проверить, был ли сгенерирован метод ранее (по инстансу - если так можно)
 *          если был: скип
 *          иначе: генерация тестового метода
 *  3) Если метод - генерировать тестовый метод по выбранному методу:
 *      - проверить был ли сгенерирован метод ранее
 *          если был: скип
 *          иначе: генерация тестового метода
 * <p>
 * По какому принципу генерировать? (посмотреть значения для типов данных в )
 * 1) примитивы
 * 2) Объектные классы примитивов
 * 3) Enums???
 * 4)
 **/