package optimizer;

import org.apache.commons.math3.analysis.MultivariateFunction;
import org.apache.commons.math3.optim.*;
import org.apache.commons.math3.optim.nonlinear.scalar.GoalType;
import org.apache.commons.math3.optim.nonlinear.scalar.ObjectiveFunction;
import org.apache.commons.math3.optim.nonlinear.scalar.noderiv.CMAESOptimizer;
import org.apache.commons.math3.random.JDKRandomGenerator;
import org.apache.commons.math3.random.RandomGenerator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import utils.PlanCalcScoreParamDoubleArrayMapping;

/**
 * Author：Milu
 * 严重落后进度啦......
 * date：2024/3/22
 * project:matsimParamCalibration
 */
public class CMAES4PlanParamCalibration {
    private static final Logger log = LogManager.getLogger(CMAES4PlanParamCalibration.class);
    public CMAESOptimizer cmaesOptimizer;
    public PointValuePair bestResult= null; public double bestFitness;
    public int popSize;
    public SimpleBounds simpleBounds;
    public InitialGuess initialGuess;
    public double[] sigma;
    PlanCalcScoreParamDoubleArrayMapping planCalcScoreParamDoubleArrayMapping;
    public CMAES4PlanParamCalibration(int maxIterations,double convergeDiffThreshold, PlanCalcScoreParamDoubleArrayMapping planCalcScoreParamDoubleArrayMapping) {
        double[] startArray = planCalcScoreParamDoubleArrayMapping.initializeDoubleArray();
        double[] lb = planCalcScoreParamDoubleArrayMapping.getDefaultParameterLowerBounds();
        double[] ub = planCalcScoreParamDoubleArrayMapping.getDefaultParameterUpperBounds();
        this.planCalcScoreParamDoubleArrayMapping = planCalcScoreParamDoubleArrayMapping;
        this.sigma = new double[startArray.length];
        this.initialGuess = new InitialGuess(startArray);
        for (int i = 0; i < startArray.length; i++) { sigma[i] = Math.random()*(ub[i] - lb[i]);}
        this.popSize = 4 + (int)(3*Math.log(startArray.length));
        this.simpleBounds = new SimpleBounds(lb, ub);
        // constructlver
        double stopFitness = 1e6; boolean isActiveCMA = true;
        int diagonalOnly = 5;
        int checkFeasibleCount = 5;
        RandomGenerator random = new JDKRandomGenerator();
        boolean generateStatistics = true;
        ConvergenceChecker<PointValuePair> checker = new SimplePointChecker(convergeDiffThreshold, convergeDiffThreshold);
        this.cmaesOptimizer = new CMAESOptimizer(maxIterations,stopFitness,isActiveCMA,diagonalOnly,checkFeasibleCount,random,generateStatistics,checker);
    }
    public CMAES4PlanParamCalibration(int maxIterations,double convergeDiffThreshold,PlanCalcScoreParamDoubleArrayMapping planCalcScoreParamDoubleArrayMapping,double[] lowerBound,double[] upperBound) {
        double[] startArray = planCalcScoreParamDoubleArrayMapping.initializeDoubleArray();
        this.planCalcScoreParamDoubleArrayMapping = planCalcScoreParamDoubleArrayMapping;
        this.sigma = new double[startArray.length];
        this.initialGuess = new InitialGuess(startArray);
        for (int i = 0; i < startArray.length; i++) { sigma[i] = Math.random()*(upperBound[i] - lowerBound[i]);}
        this.popSize = 4 + (int)(3*Math.log(startArray.length));
        this.simpleBounds = new SimpleBounds(lowerBound,upperBound);
        // constructlver
        double stopFitness = 1e6; boolean isActiveCMA = true;
        int diagonalOnly = 5;
        int checkFeasibleCount = 5;
        RandomGenerator random = new JDKRandomGenerator();
        boolean generateStatistics = true;
        ConvergenceChecker<PointValuePair> checker = new SimplePointChecker(convergeDiffThreshold, convergeDiffThreshold);
        this.cmaesOptimizer = new CMAESOptimizer(maxIterations,stopFitness,isActiveCMA,diagonalOnly,checkFeasibleCount,random,generateStatistics,checker);
    }
    public void run(MultivariateFunction fitnessFunction){
        if(this.bestResult!=null){
            initialGuess = new InitialGuess(this.bestResult.getPoint());
        }
        this.bestResult =  cmaesOptimizer.optimize(new CMAESOptimizer.Sigma(sigma), new ObjectiveFunction(fitnessFunction),
                initialGuess,
                GoalType.MAXIMIZE,
                new MaxEval(this.cmaesOptimizer.getMaxIterations()), this.simpleBounds,
                new CMAESOptimizer.PopulationSize(popSize));
        this.bestFitness = bestResult.getValue();
        log.info("CMA-ES is done, end at iteration: "+this.cmaesOptimizer.getIterations()+", with best score: "+this.bestFitness);
    }
    public double[] getBestResults(){
        return this.bestResult.getPoint();
    }
    public void updateBoundsWithDesignatedValues(){

    }
}
