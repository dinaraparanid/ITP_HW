import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public final class Main {

    /** Class to read and parse data from {@link System#in} */

    private static final class Console {

        /**
         * Instance of a singleton-pattern class.
         * Initializes lazily
         */

        private static volatile Console instance;

        /**
         * Instance of a {@link Main} class
         * that provides IO/Parse operations
         */

        private final Main mainInstance = new Main();

        /**
         * Gets console's instance with monitor's protection.
         * If instance wasn't used before, it will be initialized
         * @return instance of {@link Console} synchronously
         */

        public static synchronized Console getInstance() {
            if (instance == null)
                instance = new Console();
            return instance;
        }

        /**
         * Reads and parses {@link CalculatorType}
         * @throws IOException If an I/O error occurs
         * @return parsed type or {@link CalculatorType#INCORRECT}
         */

        private CalculatorType readCalculator() throws IOException {
            return mainInstance.readCalculator();
        }

        /**
         * Reads and parses command number
         * @throws NumberFormatException if number is not in [1..50] range
         * @throws IOException If an I/O error occurs
         * @return parsed command (number in [1..50])
         */

        private int readCommandsNumber() throws NumberFormatException, IOException {
            return mainInstance.readCommandsNumber();
        }

        /**
         * Prints error that stops execution of program
         * @param err error itself
         */

        private void reportFatalError(final String err) {
            mainInstance.reportFatalError(err);
        }

        /**
         * Prints warning to console, that doesn't stop program's execution
         * @param warning warning itself
         */

        private void reportWarning(final String warning) {
            System.out.println(warning);
        }

        /**
         * Parses operation or throws error if parsing wasn't successful
         * @param operation string to parse
         * @return either {@link OperationType#ADDITION},
         * {@link OperationType#SUBTRACTION},
         * {@link OperationType#MULTIPLICATION},
         * or {@link OperationType#DIVISION}
         * @throws IllegalArgumentException if operation is not '+', or '-', or '*', or '/'
         */

        private OperationType parseOperation(final String operation) throws IllegalArgumentException {
            return mainInstance.parseOperation(operation);
        }
    }

    private static final String INTEGER = "INTEGER";
    private static final String DOUBLE = "DOUBLE";
    private static final String STRING = "STRING";

    /**
     * @deprecated Provides slow runtime and weak error handling.
     * Also, forces client to use default {@link java.nio.charset.Charset}.
     * Moreover, its imperative design limits any optimizations.
     * Use {@link BufferedReader} instead
     */

    @Deprecated
    private static Scanner scanner = new Scanner(System.in);

    private static final BufferedReader reader = new BufferedReader(
            new InputStreamReader(System.in, StandardCharsets.UTF_8)
    );

    /**
     * Reads and parses {@link CalculatorType}
     * @throws IOException If an I/O error occurs
     * @return parsed type or {@link CalculatorType#INCORRECT}
     * @see Console#readCalculator()
     */

    private CalculatorType readCalculator() throws IOException {
        final String input = reader.readLine();
        if (input.equals(INTEGER)) return CalculatorType.INTEGER;
        if (input.equals(DOUBLE)) return CalculatorType.DOUBLE;
        if (input.equals(STRING)) return CalculatorType.STRING;
        return CalculatorType.INCORRECT;
    }

    /**
     * Reads and parses command number
     * @throws NumberFormatException if number is not in [1..50] range
     * @throws IOException If an I/O error occurs
     * @return parsed command (number in [1..50])
     * @see Console#readCommandsNumber()
     */

    private int readCommandsNumber() throws NumberFormatException, IOException {
        try {
            final int commands = Integer.parseUnsignedInt(reader.readLine());
            if (commands >= 1 && commands <= 50) return commands;
            throw new NumberFormatException();
        } catch (final NumberFormatException ignore) {
            throw new NumberFormatException("Amount of commands is Not a Number");
        }
    }

    /**
     * Prints error that stops execution of program
     * @param err error itself
     * @see Console#reportFatalError(String) 
     */
    
    private void reportFatalError(final String err) { System.out.println(err); }

    /**
     * Parses operation or throws error if parsing wasn't successful
     * @param operation char to parse
     * @return parsed {@link OperationType} or {@link OperationType#INCORRECT}
     * if operation doesn't match '+', or '-', or '*', or '/'
     * @see Console#parseOperation(String)
     */
    
    private OperationType parseOperation(final char operation) {
        switch (operation) {
            case '+': return OperationType.ADDITION;
            case '-': return OperationType.SUBTRACTION;
            case '*': return OperationType.MULTIPLICATION;
            case '/': return OperationType.DIVISION;
            default: return OperationType.INCORRECT;
        }
    }

    /**
     * Parses operation or throws error if parsing wasn't successful
     * @param operation string to parse
     * @return either {@link OperationType#ADDITION},
     * {@link OperationType#SUBTRACTION},
     * {@link OperationType#MULTIPLICATION},
     * or {@link OperationType#DIVISION}
     * @throws IllegalArgumentException if operation is not '+', or '-', or '*', or '/'
     * @see Console#parseOperation(String)
     */

    private OperationType parseOperation(final String operation) throws IllegalArgumentException {
        if (operation.length() != 1) throw new IllegalArgumentException("Wrong operation type");
        return parseOperation(operation.charAt(0));
    }

    public static void main(final String[] args) throws IOException {
        try {
            // Initializing calculator or throwing error
            Calculator.init(Console.getInstance().readCalculator());

            // Parsing command or throwing error
            int commands = Console.getInstance().readCommandsNumber();

            while (commands-- > 0) try {

                // Reading and parsing command

                final String[] input = reader.readLine().split(" ");

                if (input.length != 3)
                    throw new IllegalArgumentException("Wrong argument type");

                // Executing command and printing result or throwing error

                System.out.println(
                        Calculator.getInstance().calculate(
                                new Operation(
                                        input[1],
                                        input[2],
                                        Console.getInstance().parseOperation(input[0])
                                )
                        )
                );
            } catch (final Exception warning) {
                // Reporting warning and continuing with other commands
                Console.getInstance().reportWarning(warning.getMessage());
            }
        } catch (final Exception fatalError) {
            // Reporting fatal error and finishing execution
            Console.getInstance().reportFatalError(fatalError.getMessage());
        } finally {
            // Closing all readers after execution
            reader.close();
            scanner.close();
        }
    }
}

