package org.dasxunya.diploma.Generator;

import com.intellij.psi.*;

import java.util.StringJoiner;

public class UnitTestsGenerator {

    private final StringBuilder stringBuilder;

    public UnitTestsGenerator() {
        this.stringBuilder = new StringBuilder();
    }

    private String Capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return Character.toUpperCase(str.charAt(0)) + str.substring(1);
    }

    public String Generate(PsiElement element) {
        // Элемент является классом
        if (element instanceof PsiClass) {
            return Generate((PsiClass) element);
        }
        // Элемент является методом
        if (element instanceof PsiMethod) {
            return Generate((PsiMethod) element);
        }
        // Элемент не является ни классом, ни методом
        throw new IllegalArgumentException("Неподдерживаемый тип элемента: " + element.getClass().getName());
    }

    public String Generate(PsiMethod psiMethod) {
        stringBuilder.setLength(0);
        if (psiMethod == null)
            throw new IllegalArgumentException("Передана пустая ссылка на PsiMethod");
        stringBuilder.append("Метод: ").append(psiMethod.getName());
        stringBuilder.append("Сигнатура: ").append(psiMethod.getSignature(PsiSubstitutor.EMPTY));
        stringBuilder.append("Юнит тест: \n");

        //region Генерация тела юнит теста
        String methodName = psiMethod.getName();
        PsiType returnType = psiMethod.getReturnType();
        PsiParameter[] parameters = psiMethod.getParameterList().getParameters();
        String testMethodName = "test" + Capitalize(methodName);
        StringJoiner parameterList = new StringJoiner(", ");
        for (PsiParameter parameter : parameters) {
            String type = parameter.getType().getPresentableText();
            String name = parameter.getName();
            // Здесь мы используем просто имена переменных,
            // в реальном случае может потребоваться инициализация с дефолтными значениями
            parameterList.add(type + " " + name);
        }
        stringBuilder.append("@org.junit.jupiter.api.Test\n");
        stringBuilder.append("public void ").append(testMethodName).append("() {\n");
        stringBuilder.append("    // TODO: создать экземпляр тестируемого класса и инициализировать параметры\n");
        stringBuilder.append("    // TODO: вызвать тестируемый метод с параметрами: ").append(parameterList).append("\n");
        stringBuilder.append("    // TODO: добавить проверки утверждений с помощью assert\n");
        stringBuilder.append("}\n");
        //endregion

        return stringBuilder.toString();
    }

    public String Generate(PsiClass psiClass) {
        stringBuilder.setLength(0);
        System.out.println("Методы класса:");
        for (PsiMethod method : psiClass.getAllMethods())
            stringBuilder.append(this.Generate(method));
        return stringBuilder.toString();
    }

}
