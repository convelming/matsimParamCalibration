package test;

import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.config.ConfigWriter;
import org.matsim.core.config.groups.PlanCalcScoreConfigGroup;
import org.matsim.core.config.groups.StrategyConfigGroup;

/**
 * Author：Milu
 * 严重落后进度啦......
 * date：2024/6/26
 * project:matsimParamCalibration
 */
public class CreateGridConfig {
    public static void main(String[] args) {
        Config config = ConfigUtils.createConfig();
        config.strategy().setMaxAgentPlanMemorySize(4);
        // add home
        PlanCalcScoreConfigGroup.ActivityParams homeParams = new PlanCalcScoreConfigGroup.ActivityParams("home");
        homeParams.setTypicalDuration(8*3600.);
        PlanCalcScoreConfigGroup.ActivityParams workParams = new PlanCalcScoreConfigGroup.ActivityParams("work");
        workParams.setMinimalDuration(7.0*3600.);workParams.setTypicalDuration(8.0*3600.);

        config.planCalcScore().addActivityParams(homeParams);
        config.planCalcScore().addActivityParams(workParams);
//        <!-- strategyName of strategy.  Possible default names: SelectRandom BestScore KeepLastSelected ChangeExpBeta SelectExpBeta SelectPathSizeLogit
//        (selectors),
//                ReRoute TimeAllocationMutator TimeAllocationMutator_ReRoute ChangeSingleTripMode ChangeTripMode SubtourModeChoice (innovative strategies). -->

        StrategyConfigGroup.StrategySettings strategySettingsSelector = new StrategyConfigGroup.StrategySettings();
        strategySettingsSelector.setSubpopulation("test");strategySettingsSelector.setStrategyName("BestScore");strategySettingsSelector.setWeight(1.0);
        StrategyConfigGroup.StrategySettings strategySettingsInnovStr = new StrategyConfigGroup.StrategySettings();
        strategySettingsInnovStr.setSubpopulation("test");strategySettingsInnovStr.setStrategyName("TimeAllocationMutator_ReRoute");strategySettingsInnovStr.setWeight(1.0);
        StrategyConfigGroup.StrategySettings strategySettingsInnovStr1 = new StrategyConfigGroup.StrategySettings();
        strategySettingsInnovStr1.setSubpopulation("test");strategySettingsInnovStr1.setStrategyName("ChangeTripMode");strategySettingsInnovStr1.setWeight(1.0);
        config.strategy().addStrategySettings(strategySettingsSelector);        config.strategy().addStrategySettings(strategySettingsInnovStr);
        config.strategy().addStrategySettings(strategySettingsInnovStr1);
        StrategyConfigGroup.StrategySettings strategySettingsSelectorNull = new StrategyConfigGroup.StrategySettings();
        strategySettingsSelectorNull.setStrategyName("BestScore");strategySettingsSelectorNull.setWeight(1.0);
        StrategyConfigGroup.StrategySettings strategySettingsInnovStrnull = new StrategyConfigGroup.StrategySettings();
        strategySettingsInnovStrnull.setStrategyName("TimeAllocationMutator_ReRoute");strategySettingsInnovStrnull.setWeight(1.0);
        StrategyConfigGroup.StrategySettings strategySettingsInnovStrnull1 = new StrategyConfigGroup.StrategySettings();
        strategySettingsInnovStrnull1.setStrategyName("ChangeTripMode");strategySettingsInnovStrnull1.setWeight(1.0);
        config.strategy().addStrategySettings(strategySettingsSelectorNull);        config.strategy().addStrategySettings(strategySettingsInnovStrnull);
        config.strategy().addStrategySettings(strategySettingsInnovStrnull1);
        config.planCalcScore().setLearningRate(0.5);
        // input file
        config.global().setCoordinateSystem("epsg:3857");
        config.plans().setInputFile("gridPop.xml");
        config.network().setInputFile("gridNetwork.xml");

//        config.transit().setUseTransit(true);
//        config.transit().setTransitScheduleFile("");
//        config.transit().setVehiclesFile("");
//        config.changeMode().setIgnoreCarAvailability(false);
//        config.changeMode().setModes(new String[]{"car","pt"});
        new  ConfigWriter(config).write("testInput2/gridConfig.xml");
    }
}
