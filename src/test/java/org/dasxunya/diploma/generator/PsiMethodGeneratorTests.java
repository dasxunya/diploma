package org.dasxunya.diploma.generator;

import com.intellij.psi.*;
import com.intellij.psi.util.MethodSignature;
import org.dasxunya.diploma.constants.Constants;
import org.dasxunya.diploma.constants.TestType;
import org.junit.Assert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PsiMethodGeneratorTests {


    //region Поля
    UnitTestsGenerator generator;
    @Mock
    private PsiClass mockPsiClass;

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

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        generator = new UnitTestsGenerator(true);

        // Класс и конструктор
        when(mockPsiClass.getName()).thenReturn("Car");
        when(mockPsiClass.getQualifiedName()).thenReturn("com.example.Car");

        setupPrimitiveParameters();
        // Настройка для конструктора
        setUpConstructor();

        // Настройка для других методов
        setupVoidMethod();
        setupReturnMethod();
        setupNoParamMethod();
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
            Assertions.assertEquals(expectedInfoStrs[i],infoStr);
        }
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
        String parameterizedTestStr = generator.generate(mockVoidMethod, TestType.PARAMETERIZED);
        System.out.println(parameterizedTestStr);
    }
    //endregion
}
