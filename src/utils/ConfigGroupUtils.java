package utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.matsim.core.config.ConfigGroup;
import org.matsim.core.config.groups.PlanCalcScoreConfigGroup;
import planParam.EstimateUpdateScoringParameterListener;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Set;

/**
 * Author：Milu
 * 严重落后进度啦......
 * this class unlock parameters in ConfigGroup. Negative effects need to be discovered and further discussed.
 * It must have a reason to lock those parameters...
 * date：2024/3/6
 * project:matsimParamCalibration
 */
public class ConfigGroupUtils {
    private static final Logger log = LogManager.getLogger(ConfigGroupUtils.class);

    public static void unlock(ConfigGroup config){
        Class<?> zlass = config.getClass();
        while (zlass != null && !zlass.getName().endsWith(".ConfigGroup")) {
            zlass = zlass.getSuperclass();
        }

        if(zlass == null){
            log.error(config.getClass() + " is not configGroup . ");
            return;
        }

        try {
            Field locked = zlass.getDeclaredField("locked");
            locked.setAccessible(true);
            locked.set(config, false);
            log.info(config.getClass() + " locked set false success .");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static void unlockPlanCalScoreConfigGroup(PlanCalcScoreConfigGroup planCalcScoreConfigGroup){
        // set first layer parameters unlock
        unlock(planCalcScoreConfigGroup);

        // set scoring parameters unlock
        unlock(planCalcScoreConfigGroup.getScoringParameters(null));

        // set activityParameters unlock it is a set so need to be looped
        unlock(planCalcScoreConfigGroup.getScoringParameters(null));

        // set activityParameters unlock it is a set so need to be looped
        Set<String> actList = (Set<String>) planCalcScoreConfigGroup.getActivityTypes();
        actList.forEach(act->{
            log.info("act: "+act+ " in planCalcScoreConfigGroup is unlocked");
            unlock(planCalcScoreConfigGroup.getScoringParameters(null).getActivityParams(act));

        });
        // set modeParameters unlock it is a set so need to be looped
        Set<String> modeList = (Set<String>) planCalcScoreConfigGroup.getAllModes();
        modeList.forEach(mode->{
            log.info("mode: "+mode+ " in planCalcScoreConfigGroup is unlocked");
            unlock(planCalcScoreConfigGroup.getModes().get(mode));
        });

        log.info("");
    }
}
