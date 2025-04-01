package population;

import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.*;
import org.matsim.api.core.v01.population.PopulationFactory;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.population.PopulationUtils;

import java.util.Random;

/**
 * Author：Milu
 * 严重落后进度啦......
 * date：2024/1/15
 * project:matsimParamCalibration
 */
public class SynExamplePop {
    public static void main(String[] args) {
        // creat test population for testInput
        Population population  = PopulationUtils.createPopulation(ConfigUtils.createConfig());
        PopulationFactory pf = population.getFactory();
        Random random = new Random();
        for (int i = 0; i < 3000; i++) {
            Person person = pf.createPerson(Id.createPersonId("person_"+i));
            if(i%10==0) person.getAttributes().putAttribute("subpopulation","test");
            Plan plan = pf.createPlan();
            Coord homeCoord = new Coord(-300+random.nextDouble()*2500.,-150+1500*random.nextDouble());
            Activity homeAct = pf.createActivityFromCoord("home",homeCoord);
            double tmpTime = 3600*7.0+7200* random.nextDouble();
            homeAct.setEndTime(tmpTime);
            plan.addActivity(homeAct);
            tmpTime += 3600*6.0+7200* random.nextDouble();
            Leg leg = pf.createLeg(random.nextDouble()>0.4?"pt":"car");
            plan.addLeg(leg);
            Activity workAct = pf.createActivityFromCoord("work",new Coord(-300+random.nextDouble()*2500.,-150+1500*random.nextDouble()));
            workAct.setEndTime(tmpTime);
            plan.addActivity(workAct);
            plan.addLeg(leg);
            plan.addActivity(pf.createActivityFromCoord("home",homeCoord));
            person.addPlan(plan);
            population.addPerson(person);
        }

        new PopulationWriter(population).write("/Users/convel/IdeaProjects/matsimParamCalibration/testInput/testPopulation.xml");
    }
}
