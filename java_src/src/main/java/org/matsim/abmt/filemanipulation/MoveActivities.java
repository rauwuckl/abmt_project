package org.matsim.abmt.filemanipulation;

import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.population.*;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.network.algorithms.NetworkCleaner;
import org.matsim.core.network.io.MatsimNetworkReader;
import org.matsim.core.network.io.NetworkWriter;
import org.matsim.core.population.io.PopulationReader;
import org.matsim.core.router.EmptyStageActivityTypes;
import org.matsim.core.router.TripStructureUtils;
import org.matsim.core.scenario.ScenarioUtils;

import java.util.List;

public class MoveActivities {
    public static void main(String... args) {
        String original_network_file = "/Users/clemens/Documents/courses/abmt/projects/abmt_project/simulations/original/ile_de_france_network_simplified.xml.gz";
        String original_population_file = "/Users/clemens/Documents/courses/abmt/projects/abmt_project/simulations/original/ile_de_france_population_diluted_caronly.xml.gz";

        Config original_config = ConfigUtils.createConfig();
        Scenario original_scenario = ScenarioUtils.createScenario(original_config);

        new MatsimNetworkReader(original_scenario.getNetwork()).readFile(original_network_file);
        Network original_network = original_scenario.getNetwork();

        new PopulationReader(original_scenario).readFile(original_population_file);
        Population original_population = original_scenario.getPopulation();


        //////////////////
        String modified_network_file = "/Users/clemens/Documents/courses/abmt/projects/abmt_project/analysis/network_analysis/large_network_deleted_road_cleaned.xml.gz";

        Config deleted_road_config = ConfigUtils.createConfig();
        Scenario deleted_road_scenario = ScenarioUtils.createScenario(deleted_road_config);


        new MatsimNetworkReader(deleted_road_scenario.getNetwork()).readFile(modified_network_file);
        Network deleted_road_network = deleted_road_scenario.getNetwork();



        for (Person person: original_population.getPersons().values()){
            List<? extends Plan> all_plans =  person.getPlans();
            if(1 != all_plans.size()){
                throw new IllegalArgumentException();
            }

            Plan plan = all_plans.get(0);

            for (Activity activity : TripStructureUtils.getActivities(plan, EmptyStageActivityTypes.INSTANCE)) {

                Id<Link> link_of_activity = activity.getLinkId();
                if(!deleted_road_network.getLinks().containsKey(link_of_activity)){
                    Coord position = activity.getCoord();
                    Link closestLink = NetworkUtils.getNearestLink(deleted_road_network, position);
                    activity.setLinkId(closestLink.getId());
                }
            }

            for (Leg leg: TripStructureUtils.getLegs(plan)){
                leg.setRoute(null);
            }
        }

        String output_population_path = "/Users/clemens/Documents/courses/abmt/projects/abmt_project/analysis/network_analysis/population_for_cleaned_deleted_net.xml.gz";
        new PopulationWriter(original_population).write(output_population_path);





//
//        ConfigUtils.loadConfig(original_config, original_config_file);
//        Scenario original_scenario = ScenarioUtils.loadScenario(original_config);
//
//        Network original_network = original_scenario.getNetwork();

    }
}
