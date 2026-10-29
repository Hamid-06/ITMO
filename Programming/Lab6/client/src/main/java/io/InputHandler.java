package io;

public class InputHandler {

    private final Console console;

    public InputHandler(Console console) {
        this.console = console;
    }

    public String readNonEmpty(String prompt) {
        while (true) {
            console.print(prompt + ": ");
            String val = console.readLine();
            if (val == null) continue;
            val = val.trim();
            if (!val.isBlank()) return val;
            console.error("Value cannot be empty. Try again.");
        }
    }

    public String readNullableString(String prompt) {
        console.print(prompt + " (Enter = skip): ");
        String val = console.readLine();
        if (val == null || val.isBlank()) return null;
        return val.trim();
    }

    public Float readFloat(String prompt) {
        while (true) {
            console.print(prompt + ": ");
            String val = console.readLine();
            if (val == null) continue;
            try {
                return Float.parseFloat(val.trim());
            } catch (NumberFormatException e) {
                console.error("Expected a decimal number (e.g. 3.14). Try again.");
            }
        }
    }

    public Float readFloatGreaterThan(String prompt, float minExclusive) {
        while (true) {
            Float val = readFloat(prompt + " (> " + minExclusive + ")");
            if (val > minExclusive) return val;
            console.error("Value must be > " + minExclusive + ". Try again.");
        }
    }

    public Long readLong(String prompt) {
        while (true) {
            console.print(prompt + ": ");
            String val = console.readLine();
            if (val == null) continue;
            try {
                return Long.parseLong(val.trim());
            } catch (NumberFormatException e) {
                console.error("Expected an integer. Try again.");
            }
        }
    }

    public long readPrimitiveLong(String prompt) {
        return readLong(prompt);
    }

    public Long readLongGreaterThan(String prompt, long minExclusive) {
        while (true) {
            Long val = readLong(prompt + " (> " + minExclusive + ")");
            if (val > minExclusive) return val;
            console.error("Value must be > " + minExclusive + ". Try again.");
        }
    }

    public Long readLongNullable(String prompt) {
        while (true) {
            console.print(prompt + " (Enter = skip): ");
            String val = console.readLine();
            if (val == null || val.isBlank()) return null;
            try {
                return Long.parseLong(val.trim());
            } catch (NumberFormatException e) {
                console.error("Expected an integer or Enter. Try again.");
            }
        }
    }

    public Long readLongNullableGreaterThan(String prompt, long minExclusive) {
        while (true) {
            Long val = readLongNullable(prompt + " (> " + minExclusive + " or Enter)");
            if (val == null) return null;
            if (val > minExclusive) return val;
            console.error("Value must be > " + minExclusive + ". Try again.");
        }
    }

    public Double readDouble(String prompt) {
        while (true) {
            console.print(prompt + ": ");
            String val = console.readLine();
            if (val == null) continue;
            try {
                return Double.parseDouble(val.trim());
            } catch (NumberFormatException e) {
                console.error("Expected a decimal number. Try again.");
            }
        }
    }

    public boolean readConfirm(String prompt) {
        console.print(prompt + " (y/n): ");
        String val = console.readLine();
        if (val == null) return false;
        val = val.trim().toLowerCase();
        return val.equals("y") || val.equals("yes") || val.equals("д") || val.equals("да");
    }

    public Console getConsole() { return console; }
}