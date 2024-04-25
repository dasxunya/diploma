package org.dasxunya.diploma.generator;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;

import java.util.ArrayList;
import java.util.Objects;
import java.util.StringJoiner;

/**
 * Генератор юнит тестов для классов и методов
 */
public class UnitTestsGenerator {

    //region Поля
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
        this.setDebug(false);
    }

    public UnitTestsGenerator(boolean isDebug) {
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
        StringBuilder stringBuilder = new StringBuilder();
        //region Список добавляемых библиотек и сборок
        ArrayList<String> imports = new ArrayList<>();
        // Получение имени пакета класса
        PsiFile psiFile = psiClass.getContainingFile();
        PsiDirectory psiDirectory = psiFile.getContainingDirectory();
        PsiPackage psiPackage = JavaDirectoryService.getInstance().getPackage(psiDirectory);
        if (psiPackage == null)
            throw new NullPointerException("Не удалось получить пакет, которому принадлежит класс");
        // Импорт пакета
        stringBuilder.append(String.format("package %s;\n", psiPackage.getQualifiedName()));
        // Добавление библиотек
        imports.add("org.junit.jupiter.api.Test");
        if (testType == TestType.PARAMETERIZED) {
            imports.add("org.junit.jupiter.params.ParameterizedTest");
            imports.add("org.junit.jupiter.params.provider.Arguments");
            imports.add("org.junit.jupiter.params.provider.MethodSource");
        }
        //endregion
        //region Добавление бибилотек в код тестирующего класса
        for (String importStr : imports)
            stringBuilder.append(String.format("import %s;\n", importStr));
        //endregion
        //region Формирование имени класса
        stringBuilder.append(String.format("class %sTests", psiClass.getName()));
        //endregion
        //region Формирование тела тестирующего класса
        stringBuilder.append("{");
        stringBuilder.append("\n");
        if (this.isDebug)
            stringBuilder.append("Методы класса:\n");
        for (PsiMethod method : psiClass.getMethods())
            stringBuilder.append(this.generate(method, testType));
        stringBuilder.append("}");
        stringBuilder.append("\n");
        //endregion
        return stringBuilder.toString();
    }

    public String generate(PsiMethod psiMethod, TestType testType) {
        StringBuilder stringBuilder = new StringBuilder();
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

    public void generate(Project project, PsiElement element, PsiDirectory directory, TestType testType) {
        if (directory == null) {
            System.out.println("PsiDirectory is null");
            return;
        }

        // Генерация содержимого файла в зависимости от типа теста
        String fileContent = this.generate(element, testType);
        String fileName = "TestClass1.java";//generateFileName(testType);

        // Создание PsiFile
        PsiFile file = PsiFileFactory.getInstance(project).createFileFromText(fileName, fileContent);

        // Добавление файла в директорию
        directory.add(file);
    }
    //endregion

}
