package optimizer;

import org.apache.commons.math3.linear.EigenDecomposition;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.stat.correlation.Covariance;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.Plan;
import utils.CalcPlanScore;
import utils.PlanCalcScoreParamDoubleArrayMapping;

import java.util.*;

/**
 * Author：Milu
 * this is deprecated ......
 * date：2024/3/13
 * project:matsimParamCalibration
 */
public class CMAES_Hansen {
    private static final Logger log = LogManager.getLogger(CMAES_Hansen.class);

    int N; // number of variables to be estimated, dimension
    RealMatrix xmean, zmean;//  new double[N][1] objective variables initial point
    double sigma = 0.5;// coordinate wise standard deviation (step-size), there is a series of complicated procedures to update this stupid parameter!
    OffSpring bestOffspring;
    double stopfitness = 1e-10;// stop if last iStopFitness >=5
    int iStopFitness = 0; // if last iteration's best fitness is better or difference <=stopfitness
    double stopeval;//  default:1e3*N^2  stop after stopeval number of function evaluations

    // Strategy parameter setting: Selection
    int lambda;  // default value: 4+floor(3*log(N)), population size, offspring number
    double mu;   // default value: lambda/2, top mu offsprings in the population, sorted according to fitness function
    //
    RealMatrix weights; // size: [mu][1] = Math.log(mu+1/2)-log(1:mu)';  all sorted offsprings  are using this to weight, when calculate means of current generation
    // muXone recombination weights
    double mueff; // value=sum(weights)^2/sum(weights.^2); // variance-effective size of mu

    // Strategy parameter setting: Adaptation
    // TODO: 2024/3/13 not sure why these parameters are set like this? They are all hyper parameters set by experience???
    double cc;  // cc = (4+mueff/N) / (N+4 + 2*mueff/N);  // time constant for cumulation for C
    double cs; // cs = (mueff+2)/(N+mueff+5);  // t-const for cumulation for sigma control
    double c1;  // c1 = 2 / ((N+1.3)^2+mueff);  // learning rate for rank-one update of C and
    double cmu; // cmu  = min(1-c1, 2*(mueff-2+1/mueff) / ((N+2)^2+2*mueff/2));  // for rank-mu update
    double damps;//  = 1 + 2*max(0, sqrt((mueff-1)/(N+1))-1) + cs; // damping for sigma

    // Initialize dynamic (internal) strategy parameters and constants //
    RealMatrix pc;  // default = zeros(N,1); size: double[N][1] evolution paths for C
    RealMatrix ps;  //  = zeros(N,1);   size: [N][1]// evolution paths for sigma
    RealMatrix B;   // default [N][N]= eye(N); B defines the coordinate system
    RealMatrix D;   // eye(N); diagonal matrix D defines the scaling
    RealMatrix C;   // c = B*D*(B*D)';                     // covariance matrix
    RealMatrix arz; // this gen normal based in each column, and total colSize = lambda, row = N
    int eigeneval = 0; // B and D updated at counteval == 0
    int counteval = 0; // generation loop index
    double chiN; // chiN = N^0.5*(1-1/(4*N)+1/(21*N^2));  // expectation of ||N(0,I)|| == norm(randn(N,1)) // TODO: 2024/3/13 but why set like this?
    double[] defaultParameters;
    List<OffSpring> population;// arx and arfitness in matsim code. each individual has genes which is Nx1 parameters to be optimized, with a fitness to be sorted later
//    RealMatrix arz; // arz = randn(N,1); % standard normally distributed vector
    double[][] paramBounds;
    public static void main(String[] args) {

    }

    public CMAES_Hansen(double[] defaultParameters,double[][] paramBounds) {
        N = defaultParameters.length; this.defaultParameters=defaultParameters;
        this.xmean = MatrixUtils.createColumnRealMatrix(defaultParameters);
        this.paramBounds = paramBounds;
    }

