package optimizer;


import org.apache.commons.math3.distribution.MultivariateNormalDistribution;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.Plan;
import org.matsim.core.utils.collections.Tuple;
import utils.CalcPlanScore;
import utils.PlanCalcScoreParamDoubleArrayMapping;

import java.util.*;

public class CMAES implements EvolutionStrategy{
    private static final Logger log = LogManager.getLogger( CMAES.class ) ;
    public int popSize = 100; // population size
    public int mu; // parent population size
    public int numVariables; // number of variables
    public Individual orgIndividual;
    public double[] mean; // mean vector
    public double[][] covarianceMatrix; // covariance matrix
    public double sigma = 1.0; // initial step size
    public double learningRate = 0.01;
    public int maxIterations = 100; // maximum number of iterations
    public double stopFitness = 0.00001; // end while
    public int iRemainNoImprovement = 0;
    public double bestFitness = Double.MAX_VALUE;
    public double[][] bounds;
    
    public List<Individual> population;
    public CMAES(double[][] variablesRanges,Individual orgIndividual, int popSize){
        this.popSize = popSize;
        this.orgIndividual = orgIndividual;
        initializePopulation();
    }
    public double[] returnBestGenes(){
        return this.population.get(0).getGenes();
    }
    /**
     *
     * @param orgIndividual - default values from
     * @param popSize
     */
    public CMAES(Individual orgIndividual, int popSize){
        this.popSize = popSize;
        this.orgIndividual = orgIndividual;        this.numVariables =orgIndividual.getGenes().length;
//        this.individuals = orgIndividual.genRndIndividualList(popSize,0.15);
    }
    public CMAES(Individual orgIndividual, int popSize,double[][] bounds,double scale){
        this.popSize = popSize;
        this.numVariables = orgIndividual.getGenes().length;
        this.orgIndividual = orgIndividual;this.bounds = bounds;
        this.population = orgIndividual.genRndIndividualList(popSize,bounds,scale);
        this.mean = StatisticalUtils.calPopMeanGenes(this.population);
        this.covarianceMatrix = StatisticalUtils.calPopCovariance(this.population);
        this.mu = popSize / 2;
    }
    public CMAES(Individual orgIndividual){
        this.orgIndividual = orgIndividual;
        this.numVariables =orgIndividual.getGenes().length;
    }
    public CMAES(){}
    @Override
    public void initializePopulation() {
    }

    @Override
    public void evaluatePopulation() { // use calFitnees instead 'cause input variables are needed.

    }

    @Override
    public List<Individual> selectParents() {
        List<Individual>  tmpParents = new ArrayList<>();
        // make sure the parents are sorted!!
        for (int i = 0; i < mu; i++) {
            tmpParents.add(this.population.get(i));
        }
        return tmpParents;
    }

    @Override public List<Individual> reproduce(List<Individual> selectedParents) {return null;}
    public void reproduce(List<Individual> selectedParents,double keepRatio,double mutateRatio,double mutateScale) {
        Tuple<double[],double[][]> tmpParentMatrices = StatisticalUtils.individualGene2Matrix(selectedParents);
//        double[][] x = MatrixUtils.createRealMatrix(tmpParentMatrices.getSecond()).getData();    //todo  the yws are normally weighted according to ranked fitness values, but here it is set to
        double[] yw = StatisticalUtils.matrixSumAvg(tmpParentMatrices.getSecond());
        // m = m+sigma*yw
        for (int i = 0; i < mean.length; i++) {
            mean[i] += sigma*yw[i];
        }
        // c = (1-learningRate)*c +learningRate * yw * YwT
        double[][] ywMatrix = {yw};
        double[][] ywYwT = MatrixUtils.createRealMatrix(ywMatrix).transpose().multiply(MatrixUtils.createRealMatrix(ywMatrix)).getData();
        double[][] test1 = MatrixUtils.createRealMatrix(covarianceMatrix).scalarMultiply(1-learningRate).getData();
        double[][] test2 = MatrixUtils.createRealMatrix(ywYwT).scalarMultiply(learningRate).getData();
        covarianceMatrix = MatrixUtils.createRealMatrix(test1).add( MatrixUtils.createRealMatrix(test2)).getData();
        // enforce symetry
        for (int i = 0; i < covarianceMatrix.length; i++) {
            for (int j = 0; j < covarianceMatrix[i].length; j++) {
                if (i>j){
                    covarianceMatrix[i][j] =covarianceMatrix[j][i];
                }
            }
        }

        MultivariateNormalDistribution mnd = new MultivariateNormalDistribution(mean,covarianceMatrix);
        List<Individual> offspring = StatisticalUtils.matrix2IndividualGenes(mnd.sample((int)(popSize*(1-keepRatio-mutateRatio))));
        // add offSpring to list
        for (int i = (int)(popSize*keepRatio); i < offspring.size(); i++) {
            population.set(i,offspring.get(i-(int)(popSize*keepRatio)));
        }
        for (int i = popSize-1; i >popSize-popSize*mutateRatio; i--) {
            population.set(i,orgIndividual.rndProduceIndividualsInBounds(this.bounds,mutateScale));
        }
    }

    @Override
    public void mutate(List<Individual> offspring) {

    }
    public void mutate(double mutationRate, double scale) {
        int mutationSize = (int) (mutationRate*popSize);
        for (int i = popSize-1; i < mutationSize; i--) {
            population.set(i,orgIndividual.rndProduceIndividualsInBounds(this.bounds,scale));
        }
    }

    @Override
    public void replacePopulation(List<Individual> newPopulation) {
        this.population = newPopulation;
    }

    @Override
    public void evolve(int numGenerations, double stopFitness) {

    }
    public void selectOffSpring(List<Individual> offSprings,double selectRatio){
        for (int i = population.size()-1; i >popSize*selectRatio ; i--) {
            population.set(i,offSprings.get(i));
        }
    }
    public void evolve(int numGenerations, Map<Person,List<Plan>> planList,Scenario scenario,double mutationRate) {

        int iGen = 0;
        while (iGen<numGenerations ){

            calFitness(planList,scenario);
            double tmpBestFitness = this.population.get(0).getFitness();
            if (Math.abs(this.bestFitness-tmpBestFitness)<stopFitness){ iRemainNoImprovement++;
            }else {iRemainNoImprovement= 0;}
            if (iRemainNoImprovement>5){
                log.info("no better result can be found for 5 iterations, stops");
                break;
            }
            Collections.sort(population);//DO NOT forget to sort the individual accroding to its finess
            List<Individual>  selectedParents = selectParents();
            if (iGen==5){
                System.out.println();
            }
            reproduce(selectedParents,0.2,0.1,0.5);
            log.info("CMA_ES iteration: "+ iGen+", is done...");
            iGen++;
        }
    }

    /**
     * this is the core here, the fitness is the maximumLogLikelihood of each plan
     * @param planList
     */
    public void calFitness(Map<Person,List<Plan>> planList, Scenario scenario){
        for (int i = 0; i < this.population.size(); i++) {
            double[] estimatedParams = this.population.get(i).getGenes();
            PlanCalcScoreParamDoubleArrayMapping planCalcScoreParamDoubleArrayMapping = new PlanCalcScoreParamDoubleArrayMapping(scenario.getConfig().planCalcScore());
            planCalcScoreParamDoubleArrayMapping.updateParamsPerDoubleArray(estimatedParams);
            CalcPlanScore.calAltPlanScores(planList,scenario);
            this.population.get(i).setFitness(MaximumLogLikelihoodFunction.cal(planList));
        }
    }
}
