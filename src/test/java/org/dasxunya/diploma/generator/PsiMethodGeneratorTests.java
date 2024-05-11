package org.dasxunya.diploma.generator;

import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.psi.*;
import com.intellij.psi.util.MethodSignature;
import org.dasxunya.diploma.constants.Constants;
import org.dasxunya.diploma.constants.TestType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Objects;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.io.IOException;
import java.util.StringJoiner;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class PsiMethodGeneratorTests {


    //region Поля

    //region Основные
    /**
     * Флаг удаления выходных файлов тестов
     */
    boolean isDeleteActualFiles = false;
    /**
     * Флаг отладкиы
     */
    boolean isDebug = true;
    MockedStatic<ApplicationManager> mockedApplicationManager;
    /**
     * Абсолютный путь до папки с полученными значениями тестов
     */
    String actualFolderPath = "src\\test\\java\\org\\dasxunya\\diploma\\generator\\actualTests";
    /**
     * Абсолютный путь до папки с ожидаемыми значениями тестов
     */
    String expectedFolderPath = "src\\test\\java\\org\\dasxunya\\diploma\\generator\\expectedTests";
    /**
     * Генератор тестов
     */
    UnitTestsGenerator generator;
    //endregion

    //region Макеты Psi элементов
    @Mock
    private PsiClass mockPsiClass;
    @Mock
    private PsiFile mockPsiFile;
    @Mock
    private PsiDirectory mockPsiDirectory;
    @Mock
    private PsiPackage mockPsiPackage;
    //endregion

    //region Макеты для методов
    /**
     * Контсруктор с параметрами
     */
    @Mock
    private PsiMethod mockConstructor;
    /**
     * Метод с параметрами без возвращаемого значения
     */
    @Mock
    private PsiMethod mockVoidMethod;
    /**
     * Метод с параметрами и возвращаемым значением
     */
    @Mock
    private PsiMethod mockReturnMethod;
    /**
     * Метод без параметров с возвращаемым значением
     */
    @Mock
    private PsiMethod mockNoParamMethod;
    //endregion

    //region Примитивные типы
    @Mock
    private PsiType mockPsiTypeInt, mockPsiTypeDouble, mockPsiTypeBoolean, mockPsiTypeByte, mockPsiTypeChar, mockPsiTypeShort, mockPsiTypeLong, mockPsiTypeFloat, mockPsiTypeString;
    @Mock
    private PsiParameter mockPsiParameterInt, mockPsiParameterDouble, mockPsiParameterBoolean, mockPsiParameterByte, mockPsiParameterChar, mockPsiParameterShort, mockPsiParameterLong, mockPsiParameterFloat, mockPsiParameterString;
    //endregion

    //endregion

    //region Вспомогательные методы

    public void print(String message) {
        System.out.print(message);
    }

    public void println(String message) {
        System.out.println(message);
    }

    public String combinePath(String folderPath, String fileName, String extension) {
        return String.format("%s\\%s.%s", folderPath, fileName, extension);
    }

    public void saveFile(String fileContent, String basePath, String fileName, String extension) {
        // Создание пути к файлу
        Path path = Paths.get(basePath, fileName + "." + extension);
        try {
            // Создание директории, если она не существует
            Files.createDirectories(path.getParent());
            // Запись строки в файл, если файл не существует, он будет создан
            Files.write(path, fileContent.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            System.out.printf("Файл '%s' успешно сохранен\n", path);
        } catch (IOException e) {
            System.err.println("Error writing to file: " + e.getMessage());
        }
    }

    /**
     * Удаляет файл по указанному пути.
     *
     * @param basePath  базовый путь к директории файла
     * @param fileName  имя файла для удаления
     * @param extension расщирение файла для удаления
     */
    public void deleteFile(String basePath, String fileName, String extension) {
        Path path = Paths.get(basePath, fileName + "." + extension);
        try {
            if (Files.exists(path)) {  // Проверка существования файла
                Files.delete(path);    // Удаление файла
                System.out.println("File was successfully deleted: " + path);
            } else {
                System.out.println("File does not exist, no need to delete: " + path);
            }
        } catch (IOException e) {
            System.err.println("Error deleting file: " + e.getMessage());
        }
    }

    /**
     * Сравнивает два текстовых файла построчно.
     *
     * @param pathToFile1 путь к первому файлу
     * @param pathToFile2 путь ко второму файлу
     * @throws IOException если произошла ошибка при чтении файлов
     */
    public void compareFilesByPath(String pathToFile1, String pathToFile2) throws Exception {
        Path file1 = Paths.get(pathToFile1);
        Path file2 = Paths.get(pathToFile2);

        assertFalse(Files.notExists(file1), "Файл " + pathToFile1 + " не существует");
        assertFalse(Files.notExists(file2), "Файл " + pathToFile2 + " не существует");

        if (file1.equals(file2)) {
            throw new Exception("Сравнивается один и тот же файл!");
        }

        List<String> linesOfFile1 = Files.readAllLines(file1);
        List<String> linesOfFile2 = Files.readAllLines(file2);

        // Проверяем, что количество строк одинаково
        assertEquals(linesOfFile1.size(), linesOfFile2.size(), "Количество строк в файлах различается.");

        // Сравниваем строки одна за другой
        for (int i = 0; i < linesOfFile1.size(); i++) {
            assertEquals(linesOfFile1.get(i), linesOfFile2.get(i), "Строка " + (i + 1) + " различается.");
        }

        println(String.format("Файлы '%s' и '%s' полностью совпадают", pathToFile1, pathToFile2));
    }

    /**
     * Сравнивает файлы построчно
     *
     * @param fileNameExpected Имя файла с ожмдаемыми результатами
     * @param fileNameActual   Имя файла с полученными результатами
     * @param extension        расширение файлов
     * @throws IOException если произошла ошибка при чтении файлов
     */
    public void compareFilesByName(String fileNameExpected, String fileNameActual, String extension) throws Exception {
        String expectedPath = this.combinePath(this.expectedFolderPath, fileNameExpected, extension);
        String actualPath = this.combinePath(this.actualFolderPath, fileNameActual, extension);
        this.compareFilesByPath(expectedPath, actualPath);
    }

    /**
     * Создание сигнатуры метода с учетом всех типов
     *
     * @param psiMethod  Мок-метод для задания сигнатуры
     * @param parameters Параметры мок-метода
     */
    private String generateSignature(@Mock PsiMethod psiMethod, PsiParameter[] parameters) {
        StringBuilder signatureBuilder = new StringBuilder(psiMethod.getName()).append("(");
        signatureBuilder.append("[");
        for (int i = 0; i < parameters.length; i++) {
            if (i > 0) signatureBuilder.append(", ");
            PsiParameter param = parameters[i];
            PsiType type = param.getType();
            String typeName = type.getPresentableText();
            // Отображение типа в квадратных скобках без имени параметра
            signatureBuilder.append("PsiType:").append(typeName);
        }
        signatureBuilder.append("]");
        signatureBuilder.append(")");
        return signatureBuilder.toString();
    }

    /**
     * Генерирует исходный код Java метода на основе данных PsiMethod.
     *
     * @param psiMethod метод для генерации исходного кода
     * @return строка с исходным кодом метода
     */
    public String generateJavaMethod(PsiMethod psiMethod) {
        if (psiMethod == null) throw new NullPointerException("PsiMethod is null");

        StringBuilder methodBuilder = new StringBuilder();
        PsiType returnType = psiMethod.getReturnType();
        String methodName = psiMethod.getName();
        PsiParameter[] parameters = psiMethod.getParameterList().getParameters();
        StringJoiner parameterList = new StringJoiner(", ");

        // Составление списка параметров
        for (PsiParameter parameter : parameters) {
            String parameterType = parameter.getType().getPresentableText();
            String parameterName = parameter.getName();
            parameterList.add(parameterType + " " + parameterName);
        }

        // Определение возвращаемого типа
        String returnTypeName = (returnType != null) ? returnType.getCanonicalText() : "void";
        methodBuilder.append(returnTypeName).append(" ").append(methodName).append("(");
        methodBuilder.append(parameterList).append(") {\n");

        // Добавление тела метода в зависимости от возвращаемого типа
        if ("void".equals(returnTypeName)) {
            methodBuilder.append("    // TODO: Реализация метода\n");
        } else {
            methodBuilder.append("    return ").append(getDefaultValue(returnTypeName)).append(";\n");
        }
        methodBuilder.append("}\n");

        return methodBuilder.toString();
    }

    /**
     * Возвращает значение по умолчанию для типа данных.
     *
     * @param typeName имя типа данных
     * @return строковое представление значения по умолчанию
     */
    private String getDefaultValue(String typeName) {
        if (typeName == null) {
            return "null";
        }
        return switch (typeName) {
            case "boolean" -> "false";
            case "int", "short", "long", "byte" -> "0";
            case "double", "float" -> "0.0";
            case "char" -> "'\\0'";
            default -> "null";
        };
    }

    @SuppressWarnings("UnstableApiUsage")
    private PsiType[] getTypesList() {
        //region Типы
        return new PsiType[]{PsiType.INT, PsiType.BOOLEAN, PsiType.BYTE, PsiType.CHAR, PsiType.SHORT, PsiType.LONG, PsiType.FLOAT, PsiType.DOUBLE, PsiType.VOID, mockPsiTypeString};
    }

    private PsiParameter createPsiParameter(PsiType type, String name) {
        PsiParameter parameter = mock(PsiParameter.class);
        when(parameter.getType()).thenReturn(type);
        when(parameter.getName()).thenReturn(name);
        return parameter;
    }

    @SuppressWarnings({"UnstableApiUsage", "deprecation"})
    public PsiMethod createPsiMethod(PsiType returnType, String name, PsiParameter[] parameters) {
        PsiMethod method = mock(PsiMethod.class);
        when(method.getName()).thenReturn(name);
        when(method.getReturnType()).thenReturn(Objects.requireNonNullElse(returnType, PsiType.VOID));
        PsiParameterList parameterList = mock(PsiParameterList.class);
        // Мокирование PsiParameterList для возвращения списка параметров
        when(parameterList.getParameters()).thenReturn(Objects.requireNonNullElseGet(parameters, () -> new PsiParameter[]{}));
        // Настройка метода getParameterList для возвращения mockVoidParameterList
        when(method.getParameterList()).thenReturn(parameterList);

        // Сигнатура конструктора
        if (parameters == null) parameters = new PsiParameter[]{};
        String signature = this.generateSignature(method, parameters);
        MethodSignature methodSignature = mock(MethodSignature.class);
        when(methodSignature.toString()).thenReturn(signature);
        when(method.getSignature(PsiSubstitutor.EMPTY)).thenReturn(methodSignature);
        return method;
    }

    /**
     * Запускает тестирование генератора для указанного макета метода
     *
     * @param testName            Название теста, совпадает с именем выходного файла
     * @param actualFileExtension Расширение выходного файла
     * @param psiMethod           Тестируемый макет метода
     * @param testType            Тип теста
     * @param isDebug             Флаг режима отладки
     */
    public void startGeneratorTest(String testName, String actualFileExtension, PsiMethod psiMethod, TestType testType, boolean isDebug) {
        this.generator.setDebug(isDebug);

        //region Генерация тестируемого метода и тестирующего метода
        String methodImplementationStr = generateJavaMethod(psiMethod);
        String parameterizedTestStr = generator.generate(psiMethod, testType);
        if (isDebug) {
            System.out.println(methodImplementationStr);
            System.out.println(parameterizedTestStr);
        }
        //endregion

        //region Тестирование
        try {
            //region Генерация тела теста
            String testBodyStr = this.generator.getClassHeader(mockPsiClass, testType) +
                                 "\n" +
                                 "{\n" +

                                 // Сохранение содержимого в файл
                                 methodImplementationStr + "\n" +
                                 parameterizedTestStr + "\n" +
                                 "}\n";
            //endregion
            // Сохранение теста
            saveFile(testBodyStr, this.actualFolderPath, testName, actualFileExtension);
            // Сравнение результатов
            compareFilesByName(testName, testName, actualFileExtension);
            // Удаление файла после тестирования
            if (this.isDeleteActualFiles)
                deleteFile(this.actualFolderPath, testName, actualFileExtension);
        } catch (Exception e) {
            if (isDebug)
                throw new RuntimeException(e);
            else
                println(e.getMessage());
        }
        //endregion

    }
    //endregion

    //region Инициализация перед тестом

    /**
     * Настройка типов
     */
    private void setupPrimitiveParameters() {

        //region getPresentableText
        when(mockPsiTypeBoolean.getPresentableText()).thenReturn("boolean");
        when(mockPsiTypeByte.getPresentableText()).thenReturn("byte");
        when(mockPsiTypeChar.getPresentableText()).thenReturn("char");
        when(mockPsiTypeShort.getPresentableText()).thenReturn("short");
        when(mockPsiTypeInt.getPresentableText()).thenReturn("int");
        when(mockPsiTypeLong.getPresentableText()).thenReturn("long");
        when(mockPsiTypeFloat.getPresentableText()).thenReturn("float");
        when(mockPsiTypeDouble.getPresentableText()).thenReturn("double");
        when(mockPsiTypeString.getPresentableText()).thenReturn("String");
        //endregion

        //region getCanonicalText
        when(mockPsiTypeBoolean.getCanonicalText()).thenReturn("boolean");
        when(mockPsiTypeByte.getCanonicalText()).thenReturn("byte");
        when(mockPsiTypeChar.getCanonicalText()).thenReturn("char");
        when(mockPsiTypeShort.getCanonicalText()).thenReturn("short");
        when(mockPsiTypeInt.getCanonicalText()).thenReturn("int");
        when(mockPsiTypeLong.getCanonicalText()).thenReturn("long");
        when(mockPsiTypeFloat.getCanonicalText()).thenReturn("float");
        when(mockPsiTypeDouble.getCanonicalText()).thenReturn("double");
        when(mockPsiTypeString.getCanonicalText()).thenReturn("String");
        //endregion

        //region Создание параметров с помощью вспомогательного метода
        mockPsiParameterBoolean = createPsiParameter(mockPsiTypeBoolean, "flag");
        mockPsiParameterByte = createPsiParameter(mockPsiTypeByte, "b");
        mockPsiParameterChar = createPsiParameter(mockPsiTypeChar, "c");
        mockPsiParameterShort = createPsiParameter(mockPsiTypeShort, "s");
        mockPsiParameterInt = createPsiParameter(mockPsiTypeInt, "i");
        mockPsiParameterLong = createPsiParameter(mockPsiTypeLong, "l");
        mockPsiParameterFloat = createPsiParameter(mockPsiTypeFloat, "f");
        mockPsiParameterDouble = createPsiParameter(mockPsiTypeDouble, "d");
        mockPsiParameterString = createPsiParameter(mockPsiTypeString, "str");
        //endregion

    }

    private void setUpConstructor() {
        PsiParameter[] parameters = {createPsiParameter(mockPsiTypeString, "brand"), createPsiParameter(mockPsiTypeString, "model"), createPsiParameter(mockPsiTypeInt, "year"), createPsiParameter(mockPsiTypeDouble, "price")};
        mockConstructor = createPsiMethod(null, "Car", parameters);
    }

    @SuppressWarnings({"UnstableApiUsage", "deprecation"})
    private void setupVoidMethod() {
        PsiParameter[] parameters = {mockPsiParameterString, mockPsiParameterInt, mockPsiParameterBoolean, mockPsiParameterByte, mockPsiParameterChar, mockPsiParameterShort, mockPsiParameterLong, mockPsiParameterFloat, mockPsiParameterDouble};
        mockVoidMethod = createPsiMethod(PsiType.VOID, "voidMethod", parameters);
    }

    private void setupReturnMethod() {
        // Подготовка всех параметров
        PsiParameter[] parameters = {mockPsiParameterString, mockPsiParameterInt, mockPsiParameterBoolean, mockPsiParameterByte, mockPsiParameterChar, mockPsiParameterShort, mockPsiParameterLong, mockPsiParameterFloat, mockPsiParameterDouble};
        mockReturnMethod = createPsiMethod(mockPsiTypeBoolean, "returnMethod", parameters);
    }

    private PsiMethod setupReturnMethod(PsiType returnType) {
        // Подготовка всех параметров
        PsiParameter[] parameters = {mockPsiParameterString, mockPsiParameterInt, mockPsiParameterBoolean, mockPsiParameterByte, mockPsiParameterChar, mockPsiParameterShort, mockPsiParameterLong, mockPsiParameterFloat, mockPsiParameterDouble};
        return createPsiMethod(returnType, String.format("returnMethod_%s", returnType.getPresentableText()), parameters);
    }

    private void setupNoParamMethod() {
        mockNoParamMethod = createPsiMethod(mockPsiTypeString, "noParamMethod", null);
    }

    @BeforeEach
    void setUp() {
        mockedApplicationManager = Mockito.mockStatic(ApplicationManager.class);
        MockitoAnnotations.openMocks(this);
        generator = new UnitTestsGenerator(true);

        //region Настройка приложения для JavaDirectoryService
        Application mockApplication = mock(Application.class);
        JavaDirectoryService mockJavaDirectoryService = Mockito.mock(JavaDirectoryService.class);
        mockedApplicationManager.when(ApplicationManager::getApplication).thenReturn(mockApplication);
        Mockito.when(mockApplication.getService(JavaDirectoryService.class)).thenReturn(mockJavaDirectoryService);
        when(mockJavaDirectoryService.getPackage(mockPsiDirectory)).thenReturn(mockPsiPackage);
        //endregion
        //region Настройка моков для PsiClass
        when(mockPsiClass.getName()).thenReturn("Car");
        when(mockPsiClass.getContainingFile()).thenReturn(mockPsiFile);
        //endregion
        //region Настройка моков для PsiFile
        when(mockPsiFile.getContainingDirectory()).thenReturn(mockPsiDirectory);
        //endregion
        //region Настройка моков для PsiPackage
        when(mockPsiPackage.getQualifiedName()).thenReturn("org.dasxunya.diploma.generator.actualTests");
        //endregion
        //region Настройка параметров
        setupPrimitiveParameters();
        //endregion
        //region Настройка для конструктора
        setUpConstructor();
        //endregion
        //region Настройка для других методов
        setupVoidMethod();
        setupReturnMethod();
        setupNoParamMethod();
        //endregion
    }

    @AfterEach
    void tearDown() {
        // Очистка мокирования статического метода
        mockedApplicationManager.close();
    }
    //endregion

    //region Тесты

    @Test
    void testGenerateTypeAssert() {
        //region Типы
        PsiType[] types = this.getTypesList();
        //endregion
        //region Параметры
        PsiParameter[] parameters = {mockPsiParameterString, mockPsiParameterInt, mockPsiParameterBoolean, mockPsiParameterByte, mockPsiParameterChar, mockPsiParameterShort, mockPsiParameterLong, mockPsiParameterFloat, mockPsiParameterDouble};
        //endregion
        StringBuilder stringBuilder = new StringBuilder();
        for (PsiType type : types) {
            stringBuilder.append(String.format("Возвращаемый тип '%s'", type.getPresentableText()));
            stringBuilder.append("\n");
            mockReturnMethod = createPsiMethod(type, String.format("%sMethod", type.getPresentableText()), parameters);
            stringBuilder.append(generator.generateTypeAssert(mockReturnMethod.getReturnType(), generator.getMethodCallString(mockReturnMethod)));
            stringBuilder.append("\n");
        }
        String actualFileName = "testGenerateTypeAssert";
        String expectedFileName = "testGenerateTypeAssert";
        this.saveFile(stringBuilder.toString(), this.actualFolderPath, actualFileName, Constants.Strings.Extensions.txt);
        try {
            this.compareFilesByPath(
                    combinePath(this.expectedFolderPath, expectedFileName, Constants.Strings.Extensions.txt),
                    combinePath(this.actualFolderPath, actualFileName, Constants.Strings.Extensions.txt));
            if (this.isDeleteActualFiles)
                deleteFile(this.actualFolderPath, actualFileName, Constants.Strings.Extensions.txt);
        } catch (Exception e) {
            println(e.getMessage());
        }
    }

    @SuppressWarnings("ConstantValue")
    @Test
    void testGenerate_NullMethod() {
        Exception exception = assertThrows(NullPointerException.class, () -> {
            PsiMethod testMethod = null;
            generator.generate(testMethod, TestType.UNIT);
        });
        // Проверяем, что сообщение исключения соответствует ожидаемому
        assertEquals(Constants.Strings.Release.Errors.NULL_POINTER, exception.getMessage());

        exception = assertThrows(NullPointerException.class, () -> {
            PsiMethod testMethod = null;
            generator.generate(testMethod, TestType.PARAMETERIZED);

        });

        // Проверяем, что сообщение исключения соответствует ожидаемому
        assertEquals(Constants.Strings.Release.Errors.NULL_POINTER, exception.getMessage());
    }

    @Test
    void testGetInfo() {
        this.generator.setDebug(true);
        PsiMethod[] methods = {mockConstructor, mockVoidMethod, mockReturnMethod, mockNoParamMethod};
        StringBuilder stringBuilder = new StringBuilder();
        for (PsiMethod method : methods) {
            stringBuilder.append(generator.getInfo(method));
            stringBuilder.append("\n");
        }
        String actualFileName = "testGetInfo";
        String expectedFileName = "testGetInfo";
        this.saveFile(stringBuilder.toString(), this.actualFolderPath, actualFileName, Constants.Strings.Extensions.txt);
        try {
            this.compareFilesByName(actualFileName, expectedFileName, Constants.Strings.Extensions.txt);
            if (this.isDeleteActualFiles)
                deleteFile(this.actualFolderPath, actualFileName, Constants.Strings.Extensions.txt);
        } catch (Exception e) {
            println(e.getMessage());
        }

    }

    @Test
    void testGetClassHeader() {
        String actualFileName = "testGetClassHeader";
        String psiClassHeader = this.generator.getClassHeader(this.mockPsiClass, TestType.PARAMETERIZED);
        System.out.println(psiClassHeader);
        this.saveFile(psiClassHeader, this.actualFolderPath, "testGetClassHeader", Constants.Strings.Extensions.txt);
        try {
            this.compareFilesByName(actualFileName, "testGetClassHeader", Constants.Strings.Extensions.txt);
            if (this.isDeleteActualFiles)
                deleteFile(this.actualFolderPath, actualFileName, Constants.Strings.Extensions.txt);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void testGenerateMethod_UnitTest() {
//        this.generator.setDebug(true);
//        String parameterizedTestStr = generator.generate(mockVoidMethod, TestType.PARAMETERIZED);
//        System.out.println(parameterizedTestStr);
        PsiParameter[] parameters = {mockPsiParameterString, mockPsiParameterInt, mockPsiParameterBoolean, mockPsiParameterByte,
                mockPsiParameterChar, mockPsiParameterShort, mockPsiParameterLong, mockPsiParameterFloat, mockPsiParameterDouble};
        for (PsiType type : this.getTypesList()) {
            mockReturnMethod = setupReturnMethod(type);
            this.startGeneratorTest("testGenerateMethod_UnitTest", Constants.Strings.Extensions.txt, mockReturnMethod, TestType.UNIT, this.isDebug);
        }
    }

    //region Тесты с параметрами
    @Test
    void testGenerateMethod_ParameterizedTest() {
        this.generator.setDebug(true);
        String methodImplementationStr = generateJavaMethod(mockVoidMethod);
        String parameterizedTestStr = generator.generate(mockVoidMethod, TestType.PARAMETERIZED);
        System.out.println(methodImplementationStr);
        System.out.println(parameterizedTestStr);

        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append(this.generator.getClassHeader(mockPsiClass, TestType.PARAMETERIZED));
        stringBuilder.append("\n");
        stringBuilder.append("{\n");

        String actualFileName = "testGenerateMethod_ParameterizedTest";

        // Сохранение содержимого в файл
        stringBuilder.append(methodImplementationStr).append("\n");
        stringBuilder.append(parameterizedTestStr).append("\n");
        stringBuilder.append("}\n");
        saveFile(stringBuilder.toString(), this.actualFolderPath, actualFileName, Constants.Strings.Extensions.txt);
        try {
            compareFilesByName(actualFileName, actualFileName, Constants.Strings.Extensions.txt);
            if (this.isDeleteActualFiles)
                deleteFile(this.actualFolderPath, actualFileName, Constants.Strings.Extensions.txt);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testGenerateConstructor_ParameterizedTest() {
        this.generator.setDebug(true);
        String methodImplementationStr = generateJavaMethod(mockConstructor);
        String parameterizedTestStr = generator.generate(mockConstructor, TestType.PARAMETERIZED);
        System.out.println(methodImplementationStr);
        System.out.println(parameterizedTestStr);

        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append(this.generator.getClassHeader(mockPsiClass, TestType.PARAMETERIZED));
        stringBuilder.append("\n");
        stringBuilder.append("{\n");

        String actualFileName = "testGenerateConstructor_ParameterizedTest";

        // Сохранение содержимого в файл
        stringBuilder.append(methodImplementationStr).append("\n");
        stringBuilder.append(parameterizedTestStr).append("\n");
        stringBuilder.append("}\n");
        saveFile(stringBuilder.toString(), this.actualFolderPath, actualFileName, Constants.Strings.Extensions.txt);
        try {
            compareFilesByName(actualFileName, actualFileName, Constants.Strings.Extensions.txt);
            if (this.isDeleteActualFiles)
                deleteFile(this.actualFolderPath, actualFileName, Constants.Strings.Extensions.txt);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testGenerateReturnMethod_ParameterizedTest() {
        this.generator.setDebug(true);
        String methodImplementationStr = generateJavaMethod(mockReturnMethod);
        String parameterizedTestStr = generator.generate(mockReturnMethod, TestType.PARAMETERIZED);
        System.out.println(methodImplementationStr);
        System.out.println(parameterizedTestStr);

        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append(this.generator.getClassHeader(mockPsiClass, TestType.PARAMETERIZED));
        stringBuilder.append("\n");
        stringBuilder.append("{\n");

        String actualFileName = "testGenerateReturnMethod_ParameterizedTest";

        // Сохранение содержимого в файл
        stringBuilder.append(methodImplementationStr).append("\n");
        stringBuilder.append(parameterizedTestStr).append("\n");
        stringBuilder.append("}\n");
        saveFile(stringBuilder.toString(), this.actualFolderPath, actualFileName, Constants.Strings.Extensions.txt);
        try {
            compareFilesByName(actualFileName, actualFileName, Constants.Strings.Extensions.txt);
            if (this.isDeleteActualFiles)
                deleteFile(this.actualFolderPath, actualFileName, Constants.Strings.Extensions.txt);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testGenerateNoParamMethod_ParameterizedTest() {
        this.generator.setDebug(true);
        String methodImplementationStr = generateJavaMethod(mockNoParamMethod);
        String parameterizedTestStr = generator.generate(mockNoParamMethod, TestType.PARAMETERIZED);
        System.out.println(methodImplementationStr);
        System.out.println(parameterizedTestStr);

        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append(this.generator.getClassHeader(mockPsiClass, TestType.PARAMETERIZED));
        stringBuilder.append("\n");
        stringBuilder.append("{\n");

        String actualFileName = "testGenerateNoParamMethod_ParameterizedTest";

        // Сохранение содержимого в файл
        stringBuilder.append(methodImplementationStr).append("\n");
        stringBuilder.append(parameterizedTestStr).append("\n");
        stringBuilder.append("}\n");
        saveFile(stringBuilder.toString(), this.actualFolderPath, actualFileName, Constants.Strings.Extensions.txt);
        try {
            compareFilesByName(actualFileName, actualFileName, Constants.Strings.Extensions.txt);
            //if (this.isDeleteActualFiles)
            //     deleteFile(this.actualFolderPath, actualFileName, Constants.Strings.Extensions.txt);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    //endregion
    //endregion
}