    /**
     * initialize some hyper-parameters
     */
    public void initialize(){
        stopeval = 1e3*N*N; // by default;
        // Strategy parameter setting: Selection initialization
        lambda = (int)(4 + Math.floor(3*Math.log(N)));
        mu = lambda/2.0;
        double[][] tmpWeights = new double[(int)mu][1];
        double sumWeights = 0.0;
        for (int i = 0; i < (int)mu; i++) {
            tmpWeights[i] = new double[]{Math.log(mu + 1.5) - Math.log(i+1.0)};
            sumWeights += tmpWeights[i][0];
        }
        mueff = calVarianceEffectiveSizeOfMu(tmpWeights);
        weights = MatrixUtils.createRealMatrix(tmpWeights).scalarMultiply(1/sumWeights);

        // Strategy parameter setting: Adaptation
        cc = (4.0+mueff/N) / (N+4.0 + 2.0*mueff/N);  // time constant for cumulation for C
        cs = (mueff+2.0)/(N+mueff+5.0);  // t-const for cumulation for sigma control
        c1 = 2.0 / ((N+1.3)*(N+1.3)+mueff);  //learning rate for rank-one update of C and
        cmu = Math.min(1.0-c1, 2.0*(mueff-2.0+1.0/mueff) / ((N+2.0)*(N+2.0)+2.0*mueff/2.0));  // for rank-mu update  ???????  2.0*mueff/2.0
        damps = 1.0 + 2.0*Math.max(0.0, Math.sqrt((mueff-1.0)/(N+1.0))-1.0) + cs; // damping for sigma
        //  Initialize dynamic (internal) strategy parameters and constants
        pc = MatrixUtils.createRealMatrix(N,1);
        ps = MatrixUtils.createRealMatrix(N,1); // evolution paths for C and sigma
        B = MatrixUtils.createRealIdentityMatrix(N); D = MatrixUtils.createRealIdentityMatrix(N);
        C = B.multiply(D).multiply(B.multiply(D).transpose());
        eigeneval = 0;                      // B and D updated at counteval == 0
        chiN = Math.pow(N,0.5)*(1-1/(4.0*N)+1/(21.0*N*N));  // expectation of ||N(0,I)|| == norm(randn(N,1))
        arz = MatrixUtils.createRealMatrix(N, lambda);
        // initilize first generation
        population = new ArrayList<>();
        for (int i = 0; i < lambda; i++) {
            RealMatrix tmpGeneMatrix = MatrixUtils.createRealMatrix(N,1);
            double[] tmpArz = new double[N];
            if (i ==0 ) {
                tmpGeneMatrix = xmean; for (int j = 0; j < N; j++) {tmpArz[j] =0.0; }
            }else {
                // make the random generated values in bounded range
                for (int j = 0; j < N; j++) { // note the paramBounds are in rows but tmpGenes and arzColumn are in column
                    double rndDouble = new Random().nextDouble();
                    tmpGeneMatrix.setEntry(j, 0, paramBounds[j][0] + (paramBounds[j][1] - paramBounds[j][0]) * rndDouble);
                    tmpArz[j] = rndDouble;
                }
//                tmpArz = StatisticalUtils.updateArzArray(xmean, tmpGeneMatrix, B, D, sigma).getColumn(0);
            }
            population.add(new OffSpring(tmpGeneMatrix,tmpArz));
        }
        C = new Covariance(popList2MatrixGenes(population).transpose()).getCovarianceMatrix();
        log.info("Initialization is done....");
    }

    /**
     *  Generate, evaluate and sort offspring
     */

