package org.dasxunya.diploma;

import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
//нужен для генерации структуры файлов в тестах
import com.intellij.psi.PsiType;
import com.intellij.psi.PsiParameter;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiSubstitutor;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;

import java.util.StringJoiner;

public class ParameterizedTestsPlugin extends AnAction {

    private void ShowMessage(Project project, String message) {
        Messages.showMessageDialog(project, message,
                "Info", Messages.getInformationIcon());
    }

    private static String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return Character.toUpperCase(str.charAt(0)) + str.substring(1);
    }

    public String generateUnitTest(PsiMethod method) {
        if (method == null) {
            throw new IllegalArgumentException("PsiMethod is null");
        }

        String methodName = method.getName();
        PsiType returnType = method.getReturnType();
        PsiParameter[] parameters = method.getParameterList().getParameters();

        String testMethodName = "test" + capitalize(methodName);
        StringJoiner parameterList = new StringJoiner(", ");

        for (PsiParameter parameter : parameters) {
            String type = parameter.getType().getPresentableText();
            String name = parameter.getName();
            // Здесь мы используем просто имена переменных,
            // в реальном случае может потребоваться инициализация с дефолтными значениями
            parameterList.add(type + " " + name);
        }

        StringBuilder testMethodBuilder = new StringBuilder();
        testMethodBuilder.append("@org.junit.jupiter.api.Test\n");
        testMethodBuilder.append("public void ").append(testMethodName).append("() {\n");
        testMethodBuilder.append("    // TODO: создать экземпляр тестируемого класса и инициализировать параметры\n");
        testMethodBuilder.append("    // TODO: вызвать тестируемый метод с параметрами: ").append(parameterList).append("\n");
        testMethodBuilder.append("    // TODO: добавить проверки утверждений с помощью assert\n");
        testMethodBuilder.append("}\n");

        return testMethodBuilder.toString();
    }


    @Override
    public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
        //anActionEvent.getClass();
        /*1)Проверить инстанс класса или метода был получен*/
        //сгенерить иерархию тестов
        Project project = anActionEvent.getProject();
        try {
            //если тыкнули на generate test template вывести это
            PsiElement data = anActionEvent.getData(CommonDataKeys.PSI_ELEMENT);
            if (data != null) {
                if (data instanceof PsiClass) {
                    PsiClass psiClass = (PsiClass) data;
                    PsiMethod[] methods = psiClass.getAllMethods();
                    System.out.println("This is a class. Listing methods:");

                    for (PsiMethod method : methods) {
                        // Выводим информацию о методе в консоль
                        System.out.println("Method: " + method.getName());
                        System.out.println("Signature: " + method.getSignature(PsiSubstitutor.EMPTY));
                        System.out.println("UnitTest: "+ generateUnitTest(method));
                    }

                    // Элемент является классом
                    System.out.println("This is a class.");
                } else if (data instanceof PsiMethod) {
                    // Элемент является методом
                    System.out.println("This is a method.");
                } else {
                    // Элемент не является ни классом, ни методом
                    System.out.println("This is neither a class nor a method.");
                }
            }
        } catch (Exception ex) {
            ShowMessage(project, ex.getMessage());
        }


        Messages.showMessageDialog(project, "Hello, You just clicked the IntelliJ project view popup menu action item.",
                "Project View Action", Messages.getInformationIcon());
    }

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return super.getActionUpdateThread();
    }

    @Override
    public void update(@NotNull AnActionEvent anActionEvent) {
        super.update(anActionEvent);
    }

//    int sum(int a,int b){
//        return a+b;
//    }

    //совет от опенапи String, int,
//    @ParameterizedTest(fkdjf)
//    void TestSum(){
//        // Asset_Eual(sum(ПАРАМЕТР_1,ПАРАМЕТР_2),ОЖИДАЕМОЕ_ЗНАЧЕНИЕ);
//    }

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