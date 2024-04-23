package org.dasxunya.diploma.generator;

import com.intellij.psi.*;
import com.intellij.psi.util.MethodSignature;
import org.dasxunya.diploma.generator.sampleTestClasses.Car;
import org.junit.jupiter.api.Assertions;
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
    void Print(Object message) {
        System.out.println(message);
    }
    //endregion

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        generator = new UnitTestsGenerator();

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
    @Test
    void testGenerateWithPsiClass() {
        // Подготовка
        when(mockPsiClass.getAllMethods()).thenReturn(new PsiMethod[]{mockPsiMethod});
        //when(generator.generate(mockPsiClass)).thenCallRealMethod(); // Вызов реального метода

        // Действие
        String result = generator.generate(mockPsiClass, TestType.UNIT);
        Print(result);
        // Проверка
        assertNotNull(result);
        assertTrue(result.contains("Car"));
        assertTrue(result.contains("[PsiType:String, PsiType:String, PsiType:int, PsiType:double]"));
    }
    //endregion

}
