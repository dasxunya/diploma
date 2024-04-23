package org.dasxunya.diploma.generator;

import com.intellij.psi.*;

import java.util.StringJoiner;

/**
 * Генератор юнит тестов для классов и методов
 */
public class UnitTestsGenerator {

    //region Поля
    private final StringBuilder stringBuilder;

    private boolean isDebug;
    //endregion

    //region Сеттеры/Геттеры
    public boolean isDebug() {
        return isDebug;
    }

    public void setDebug(boolean debug) {
        isDebug = debug;
    }
    //endregion

    //region Конструкторы
    public UnitTestsGenerator() {
        this.stringBuilder = new StringBuilder();
        this.setDebug(false);
    }

    public UnitTestsGenerator(boolean isDebug) {
        this.stringBuilder = new StringBuilder();
        this.setDebug(isDebug);
    }
    //endregion

    //region Методы
    private String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return Character.toUpperCase(str.charAt(0)) + str.substring(1);
    }

    public String generate(PsiElement element, TestType testType) {
        // Элемент является классом
        if (element instanceof PsiClass) {
            return generate((PsiClass) element, testType);
        }
        // Элемент является методом
        if (element instanceof PsiMethod) {
            return generate((PsiMethod) element, testType);
        }
        // Элемент не является ни классом, ни методом
        throw new IllegalArgumentException("Неподдерживаемый тип элемента: " + element.getClass().getName());
    }

    public String generate(PsiClass psiClass, TestType testType) {
        stringBuilder.setLength(0);
        if (this.isDebug)
            stringBuilder.append("Методы класса:");
        for (PsiMethod method : psiClass.getAllMethods())
            stringBuilder.append(this.generate(method, testType));
        return stringBuilder.toString();
    }

    public String generate(PsiMethod psiMethod, TestType testType) {
        stringBuilder.setLength(0);
        if (psiMethod == null)
            throw new IllegalArgumentException("Передана пустая ссылка на PsiMethod");
        if (this.isDebug) {
            stringBuilder.append("Метод: ").append(psiMethod.getName());
            stringBuilder.append("Сигнатура: ").append(psiMethod.getSignature(PsiSubstitutor.EMPTY));
            stringBuilder.append("Юнит тест: \n");
        }
        if (testType == TestType.PARAMETERIZED) {
            //region Генерация тела параметризованного теста
            stringBuilder.append("Генерация тела параметризованного теста - не реализована");
            //endregion
        } else {
            //region Генерация тела юнит теста
            String methodName = psiMethod.getName();
            PsiType returnType = psiMethod.getReturnType();
            PsiParameter[] parameters = psiMethod.getParameterList().getParameters();
            String testMethodName = "test" + capitalize(methodName);
            StringJoiner parameterList = new StringJoiner(", ");
            for (PsiParameter parameter : parameters) {
                String paramerType = parameter.getType().getPresentableText();
                String name = parameter.getName();
                // Здесь мы используем просто имена переменных,
                // в реальном случае может потребоваться инициализация с дефолтными значениями
                parameterList.add(paramerType + " " + name);
            }
            stringBuilder.append("@Test\n");
            stringBuilder.append("public void ").append(testMethodName).append("() {\n");
            stringBuilder.append("    // TODO: создать экземпляр тестируемого класса и инициализировать параметры\n");
            stringBuilder.append("    // TODO: вызвать тестируемый метод с параметрами: ").append(parameterList).append("\n");
            stringBuilder.append("    // TODO: добавить проверки утверждений с помощью assert\n");
            stringBuilder.append("}\n");
            //endregion
        }
        String result = stringBuilder.toString();
        stringBuilder.setLength(0);
        return result;
    }
    //endregion

}
