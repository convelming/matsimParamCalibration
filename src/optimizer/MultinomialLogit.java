package optimizer;
import org.apache.commons.math3.analysis.MultivariateFunction;
import org.apache.commons.math3.optim.*;
import org.apache.commons.math3.optim.nonlinear.scalar.GoalType;
import org.apache.commons.math3.optim.nonlinear.scalar.GradientMultivariateOptimizer;
import org.apache.commons.math3.optim.nonlinear.scalar.ObjectiveFunction;
import org.apache.commons.math3.optim.nonlinear.scalar.ObjectiveFunctionGradient;
import org.apache.commons.math3.optim.nonlinear.scalar.gradient.NonLinearConjugateGradientOptimizer;
import org.apache.commons.math3.optim.nonlinear.scalar.noderiv.BOBYQAOptimizer;
import org.apache.commons.math3.optim.nonlinear.scalar.noderiv.CMAESOptimizer;
import org.apache.commons.math3.optim.nonlinear.scalar.noderiv.NelderMeadSimplex;
import org.apache.commons.math3.optim.nonlinear.scalar.noderiv.SimplexOptimizer;
import utils.PlanCalcScoreParamDoubleArrayMapping;

import java.util.Arrays;

/**
 * Author：Milu
 * 严重落后进度啦......
 * date：2024/11/22
 * project:matsimParamCalibration
 */
public class MultinomialLogit {

    public SimpleBounds simpleBounds;
    public double[] lb ;
    public double[] ub ;
    public double[] coefficients;
    MultivariateFunction fitnessFunction;
    double convergeDiffThreshold;
    public MultinomialLogit(PlanCalcScoreParamDoubleArrayMapping planCalcScoreParamDoubleArrayMapping, double[] lowerBound, double[] upperBound,
                            MultivariateFunction fitnessFunction,double convergeDiffThreshold) {
        this.simpleBounds = new SimpleBounds(lb, ub);
        this.coefficients = planCalcScoreParamDoubleArrayMapping.initializeDoubleArray();
        this.convergeDiffThreshold = convergeDiffThreshold;
        this.fitnessFunction = fitnessFunction;
    }
    public MultinomialLogit(PlanCalcScoreParamDoubleArrayMapping planCalcScoreParamDoubleArrayMapping, 
                            MultivariateFunction fitnessFunction) {
        this.lb = new double[planCalcScoreParamDoubleArrayMapping.initializeDoubleArray().length];
        this.ub = new double[planCalcScoreParamDoubleArrayMapping.initializeDoubleArray().length];
        Arrays.fill(lb,-999999.9);Arrays.fill(ub,3600*24.0);
        this.simpleBounds = new SimpleBounds(lb, ub);
        this.coefficients = planCalcScoreParamDoubleArrayMapping.initializeDoubleArray();
        this.fitnessFunction = fitnessFunction;
    }

    // Define a custom convergence checker
    ConvergenceChecker<PointValuePair> customChecker = (iteration, previous, current) -> {
        if (previous != null && current != null) {
            // Stop if the relative difference in function value is small
            double prevValue = previous.getValue();
            double currValue = current.getValue();
            return Math.abs(currValue - prevValue) < 1e-6;
        }
        return false; // Continue iterating
    };
    public void trainBySimplexOptimizer(){
//        NonLinearConjugateGradientOptimizer optimizer = new NonLinearConjugateGradientOptimizer(
//            NonLinearConjugateGradientOptimizer.Formula.FLETCHER_REEVES, // 使用 Fletcher-Reeves 公式
//            customChecker);
        SimplexOptimizer simplexOptimizer=new SimplexOptimizer(customChecker);
        // Define the simplex
        NelderMeadSimplex simplex = new NelderMeadSimplex(coefficients.length); // Dimension of the problem
        try {
            PointValuePair solution = simplexOptimizer.optimize(
                    new MaxEval(100000),
                    new ObjectiveFunction(fitnessFunction),
                    GoalType.MAXIMIZE,
                    new InitialGuess(coefficients),simplex
                    );
            coefficients = solution.getPoint();
            System.out.println("Optimal value: f(x, y) = " + solution.getValue());
        } catch (Exception e) {
            System.err.println("Optimizer reached maximum iterations. Returning best result found so far.");
            // Use the best result so far (this requires custom tracking of progress)
        }
    }

    public void trainByBOBYQAOptimizer() {
        // Define optimizer
        // 创建优化器
//        NonLinearConjugateGradientOptimizer optimizer = new NonLinearConjugateGradientOptimizer(
//                NonLinearConjugateGradientOptimizer.Formula.FLETCHER_REEVES, // 使用 Fletcher-Reeves 公式
//                new SimplePointChecker(convergeDiffThreshold, convergeDiffThreshold) // 步长
//        );

        BOBYQAOptimizer optimizer = new BOBYQAOptimizer(2*coefficients.length+1);
        // Train the models
        try {
            PointValuePair solution = optimizer.optimize(
                    new MaxEval(10000000),
                    new ObjectiveFunction(fitnessFunction),
                    GoalType.MAXIMIZE,
                    new InitialGuess(coefficients)
            );
            coefficients = solution.getPoint();
            System.out.println("Optimal value: f(x, y) = " + solution.getValue());
        } catch (Exception e) {
            System.err.println("Optimizer reached maximum iterations. Returning best result found so far.");
            // Use the best result so far (this requires custom tracking of progress)
        }

    }

}
