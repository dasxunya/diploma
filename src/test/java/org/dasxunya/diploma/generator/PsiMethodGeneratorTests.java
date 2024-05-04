package org.dasxunya.diploma.generator;

import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.util.MethodSignature;
import org.dasxunya.diploma.constants.Constants;
import org.dasxunya.diploma.constants.TestType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Objects;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.io.IOException;
import java.util.StringJoiner;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

public class PsiMethodGeneratorTests {


    //region Поля
    UnitTestsGenerator generator;
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
    @Mock
    private JavaDirectoryService mockJavaDirectoryService;
    @Mock
    private MethodSignature mockConstructorMethodSignature, mockPsiVoidMethodSignature, mockReturnMethodSignature;
    @Mock
    private PsiParameterList mockConstructorParameterList, mockVoidParameterList, mockReturnParameterList;
    @Mock
    private PsiParameter mockPsiParameterBrand, mockPsiParameterModel, mockPsiParameterYear, mockPsiParameterPrice;
    //region Примитивные типы
    @Mock
    private PsiType mockPsiTypeInt, mockPsiTypeDouble, mockPsiTypeBoolean, mockPsiTypeByte, mockPsiTypeChar,
            mockPsiTypeShort, mockPsiTypeLong, mockPsiTypeFloat, mockPsiTypeString;
    @Mock
    private PsiParameter mockPsiParameterInt, mockPsiParameterDouble, mockPsiParameterBoolean, mockPsiParameterByte, mockPsiParameterChar,
            mockPsiParameterShort, mockPsiParameterLong, mockPsiParameterFloat, mockPsiParameterString;
    //endregion
    //endregion

    //region Вспомогательные методы

    public void saveTestToFile(String testContent, String basePath, String fileName) {
        // Создание пути к файлу
        Path path = Paths.get(basePath, fileName + ".java");
        try {
            // Создание директории, если она не существует
            Files.createDirectories(path.getParent());
            // Запись строки в файл, если файл не существует, он будет создан
            Files.write(path, testContent.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            System.out.println("Test file was successfully saved: " + path);
        } catch (IOException e) {
            System.err.println("Error writing to file: " + e.getMessage());
        }
    }

    /**
     * Удаляет файл по указанному пути.
     *
     * @param basePath базовый путь к директории файла
     * @param fileName имя файла для удаления
     */
    public void deleteFile(String basePath, String fileName) {
        Path path = Paths.get(basePath, fileName + ".java");
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
        if (psiMethod == null)
            throw new NullPointerException("PsiMethod is null");

        StringBuilder methodBuilder = new StringBuilder();
        PsiType returnType = psiMethod.getReturnType();
        String methodName = psiMethod.getName();
        PsiParameter[] parameters = psiMethod.getParameterList().getParameters();
        StringJoiner parameterList = new StringJoiner(", ");

        // Составление списка параметров
        for (PsiParameter parameter : parameters) {
            String parameterType = parameter.getType().getCanonicalText();
            String parameterName = parameter.getName();
            parameterList.add(parameterType + " " + parameterName);
        }

        // Определение возвращаемого типа
        String returnTypeName = (returnType != null) ? returnType.getCanonicalText() : "void";
        methodBuilder.append(returnTypeName).append(" ").append(methodName).append("(");
        methodBuilder.append(parameterList.toString()).append(") {\n");

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
        switch (typeName) {
            case "boolean":
                return "false";
            case "int":
            case "short":
            case "long":
            case "byte":
                return "0";
            case "double":
            case "float":
                return "0.0";
            case "char":
                return "'\\0'";
            default:
                return "null";
        }
    }

    private PsiParameter createPsiParameter(PsiType type, String name) {
        PsiParameter parameter = mock(PsiParameter.class);
        when(parameter.getType()).thenReturn(type);
        when(parameter.getName()).thenReturn(name);
        return parameter;
    }

    @SuppressWarnings("UnstableApiUsage")
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
        if (parameters == null)
            parameters = new PsiParameter[]{};
        String signature = this.generateSignature(method, parameters);
        MethodSignature methodSignature = mock(MethodSignature.class);
        when(methodSignature.toString()).thenReturn(signature);
        when(method.getSignature(PsiSubstitutor.EMPTY)).thenReturn(methodSignature);
        return method;
    }
    //endregion

