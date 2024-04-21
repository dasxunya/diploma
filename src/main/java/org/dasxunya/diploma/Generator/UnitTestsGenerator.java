package org.dasxunya.diploma.Generator;

import com.intellij.psi.*;

import java.util.StringJoiner;

public class UnitTestsGenerator {
    private final StringBuilder stringBuilder;

    public UnitTestsGenerator() {
        this.stringBuilder = new StringBuilder();
    }

    private String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return Character.toUpperCase(str.charAt(0)) + str.substring(1);
    }

    private String generateUnitTest(PsiMethod method) {
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
        stringBuilder.append("Метод: ").append(psiMethod.getName());
        stringBuilder.append("Сигнатура: ").append(psiMethod.getSignature(PsiSubstitutor.EMPTY));
        stringBuilder.append("Юнит тест: ").append(generateUnitTest(psiMethod));
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
