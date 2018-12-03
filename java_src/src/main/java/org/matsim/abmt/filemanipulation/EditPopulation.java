package ch.ethz.matsim.abmt.filemanipulation;

import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.population.*;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.population.io.PopulationReader;
import org.matsim.core.router.EmptyStageActivityTypes;
import org.matsim.core.router.TripStructureUtils;
import org.matsim.core.scenario.ScenarioUtils;

public class EditPopulation {

    public static void main(String... args) {
        // Those lines create the config object, which stores the information described in the config.xml file,
        // and the scenario object, which stores all data (population, network...)
        // To see what they give access to, in IntelliJ or Eclipse, type a dot after the variable name
        // (eg. "config.") to trigger code completion.
        Config config = ConfigUtils.createConfig();
        Scenario scenario = ScenarioUtils.createScenario(config);

        // this line creates a reader, that will store what it reads in the population object stored in the scenario.
        // adapt the path to the path on your filesystem.
        new PopulationReader(scenario).readFile("somefile.xml");


        // this part gets the population object from the scenario,
        // iterates over persons (agents), and for each agent,
        // modify the selected plan (one could also iterate through all plans of the agent)
        Population population = scenario.getPopulation();
        for (Person person : population.getPersons().values()) {
            Plan plan = person.getSelectedPlan();

            // This allows to access the attributes attached to person in the XML file.
            // The type (String) of the attribute can be seen in the XML file:
            // <attribute name="hasLicense" class="java.lang.String" >yes</attribute>
            String hasLicense = (String) person.getAttributes().getAttribute("hasLicense");

            // Plans are a sequence of plan elements. Plan elements are either legs, or activities.
            // TripStructureUtils provides methods to filter plan elements of a given type.
            for (Leg leg : TripStructureUtils.getLegs(plan)) {
                leg.setMode("pt");
                leg.setRoute(null);
            }

            // trips might be composed of several legs, separated by "stage" activities, which are activities that simply
            // represent "being somewhere". We do not care about this here, so we do not identify any activity as a stage
            // activity: this is what the "EmptyStageActivityTypes.INSTANCE" does.
            for (Activity activity : TripStructureUtils.getActivities(plan, EmptyStageActivityTypes.INSTANCE)) {
                if (activity.getType().equals("home")) {
                    activity.setEndTime(24 * 3600);
                }
            }
        }


        new PopulationWriter(scenario.getPopulation()).write("pop.xml");
    }
}