enum CalculatorType { INTEGER, DOUBLE, STRING, INCORRECT }

enum OperationType { ADDITION, SUBTRACTION, MULTIPLICATION, DIVISION, INCORRECT }

/**
 * Operation struct with two args and type of operation.
 * While args allowed to be invalid,
 * {@link Operation#type} should always be correct
 */

final class Operation {
    final String arg1;
    final String arg2;
    final OperationType type;

    /**
     * @param arg1 first argument of operation
     * @param arg2 second argument of operation
     * @param type correct {@link OperationType}
     * @throws IllegalArgumentException if type is {@link OperationType#INCORRECT}
     */

    Operation(
            final String arg1,
            final String arg2,
            final OperationType type
    ) throws IllegalArgumentException {
        if (type == OperationType.INCORRECT)
            throw new IllegalArgumentException("Wrong operation type");

        this.arg1 = arg1;
        this.arg2 = arg2;
        this.type = type;
    }
}

/** Class that handles all calculations */

abstract class Calculator {

    /**
     * Instance of a singleton-pattern class.
     * Should be initialized with {@link Calculator#init(CalculatorType)}
     */

    private static volatile Calculator instance;

    /**
     * Initializes calculator by given type with monitor's protection.
     * @param type type of calculator
     * @throws IllegalArgumentException if type is {@link CalculatorType#INCORRECT}
     */

    public static synchronized void init(final CalculatorType type) throws IllegalArgumentException {
        switch (type) {
            case INTEGER:
                instance = new IntegerCalculator();
                break;

            case DOUBLE:
                instance = new DoubleCalculator();
                break;

            case STRING:
                instance = new StringCalculator();
                break;

            default: throw new IllegalArgumentException("Wrong calculator type");
        }
    }

    /**
     * Gets previously initialized calculator's instance with monitor's protection.
     * @return instance of {@link Calculator} synchronously
     * @throws IllegalStateException if {@link Calculator#init(CalculatorType)} wasn't called before
     */

