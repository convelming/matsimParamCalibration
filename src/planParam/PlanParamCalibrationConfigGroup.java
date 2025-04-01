package planParam;

import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.config.ConfigWriter;
import org.matsim.core.config.ReflectiveConfigGroup;
import utils.CalibrationEndCriteria;
import utils.MLEoptimizer;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Author：Milu
 * 严重落后进度啦......
 * date：2024/1/8
 * project:matsimParamCalibration
 */
public class PlanParamCalibrationConfigGroup extends ReflectiveConfigGroup {

    public static final String GROUP_NAME = "PlanParamCalibrationConfigGroup";

    private static final String FIXEDSUBPOPULATION = "fixedSubPopulation";
    private String groundTruthSubPopulation = "";
    private String updatePlanScorePopParamSubpopulation; // TODO: 2024/3/7  config this , as the planCalc perametersets is a set bounded with population
    private static final String MLEOPTIMIZER = "cmaesOptimizer";
    private MLEoptimizer mleOptimizer = MLEoptimizer.CMAES;

    //There are two strategies to end the mle algorithm, reach convergence or iterate a designated number of times
    private static final String CALIBRATIONENDCRITERIA = "calibrationEndCriteria";
    private CalibrationEndCriteria calibrationEndCriteria = CalibrationEndCriteria.designatedIteration;

    private static final String DESIGNATEDITERATION = "designatedIteration";
    private int designatedIteration = 100; // default is 100 only activated when CalibrationEndCriteria.designatedIteration
    // if the difference of consecutive 10 iterations is smaller than this threshold value the mle will stop
    private static final String CONVERGEDIFFTHRESHOLD = "convergeDiffThreshold";

    private double convergeDiffThreshold = 0.00001; //

    private  static final String MAXALTERNATIVEPLANS = "maxAlternativePlans";

    private int maxAlternativePlans = 5; // default is 5
    public PlanParamCalibrationConfigGroup() {super(GROUP_NAME);}

    public static PlanParamCalibrationConfigGroup createDefaultConfig(){
        PlanParamCalibrationConfigGroup config = new PlanParamCalibrationConfigGroup();
        return config;
    }

    /**
     * Loads a PlanParamCalibrationConfigGroup Config File, <p/>
     * // TODO: 2024/1/8  check if other config group can also be loaded.
     * @param configFile the PublicTransitMapping config file (xml)
     */
    public static PlanParamCalibrationConfigGroup loadConfig(String configFile) {
        Config configAll = ConfigUtils.loadConfig(configFile, new PlanParamCalibrationConfigGroup());
        return ConfigUtils.addOrGetModule(configAll, PlanParamCalibrationConfigGroup.GROUP_NAME, PlanParamCalibrationConfigGroup.class);
    }

    /**
     * removes all other modules and only save the PlanParamCalibrationConfigGroup
     * @param filename
     */
    public void writeToFile(String filename) {
        Config matsimConfig = ConfigUtils.createConfig();
        matsimConfig.addModule(this);
        Set<String> toRemove = matsimConfig.getModules().keySet().stream().filter(module -> !module.equals(PlanParamCalibrationConfigGroup.GROUP_NAME)).collect(Collectors.toSet());
        toRemove.forEach(matsimConfig::removeModule);
        new ConfigWriter(matsimConfig).write(filename);
    }


    /**
     * config setup comments of this config group
     */
    @Override
    public final Map<String, String> getComments() {
        Map<String, String> map = super.getComments();
        map.put(FIXEDSUBPOPULATION,"fixed subpopulation addresses initial plans’ subpopulation attribute. " +
                "This module uses these subpopulations’ selected plan to adjust some act and trip parameters in planCalcScore module. Parameter update only focuses on null subpopulation");
        map.put(MLEOPTIMIZER,"optimizer used in mle, CmaEvolutionStrategy, SGD, gradientDescent, RMSprop,adam is planned, but only CmaEvolutionStrategy is considered and developed");
        map.put(CALIBRATIONENDCRITERIA,"Criteria to end iterations of mle. This parameter specifies when to stop update the parameters before MATSim Qsim-iteration starts, options: designatedIteration, reachConvergence, default:designatedIteration(default maxItertaion=100). By default, the module will update parameters in planCalcScore till min(designatedIteration, iteration). If reachCovergence is set, updating parameters stops when average estimated difference is smaller than 0.001.");
        map.put(DESIGNATEDITERATION,"");
        map.put(CONVERGEDIFFTHRESHOLD,"");
        map.put(MAXALTERNATIVEPLANS,"max alterantive plans for each trip maker, including the one being chosen,should be the same as in strategy's maxAgentPlanMemorySize.");
        return map;
    }
    @StringGetter(FIXEDSUBPOPULATION)
    public String getGroundTruthSubPopulation() {
        return groundTruthSubPopulation;
    }
    @StringGetter(MLEOPTIMIZER)
    public MLEoptimizer getMleOptimizer() {
        return mleOptimizer;
    }
    @StringGetter(CALIBRATIONENDCRITERIA)
    public CalibrationEndCriteria getCalibrationEndCriteria() {
        return calibrationEndCriteria;
    }
    @StringGetter(DESIGNATEDITERATION) public int getDesignatedIteration() {
        return designatedIteration;
    }

    @StringGetter(CONVERGEDIFFTHRESHOLD) public double getConvergeDiffThreshold() {
        return convergeDiffThreshold;
    }

    @StringGetter(MAXALTERNATIVEPLANS) public int getMaxAlternativePlans() {
        return maxAlternativePlans;
    }

    @StringSetter(FIXEDSUBPOPULATION)
    public void setGroundTruthSubPopulation(String groundTruthSubPopulation) {
        this.groundTruthSubPopulation = groundTruthSubPopulation;
    }

    @StringSetter(MLEOPTIMIZER)
    public void setMleOptimizer(MLEoptimizer mleOptimizer) {
        this.mleOptimizer = mleOptimizer;
    }

    @StringSetter(CALIBRATIONENDCRITERIA)
    public void setCalibrationEndCriteria(CalibrationEndCriteria calibrationEndCriteria) {
        this.calibrationEndCriteria = calibrationEndCriteria;
    }

    @StringSetter(DESIGNATEDITERATION)
    public void setDesignatedIteration(int designatedIteration) {
        this.designatedIteration = designatedIteration;
    }

    @StringSetter(CONVERGEDIFFTHRESHOLD)
    public void setConvergeDiffThreshold(double convergeDiffThreshold) {
        this.convergeDiffThreshold = convergeDiffThreshold;
    }

    @StringSetter(MAXALTERNATIVEPLANS) public void setMaxAlternativePlans(int maxAlternativePlans) {
        this.maxAlternativePlans = maxAlternativePlans;
    }


}

