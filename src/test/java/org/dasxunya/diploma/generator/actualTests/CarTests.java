package org.dasxunya.diploma.generator.actualTests;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.*;
import org.junit.jupiter.params.provider.*;
class CarTests
{
void voidMethod(String str, int i, boolean flag, byte b, char c, short s, long l, float f, double d) {
    // TODO: Реализация метода
}

@ParameterizedTest
@CsvSource({
    "exampleString, 0, true, 0, 'a', 0, 0, 0.0f, 0.0",
    "exampleString, 0, true, 0, 'a', 0, 0, 0.0f, 0.0"
})
public void testVoidMethod(String str, int i, boolean flag, byte b, char c, short s, long l, float f, double d) {
    // TODO: Тестируемая логика
	//Assertions.assertNotNull(voidMethod(str, i, flag, b, c, s, l, f, d));
    // TODO: добавить другие утверждения
}

}
