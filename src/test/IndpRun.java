package test;

import org.matsim.api.core.v01.Scenario;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.config.groups.PlansCalcRouteConfigGroup;
import org.matsim.core.controler.Controler;
import org.matsim.core.controler.OutputDirectoryHierarchy;
import org.matsim.core.scenario.ScenarioUtils;
import planParam.PlanParamCalibrationConfigGroup;
import planParam.PlanParamCalibrationModule;

public class IndpRun {

    public static void main(String[] args) {
        String iterFile = "testInput/config.xml";
        String outputFile = "paramCalibration/indepOutput";
        for (int i =0;i < 10; i++) {

            Config config = ConfigUtils.loadConfig(iterFile, PlanParamCalibrationConfigGroup.createDefaultConfig());
            PlanParamCalibrationConfigGroup planParamCalibrationConfigGroup = (PlanParamCalibrationConfigGroup)config.getModules().get("PlanParamCalibrationConfigGroup");
            planParamCalibrationConfigGroup.setGroundTruthSubPopulation("test");
            Scenario scenario = ScenarioUtils.loadScenario(config) ;
            Controler controler = new Controler( scenario ) ;
            controler.getConfig().controler().setLastIteration(20);
//            controler.getConfig().plans().setInputFile(outputFile+(i-1)+"/output_plans.xml.gz");
            controler.addOverridingModule(new PlanParamCalibrationModule(config));
            config.plansCalcRoute().setAccessEgressType(PlansCalcRouteConfigGroup.AccessEgressType.accessEgressModeToLink);
            controler.getConfig().controler().setOverwriteFileSetting( OutputDirectoryHierarchy.OverwriteFileSetting.deleteDirectoryIfExists );
            controler.getConfig().controler().setOutputDirectory(outputFile+i);
            controler.run();
//            iterFile = outputFile+i+"/output_config.xml";

        }

    }
}
