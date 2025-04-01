package optimizer;

import org.apache.commons.math3.analysis.MultivariateFunction;
import org.apache.commons.math3.optim.*;
import org.apache.commons.math3.optim.nonlinear.scalar.GoalType;
import org.apache.commons.math3.optim.nonlinear.scalar.ObjectiveFunction;
import org.apache.commons.math3.optim.nonlinear.scalar.noderiv.CMAESOptimizer;
import org.apache.commons.math3.random.JDKRandomGenerator;
import org.apache.commons.math3.random.RandomGenerator;

/**
 * Author：Milu
 * 严重落后进度啦......
 * date：2024/3/22
 * project:matsimParamCalibration
 */
public class testCMAES {
    public static void main(String[] args) {

        double[] lb = new double[5];
        double[] ub = new double[5];
        double[] s = new double[5];
        double[] sigma = new double[5];
        for (int i = 0; i < 5; i++) {
            lb[i] = -10.0;
            ub[i] =  10.0;
            s[i] = (lb[i] + ub[i]) / 2;/*from w  w  w . java2  s .  c o m*/
            sigma[i] = ub[i] - lb[i];
        }
        MultivariateFunction target = new MultivariateFunction() {
            @Override
            public double value(double[] arg0) {
                double fitness=0.0;
                for (int i = 0; i < arg0.length; i++) {
                    if (i%2==0){fitness +=(arg0[i]*arg0[i]*arg0[i]);} else {fitness-=(arg0[i]);}
                }
                return fitness;
            }
        };
        // construct solver
        int maxIterations = 2000; double stopFitness = 30000; boolean isActiveCMA = true;
        int diagonalOnly = 5;
        int checkFeasableCount = 5;
        RandomGenerator random = new JDKRandomGenerator();
        boolean generateStatistics = false;
        ConvergenceChecker<PointValuePair> checker = new SimplePointChecker<PointValuePair>(0.000001, 0.00001);
        CMAESOptimizer optimizer = new CMAESOptimizer(maxIterations,stopFitness,isActiveCMA,diagonalOnly,checkFeasableCount,random,generateStatistics,checker);
        PointValuePair val = optimizer.optimize(new CMAESOptimizer.Sigma(sigma), new ObjectiveFunction(target),
                new InitialGuess(s),
                GoalType.MAXIMIZE,
                new MaxEval(maxIterations), new SimpleBounds(lb, ub),
                new CMAESOptimizer.PopulationSize(20));
        System.out.println("optimized values are:"+val.getValue()+", at iteration: "+optimizer.getIterations());
        for (int i = 0; i < val.getPoint().length; i++) {
            System.out.print(val.getPoint()[i]+", ");
        }
    }
}
