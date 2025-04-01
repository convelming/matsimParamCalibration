package population;

import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.Population;
import org.matsim.api.core.v01.population.PopulationFactory;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.router.RoutingRequest;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.facilities.Facility;
import org.matsim.pt.router.FakeFacility;
import org.matsim.pt.transitSchedule.api.TransitStopFacility;
import org.matsim.utils.objectattributes.attributable.Attributes;

import java.util.UUID;

public class RoutingRequestImpl implements RoutingRequest {

    private Attributes attributes;
    private Facility fromFactility;
    private Facility toFacility;
    private double departureTime;
    private Person person;

    RoutingRequestImpl(Facility fromFacility, Facility toFacility, double departureTime, Person person, Attributes attributes) {
        this.fromFactility = fromFacility;
        this.toFacility = toFacility;
        this.departureTime = departureTime;
        this.person = person;
        this.attributes = attributes;
    }
    RoutingRequestImpl(Facility fromFacility, Facility toFacility, double departureTime) {
        this.fromFactility = fromFacility;
        this.toFacility = toFacility;
        this.departureTime = departureTime;
        this.person = null;
        this.attributes = null;
    }
    RoutingRequestImpl(Coord fromCoord, Coord toCoord) {

        this.fromFactility = new FakeFacility(fromCoord);
        this.toFacility = new FakeFacility(toCoord);
        this.departureTime = 3600 * 12.0;
        Config config = ConfigUtils.createConfig();
        Scenario sc = ScenarioUtils.createScenario(config);
        Population population = sc.getPopulation();
        PopulationFactory populationFactory = population.getFactory();
        this.person = populationFactory.createPerson(Id.createPersonId("dummy"));
        this.attributes = new Attributes();

    }
    RoutingRequestImpl(TransitStopFacility fromCoord, TransitStopFacility toCoord, double dpTime, Person dummy) {

        this.fromFactility = fromCoord;
        this.toFacility = toCoord;
        this.departureTime = dpTime;

        this.person = dummy;
        this.attributes = new Attributes();

    }

    RoutingRequestImpl(Coord fromCoord, Coord toCoord, double departureTime, Population population) {
        this.fromFactility = new FakeFacility(fromCoord);
        this.toFacility = new FakeFacility(toCoord);
        this.departureTime = departureTime;
        PopulationFactory populationFactory = population.getFactory();
        this.person = populationFactory.createPerson(Id.createPersonId(UUID.randomUUID().toString()));
        this.attributes = new Attributes();

    }

    @Override
    public Facility getFromFacility() {
        return this.fromFactility;
    }

    @Override
    public Facility getToFacility() {
        return this.toFacility;
    }

    @Override
    public double getDepartureTime() {
        return this.departureTime;
    }

    @Override
    public Person getPerson() {
        return this.person;
    }

    @Override
    public Attributes getAttributes() {
        return this.attributes;
    }

    public void setAttributes(Attributes attributes) {
        this.attributes = attributes;
    }

    public Facility getFromFactility() {
        return fromFactility;
    }

    public void setFromFactility(Facility fromFactility) {
        this.fromFactility = fromFactility;
    }

    public void setToFacility(Facility toFacility) {
        this.toFacility = toFacility;
    }

    public void setDepartureTime(double departureTime) {
        this.departureTime = departureTime;
    }

    public void setPerson(Person person) {
        this.person = person;
    }


}