    public static synchronized Calculator getInstance() throws IllegalStateException {
        if (instance == null)
            throw new IllegalStateException("Calculator is not initialized");
        return instance;
    }

    /**
     * Either concatenates two strings or sums two numbers.
     * @param a first arg (either {@link Integer}, {@link Double} or {@link String})
     * @param b second arg (either {@link Integer}, {@link Double} or {@link String})
     * @return a + b if args are numbers or 'ab'
     */

    public abstract String add(String a, String b);

    /**
     * Subtracts two numbers. Not supported for strings
     * @param a first arg (either {@link Integer}, {@link Double} or {@link String})
     * @param b first arg (either {@link Integer}, {@link Double} or {@link String})
     * @return a - b
     */

    public abstract String subtract(String a, String b);

    /**
     * Either repeat 'a' b times (if b is number) or multiplies two number.
     * @param a first arg (either {@link Integer}, {@link Double} or {@link String})
     * @param b second arg (either {@link Integer}, {@link Double} or {@link String})
     * @return a * b if args are numbers or 'a'.repeat(b)
     */

    public abstract String multiply(String a, String b);

    /**
     * Divides two numbers. Not supported for strings
     * @param a first arg (either {@link Integer}, {@link Double} or {@link String})
     * @param b second arg (either {@link Integer}, {@link Double} or {@link String})
     * @return a / b if b != 0
     * @throws ArithmeticException if b == 0
     */

    public abstract String divide(String a, String b) throws ArithmeticException;

    /**
     * Executes given operation
     * @param operation {@link Operation} to execute
     * @return result as string to print
     * @throws IllegalArgumentException operation type is {@link OperationType#INCORRECT}
     * @throws IllegalStateException not supported for strings
     * @throws ArithmeticException division by zero
     * @throws NumberFormatException args of operation cannot be parsed
     */

    public final String calculate(final Operation operation)
            throws IllegalArgumentException, IllegalStateException, ArithmeticException, NumberFormatException {
        switch (operation.type) {
            case ADDITION: return add(operation.arg1, operation.arg2);
            case SUBTRACTION: return subtract(operation.arg1, operation.arg2);
            case MULTIPLICATION: return multiply(operation.arg1, operation.arg2);
            case DIVISION: return divide(operation.arg1, operation.arg2);
            default: throw new IllegalArgumentException("Wrong operation type");
        }
    }

    /** {@link Calculator} for integers */

    private static final class IntegerCalculator extends Calculator {

        /**
         * Parses integer from string
         * @param s string to parse
         * @return parsed integer
         * @throws NumberFormatException parse error
         */

        private static int Int(final String s) throws NumberFormatException {
            try {
                return Integer.parseInt(s);
            } catch (final NumberFormatException ignore) {
                throw new NumberFormatException("Wrong argument type");
            }
        }

        /**
         * Parses args and returns a + b
         * @param a first arg (must be {@link Integer})
         * @param b second arg (must be {@link Integer})
         * @return a + b as string
         * @throws NumberFormatException parse error
         */

        @Override
        public String add(final String a, final String b) throws NumberFormatException {
            return Integer.toString(Int(a) + Int(b));
        }

        /**
         * Parses args and returns a - b
         * @param a first arg (must be {@link Integer})
         * @param b first arg (must be {@link Integer})
         * @return a - b as string
         * @throws NumberFormatException parse error
         */

        @Override
        public String subtract(final String a, final String b) throws NumberFormatException {
            return Integer.toString(Int(a) - Int(b));
        }

        /**
         * Parses args and returns a * b
         * @param a first arg (must be {@link Integer})
         * @param b second arg (must be {@link Integer})
         * @return a * b as string
         * @throws NumberFormatException parse error
         */

        @Override
        public String multiply(final String a, final String b) throws NumberFormatException {
            return Integer.toString(Int(a) * Int(b));
        }

        /**
         * Parses args and returns a / b
         * @param a first arg (must be {@link Integer})
         * @param b second arg (must be {@link Integer})
         * @return a / b
         * @throws ArithmeticException b == 0
         * @throws NumberFormatException parse error
         */

