package io.burt.jmespath.function;

public class AddFunction extends MathBiFunction {
    @Override
    protected double performMathOperation(double x, double y) {
        return x + y;
    }
}