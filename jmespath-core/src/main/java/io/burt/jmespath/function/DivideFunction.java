package io.burt.jmespath.function;

public class DivideFunction extends MathBiFunction {
    @Override
    protected double performMathOperation(double x, double y) {
        return x / y;
    }
}