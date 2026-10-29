package io.console;

/**
 * Обработчик пользовательского ввода с валидацией и повтором при ошибке.
 *
 * <p>Каждый метод выводит приглашение, читает строку и пытается её
 * преобразовать в нужный тип. При ошибке — показывает сообщение и
 * просит ввести значение снова. Цикл продолжается до успешного ввода.</p>
 *
 * @author Hamid
 * @version 1.0
 */
public class InputHandler {

    /** Консоль для ввода и вывода приглашений. */
    private final Console console;

    /**
     * Создаёт обработчик с заданной консолью.
     *
     * @param console консоль для ввода/вывода
     */
    public InputHandler(Console console) {
        this.console = console;
    }

    /**
     * Читает непустую строку.
     *
     * <p>Повторяет запрос пока пользователь не введёт хотя бы один символ.</p>
     *
     * @param prompt приглашение к вводу
     * @return непустая строка
     */
    public String readNonEmpty(String prompt) {
        while (true) {
            console.print(prompt + ": ");
            String value = console.readLine();
            if (value == null) continue;
            value = value.trim();
            if (!value.isBlank()) return value;
            console.error("Строка не может быть пустой. Попробуйте снова.");
        }
    }

    /**
     * Читает строку, допуская пустое значение как {@code null}.
     *
     * <p>Если пользователь нажмёт Enter — возвращает {@code null}.</p>
     *
     * @param prompt приглашение к вводу
     * @return строка или {@code null} при пустом вводе
     */
    public String readNullableString(String prompt) {
        console.print(prompt + " (Enter = пропустить): ");
        String value = console.readLine();
        if (value == null || value.isBlank()) return null;
        return value.trim();
    }

    /**
     * Читает число типа {@code Float}.
     *
     * <p>Повторяет запрос при нечисловом вводе.</p>
     *
     * @param prompt приглашение к вводу
     * @return введённое значение
     */
    public Float readFloat(String prompt) {
        while (true) {
            console.print(prompt + ": ");
            String value = console.readLine();
            if (value == null) continue;
            try {
                return Float.parseFloat(value.trim());
            } catch (NumberFormatException e) {
                console.error("Ожидается дробное число (например, 3.14). Попробуйте снова.");
            }
        }
    }

    /**
     * Читает {@code Float} со строгой нижней границей (не включительно).
     *
     * <p>Повторяет запрос если значение &lt;= minExclusive.</p>
     *
     * @param prompt      приглашение к вводу
     * @param minExclusive нижняя граница (не включительно)
     * @return значение строго больше minExclusive
     */
    public Float readFloatGreaterThan(String prompt, float minExclusive) {
        while (true) {
            Float value = readFloat(prompt + " (> " + minExclusive + ")");
            if (value > minExclusive) return value;
            console.error("Значение должно быть > " + minExclusive + ". Попробуйте снова.");
        }
    }

    /**
     * Читает число типа {@code Long}.
     *
     * <p>Повторяет запрос при нечисловом вводе.</p>
     *
     * @param prompt приглашение к вводу
     * @return введённое значение
     */
    public Long readLong(String prompt) {
        while (true) {
            console.print(prompt + ": ");
            String value = console.readLine();
            if (value == null) continue;
            try {
                return Long.parseLong(value.trim());
            } catch (NumberFormatException e) {
                console.error("Ожидается целое число. Попробуйте снова.");
            }
        }
    }

    /**
     * Читает {@code Long} со строгой нижней границей (не включительно).
     *
     * @param prompt       приглашение к вводу
     * @param minExclusive нижняя граница (не включительно)
     * @return значение строго больше minExclusive
     */
    public Long readLongGreaterThan(String prompt, long minExclusive) {
        while (true) {
            Long value = readLong(prompt + " (> " + minExclusive + ")");
            if (value > minExclusive) return value;
            console.error("Значение должно быть > " + minExclusive + ". Попробуйте снова.");
        }
    }

    /**
     * Читает {@code Long}, допуская пустой ввод как {@code null}.
     *
     * <p>Если пользователь нажмёт Enter — возвращает {@code null}.
     * При нечисловом непустом вводе — повторяет запрос.</p>
     *
     * @param prompt приглашение к вводу
     * @return число или {@code null}
     */
    public Long readLongNullable(String prompt) {
        while (true) {
            console.print(prompt + " (Enter = пропустить): ");
            String value = console.readLine();
            if (value == null || value.isBlank()) return null;
            try {
                return Long.parseLong(value.trim());
            } catch (NumberFormatException e) {
                console.error("Ожидается целое число или Enter. Попробуйте снова.");
            }
        }
    }

    /**
     * Читает {@code Long} nullable со строгой нижней границей.
     *
     * <p>Если значение задано — оно должно быть &gt; minExclusive.</p>
     *
     * @param prompt       приглашение к вводу
     * @param minExclusive нижняя граница (не включительно)
     * @return число больше minExclusive или {@code null}
     */
    public Long readLongNullableGreaterThan(String prompt, long minExclusive) {
        while (true) {
            Long value = readLongNullable(prompt + " (> " + minExclusive + " или Enter)");
            if (value == null) return null;
            if (value > minExclusive) return value;
            console.error("Значение должно быть > " + minExclusive + ". Попробуйте снова.");
        }
    }

    /**
     * Читает число типа {@code long} (примитив).
     *
     * @param prompt приглашение к вводу
     * @return введённое значение
     */
    public long readPrimitiveLong(String prompt) {
        return readLong(prompt);
    }

    /**
     * Читает {@code Double}, допуская пустой ввод как {@code null}.
     *
     * @param prompt приглашение к вводу
     * @return число или {@code null}
     */
    public Double readDoubleNullable(String prompt) {
        while (true) {
            console.print(prompt + " (Enter = пропустить): ");
            String value = console.readLine();
            if (value == null || value.isBlank()) return null;
            try {
                return Double.parseDouble(value.trim());
            } catch (NumberFormatException e) {
                console.error("Ожидается дробное число или Enter. Попробуйте снова.");
            }
        }
    }

    /**
     * Читает {@code Double} (обязательное поле).
     *
     * @param prompt приглашение к вводу
     * @return введённое значение
     */
    public Double readDouble(String prompt) {
        while (true) {
            console.print(prompt + ": ");
            String value = console.readLine();
            if (value == null) continue;
            try {
                return Double.parseDouble(value.trim());
            } catch (NumberFormatException e) {
                console.error("Ожидается дробное число. Попробуйте снова.");
            }
        }
    }

    /**
     * Читает подтверждение да/нет.
     *
     * <p>Принимает {@code y}, {@code yes}, {@code д}, {@code да} как истину.
     * Любой другой ввод — ложь. Пустая строка — ложь (нет).</p>
     *
     * @param prompt приглашение к вводу
     * @return {@code true} если пользователь подтвердил
     */
    public boolean readConfirm(String prompt) {
        while (true) {
            console.print(prompt + " (y/n): ");
            String value = console.readLine();

            if (value == null) return false;

            value = value.trim().toLowerCase();

            if (value.equals("y") || value.equals("yes")
                    || value.equals("д") || value.equals("да")) {
                return true;
            }

            if (value.equals("n") || value.equals("no")
                    || value.equals("н") || value.equals("нет")) {
                return false;
            }

            console.error("Введите y/n (да/нет)");
        }
    }

    /**
     * Возвращает консоль, используемую этим обработчиком.
     *
     * @return консоль
     */
    public Console getConsole() {
        return console;
    }
}