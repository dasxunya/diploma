package org.dasxunya.diploma.generator;

import org.dasxunya.diploma.constants.Constants;
import org.dasxunya.diploma.constants.TestType;
import org.junit.jupiter.api.Test;

public class PsiClassGeneratorTests extends BaseTest {

    @Test
    void testGenerateClassTests() {
        String psiClassTestStr = this.generator.generate(this.mockPsiClass, TestType.PARAMETERIZED);
        this.saveFile(psiClassTestStr, this.actualFolderPath, "testGenerateClassTests", Constants.Strings.Extensions.txt);
    }


}
