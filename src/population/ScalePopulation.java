package population;

import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.population.Population;
import org.matsim.api.core.v01.population.PopulationWriter;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.population.PopulationUtils;
import org.matsim.core.scenario.ScenarioUtils;

import java.io.File;
import java.util.Random;

public class


ScalePopulation {
    public static Population loadPopulation(String file){
        Config config = ConfigUtils.createConfig();
        config.plans().setInputFile(file);
        Scenario sc = ScenarioUtils.loadScenario(config);
        return sc.getPopulation();
    }
    public static Population scalePopulation(Population population, double scaleFactor){
        Random random = new Random();
        System.out.println("Full population size: " + population.getPersons().size());
        Population newPop = PopulationUtils.createPopulation(ConfigUtils.createConfig());
        population.getPersons().forEach((personId, person) -> {
            if(random.nextDouble()<=scaleFactor){
                newPop.addPerson(person);
            }
        });
        System.out.println("Scaled population size: " + newPop.getPersons().size());
        return newPop;
    }
    public static Population mergePopulation(String folder){
        Population population = PopulationUtils.createPopulation(ConfigUtils.createConfig());
        File folderFile = new File(folder);
        File[] popFile = folderFile.listFiles();
        for (int i = 0; i < popFile.length; i++) {
            String abPath = popFile[i].getAbsolutePath();
            System.out.println(abPath);
            if (abPath.contains(".xml")){
                Population tmpPopulation = PopulationUtils.readPopulation(popFile[i].getAbsolutePath());
                tmpPopulation.getPersons().forEach((id,person)->{
                    if(!population.getPersons().containsKey(id)){
                        population.addPerson(person);
                    }
                });
            }
        }
        System.out.println(population.getPersons().size());
        return population;
    }
    public static void main(String[] args) {
        Population pop = loadPopulation("/Users/convel/Documents/gzpi/MATSimGZ/scenarioSimNets/240407/hpPop100w.xml.gz");
        System.out.println(pop.getPersons().size());
//        Population pop = scalePopulation(loadPopulation("data/cityRenewal/kangle/population/originalPopFromCellularData.xml"),0.001);
//        new PopulationWriter(pop).write("./data/pop/testPop.xml");
        Population newPop = scalePopulation(pop,0.01);
        new PopulationWriter(newPop).write("/Users/convel/Documents/gzpi/MATSimGZ/scenarioSimNets/240407/hpPop100w1per.xml");
    }
}
