package population;

import org.apache.commons.math3.distribution.MultivariateNormalDistribution;
import org.matsim.api.core.v01.Coord;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.router.TripRouter;
import org.matsim.facilities.ActivityFacilities;
import org.matsim.facilities.ActivityFacility;
import facilities.BuildingFacilities2Act;

import java.util.Random;

/**
 * Author：Milu
 * 严重落后进度啦......
 * date：2024/1/11
 * project:matsimParamCalibration
 */
public class Test {
    public static void main(String[] args) throws Exception{
        System.out.println(-0.0==0.0);
    }
    public static void testPlanRouter(){

        Config config = ConfigUtils.createConfig();
        TripRouter.Builder trBuilder = new TripRouter.Builder(config);
//        trBuilder.setRoutingModule("car", RoutingModule)
//        TripRouter tr = new TripRouter.Builder(config);
//        PlanRouter pr = new PlanRouter()
    }
}