    //region Инициализация перед тестом

    private void setupPrimitiveParameters() {
        // Настройка типов
        when(mockPsiTypeBoolean.getPresentableText()).thenReturn("boolean");
        when(mockPsiTypeByte.getPresentableText()).thenReturn("byte");
        when(mockPsiTypeChar.getPresentableText()).thenReturn("char");
        when(mockPsiTypeShort.getPresentableText()).thenReturn("short");
        when(mockPsiTypeInt.getPresentableText()).thenReturn("int");
        when(mockPsiTypeLong.getPresentableText()).thenReturn("long");
        when(mockPsiTypeFloat.getPresentableText()).thenReturn("float");
        when(mockPsiTypeDouble.getPresentableText()).thenReturn("double");
        when(mockPsiTypeString.getPresentableText()).thenReturn("String");

        // Создание параметров с помощью вспомогательного метода
        mockPsiParameterBoolean = createPsiParameter(mockPsiTypeBoolean, "flag");
        mockPsiParameterByte = createPsiParameter(mockPsiTypeByte, "b");
        mockPsiParameterChar = createPsiParameter(mockPsiTypeChar, "c");
        mockPsiParameterShort = createPsiParameter(mockPsiTypeShort, "s");
        mockPsiParameterInt = createPsiParameter(mockPsiTypeInt, "i");
        mockPsiParameterLong = createPsiParameter(mockPsiTypeLong, "l");
        mockPsiParameterFloat = createPsiParameter(mockPsiTypeFloat, "f");
        mockPsiParameterDouble = createPsiParameter(mockPsiTypeDouble, "d");
        mockPsiParameterString = createPsiParameter(mockPsiTypeString, "str");
    }

    private void setUpConstructor() {
        PsiParameter[] parameters = {
                createPsiParameter(mockPsiTypeString, "brand"),
                createPsiParameter(mockPsiTypeString, "model"),
                createPsiParameter(mockPsiTypeInt, "year"),
                createPsiParameter(mockPsiTypeDouble, "price")
        };
        mockConstructor = createPsiMethod(null, "Car", parameters);
    }

    @SuppressWarnings({"UnstableApiUsage", "deprecation"})
    private void setupVoidMethod() {
        PsiParameter[] parameters = {
                mockPsiParameterString, mockPsiParameterInt, mockPsiParameterBoolean, mockPsiParameterByte,
                mockPsiParameterChar, mockPsiParameterShort, mockPsiParameterLong, mockPsiParameterFloat, mockPsiParameterDouble
        };
        mockVoidMethod = createPsiMethod(PsiType.VOID, "voidMethod", parameters);
    }

    private void setupReturnMethod() {
        // Подготовка всех параметров
        PsiParameter[] parameters = {
                mockPsiParameterString, mockPsiParameterInt, mockPsiParameterBoolean, mockPsiParameterByte,
                mockPsiParameterChar, mockPsiParameterShort, mockPsiParameterLong, mockPsiParameterFloat, mockPsiParameterDouble
        };
        mockReturnMethod = createPsiMethod(mockPsiTypeBoolean, "returnMethod", parameters);
    }

    private void setupNoParamMethod() {
        mockNoParamMethod = createPsiMethod(mockPsiTypeString, "noParamMethod", null);
    }

    @SuppressWarnings("resource")
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        generator = new UnitTestsGenerator(true);

        //region Настройка приложения для JavaDirectoryService
        Application mockApplication = mock(Application.class);
        mockStatic(ApplicationManager.class);
        when(ApplicationManager.getApplication()).thenReturn(mockApplication);
        when(mockApplication.getService(JavaDirectoryService.class)).thenReturn(mockJavaDirectoryService);
        //endregion
        //region Настройка моков для PsiClass
        when(mockPsiClass.getName()).thenReturn("Car");
        when(mockPsiClass.getContainingFile()).thenReturn(mockPsiFile);
        //endregion
        //region Настройка моков для PsiFile
        when(mockPsiFile.getContainingDirectory()).thenReturn(mockPsiDirectory);
        //endregion
        //region Подмена статического метода для получения экземпляра JavaDirectoryService
        // Предполагается, что JavaDirectoryService можно мокировать или подменять другим способом, например, через PowerMock
        JavaDirectoryService instance = JavaDirectoryService.getInstance();
        when(instance.getPackage(mockPsiDirectory)).thenReturn(mockPsiPackage);
        //endregion
        //region Настройка моков для PsiPackage
        when(mockPsiPackage.getQualifiedName()).thenReturn("com.example.package");
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
    //endregion

