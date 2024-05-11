package org.dasxunya.diploma.generator;

import com.intellij.psi.*;
import org.dasxunya.diploma.constants.Constants;
import org.dasxunya.diploma.constants.TestType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class UnitTestGeneratorTests {

    //region Поля
    UnitTestsGenerator generator;
    @Mock
    private PsiClass mockPsiClass;
    @Mock
    private PsiMethod mockPsiMethod;
    @Mock
    private PsiParameterList mockPsiParameterList;
    @Mock
    private PsiParameter mockPsiParameter;
    @Mock
    private PsiType mockPsiTypeString;
    @Mock
    private PsiType mockPsiTypeInt;
    @Mock
    private PsiType mockPsiTypeDouble;
    //endregion

    //region Методы
    void print(Object message) {
        System.out.println(message);
    }
    //endregion

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        generator = new UnitTestsGenerator(true);

        when(mockPsiClass.getName()).thenReturn("CarName");
        when(mockPsiClass.getQualifiedName()).thenReturn("CarQualifiedName");

        when(mockPsiMethod.getParameterList()).thenReturn(mockPsiParameterList);
        when(mockPsiParameterList.getParameters()).thenReturn(new PsiParameter[]{
                mockPsiParameter, mockPsiParameter, mockPsiParameter, mockPsiParameter
        });
        when(mockPsiParameter.getType()).thenReturn(mockPsiTypeString, mockPsiTypeString, mockPsiTypeInt, mockPsiTypeDouble);
        when(mockPsiTypeString.getPresentableText()).thenReturn("String");
        when(mockPsiTypeInt.getPresentableText()).thenReturn("int");
        when(mockPsiTypeDouble.getPresentableText()).thenReturn("double");
        when(mockPsiParameter.getName()).thenReturn("brand", "model", "year", "price");

        when(mockPsiMethod.getName()).thenReturn("Car");
        when(mockPsiMethod.getReturnType()).thenReturn(null);

        // Настройка мокирования для метода getSignature, генерация строки
        String signature = "Car([PsiType:String, PsiType:String, PsiType:int, PsiType:double])";
        when(mockPsiMethod.toString()).thenReturn(signature);
    }

    //region Тесты

    //region Генерация тестов для методов




    //endregion

    //region Генерация тестов для классов
    @SuppressWarnings("ConstantValue")
    @Test
    void testGenerate_NullClass() {
        Exception exception = assertThrows(NullPointerException.class, () -> {
            PsiClass testClass = null;
            generator.generate(testClass, TestType.UNIT);
        });
        // Проверяем, что сообщение исключения соответствует ожидаемому
        assertEquals(Constants.Strings.Release.Errors.NULL_POINTER, exception.getMessage());

        exception = assertThrows(NullPointerException.class, () -> {
            PsiClass testClass = null;
            generator.generate(testClass, TestType.PARAMETERIZED);
        });

        // Проверяем, что сообщение исключения соответствует ожидаемому
        assertEquals(Constants.Strings.Release.Errors.NULL_POINTER, exception.getMessage());
    }

    @Test
    void testGenerateClass_SpecificMethod_ParameterizedTest() {
        this.generator.setDebug(false);
        String parameterizedTestStr = generator.generate(mockPsiClass, mockPsiMethod, TestType.PARAMETERIZED);
        System.out.println(parameterizedTestStr);
    }
    //endregion

    @Test
    void testGenerateWithPsiClass() {
        // Подготовка
        when(mockPsiClass.getAllMethods()).thenReturn(new PsiMethod[]{mockPsiMethod});
        //when(generator.generate(mockPsiClass)).thenCallRealMethod(); // Вызов реального метода

        // Действие
        String result = generator.generate(mockPsiClass, TestType.UNIT);
        print(result);
        // Проверка
        assertNotNull(result);
        assertTrue(result.contains("Car"));
        assertTrue(result.contains("[PsiType:String, PsiType:String, PsiType:int, PsiType:double]"));
    }
    //endregion

}
