package optimizer;

import org.apache.commons.math3.linear.*;
import org.apache.commons.math3.optim.ConvergenceChecker;
import org.apache.commons.math3.optim.OptimizationData;
import org.apache.commons.math3.optim.PointValuePair;
import org.apache.commons.math3.optim.nonlinear.scalar.noderiv.CMAESOptimizer;
import org.apache.commons.math3.random.JDKRandomGenerator;
import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.text.similarity.LevenshteinDistance;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.config.*;
import utils.PlanCalcScoreParamDoubleArrayMapping;

import java.io.BufferedWriter;
import java.io.FileWriter;

/**
 * Author：Milu
 * 严重落后进度啦......
 * date：2024/2/28
 * project:matsimParamCalibration
 */
public class test {
    public static void main(String[] args) throws Exception{
//        Config config = ConfigUtils.loadConfig("testInput2/gridConfig.xml");
//        PlanCalcScoreParamDoubleArrayMapping planCalcScoreParamDoubleArrayMapping = new PlanCalcScoreParamDoubleArrayMapping(config.planCalcScore());
//        BufferedWriter bw = new BufferedWriter(new FileWriter("testInput2/gridConfigPlanCalcParam.csv") );
//        bw.write("matsimIter,cmaesIter,bestFitness,"+CMAESwriter.array2Str(planCalcScoreParamDoubleArrayMapping.arrayIndex)+"\n");
//        bw.write("0,0,0,"+CMAESwriter.array2Str(planCalcScoreParamDoubleArrayMapping.initializeDoubleArray())+"\n");
//        bw.flush();bw.close();
//        String l = "南沙7路,2023/6/19 09:07:32,2,2,";
//        String[] tmp = l.split(",",-1);
//        System.out.println(tmp[4]==null);
//        LevenshteinDistance levenshtein = new LevenshteinDistance();
//        int test = levenshtein.apply("南沙68路(快)", "沙68路");
//        System.out.println(test);
        System.out.println(Math.exp(-5000578.424175608)==0.0);
        System.out.println(Math.exp(-7000939.20217822));
    }
    static class CustomConvergenceChecker implements ConvergenceChecker<PointValuePair> {
        @Override
        public boolean converged(int iteration, PointValuePair previous, PointValuePair current) {
            // 此处定义你的收敛检查逻辑
            // 示例：当两次迭代的目标函数值之差小于某个阈值时认为收敛
            double previousValue = previous.getValue();
            double currentValue = current.getValue();
            double threshold = 1e-6; // 设定阈值
            return Math.abs(currentValue - previousValue) < threshold;
        }
    }
    class OptData implements OptimizationData{

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
    public RealMatrix updateArzArray(RealMatrix xmean, RealMatrix newXmean, RealMatrix B, RealMatrix D, double sigma){
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
    private static void printMatrix(RealMatrix matrix) {
        int rows = matrix.getRowDimension();
        int columns = matrix.getColumnDimension();

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                System.out.print(matrix.getEntry(i, j) + " ");
            }
            System.out.println();
        }
    }
}