        @Override
        public String divide(final String a, final String b) throws ArithmeticException, NumberFormatException {
            try {
                return Integer.toString(Int(a) / Int(b));
            } catch (final ArithmeticException ignore) {
                throw new ArithmeticException("Division by zero");
            }
        }
    }

    /** {@link Calculator} for doubles */

    private static final class DoubleCalculator extends Calculator {

        /**
         * Parses double from string
         * @param s string to parse
         * @return parsed double
         * @throws NumberFormatException parse error
         */

        private static double Decimal(final String s) throws NumberFormatException {
            try {
                return Double.parseDouble(s);
            } catch (final NumberFormatException ignore) {
                throw new NumberFormatException("Wrong argument type");
            }
        }

        /**
         * Parses args and returns a + b
         * @param a first arg (must be {@link Double})
         * @param b second arg (must be {@link Double})
         * @return a + b as string
         * @throws NumberFormatException parse error
         */

        @Override
        public String add(final String a, final String b) throws NumberFormatException {
            return Double.toString(Decimal(a) + Decimal(b));
        }

        /**
         * Parses args and returns a - b
         * @param a first arg (must be {@link Double})
         * @param b first arg (must be {@link Double})
         * @return a - b as string
         * @throws NumberFormatException parse error
         */

        @Override
        public String subtract(final String a, final String b) throws NumberFormatException {
            return Double.toString(Decimal(a) - Decimal(b));
        }

        /**
         * Parses args and returns a * b
         * @param a first arg (must be {@link Double})
         * @param b second arg (must be {@link Double})
         * @return a * b as string
         * @throws NumberFormatException parse error
         */

        @Override
        public String multiply(final String a, final String b) throws NumberFormatException {
            return Double.toString(Decimal(a) * Decimal(b));
        }

        /**
         * Parses args and returns a / b
         * @param a first arg (must be {@link Double})
         * @param b second arg (must be {@link Double})
         * @return a / b
         * @throws ArithmeticException b == 0
         * @throws NumberFormatException parse error
         */

        @Override
        public String divide(final String a, final String b) throws ArithmeticException, NumberFormatException {
            try {
                final double second = Decimal(b);
                if (second == 0) throw new ArithmeticException();
                return Double.toString(Decimal(a) / second);
            } catch (final ArithmeticException ignore) {
                throw new ArithmeticException("Division by zero");
            }
        }
    }

    /** {@link Calculator} for strings */

    private static final class StringCalculator extends Calculator {

        /**
         * Concatenates two strings
         * @param a first arg
         * @param b second arg
         * @return 'ab'
         */

        @Override
        public String add(final String a, final String b) {
            return a + b;
        }

        /**
         * Repeats string n times
         * @param s string to repeat
         * @param times times to repeat string
         * @return 's'.repeat(times)
         * @throws IllegalArgumentException times < 0
         */

        private static String repeat(final String s, final int times) throws IllegalArgumentException {
            if (times < 0) throw new IllegalArgumentException("Times must be natural");
            final StringBuilder builder = new StringBuilder(s.length() * times);
            for (int i = 0; i < times; ++i) builder.append(s);
            return builder.toString();
        }

        /** @deprecated  not supported for strings */

        @Override
        @Deprecated
        public String subtract(final String a, final String b) throws IllegalStateException {
            throw new IllegalStateException("Unsupported operation for strings");
        }

        /**
         * Repeats a b times.
         * @param a string to repeat
         * @param b times to repeat (must be {@link  Integer})
         * @return 'a'.repeat(b)
         * @throws NumberFormatException b isn't number
         * @see StringCalculator#repeat(String, int)
         */

        @Override
        public String multiply(final String a, final String b) throws NumberFormatException {
            try {
                return repeat(a, Integer.parseUnsignedInt(b));
            } catch (final NumberFormatException ignore) {
                throw new NumberFormatException("Wrong argument type");
            }
        }

        /** @deprecated  not supported for strings */

        @Override
        @Deprecated
        public String divide(final String a, final String b) throws IllegalStateException {
            throw new IllegalStateException("Unsupported operation for strings");
        }
    }
}
