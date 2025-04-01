package optimizer;

import org.apache.commons.math3.distribution.MultivariateNormalDistribution;
import org.apache.commons.math3.linear.*;
import org.apache.commons.math3.stat.correlation.Covariance;
import org.matsim.core.utils.collections.Tuple;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Author：Milu
 * 严重落后进度啦......
 * date：2024/2/28
 * project:matsimParamCalibration
 */
public class StatisticalUtils {

    public StatisticalUtils(){}
    /**
     * this mehtod returns a
     * @param variablesBounds upper and lower boudn
     * @return
     */
    public MultivariateNormalDistribution getRndMultivariateNomralParams(Tuple<Double,Double>[] variablesBounds){
        int numVariables = variablesBounds.length;
        Random random = new Random();
        double[] means = new double[numVariables];
        double[][] rndSamples = new double[numVariables][];
        for (int i = 0; i < variablesBounds.length; i++) {
            rndSamples[i] = new double[100];
            for (int j = 0; j < 100; j++) {
                rndSamples[i][j] = random.nextDouble()*(variablesBounds[i].getSecond()-variablesBounds[i].getFirst())+variablesBounds[i].getFirst();
            }
            double sum = 0;
            for (int j = 0; j < 100; j++) {
                sum += rndSamples[i][j];
            }
            means[i] = sum/100;
        }
        Covariance covariance = new Covariance(MatrixUtils.createRealMatrix(rndSamples).transpose());
        double[][] covarianceMatrix = covariance.getCovarianceMatrix().getData();

        return new MultivariateNormalDistribution(means, covarianceMatrix);
    }
    public static double[][] calPopCovariance(List<Individual> population){
        double genes[][] = new double[population.size()][];
        for (int i = 0; i < population.size(); i++) {
            genes[i] = population.get(i).getGenes();
        }
        return new Covariance(MatrixUtils.createRealMatrix(genes)).getCovarianceMatrix().getData();
    }
    /**
     * for the random values for a variable, it is stored in each column
     * @param numVariables
     * @return
     */
    public static MultivariateNormalDistribution genRndZeroMnd(int numVariables){
        Random random = new Random();
        int rndRows = 100;
        double[] means = new double[numVariables];
        double[][] rndSamples = new double[numVariables][];
        for (int i = 0; i < numVariables; i++) {
            rndSamples[i] = new double[rndRows];
            for (int j = 0; j < rndRows; j++) {
                rndSamples[i][j] = random.nextGaussian();
                means[i] =0.0;
            }
        }
        double[][] covarianceMatrix = new Covariance(MatrixUtils.createRealMatrix(rndSamples).transpose()).getCovarianceMatrix().getData();
        return new MultivariateNormalDistribution(means, covarianceMatrix);
    }
    /**
     *              newXmean[N,1] = xmean[N,1] +sigma(double) * B[N,N] * D[N,N] * arz[N,1]
     *              arz = 1/sigma * D^-1*B^-1*(newXmean-xmean)
     * @param xmean
     * @param newXmean
     * @param B
     * @param D
     * @param sigma
     * @return
     */
    public static RealMatrix updateArzArray(RealMatrix xmean, RealMatrix newXmean, RealMatrix B, RealMatrix D, double sigma){
        return getInverseMatrix(D).multiply(getInverseMatrix(B)).multiply(newXmean.subtract(xmean)).scalarMultiply(1.0/sigma);
    }
    public static RealMatrix getInverseMatrix(RealMatrix matrix){
        DecompositionSolver solver = new LUDecomposition(matrix).getSolver();
        if (!solver.isNonSingular()){
            return  new SingularValueDecomposition(matrix).getSolver().getInverse();
        }else{
            return solver.getInverse();
        }

    }
    /**
     *
     * @param range each row is a two elements array respected to lower and upper bound for a certain variables
     * @return
     */
    public static double[] genRndMeansInRange(double[][] range){
        int numVariables = range.length;
        Random random = new Random();
        double[] means = new double[numVariables];
        for (int i = 0; i < range.length; i++) {
            means[i] = random.nextDouble()*(range[i][1]-range[i][0])+range[i][0];
        }
        return means;
    }
    public static void main(String[] args) {


        RealMatrix m1= MatrixUtils.createRealMatrix(new double[][]{{1,1,2,3},{1,2,1,2},{3,1,1,3}});
        Covariance cov = new Covariance(m1);
        System.out.println();
    }
    public static Tuple<double[],double[][]> individualGene2Matrix(List<Individual> individuals){
        Collections.sort(individuals);
        double[] fitness = new double[individuals.size()];
        double geneMatrix [][]= new double[individuals.size()][];
        for (int i = 0; i < individuals.size(); i++) {
            geneMatrix[i] = individuals.get(i).getGenes();
            fitness[i] = individuals.get(i).getFitness();
        }
        return new Tuple<>(fitness,geneMatrix);
    }
    public static double[] calPopMeanGenes(List<Individual> population){
        double[] mean = new double[population.get(0).getGenes().length];
        double[][] tmpGenes = new double[population.size()][];
        for (int i = 0; i < population.get(0).getGenes().length; i++) {
            for (int j = 0; j < population.size(); j++) {
                mean[i] += population.get(j).getGenes()[i]/population.size();
            }
        }

        return mean;
    }
    public static List<Individual> matrix2IndividualGenes(double[][] matrix){
        List<Individual> individuals = new ArrayList<>();
        for (int i = 0; i < matrix.length; i++) {
            individuals.add(new Individual(matrix[i]));
        }
        return individuals;
    }
    public static double[] matrixSumAvg(double[][] data){
        // get avg value for each column
        // each row of data must have same columns
        double mean[] = new double[data[0].length];

        for (int i = 0; i < data[0].length; i++) {
            for (int j = 0; j < data.length; j++) {
                mean[i] += data[j][i]/data.length;
            }
        }
        return mean;
    }
    public static double[] matrixSumWeightWithFitness(Tuple<double[],double[][]> weightedData){
        double[][] data = weightedData.getSecond();
        double[] fitnesses = weightedData.getFirst();
        double fitnessSum = 0.;
        for (int i = 0; i < fitnesses.length; i++) {fitnessSum+=fitnesses[i];} // TODO: 2024/2/29 what if fitness is negative?
        
        // get avg value for each column
        // each row of data must have same columns
        double mean[] = new double[data[0].length];

        for (int i = 0; i < data[0].length; i++) {
            for (int j = 0; j < data.length; j++) {
                mean[i] += data[j][i]*fitnesses[j]/fitnessSum;
            }
        }
        return mean;
    }
}
