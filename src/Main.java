import java.io.*;
import java.math.BigInteger;
import java.util.*;

public class Main {
    private final static Deque<BigInteger> deque = new ArrayDeque<>();
    private final static Stack<Integer> stack = new Stack<>();
    private final static Scanner stdin = new Scanner(System.in);
    private static final int NOP = 10000;
    private static final int INPUT = 10001;
    private static final int ROT = 10010;
    private static final int SWAP = 10011;
    private static final int PUSH = 10012;
    private static final int RROT = 10100;
    private static final int DUP = 10101;
    private static final int ADD = 10102;
    private static final int LBRACE = 10110;
    private static final int OUTPUT = 10111;
    private static final int MULTIPLY = 10112;
    private static final int EXECUTE = 10120;
    private static final int NEGATE = 10121;
    private static final int POP = 10122;
    private static final int RBRACE = 10123;
    private static Integer base;

    public static void main(String[] args) {

        File inFile = new File(args[0]);
        base = args.length > 1 ? Integer.parseInt(args[1]) : 10;
        BufferedReader br = null;
        int index = 0, count = 0, result = 0;

        try {
            String sCurrentLine;
            br = new BufferedReader(new FileReader(inFile));
            while ((sCurrentLine = br.readLine()) != null) {
                checkForErrors(sCurrentLine);
                checkValidParantheses(sCurrentLine);

                for (int i = 0; i < sCurrentLine.length(); i = i + 4, index = i/4) {
                    result = compute(sCurrentLine, i);
                    checkForExceptions(result, index);

                    if (result == LBRACE) {
                        if (deque.getLast().equals(BigInteger.valueOf(0))) {
                           for(; i < sCurrentLine.length(); i = i+4) {
                               if (compute(sCurrentLine, i) == LBRACE) {
                                   count++;
                               } else if (compute(sCurrentLine, i) == RBRACE) {
                                   count--;
                               }
                               if (count == 0) {
                                   break;
                               }
                           }
                        } else {
                            stack.push(i-4);
                        }
                    } else if (result == RBRACE) {
                        if (stack.size() > 0) {
                            if (deque.size() == 0) {
                                i = stack.pop();
                            } else if (!deque.getLast().equals(BigInteger.valueOf(0))) {
                                i = stack.pop();
                            } else {
                                stack.pop();
                            }
                        }
                    }
                    executeCommands(result, index);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (br != null)br.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    /**
     * interpret GLYPHO commands
     * @param commandType Add,multiply,rot,push,pop and so on..
     * @param index index of the command
     */
    private static void executeCommands(int commandType, int index) {
         if (commandType == INPUT) {
            String line = stdin.nextLine();

            BigInteger number = BigInteger.valueOf(0);
            try {
                number = new BigInteger(line, base);
            } catch (Exception e) {
                System.err.println("Exception:" + index);
                System.exit(254);
            }
            deque.addLast(number);
        } else if (commandType == ROT) {
            deque.addFirst(deque.removeLast());
        } else if (commandType == SWAP) {
            BigInteger num1 = deque.removeLast();
            BigInteger num2 = deque.removeLast();
            deque.addLast(num1);
            deque.addLast(num2);
        } else if (commandType == PUSH) {
            deque.addLast(BigInteger.valueOf(1));
        } else if (commandType == RROT) {
            deque.addLast(deque.removeFirst());
        } else if (commandType == DUP) {
            deque.addLast(deque.getLast());
        } else if (commandType == ADD) {
            BigInteger num1 = deque.removeLast();
            BigInteger num2 = deque.removeLast();
            deque.addLast(num1.add(num2));
        } else if (commandType == OUTPUT) {
            System.out.println(deque.removeLast().toString(base).toUpperCase());
        } else if (commandType == MULTIPLY) {
            BigInteger num1 = deque.removeLast();
            BigInteger num2 = deque.removeLast();
            deque.addLast(num1.multiply(num2));
        } else if (commandType == EXECUTE) {
            List<BigInteger> list = new LinkedList<>();
            for (int i = 0; i < 4; i++) {
                list.add(deque.removeLast());
            }
            int result = removeNumbersDuplicates(list);

            checkForExceptions(result, index);
            executeCommands(result, index);
        } else if (commandType == NEGATE) {
            deque.addLast(deque.removeLast().negate());
        } else if (commandType == POP) {
            deque.removeLast();
        }
    }

    /**
     * remove duplicates characters and computes the glyph command
     * @return the glyph command
     */
    private static int compute(String sCurrentLine, int index) {
        String subString = sCurrentLine.substring(index, index + 4);
        String uniqueSubString = removeDuplicate(subString.toCharArray(), 4);

        int result = 1;
        for (int j = 0; j < subString.length(); j++) {
            for (int k = 0; k < uniqueSubString.length(); k++) {
                if (subString.charAt(j) == uniqueSubString.charAt(k)) {
                    result = result * 10 + k;
                    break;
                }
            }
        }
        return result;
    }

    /**
     * @param sCurrentLine check if the currentLine has valid parantheses
     */
    private static void checkValidParantheses(String sCurrentLine) {
        int left = 0;
        for (int i = 0; i < sCurrentLine.length(); i = i+4) {
            int result = compute(sCurrentLine, i);
            if (result == LBRACE) {
                left++;
            } else if (result == RBRACE) {
                if (left == 0) { // LBrace without corresponding RBRACE
                    System.err.println("Error:" + i/4);
                    System.exit(-1);
                } else {
                    left--;
                }
            }
        }
        if (left > 0) { // RBrace without a corresponding LBrace
            System.err.println("Error:" + sCurrentLine.length()/4);
            System.exit(-1);
        }
    }

    private static void checkForExceptions(Integer commandType, int index) {
        if ((deque.size() == 0) && ((commandType == POP) || (commandType == ROT) || (commandType == RROT)
                || (commandType == DUP) || (commandType == LBRACE) || (commandType == OUTPUT) ||
                (commandType == NEGATE))) {
            System.err.println("Exception:" + index);
            System.exit(-2);
        }
        else if ((deque.size() < 2) && ((commandType == ADD) || (commandType == MULTIPLY) || (commandType == SWAP))) {
            System.err.println("Exception:" + index);
            System.exit(-2);
        }
        else if ((deque.size() < 4) && (commandType == EXECUTE)) {
            System.err.println("Exception:" + index);
            System.exit(-2);
        }
    }

    /***
     * @param list remove duplicates number from list and computes the glyph commandType
     * @return glyph command
     */
    private static int removeNumbersDuplicates(List<BigInteger> list) {
        Set<BigInteger> set = new LinkedHashSet<>();
        for (int i = 0; i < 4; i++) {
            set.add(list.get(i));
        }
        int result = 1;
        for (BigInteger bigInteger : list) {
            Iterator it = set.iterator();
            int j = 0;
            while (it.hasNext()) {
                if (it.next().equals(bigInteger)) {
                    result = result * 10 + j;
                }
                j++;
            }
        }
        return result;
    }

    /**
     * @param currentLine check the string for errors regarding length
     */
    private static void checkForErrors(String currentLine) {
        if (currentLine.length() % 4 != 0) {
            System.err.println("Error:" + currentLine.length()/4);
            System.exit(-1);
        }
    }


    /**
     * remove duplicate characters from a charArray
     * @param str charArray
     * @param n size of the charArray
     * @return String with unique characters
     */
    private static String removeDuplicate(char str[], int n) {
        int index = 0;
        for (int i = 0, j; i < n; i++) {
            // Check if str[i] is present before it
            for (j = 0; j < i; j++) {
                if (str[i] == str[j]) {
                    break;
                }
            }
            // If not present, then add it to the result.
            if (j == i) {
                str[index++] = str[i];
            }
        }
        return String.valueOf(Arrays.copyOf(str, index));
    }
}
