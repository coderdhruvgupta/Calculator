package com.coderdhruv.calculator;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import com.coderdhruv.calculator.databinding.ActivityCalculatorBinding;
import java.text.DecimalFormat;
import java.util.Stack;

public class CalculatorActivity extends AppCompatActivity {

    ActivityCalculatorBinding binding;
    private boolean isOperatorPressed = false;
    private DecimalFormat decimalFormat = new DecimalFormat("#.########");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityCalculatorBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.clear.setOnClickListener(v -> clearLastCharacter());

        setNumberPadListeners();

        binding.calculationText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                calculateResult();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void setNumberPadListeners() {
        View.OnClickListener numberClickListener = v -> {
            String input = v.getTag().toString();
            addToEditText(input);
        };

        // Set tags for numbers and operators
        binding.one.setTag("1");
        binding.two.setTag("2");
        binding.three.setTag("3");
        binding.four.setTag("4");
        binding.five.setTag("5");
        binding.six.setTag("6");
        binding.seven.setTag("7");
        binding.eight.setTag("8");
        binding.nine.setTag("9");
        binding.zero.setTag("0");
        binding.point.setTag(".");

        binding.divide.setTag("÷");
        binding.add.setTag("+");
        binding.multiply.setTag("×");
        binding.minus.setTag("-");

        // Attach listeners
        binding.one.setOnClickListener(numberClickListener);
        binding.two.setOnClickListener(numberClickListener);
        binding.three.setOnClickListener(numberClickListener);
        binding.four.setOnClickListener(numberClickListener);
        binding.five.setOnClickListener(numberClickListener);
        binding.six.setOnClickListener(numberClickListener);
        binding.seven.setOnClickListener(numberClickListener);
        binding.eight.setOnClickListener(numberClickListener);
        binding.nine.setOnClickListener(numberClickListener);
        binding.zero.setOnClickListener(numberClickListener);
        binding.point.setOnClickListener(numberClickListener);

        binding.divide.setOnClickListener(v -> addOperator("÷"));
        binding.add.setOnClickListener(v -> addOperator("+"));
        binding.multiply.setOnClickListener(v -> addOperator("×"));
        binding.minus.setOnClickListener(v -> addOperator("-"));
    }

    private void addToEditText(String value) {
        String currentText = binding.calculationText.getText().toString();

        // Check if operator was pressed, do not reset the input
        if (isOperatorPressed) {
            isOperatorPressed = false;
        }

        // Prevent multiple dots in a number
        if (value.equals(".") && currentText.endsWith(".")) {
            return;
        }

        // Prevent starting with multiple 0's
        if (currentText.equals("0") && !value.equals(".")) {
            currentText = "";
        }

        // Append the new value
        binding.calculationText.setText(currentText + value);

        // Ensure the last value is always visible
        binding.calculationText.setSelection(binding.calculationText.getText().length());

        // Update result in totalText
        calculateResult();
    }

    private void addOperator(String operator) {
        String currentText = binding.calculationText.getText().toString();

        // Check if the last character is an operator and replace it
        if (!currentText.isEmpty()) {
            if (isOperator(currentText.substring(currentText.length() - 1))) {
                currentText = currentText.substring(0, currentText.length() - 1) + operator;
            } else {
                currentText += operator;
            }
            binding.calculationText.setText(currentText);
            isOperatorPressed = true;
        }

        calculateResult();
    }

    private void calculateResult() {
        String expression = binding.calculationText.getText().toString();

        // Check if the expression ends with an operator
        if (expression.isEmpty() || isOperator(String.valueOf(expression.charAt(expression.length() - 1)))) {
            binding.totalText.setText(""); // Set to empty string if expression ends with an operator
            return;
        }

        try {
            // Evaluate the expression and show the result in totalText
            double result = evaluateExpression(expression);
            binding.totalText.setText(decimalFormat.format(result));
        } catch (Exception e) {

        }
    }


    private double evaluateExpression(String expression) {
        // Replace operator symbols for parsing
        expression = expression.replace("÷", "/").replace("×", "*");
        return evaluatePostfix(infixToPostfix(expression));
    }

    private String[] infixToPostfix(String expression) {
        Stack<String> operators = new Stack<>();
        StringBuilder postfix = new StringBuilder();
        StringBuilder number = new StringBuilder();

        // Iterate through each character in the expression
        for (int i = 0; i < expression.length(); i++) {
            char c = expression.charAt(i);

            if (Character.isDigit(c) || c == '.') {
                // Build the number (including decimal points)
                number.append(c);
            } else if (isOperator(String.valueOf(c))) {
                // If there is a number being built, add it to the postfix expression
                if (number.length() > 0) {
                    postfix.append(number).append(" ");
                    number.setLength(0);  // Clear the number builder
                }
                // Process the operator
                while (!operators.isEmpty() && precedence(operators.peek()) >= precedence(String.valueOf(c))) {
                    postfix.append(operators.pop()).append(" ");
                }
                operators.push(String.valueOf(c));
            }
        }

        // Add the last number to the postfix expression
        if (number.length() > 0) {
            postfix.append(number).append(" ");
        }

        // Add remaining operators to the postfix expression
        while (!operators.isEmpty()) {
            postfix.append(operators.pop()).append(" ");
        }

        return postfix.toString().trim().split(" ");
    }

    private double evaluatePostfix(String[] tokens) {
        Stack<Double> stack = new Stack<>();

        for (String token : tokens) {
            if (isOperator(token)) {
                double b = stack.pop();
                double a = stack.pop();

                switch (token) {
                    case "+":
                        stack.push(a + b);
                        break;
                    case "-":
                        stack.push(a - b);
                        break;
                    case "*":
                        stack.push(a * b);
                        break;
                    case "/":
                        if (b == 0) {
                            throw new ArithmeticException("Division by zero");
                        }
                        stack.push(a / b);
                        break;
                }
            } else {
                stack.push(Double.parseDouble(token));
            }
        }

        return stack.pop();
    }

    private boolean isOperator(String token) {
        return token.equals("+") || token.equals("-") || token.equals("*") || token.equals("/") || token.equals("÷") || token.equals("×");
    }


    private int precedence(String operator) {
        switch (operator) {
            case "+":
            case "-":
                return 1;
            case "*":
            case "/":
                return 2;
        }
        return -1;
    }




    private void clearLastCharacter() {
        String currentText = binding.calculationText.getText().toString();

        if (!currentText.isEmpty()) {
            currentText = currentText.substring(0, currentText.length() - 1);
        }

        if (currentText.isEmpty()) {
            currentText = "0";
        }
        binding.calculationText.setSelection(binding.calculationText.getText().length());
        binding.calculationText.setText(currentText);
    }
}
