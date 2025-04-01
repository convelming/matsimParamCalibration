package utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.population.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.matsim.core.config.ConfigUtils;
import org.matsim.core.gbl.MatsimRandom;
import org.matsim.core.population.PopulationUtils;
import org.matsim.core.population.routes.NetworkRoute;
import org.matsim.core.router.TripRouter;
import org.matsim.core.router.TripStructureUtils;
import org.matsim.core.utils.timing.TimeInterpretation;
import org.matsim.core.utils.timing.TimeTracker;
import org.matsim.facilities.FacilitiesUtils;
import org.matsim.vehicles.Vehicle;
import planParam.PlanParamCalibrationConfigGroup;


/**
 * Author：Milu
 * 严重落后进度啦......
 * this class generate alternative plans according TimeAllocationMutator, ChangeLegMode, TimeAllocationMutator_ReRoute,
 * ChangeTripMode in DefaultPlanStrategiesModule
 * todo 2024/1/17 in PlanParamCalibrationConfigGroup set up probabilities of above strategies, at the moment uniformly
 * draw the above strategies.
 *
 * date：2024/1/16
 * project:matsimParamCalibration
 */
public class GenerateAlternativePlans{
    private static final Logger log = LogManager.getLogger( GenerateAlternativePlans.class) ;
    public int maxPlans;
    private Map<Person, List<Plan>> fixedPersonPlans; // scoring parameters need person to get ScoringParameters(person)
    private Scenario scenario;
    private TripRouter tripRouter; // TODO: 2024/1/17  this trip router could further modified to reference to actual or link counters!!!
    private String subPopName;
    public  GenerateAlternativePlans(PlanParamCalibrationConfigGroup planParamCalibrationConfigGroup, Scenario scenario,TripRouter tripRouter) {
        fixedPersonPlans =  new HashMap<>();
        maxPlans = planParamCalibrationConfigGroup.getMaxAlternativePlans();
        this.scenario = scenario;
        this.tripRouter = tripRouter;
        subPopName = planParamCalibrationConfigGroup.getGroundTruthSubPopulation();
        Population population = scenario.getPopulation();
        // TODO: 2024/1/17  config probabilities for above-mentioned strategies
        getSubPopulation(population, subPopName).getPersons().forEach((personId, person) -> {
            List<Plan> tmpPlanList = new ArrayList<>();
            tmpPlanList.add(person.getSelectedPlan()); // there is only on plan here in the list
            fixedPersonPlans.put(person,tmpPlanList);
        });
        log.info("Subpopulation size: " + fixedPersonPlans.size());
        generate();
    }
    public void generate(){
        log.info("Start to generate alternative plans for subpopulation: "+ subPopName+ ", note these plans are only for estimate parameters, and cannot be selected.");

        // generate alternative plans
        fixedPersonPlans.values().stream().parallel().forEach((planList) -> {

            TimeTracker timeTracker = new TimeTracker(TimeInterpretation.create(scenario.getConfig()));
            for (int i = 1; i < maxPlans; i++) {
                Plan tmpNewPlan = copyPlan(planList.get(0));

                final List<TripStructureUtils.Trip> trips = TripStructureUtils.getTrips( tmpNewPlan );

                for (TripStructureUtils.Trip oldTrip : trips) {
                    String routingMode = TripStructureUtils.identifyMainMode( oldTrip.getTripElements() );
                    timeTracker.addActivity(oldTrip.getOriginActivity());
                    if(MatsimRandom.getRandom().nextBoolean()) {routingMode = "pt";}else {routingMode="car";} // TODO: 2024/2/27  if it is needed to consider carAvail?
                    if (log.isDebugEnabled()) log.debug("about to call TripRouter with routingMode=" + routingMode);
                    double depTime = mutateDepTime(timeTracker.getTime().seconds());
                    final List<? extends PlanElement> newTrip = tripRouter.calcRoute( //
                            routingMode, //
                            FacilitiesUtils.toFacility(oldTrip.getOriginActivity(), null), //
                            FacilitiesUtils.toFacility(oldTrip.getDestinationActivity(), null), //
                            depTime, //
                            tmpNewPlan.getPerson(), //
                            oldTrip.getTripAttributes() //
                    );
                    putVehicleFromOldTripIntoNewTripIfMeaningful(oldTrip, newTrip);
                    TripRouter.insertTrip(
                            tmpNewPlan,
                            oldTrip.getOriginActivity(),
                            newTrip,
                            oldTrip.getDestinationActivity());

                    timeTracker.addElements(newTrip);
                }

                planList.add(tmpNewPlan);
            }

        });
        log.info("Generating alternative plans is done...");
    }
    public double mutateDepTime(double depTime){
        double tmpTime = depTime - 3600.0 + MatsimRandom.getRandom().nextDouble()*7200;
        while (tmpTime<0.0){
            tmpTime = depTime - 3600.0 + MatsimRandom.getRandom().nextDouble()*7200;
        }
        return tmpTime;
    }
    private static void putVehicleFromOldTripIntoNewTripIfMeaningful(TripStructureUtils.Trip oldTrip, List<? extends PlanElement> newTrip) {
        Id<Vehicle> oldVehicleId = getUniqueVehicleId(oldTrip);
        if (oldVehicleId != null) {
            for (Leg leg : TripStructureUtils.getLegs(newTrip)) {
                if (leg.getRoute() instanceof NetworkRoute) {
                    if (((NetworkRoute) leg.getRoute()).getVehicleId() == null) {
                        ((NetworkRoute) leg.getRoute()).setVehicleId(oldVehicleId);
                    }
                }
            }
        }
    }

    private static Id<Vehicle> getUniqueVehicleId(TripStructureUtils.Trip trip) {
        Id<Vehicle> vehicleId = null;
        for (Leg leg : trip.getLegsOnly()) {
            if (leg.getRoute() instanceof NetworkRoute) {
                if (vehicleId != null && (!vehicleId.equals(((NetworkRoute) leg.getRoute()).getVehicleId()))) {
                    return null; // The trip uses several vehicles.
                }
                vehicleId = ((NetworkRoute) leg.getRoute()).getVehicleId();
            }
        }
        return vehicleId;
    }
    public Plan copyPlan(Plan oldPlan){
        if (oldPlan == null) {
            return null;
        }
        Plan newPlan = PopulationUtils.createPlan(oldPlan.getPerson());
        PopulationUtils.copyFromTo(oldPlan, newPlan);
        return newPlan;
    }

    public Population getSubPopulation(Population population,String subPop){
        Population subPopulation = PopulationUtils.createPopulation(ConfigUtils.createConfig());
        // filter those
        population.getPersons().forEach((personId, person) -> {
            if(PopulationUtils.getSubpopulation(person)==null?false:PopulationUtils.getSubpopulation(person).equals(subPop)){
                subPopulation.addPerson(person);
            }
        });
        return subPopulation;
    }
    public Map<Person, List<Plan>> getFixedPersonPlans() {
        return fixedPersonPlans;
    }

}
