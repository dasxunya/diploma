package org.dasxunya.diploma.constants;

public class Constants {
    public static class Strings {
        public static class Debug {
            public static class Errors {
                public static final String NULL_POINTER = "Передана пустая ссылка на объект, ожидаемый тип:";
                public static final String ILLEGAL_ARGUMENT = "Передан неподдерживаемый тип элемента, ожидаемый тип:";
            }
        }

        public static class Release {
            public static class Errors {
                public static final String NULL_POINTER = "Передана пустая ссылка на объект";
                public static final String ILLEGAL_ARGUMENT = "Передан неподдерживаемый тип элемента";
            }
        }

    }
}
