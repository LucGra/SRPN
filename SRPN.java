import java.util.Stack;

/**
 * SRPN implements a Saturated Reverse Polish Notation Calculator.
 *
 * @version 1.0
 * @see <a href="https://en.wikipedia.org/wiki/Reverse_Polish_notation">RPN</a>
 * @see <a href="https://en.wikipedia.org/wiki/Saturation_arithmetic">Saturation</a>
 */

public class SRPN {

  /**
   * A stack of integers to contain the operands entered by the user.
   */
  private final Stack<Integer> stack = new Stack<>();

  /**
   * An integer to track the current number of generated randoms. This is used to advance through a
   * pre-determined list of randoms, matching the functionality of a legacy program.
   */
  private int randomIndex = 0;

  /**
   * Process a user entered command that could represent an operator (e.g. 10), an operand (e.g. +)
   * or an entire expression on a single line t (e.g. 10 2 + =).
   *
   * @param command The command string to be processed
   */
  public void processCommand(String command) {
    // Remove all comments from the command
    command = command.replaceAll("#.*#", "");
    // space pad all non-special operands
    command = command.replaceAll("([+/*^%])(?!=)", " $1 ");
    // space pad all special operands
    command = command.replaceAll("(\\+=|-=|/=|\\*=|\\^=|%=)", " $1 ");
    // pad all alphabetical characters provided
    command = command.replaceAll("([a-zA-Z])", " $1 ");
    // Replace instances of multiple spaces with a single space
    command = command.replaceAll("\\s+", " ");
    // Clear all leading and trailing whitespace
    command = command.trim();
    if (command.length() > 0) {
      for (String item : command.split(" ")) {
        // separate operands from operators / commands by attempting to parse commands as integers
        try {
          int operand = Integer.parseInt(item);
          if (this.isSpaceOnStack()) {
            this.stack.push(operand);
          }
        } catch (NumberFormatException e) {
          // Exception is not logged to match legacy program functionality
          processSingleCommand(item);
        }
      }
    }
  }

  /**
   * Handle single commands entered by the user.
   * <p>
   * Provides handlers to implement the specific functions of this calculator. Namely:
   * <ul>
   *   <li>= (equals)</li>
   *   <li>+ (addition)</li>
   *   <li>- (subtraction)</li>
   *   <li>* (multiplication)</li>
   *   <li>/ (division)</li>
   *   <li>% (modulo)</li>
   *   <li>^ (power)</li>
   *   <li>r (generate random)</li>
   *   <li>d (display stack)</li>
   *   <li>+= (display the last stack value and perform addition)</li>
   *   <li>-= (display the last stack value and perform subtraction)</li>
   *   <li>*= (display the last stack value and perform multiplication)</li>
   *   <li>/= (display the last stack value and perform division)</li>
   *   <li>%= (display the last stack value and perform modulo division)</li>
   *   <li>^= (display the last stack value and raise base to an exponent)</li>
   * </ul>
   * <p>
   * All operations check the number of values on the stack to prevent errors.
   *
   * @param command A single command to be processed.
   */
  public void processSingleCommand(String command) {
    switch (command) {
      case "=":
        if (this.stack.size() > 0) {
          // show the element at the top of the stack without removing or modifying it
          System.out.println(this.stack.peek());
        } else {
          System.out.println("Stack empty.");
        }
        break;

      case "+":
        if (this.enoughOperandsOnStack()) {
          this.add(this.stack.pop(), this.stack.pop());
        }
        break;

      case "+=":
        if (this.enoughOperandsOnStack()) {
          int operand1 = this.stack.pop();
          System.out.println(operand1);
          this.add(operand1, this.stack.pop());
        }
        break;

      case "-":
        if (this.enoughOperandsOnStack()) {
          this.subtract(this.stack.pop(), this.stack.pop());
        }
        break;

      case "-=":
        if (this.enoughOperandsOnStack()) {
          int operand1 = this.stack.pop();
          System.out.println(operand1);
          this.subtract(operand1, this.stack.pop());
        }
        break;

      case "*":
        if (this.enoughOperandsOnStack()) {
          this.multiply(this.stack.pop(), this.stack.pop());
        }
        break;

      case "*=":
        if (this.enoughOperandsOnStack()) {
          int operand1 = this.stack.pop();
          System.out.println(operand1);
          this.multiply(operand1, this.stack.pop());
        }
        break;

      case "/":
        if (this.enoughOperandsOnStack()) {
          this.divide(this.stack.pop(), this.stack.pop());
        }
        break;

      case "/=":
        if (this.enoughOperandsOnStack()) {
          int operand1 = this.stack.pop();
          System.out.println(operand1);
          this.divide(operand1, this.stack.pop());
        }
        break;

      case "%":
        if (this.enoughOperandsOnStack()) {
          this.mod(this.stack.pop(), this.stack.pop());
        }
        break;

      case "%=":
        if (this.enoughOperandsOnStack()) {
          int operand1 = this.stack.pop();
          System.out.println(operand1);
          this.mod(operand1, this.stack.pop());
        }
        break;

      case "^":
        if (this.enoughOperandsOnStack()) {
          this.power(this.stack.pop(), this.stack.pop());
        }
        break;

      case "^=":
        if (this.enoughOperandsOnStack()) {
          int operand1 = this.stack.pop();
          System.out.println(operand1);
          this.power(operand1, this.stack.pop());
        }
        break;

      case "d":
        if (this.stack.size() > 0) {
          this.display();
        } else {
          System.out.println(Integer.MIN_VALUE);
        }
        break;

      case "r":
        if (this.isSpaceOnStack()) {
          this.stack.push(this.generateRandom());
          this.randomIndex++;  // gradually move through the pre-defined list of randoms
        }
        break;

      default:
        System.out.printf("Unrecognised operator or operand \"%s\".\n", command);
    }
  }