    public double[] calGaussianArz(int n){
        double[] arzArray = new double[n];
        for (int i = 0; i < n; i++) {
            arzArray[i] = new Random().nextGaussian();
        }
        return arzArray;
    }
    /**
     * this is the core here, the fitness is the maximumLogLikelihood of each plan
     * @param planList
     */
    public void calFitness(Map<Person,List<Plan>> planList, Scenario scenario){
        for (int i = 0; i < this.population.size(); i++) {
            double[] estimatedParams = this.population.get(i).getGene();
            PlanCalcScoreParamDoubleArrayMapping planCalcScoreParamDoubleArrayMapping = new PlanCalcScoreParamDoubleArrayMapping(scenario.getConfig().planCalcScore());
            planCalcScoreParamDoubleArrayMapping.updateParamsPerDoubleArray(estimatedParams);
            CalcPlanScore.calAltPlanScores(planList,scenario);
            this.population.get(i).setFitness(MaximumLogLikelihoodFunction.cal(planList));
        }
    }
    public double calVarianceEffectiveSizeOfMu(double[][] weights){
        double sumWeights = 0.,sumWeightsSquare=0.;
        for (int i = 0; i < weights.length; i++) {
            sumWeights += weights[i][0];
            sumWeightsSquare += weights[i][0]*weights[i][0];
        }
        return sumWeights*sumWeightsSquare/sumWeightsSquare;
    }
    public double[] returnBestGenes(){
        return this.population.get(0).getGene();
    }
    public void genEvalSortOffsprings(Map<Person,List<Plan>> personListMap, Scenario scenario){

        for (int i = 0; i < lambda; i++) {
//            if (this.counteval>0&&i<Math.max(1,0.05*lambda)) // keep the 5 per best offsprings from last generation
//                continue;
            double[] tmpArz = calGaussianArz(N);
            RealMatrix arzColumn = MatrixUtils.createColumnRealMatrix(tmpArz);
            RealMatrix tmpGeneMatrix = xmean.add(B.multiply(D).multiply(arzColumn).scalarMultiply(sigma));// TODO: 2024/3/14 check if there are out of range,
            // make the random generated values in bounded range
            for (int j = 0; j < tmpGeneMatrix.getRowDimension(); j++) { // note the paramBounds are in rows but tmpGenes and arzColumn are in column

                while (!(paramBounds[j][0]<tmpGeneMatrix.getEntry(j,0)&&tmpGeneMatrix.getEntry(j,0)<paramBounds[j][1])){
                    double tmpRnd = new Random().nextGaussian();
                    tmpGeneMatrix.setEntry(j,0,paramBounds[j][0]+(paramBounds[j][1]-paramBounds[j][0])*tmpRnd);
                    tmpArz[j] = tmpRnd;
                }
//                tmpArz[j] = StatisticalUtils.updateArzArray(xmean,tmpGeneMatrix,B,D,sigma).getColumn(0)[j];
            }
            arz.setColumn(i,tmpArz);
            population.set(i, new OffSpring(tmpGeneMatrix,tmpArz));
        }
        calFitness(personListMap,  scenario);
        Collections.sort(population);
        if (this.bestOffspring==null){
            this.bestOffspring = population.get(0);
        }
    }
    public void loopGenerations(Map<Person,List<Plan>> planList, Scenario scenario){
        this.counteval = 0;
        calFitness(planList,scenario);
        Collections.sort(population);
        while(counteval < stopeval){
            // generate, calculate fitness and sort in descent order (maximize log likelihood)
            genEvalSortOffsprings(planList,scenario);
            // update weighted mean into xmean
            RealMatrix parentsMatrix = popList2MatrixGenes(population.subList(0,(int)mu));
            RealMatrix arzMatrix = popList2MatrixArz(population.subList(0,(int)mu));
            C =  new Covariance(parentsMatrix.transpose()).getCovarianceMatrix(); // because some variables are updated with limited ranges so here it has to re-calculate
            EigenDecomposition eigenDecompositionC = new EigenDecomposition(C);
            B = eigenDecompositionC.getV();
            D = squareRoot(eigenDecompositionC.getD());
            xmean = parentsMatrix.multiply(weights);
            zmean = arzMatrix.multiply(weights);
            // updateEvolutionPaths
            //        ps = (1-cs)*ps + (sqrt(cs*(2-cs)*mueff)) * (B * zmean);
            ps = ps.scalarMultiply(1.0-cs).add(B.multiply(zmean).scalarMultiply(Math.sqrt(cs*(1-cs)*mueff)));
            double normPs = MatrixUtils.createRealVector(ps.getColumn(0)).getNorm();
//            printMatrix(ps);
//        hsig= norm(ps)/sqrt(1-(1-cs)^(2*counteval/lambda))/chiN < 1.4+2/(N+1);
            double hsig = (normPs/Math.sqrt(1-Math.pow(1-cs,2*counteval/lambda))/chiN) < (1.4+2/(N+1))?1.0:0.0;
//        pc = (1-cc)*pc + hsig * sqrt(cc*(2-cc)*mueff) * (B*D*zmean);
            pc = pc.scalarMultiply(1-cc).add(B.multiply(D).multiply(zmean).scalarMultiply(hsig * Math.sqrt(cc*(2-cc)*mueff)));

            // adapt covariance matrix
//        C = (1-c1-cmu) * C ...                 % regard old matrix
//                + c1 * (pc*pc' ...                % plus rank one update
//                + (1-hsig) * cc*(2-cc) * C) ...  % minor correction
//        + cmu ...                         % plus rank mu update
//           * (B*D*arz(:,arindex(1:mu))) ...
//           *  diag(weights) * (B*D*arz(:,arindex(1:mu)))';
            RealMatrix diagWeights = MatrixUtils.createRealDiagonalMatrix(weights.getColumn(0));
            C = C.scalarMultiply(1-c1-cmu)
                    .add(pc.multiply(pc.transpose())
                            .add(C.scalarMultiply((1-hsig)*cc*(2-cc))).scalarMultiply(c1))
                    .add(B.multiply(D).multiply(arzMatrix).multiply(diagWeights)
                            .multiply(B.multiply(D).multiply(arzMatrix).transpose()).scalarMultiply(cmu));
            // adapt step-size sigma // TODO: 2024/3/13 there are other methods to update sigma
            sigma = sigma * Math.exp((cs/damps)*(normPs/chiN - 1));
            // update B and D from C
            if (counteval - eigeneval > lambda/(c1+cmu)/N/10.0){// to achieve O(Nˆ2)
                eigeneval = counteval;
                C = triuSymmetry(C);    //  C=triu(C)+triu(C,1)'enforce symmetry
                EigenDecomposition eigenDecompositionTmpC = new EigenDecomposition(C);
                B = eigenDecompositionTmpC.getV();
                D = squareRoot(eigenDecompositionTmpC.getD());
//                [B,D] = eig(C);       % eigen decomposition, B==normalized eigenvectors
//                D = diag(sqrt(diag(D))); % D contains standard deviations now
            }
            // break if last 5 ieration's best offspring is no better than stopfitness
            if(Math.abs(this.bestOffspring.getFitness()-population.get(0).getFitness())<stopfitness){
                iStopFitness++;
            }else {
                this.bestOffspring = population.get(0);
                iStopFitness = 0; // reset
            }
            if (iStopFitness>=10){
                log.info("no better solution for the last 10 generations, stop...");
                break;
            }
            counteval++;
            log.info("generation: "+counteval+ "/" +this.stopeval+" is done, and best score is "+this.bestOffspring.getFitness());
            System.out.print("");
        }
    }
    public static void printMatrix(RealMatrix matrix){
        for (int i = 0; i < matrix.getRowDimension(); i++) {
            for (int j = 0; j < matrix.getColumnDimension(); j++) {
                System.out.print(matrix.getEntry(i,j)+" ");
            }
            System.out.println();
        }
    }
    private static RealMatrix squareRoot(RealMatrix matrix) {
        int row = matrix.getRowDimension();
        int col = matrix.getColumnDimension();
        RealMatrix sqrtMatrix = matrix.copy();
        for (int i = 0; i < row; i++) {
            for (int j = 0; j < col; j++) {
                double newValue = Math.sqrt(sqrtMatrix.getEntry(i, j));
                sqrtMatrix.setEntry(i, j, newValue);
            }
        }
        return sqrtMatrix;
    }
    private static RealMatrix triuSymmetry(RealMatrix matrix) {
//        triu(C)+triu(C,1)';
        int rows = matrix.getRowDimension();
        int columns = matrix.getColumnDimension();
        RealMatrix newMatrix = matrix.copy(); // Create a copy of the original matrix
        // Set lower triangular elements to zero
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                if (i>j){
                    newMatrix.setEntry(i,j, matrix.getEntry(j,i));
                }
            }
        }
        return newMatrix;
    }
    public RealMatrix popList2MatrixGenes(List<OffSpring> parents){
        RealMatrix parentGenes = MatrixUtils.createRealMatrix(N,parents.size());// TODO: 2024/3/13 check if the col and row are right
        for (int i = 0; i < parents.size(); i++) {
            parentGenes.setColumn(i,parents.get(i).getGene());
        }
        return parentGenes;
    }
    public RealMatrix popList2MatrixArz(List<OffSpring> parents){
        RealMatrix parentGenes = MatrixUtils.createRealMatrix(N,(int)mu);// TODO: 2024/3/13 check if the col and row are right
        for (int i = 0; i < parents.size(); i++) {
            parentGenes.setColumn(i,parents.get(i).getArz());
        }
        return parentGenes;
    }
    public class OffSpring implements  Comparable<OffSpring> {
        private double[] gene;
        private RealMatrix geneMatrix;
        private double[] arz;
        private double fitness;
        @Override
        public int compareTo(OffSpring o) {
            return Double.compare(o.getFitness(), this.getFitness());// for descent
        }

        public double[] getGene() {
            return gene;
        }
        public OffSpring() {
        }
        public OffSpring(double[] gene) {
            this.gene = gene; this.fitness = -Double.MAX_VALUE;
            this.geneMatrix = MatrixUtils.createColumnRealMatrix(gene);
        }
        public OffSpring(RealMatrix geneMatrix, double[] arz) {
            this.geneMatrix = geneMatrix; this.fitness = -Double.MAX_VALUE;
            this.gene = geneMatrix.getColumn(0);
            this.arz = arz;
        }
        public RealMatrix getGeneMatrix() {
            return geneMatrix;
        }

        public double getFitness() {
            return fitness;
        }

        public void setGene(double[] gene) {
            this.gene = gene;
        }

        public void setGeneMatrix(RealMatrix geneMatrix) {
            this.geneMatrix = geneMatrix;
        }

        public void setFitness(double fitness) {
            this.fitness = fitness;
        }

        public void setArz(double[] arz) {
            this.arz = arz;
        }

        public double[] getArz() {
            return arz;
        }

    }
}
