package planParam;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.checkerframework.checker.units.qual.C;
import org.matsim.core.config.Config;
import org.matsim.core.controler.AbstractModule;
import org.matsim.core.replanning.GenericStrategyManager;
import org.matsim.core.replanning.StrategyManager;

/**
 * Author：Milu
 * 严重落后进度啦......
 * date：2024/1/8
 * project:matsimParamCalibration
 */
public class PlanParamCalibrationModule extends AbstractModule {
    final static String moduleName = "PlanParamCalibrationModule";
    EstimateUpdateScoringParameterListener estimateUpdateScoringParameterListener;
    public PlanParamCalibrationModule(Config config) {
        super();
        estimateUpdateScoringParameterListener = new EstimateUpdateScoringParameterListener();

    }

    @Override
    public void install() {
        this.addControlerListenerBinding().toInstance(new EstimateUpdateScoringParameterListener());
    }
}