  /**
   * Perform addition with 2 operands and add the result to the stack.
   * <p>
   * The result of the addition is checked for saturation against Integer limits before being added
   * to the stack.
   *
   * @param operand1 The first operand to be used for addition
   * @param operand2 The second operand to be used for addition
   */
  private void add(int operand1, int operand2) {
    long result = (long) operand2 + (long) operand1;
    this.stack.push(this.saturate(result));
  }

  /**
   * Perform subtraction with 2 operands and add the result to the stack.
   * <p>
   * The result of the subtraction is checked for saturation against Integer limits before being
   * added to the stack.
   *
   * @param operand1 The first operand to be used for subtraction
   * @param operand2 The second operand to be used for subtraction
   */
  private void subtract(int operand1, int operand2) {
    long result = (long) operand2 - (long) operand1;
    this.stack.push(this.saturate(result));
  }

  /**
   * Perform multiplication with 2 operands and add the result to the stack.
   * <p>
   * The result of the multiplication is checked for saturation against Integer limits before being
   * added to the stack.
   *
   * @param operand1 The first operand to be used for multiplication
   * @param operand2 The second operand to be used for multiplication
   */
  private void multiply(int operand1, int operand2) {
    long result = (long) operand2 * (long) operand1;
    this.stack.push(this.saturate(result));
  }

  /**
   * Perform division with 2 operands and add the result to the stack.
   * <p>
   * As the two operands used for this operation are generated through stack.pop(), they are passed
   * to this function in the reverse order that they were entered by the user. The operands are
   * swapped to ensure that the operation carried out reflects the operation entered by the user.
   * <p>
   * If a division by 0 is attempted, the popped operands need to be added back to the stack in the
   * same order they were entered by the user.
   * <p>
   * The result of the division is checked for saturation against Integer limits before being added
   * to the stack.
   *
   * @param operand1 The first operand to be used for division
   * @param operand2 The second operand to be used for division
   */
  private void divide(int operand1, int operand2) {
    if (operand1 != 0) {
      // operands are swapped to match order of user entry
      long result = (long) operand2 / (long) operand1;
      this.stack.push(this.saturate(result));
    } else {
      System.out.println("Divide by 0.");
      // put the values back on the stack
      this.stack.push(operand2);
      this.stack.push(operand1);
    }
  }

