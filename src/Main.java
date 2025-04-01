import org.matsim.api.core.v01.Scenario;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.config.groups.PlansCalcRouteConfigGroup;
import org.matsim.core.controler.Controler;
import org.matsim.core.controler.OutputDirectoryHierarchy;
import org.matsim.core.scenario.ScenarioUtils;
import planParam.PlanParamCalibrationConfigGroup;
import planParam.PlanParamCalibrationModule;

/**
 * Author：Milu
 * 严重落后进度啦......
 * date：${DATE}
 * project:${PROJECT_NAME}
 */
public class Main {
    public static void main(String[] args) {


//        Config config = ConfigUtils.loadConfig("/Users/convel/Documents/gzpi/MATSimGZ/paramCalibration/outputOnlySurvey0521_sigma1per_2/output_config.xml", PlanParamCalibrationConfigGroup.createDefaultConfig());
//        Config config = ConfigUtils.loadConfig("testInput/config.xml", PlanParamCalibrationConfigGroup.createDefaultConfig());
//        Config config = ConfigUtils.loadConfig("/Users/convel/IdeaProjects/matsimParamCalibration/testInput/config.xml", PlanParamCalibrationConfigGroup.createDefaultConfig());
        Config config = ConfigUtils.loadConfig("/Users/convel/Documents/gzpi/MATSimGZ/paramCalibration/config.xml", PlanParamCalibrationConfigGroup.createDefaultConfig());
        PlanParamCalibrationConfigGroup planParamCalibrationConfigGroup = (PlanParamCalibrationConfigGroup)config.getModules().get("PlanParamCalibrationConfigGroup");
//        planParamCalibrationConfigGroup.setGroundTruthSubPopulation("survey");
        planParamCalibrationConfigGroup.setGroundTruthSubPopulation("survey");
        config.plansCalcRoute().setAccessEgressType(PlansCalcRouteConfigGroup.AccessEgressType.accessEgressModeToLink);
        Scenario scenario = ScenarioUtils.loadScenario(config);
        Controler controler = new Controler( scenario );
        controler.getConfig().controler().setLastIteration(20);
        controler.addOverridingModule(new PlanParamCalibrationModule(config));
        config.qsim().setRemoveStuckVehicles(true);
        config.qsim().setStuckTime(5.0);
        controler.getConfig().controler().setOverwriteFileSetting( OutputDirectoryHierarchy.OverwriteFileSetting.deleteDirectoryIfExists );
//        controler.getConfig().controler().setOutputDirectory("/Users/convel/IdeaProjects/matsimParamCalibration/paramCalibration/outputOnlyWithCMAES0606");
        controler.getConfig().controler().setOutputDirectory("/Users/convel/desktop/cmaesWithBound1perC4Z0722");
//        controler.getConfig().plans().setInputFile("/Users/convel/IdeaProjects/matsimParamCalibration/linkCount230427/pop/testPop.xml");
        controler.run();

    }


}