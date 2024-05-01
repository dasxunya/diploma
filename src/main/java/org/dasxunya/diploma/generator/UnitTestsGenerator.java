package org.dasxunya.diploma.generator;

import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
//import lombok.Getter;
import org.dasxunya.diploma.constants.Constants;
import org.dasxunya.diploma.constants.TestType;

import java.util.ArrayList;
import java.util.StringJoiner;

/**
 * Генератор юнит тестов для классов и методов
 */
//@Getter
public class UnitTestsGenerator {

    //region Поля
    private boolean isDebug;
    //endregion

    //region Сеттеры/Геттеры
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

    //region Вывод ошибок и отладочной инофрмации
    private void print(String message) {
        System.out.print(message);
    }

    private void printLn(String message) {
        System.out.println(message);
    }

    private <T> void throwNullPointerException(Class<T> type) throws NullPointerException {
        if (this.isDebug)
            this.printLn(Constants.Strings.Debug.Errors.NULL_POINTER + type.getSimpleName());
        throw new NullPointerException(Constants.Strings.Release.Errors.NULL_POINTER);
    }

    private <T> void throwIllegalArgumentException(Class<T> type) throws IllegalArgumentException {
        if (this.isDebug)
            this.printLn(Constants.Strings.Debug.Errors.ILLEGAL_ARGUMENT + type.getSimpleName());
        throw new IllegalArgumentException(Constants.Strings.Release.Errors.ILLEGAL_ARGUMENT);
    }

    private void throwException(String messageRelease, String messageDebug) throws Exception {
        if (this.isDebug && messageDebug != null && !messageDebug.isEmpty())
            this.printLn(messageDebug);
        throw new Exception(messageRelease);
    }
    //endregion

    private String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return Character.toUpperCase(str.charAt(0)) + str.substring(1);
    }

    @SuppressWarnings({"StringBufferReplaceableByString", "DataFlowIssue"})
    public String getInfo(PsiMethod psiMethod) throws NullPointerException {
        //region Проверка ссылки на объект
        if (psiMethod == null)
            this.throwNullPointerException(PsiMethod.class);
        //endregion
        StringBuilder info = new StringBuilder();
        info.append("Название: ").append(psiMethod.getName()).append("\n");
        //region Возвращаемый тип
        PsiType returnType = psiMethod.getReturnType();
        String returnTypeName = (returnType != null) ? returnType.getPresentableText() : "void";
        info.append("Возвращаемый тип: ").append(returnTypeName).append("\n");
        //endregion
        info.append("Сигнатура: ").append(psiMethod.getSignature(PsiSubstitutor.EMPTY)).append("\n");
        //region Получение и вывод параметров метода
        PsiParameter[] parameters = psiMethod.getParameterList().getParameters();
        if (parameters.length == 0) {
            info.append("Параметры: Нет параметров");
        } else {
            info.append("Параметры: ");
            StringBuilder parametersInfo = new StringBuilder();
            for (int i = 0; i < parameters.length; i++) {
                PsiParameter parameter = parameters[i];
                String typeName = parameter.getType().getPresentableText(); // Получаем текстовое представление типа параметра
                String parameterName = parameter.getName(); // Получаем имя параметра
                if (i > 0) parametersInfo.append(", "); // Добавляем запятую между параметрами
                parametersInfo.append(typeName).append(" ").append(parameterName);
            }
            info.append(parametersInfo.toString());
        }
        //endregion
        return info.toString();
    }

    // Метод для генерации тестового класса для всех методов в классе
    public String generate(PsiClass psiClass, TestType testType) {
        return generate(psiClass, null, testType); // null означает все методы
    }

    // Перегруженный метод для генерации тестового класса для одного конкретного метода
    public String generate(PsiClass psiClass, PsiMethod psiMethod, TestType testType) {
        if (psiClass == null)
            this.throwNullPointerException(PsiClass.class);
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
            stringBuilder.append("// Методы класса:\n");
        if (psiMethod != null) {
            stringBuilder.append(this.generate(psiMethod, testType));
        } else {
            for (PsiMethod method : psiClass.getMethods())
                stringBuilder.append(this.generate(method, testType));
        }
        stringBuilder.append("}");
        stringBuilder.append("\n");
        //endregion
        return stringBuilder.toString();
    }

    @SuppressWarnings("DataFlowIssue")
    public String generate(PsiMethod psiMethod, TestType testType) {
        //region Проверка ссылки на объект
        if (psiMethod == null)
            this.throwNullPointerException(PsiMethod.class);
        //endregion
        StringBuilder stringBuilder = new StringBuilder();
        //region Вывод отладной информации о методе
        if (this.isDebug)
            this.printLn(this.getInfo(psiMethod));
        //endregion

        //region Основные свойства метода
        String methodName = psiMethod.getName();
        PsiType returnType = psiMethod.getReturnType();
        PsiParameter[] parameters = psiMethod.getParameterList().getParameters();
        String testMethodName = "test" + capitalize(methodName);
        StringJoiner parameterTypes = new StringJoiner(", ");
        StringJoiner parameterNames = new StringJoiner(", ");
        StringJoiner parameterList = new StringJoiner(", ");
        //endregion

        if (testType == TestType.PARAMETERIZED) {

            //region Генерация тела параметризованного теста
            stringBuilder.append("@ParameterizedTest\n");
            stringBuilder.append("@CsvSource({\n");

            // Добавление тестовых строк данных
            // Здесь вы можете добавить свои тестовые данные
            // Пример: "\"testString1\", 1, 2.0", "\"testString2\", 2, 3.0"
            stringBuilder.append("    \"exampleString1, 100, 200.0\",\n");
            stringBuilder.append("    \"exampleString2, 101, 201.0\"\n");
            stringBuilder.append("})\n");

            stringBuilder.append("public void ").append(testMethodName).append("(");
            stringBuilder.append(parameterTypes.toString());
            stringBuilder.append(") {\n");
            stringBuilder.append("    // TODO: Тестируемая логика\n");
            stringBuilder.append("    // TODO: добавить проверки утверждений с помощью assert\n");
            stringBuilder.append("}\n");
            //endregion
        } else {
            //region Генерация тела юнит теста

            for (PsiParameter parameter : parameters) {
                String parameterType = parameter.getType().getPresentableText();
                String name = parameter.getName();
                // Здесь мы используем просто имена переменных,
                // в реальном случае может потребоваться инициализация с дефолтными значениями
                parameterList.add(parameterType + " " + name);
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

    public void generate(Project project, PsiElement element, PsiDirectory directory, TestType testType) {
        if (!(element instanceof PsiClass) && !(element instanceof PsiMethod))
            throw new IllegalArgumentException(Constants.Strings.Release.Errors.ILLEGAL_ARGUMENT);
        //"Плагин поддерживает работу толь"

        if (directory == null) {
            System.out.println("PsiDirectory is null");
            return;
        }

        // Генерация содержимого файла в зависимости от типа теста
        String fileContent = this.generate(element, testType);
        String fileName = ((PsiNameIdentifierOwner) element).getName() + "Tests.java";//generateFileName(testType);

        // Создание PsiFile
        PsiFile file = PsiFileFactory.getInstance(project).createFileFromText(fileName, fileContent);

        // Добавление файла в директорию
        directory.add(file);
    }
    //endregion
}