  /**
   * Perform modulo division with 2 operands and add the result to the stack.
   * <p>
   * The result of the modulo division is checked for saturation against Integer limits before being
   * added to the stack.
   *
   * @param operand1 The first operand to be used for modulo division
   * @param operand2 The second operand to be used for module division
   */
  private void mod(int operand1, int operand2) {
    this.stack.push(operand2 % operand1);
  }

  /**
   * Raise a base to an exponent and add the result to the stack.
   * <p>
   * The result of the modulo division is checked for saturation against Integer limits before being
   * added to the stack.
   *
   * @param operand1 The exponent the base will be raised to
   * @param operand2 The base value that will be raised to an exponent
   */
  private void power(int operand1, int operand2) {
    this.stack.push(this.saturate((long) Math.pow(operand2, operand1)));
  }

  /**
   * Iterate through the stack and display each value on a new line.
   */
  private void display() {
    for (Object item : this.stack) {
      System.out.println(item.toString());
    }
  }

  /**
   * Generate a 'random' number from a pre-determined list of random numbers.
   * <p>
   * A pre-determined list of randoms is used to match the functionality of a legacy program.
   *
   * @return the next integer from a list of integers representing random numbers
   */
  private int generateRandom() {
    int[] randoms = {1804289383,
        846930886,
        1681692777,
        1714636915,
        1957747793,
        424238335,
        719885386,
        1649760492,
        596516649,
        1189641421,
        1025202362,
        1350490027,
        783368690,
        1102520059,
        2044897763,
        1967513926,
        1365180540,
        1540383426,
        304089172,
        1303455736,
        35005211,
        521595368};
    if (this.randomIndex == randoms.length) {
      this.randomIndex = 0;
    }
    return randoms[this.randomIndex];
  }

  /**
   * Check a provided value against the maximum and minimum size of an Integer.
   * <p>
   * If the provided value is larger than could be stored in an Integer without overflow, the
   * maximum allowable value of an Integer will be returned. If the provided value is smaller than
   * could be stored in an Integer without underflow, the minimum allowable value of an Integer will
   * be returned. If the value can be stored in an Integer without overflow or underflow, it will be
   * cast to an Integer and returned.
   *
   * @param value The value to be checked against the limits of an Integer
   * @return An allowable representation of the provided value as an Integer
   */
  private int saturate(long value) {
    if (value > Integer.MAX_VALUE) {
      return Integer.MAX_VALUE;
    } else if (value < Integer.MIN_VALUE) {
      return Integer.MIN_VALUE;
    } else {
      return (int) value;
    }
  }

  /**
   * Check that the stack contains at least a specified number of elements.
   *
   * @return true if the stack contains enough elements, false otherwise
   */
  private boolean enoughOperandsOnStack(int requiredOperands) {
    if (this.stack.size() >= requiredOperands) {
      return true;
    } else {
      System.out.println("Stack underflow.");
      return false;
    }
  }

  /**
   * Check that the stack contains at least 2 operands.
   *
   * @see SRPN#enoughOperandsOnStack(int)
   */
  private boolean enoughOperandsOnStack(){
    return enoughOperandsOnStack(2);
  }

  /**
   * Ensure the size of the stack does not exceed the maximum permitted size (23).
   * <p>
   * The maximum permitted value of 23 elements on the stack is enforced to match the functionality
   * of a legacy program.
   *
   * @return false if the stack has reached maximum capacity, true otherwise
   */
  private boolean isSpaceOnStack() {
    // limit the stack size to 23 to match legacy functionality
    if (stack.size() == 23) {
      System.out.println("Stack overflow.");
      return false;
    } else {
      return true;
    }
  }
}