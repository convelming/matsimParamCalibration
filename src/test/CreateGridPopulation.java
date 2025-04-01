package test;

import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.population.*;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.population.PopulationUtils;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;

/**
 * Author：Milu
 * 严重落后进度啦......
 * date：2024/6/26
 * project:matsimParamCalibration
 */
public class CreateGridPopulation {
    public static void main(String[] args) throws IOException {
        // creat test population for testInput
        Population population  = PopulationUtils.createPopulation(ConfigUtils.createConfig());
        PopulationFactory pf = population.getFactory();
        Random random = new Random();
        double baseX = 12640311.-2000;double baseY = 2598999.0-2000;
        BufferedWriter bw = new BufferedWriter(new FileWriter("/users/convel/desktop/testCoord.csv"));
        bw.write("act,x,y"+"\n");
        for (int i = 0; i < 40000; i++) {
            Person person = pf.createPerson(Id.createPersonId("person_"+i));
            if (random.nextDouble()<0.3){
                person.getAttributes().putAttribute("license",true);
                person.getAttributes().putAttribute("car_avail","always");
            }else {
                person.getAttributes().putAttribute("license",false);
                person.getAttributes().putAttribute("car_avail","nerver");
            }


            if(i%1000==0) person.getAttributes().putAttribute("subpopulation","test");
            Plan plan = pf.createPlan();
            Coord homeCoord = new Coord(baseX+random.nextDouble()*4000.,baseY+4000*random.nextDouble());
            Activity homeAct = pf.createActivityFromCoord("home",homeCoord);
            bw.write("home,"+homeCoord.getX()+","+homeCoord.getY()+"\n");
            double tmpTime = 3600*8.0;
            homeAct.setEndTime(tmpTime);
            plan.addActivity(homeAct);
            tmpTime += 3600*8.0;
//            Leg leg = pf.createLeg(random.nextDouble()>0.4?"pt":"car");
            Leg leg = pf.createLeg("car");
            plan.addLeg(leg);
//            Activity workAct = pf.createActivityFromCoord("work",new Coord(Math.max(baseX+2000.,Math.min(baseX+random.nextGaussian()*4000.,baseX+2000.)),Math.max(baseY+2000.,Math.min(baseY+random.nextGaussian()*4000.,baseY+2000.))));
            Activity workAct = pf.createActivityFromCoord("work",new Coord(baseX+2000+random.nextGaussian()*500,baseY+2000+random.nextGaussian()*500.));
            bw.write("work,"+workAct.getCoord().getX()+","+workAct.getCoord().getY()+"\n");

            workAct.setEndTime(tmpTime);
            plan.addActivity(workAct);
            plan.addLeg(leg);
            plan.addActivity(pf.createActivityFromCoord("home",homeCoord));
            person.addPlan(plan);
            population.addPerson(person);
        }
        bw.flush();bw.close();
        new PopulationWriter(population).write("testInput2/gridPop.xml");
    }
}