    //region Тесты
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
        PsiMethod methods[] = {mockConstructor, mockVoidMethod, mockReturnMethod, mockNoParamMethod};
        String expectedInfoStrs[] = {
                "Название: Car\n" +
                "Возвращаемый тип: void\n" +
                "Сигнатура: Car([PsiType:String, PsiType:String, PsiType:int, PsiType:double])\n" +
                "Параметры: String brand, String model, int year, double price",

                "Название: voidMethod\n" +
                "Возвращаемый тип: void\n" +
                "Сигнатура: voidMethod([PsiType:String, PsiType:int, PsiType:boolean, PsiType:byte, PsiType:char, PsiType:short, PsiType:long, PsiType:float, PsiType:double])\n" +
                "Параметры: String str, int i, boolean flag, byte b, char c, short s, long l, float f, double d",

                "Название: returnMethod\n" +
                "Возвращаемый тип: boolean\n" +
                "Сигнатура: returnMethod([PsiType:String, PsiType:int, PsiType:boolean, PsiType:byte, PsiType:char, PsiType:short, PsiType:long, PsiType:float, PsiType:double])\n" +
                "Параметры: String str, int i, boolean flag, byte b, char c, short s, long l, float f, double d",

                "Название: noParamMethod\n" +
                "Возвращаемый тип: String\n" +
                "Сигнатура: noParamMethod([])\n" +
                "Параметры: Нет параметров"
        };
        assertEquals(methods.length, expectedInfoStrs.length);
        for (int i = 0; i < methods.length; i++) {
            String infoStr = generator.getInfo(methods[i]);
            System.out.println(infoStr);
            System.out.println();
            Assertions.assertEquals(expectedInfoStrs[i], infoStr);
        }
    }

    @Test
    void testGetClassHeader() {
        String psiClassHeader = this.generator.getClassHeader(this.mockPsiClass, TestType.PARAMETERIZED);
        System.out.println(psiClassHeader);
    }

    @Test
    void testGenerateMethod_UnitTest() {
        this.generator.setDebug(true);
        String parameterizedTestStr = generator.generate(mockVoidMethod, TestType.PARAMETERIZED);
        System.out.println(parameterizedTestStr);
    }

    @Test
    void testGenerateMethod_ParameterizedTest() {
        this.generator.setDebug(true);
        String methodImplementationStr = generateJavaMethod(mockVoidMethod);
        String parameterizedTestStr = generator.generate(mockVoidMethod, TestType.PARAMETERIZED);
        System.out.println(methodImplementationStr);
        System.out.println(parameterizedTestStr);

        StringBuilder stringBuilder = new StringBuilder();

        //TODO: добавить импорты, обернуть в класс как в файле Образец_теста
//        PsiFile psiFile = mockVoidMethod.getContainingFile();
//        PsiDirectory psiDirectory = psiFile.getContainingDirectory();
//
//        VirtualFile virtualFile = psiDirectory.getVirtualFile();
//        String packagePath = virtualFile.getPath();
//
//        System.out.println(packagePath);
        stringBuilder.append(this.generator.getClassHeader(mockPsiClass, TestType.PARAMETERIZED));
        // Абсолютный путь до папки
        String basePath = "src\\test\\java\\org\\dasxunya\\diploma\\generator\\actualTests";
        String fileName = "Parameterized" + mockVoidMethod.getName(); // Например, ParameterizedVoidMethod

        // Сохранение содержимого в файл

        stringBuilder.append(methodImplementationStr).append("\n");

        stringBuilder.append(methodImplementationStr).append("\n");
        stringBuilder.append(parameterizedTestStr).append("\n");
        saveTestToFile(parameterizedTestStr, basePath, fileName);
        //TODO: добавить проверки сгенерированного теста
        //deleteFile(basePath, fileName);
    }
    //endregion
}
