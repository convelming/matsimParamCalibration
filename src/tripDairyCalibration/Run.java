package tripDairyCalibration;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Scenario;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.config.groups.PlansCalcRouteConfigGroup;
import org.matsim.core.controler.Controler;
import org.matsim.core.controler.OutputDirectoryHierarchy;
import org.matsim.core.gbl.Gbl;
import org.matsim.core.router.Dijkstra;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.core.utils.io.IOUtils;

import java.io.File;

/**
 * Author：Milu
 * 严重落后进度啦......
 * date：2023/11/7
 * project:huangpuScienceCity
 */
public class Run {
    final private static Logger LOG = LogManager.getLogger(Math.class);
    public static void run(){
        //        String configFile = "./data/configWithPt.xml";
//        String configFile = "/Users/convel/Downloads/matsim-example-project-master/scenarios/equil/config.xml";
//        String configFile = "examples/pt-simple-lineswitch/config.xml";
//        String configFile = "/Users/convel/Desktop/outputBeforeEvt/output_config.xml";\
//        String configFile = "/Users/convel/Documents/雄安新区数字推演/数据汇总/matsimInput/config.xml";
        String configFile = "/Users/convel/Documents/gzpi/MATSimGZ/networkCalibration/linkCount240123/config.xml";
        Config config = ConfigUtils.loadConfig(configFile);
        Controler controler = new Controler(config);
        controler.getConfig().controler().setOverwriteFileSetting( OutputDirectoryHierarchy.OverwriteFileSetting.deleteDirectoryIfExists );
//        controler.getConfig().qsim().setFlowCapFactor( 0.01 );
//        controler.getConfig().qsim().setStorageCapFactor( 0.01 );
//       result.setInfo( "MATSim is running. Depending on the size of your scenario , it may take from several minutes to a couple of hours..." +
//                " please wait patiently... " );
        System.out.println(" MATSim is running ... ");
        controler.getConfig().qsim().setRemoveStuckVehicles(true);
        controler.getConfig().controler().setLastIteration(10);
        controler.getConfig().controler().setOutputDirectory("/Users/convel/Desktop/output");
//        ArrayList<String> qsimMainModes = new ArrayList<>();
//        qsimMainModes.add("pt");qsimMainModes.add("car");
//        System.out.println(controler.getConfig().qsim().getMainModes());
//        controler.getConfig().qsim().setMainModes(qsimMainModes);
//        System.out.println(controler.getConfig().qsim().getMainModes());
        controler.getConfig().linkStats().setAverageLinkStatsOverIterations(5);
        controler.getConfig().linkStats().setWriteLinkStatsInterval(10);
        controler.run();// alt + command look for running instance

    }
    public static void main(String[] args) {
    runParrallel();
    }
    public static void runParrallel(){
//        String configFilename = "/Users/convel/Documents/gzpi/MATSimGZ/scenario240127/config.xml";
        String configFilename = "testInput/config.xml";
        /* Running multi-threaded. First delete the output directory. */
        Gbl.startMeasurement();
        Config config = ConfigUtils.loadConfig(configFilename);
        config.controler().setOverwriteFileSetting( OutputDirectoryHierarchy.OverwriteFileSetting.deleteDirectoryIfExists );
        /*====================================================================*/
        /* Setting parallelisation: */
        config.qsim().setNumberOfThreads(12);					/* Mobility simulation */
        config.global().setNumberOfThreads(12); 					/* Replanning */
        config.parallelEventHandling().setNumberOfThreads(12);	/* Events handling. */
        config.plansCalcRoute().setAccessEgressType(PlansCalcRouteConfigGroup.AccessEgressType.accessEgressModeToLink);
        /*====================================================================*/
        Controler controler = new Controler(config);
        controler.getConfig().qsim().setRemoveStuckVehicles(true);
        controler.getConfig().controler().setLastIteration(10);
        controler.getConfig().controler().setOutputDirectory("/Users/convel/Desktop/output");
        controler.run();
        LOG.info("Multi-threaded time:");
        Gbl.printElapsedTime();

    }
}
