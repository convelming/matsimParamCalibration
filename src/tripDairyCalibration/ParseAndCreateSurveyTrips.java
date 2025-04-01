package tripDairyCalibration;

import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.population.*;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.population.PopulationUtils;
import org.matsim.core.utils.geometry.CoordUtils;
import org.matsim.core.utils.geometry.CoordinateTransformation;
import org.matsim.core.utils.geometry.transformations.TransformationFactory;
import utils.GCJ02_WGS84;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.Random;

/**
 * Author：Milu
 * 严重落后进度啦......
 * date：2023/11/2
 * project:geotools-29.2
 */
public class ParseAndCreateSurveyTrips {
    public static void parseTxtFile(String file)throws Exception{
//        String odFile = "data/commutingOds.csv";
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file),"GBK"));
        String line= br.readLine();String header[] = line.split(",");
        System.out.println(line);
        CoordinateTransformation ct = TransformationFactory.getCoordinateTransformation("epsg:4326","epsg:4526");
        Population population = PopulationUtils.createPopulation(ConfigUtils.createConfig());
        PopulationFactory populationFactory = population.getFactory();


        while((line=br.readLine())!=null){
            String[]  tmp = line.split(",",-1);
            Person person = populationFactory.createPerson(Id.createPersonId("survey_"+tmp[0]+"_"+tmp[1]));
            //<!ATTLIST person
//        id             CDATA                    #REQUIRED
//        sex            (f|m)                    #IMPLIED
//        age            CDATA                    #IMPLIED
//        license        (yes|no)                 #IMPLIED
//        car_avail      (always|never|sometimes) #IMPLIED
//        employed       (yes|no)                 #IMPLIED>
            person.getAttributes().putAttribute("sex",tmp[4].equals("男")?"m":"f");
            person.getAttributes().putAttribute("age",getAge(tmp[5]));
            person.getAttributes().putAttribute("license",getLicense(tmp[23]));
            person.getAttributes().putAttribute("car_avail",getCarAvail(tmp[24]));
            person.getAttributes().putAttribute("employed",tmp[6].equals("x0_待业")||tmp[6].equals("'x0_离退休人员'")?false:true);
            Plan plan = PopulationUtils.createPlan();
            // home act
            Coord homeCoord = ct.transform(new Coord(GCJ02_WGS84.gcj02toWgs84(Double.parseDouble(tmp[43]),Double.parseDouble(tmp[44]))));
            Coord lastCoord = homeCoord;
            double depTime = 0.0;
            Activity lastAct = populationFactory.createActivityFromCoord("home",homeCoord);
            int iLoc = 46;
            while(!tmp[iLoc].equals("")){
                String actType = getActType(tmp[iLoc+4]);
                String mode = getMode(tmp[iLoc+2]);
                System.out.println(tmp[0]+","+ tmp[1]+", iLoc: "+iLoc);
                double tmpDepTime = (tmp[iLoc+6].equals("")?0.0:Double.parseDouble(tmp[iLoc+6])*3600.) + (tmp[iLoc+7].equals("")?0.0:Double.parseDouble(tmp[iLoc+7])*60.);
//                double tmpArrTime = (tmp[iLoc+8]!=""?Double.parseDouble(tmp[iLoc+8])*3600.:0.0)+ (tmp[iLoc+9]!=""?Double.parseDouble(tmp[iLoc+9])*60.:0.0);
//                double tmpDuration = (tmp[iLoc+10]!=""?Double.parseDouble(tmp[iLoc+10])*3600.:0.0)+ (tmp[iLoc+11]!=""?Double.parseDouble(tmp[iLoc+11])*60.:0.0);
                double filledDis = tmp[iLoc+12].equals("")?0.0:Double.parseDouble(tmp[iLoc+12]);
//                double cost = tmp[iLoc+13]!=""?Double.parseDouble(tmp[iLoc+13]):0.0;
                Coord actCoord = ct.transform(new Coord(GCJ02_WGS84.gcj02toWgs84(Double.parseDouble(tmp[iLoc]),Double.parseDouble(tmp[iLoc+1]))));
                double eucDis = CoordUtils.calcEuclideanDistance(actCoord,lastCoord);
                depTime = tmpDepTime!=0.0?tmpDepTime:(depTime+(filledDis!=0.?filledDis:eucDis/1000.0)/20.0*3600.);
                lastAct.setEndTime(depTime);
                lastCoord = actType.equals("home")?homeCoord:actCoord;
                plan.addActivity(lastAct);plan.addLeg(populationFactory.createLeg(mode));
                lastAct= populationFactory.createActivityFromCoord(actType,actType.equals("home")?homeCoord:actCoord);
                iLoc += 16;
            }
            plan.addActivity(lastAct);
            person.addPlan(plan);
            population.addPerson(person);
            // locaiton 46 47 mode 48 actype 50 deptime 52 53, arrTime 54 55, duration 56 57, estDis 58, cost 59,
            //          62 63 mode 64        66         68 69          70 71           72 73,        74,      75
            //          78 79
            //          94 95
            //          110 111
        }
        br.close();
        new PopulationWriter(population).write("/Users/convel/desktop/surveyPop.xml");
    }
    public static String getActType(String str){
        // 工作, 接送孩子，需接送共几人,买菜,其他,上学,休闲娱乐,业务办事,回住所
        if(str.equals("工作"))return "work";
        if(str.equals("接送孩子，需接送共几人"))return "pickup/dropoff";
        if(str.equals("买菜"))return "shopping";
        if(str.equals("上学"))return "education";
        if(str.equals("休闲娱乐"))return "leisure";
        if(str.equals("回住所"))return "home";
        if(str.equals("业务办事"))return "errand";
        return "other";
    }
    public static String getMode(String str){
//        步行,地铁,电动自行车,工作,公交,共享单车,共享单车/单车,摩托车,其他,私家车,网约车/出租车 todo
        if(str.equals("步行")) return "walk";
        if(str.equals("地铁"))return "pt";
        if(str.equals("私家车"))return "car";
        if(str.equals("网约车/出租车"))return "pt";
        if(str.equals("公交"))return "pt";
        return "pt";
    }
    public static int getAge(String ageStr){
        Random random = new Random();
        //'6~18岁': 0, '18~30岁': 1, '30~45岁': 2, '45~60岁': 3, '60及以上': 4}
         if(ageStr.equals("6~18岁"))return 6+random.nextInt(12);
         if(ageStr.equals("18~30岁"))return 18+random.nextInt(12);
         if(ageStr.equals("30~45岁"))return 30+random.nextInt(15);
         if(ageStr.equals("45~60岁"))return 45+random.nextInt(15);
         if(ageStr.equals("60及以上"))return 60+random.nextInt(12);
         return  6+ random.nextInt(54);
    }
    public static boolean getLicense(String str){
        if(str.equals("有驾照")) return true;
        return false;
    }
    public static String getCarAvail(String str){
        if(str.equals("有小汽车（驾驶员，单独使用车辆）"))return "always";
        if(str.equals("无小汽车"))return "never";
        return "sometimes";

    }

    public static void main(String[] args) throws Exception{
        String file = "data/预处理后数据.csv";
        parseTxtFile(file);
    }
}
