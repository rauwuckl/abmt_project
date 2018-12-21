package ch.ethz.matsim.abmt.filemanipulation;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.network.algorithms.NetworkCleaner;
import org.matsim.core.network.io.MatsimNetworkReader;
import org.matsim.core.network.io.NetworkWriter;
import org.matsim.core.scenario.ScenarioUtils;

public class EditNetwork {
    public static void main(String... args) {
        // Those lines create the config object, which stores the information described in the config.xml file,
        // and the scenario object, which stores all data (population, network...)
        // To see what they give access to, in IntelliJ or Eclipse, type a dot after the variable name
        // (eg. "config.") to trigger code completion.
        Config config = ConfigUtils.createConfig();
        Scenario scenario = ScenarioUtils.createScenario(config);

        // this line creates a reader, that will store what it reads in the network object stored in the scenario.
        // adapt the path to the path on your filesystem.
        new MatsimNetworkReader(scenario.getNetwork()).readFile("/Users/clemens/Documents/courses/abmt/projects/abmt_project/analysis/network_analysis/large_new_network.xml.gz");

        // this part gets the network object from the scenario,
        // iterates through all links, and for each link,
        // accesses the capacity of the link, multiplies it by 2,
        // and updates the link capacity with this new value. This shows how to edit all links in a network as a whole.
        // Again, typing "link." will give you access to a list of possible methods.
        // "capacity" in MATSim means flow capacity (vehicles per second)
        Network network = scenario.getNetwork();
//        for (Link link : network.getLinks().values()) {
//            double capacity = link.getCapacity();
//            link.setCapacity(capacity * 2);
//        }
//
//        // This part shows how to get a link with a particular Id.
//        // Ids in MATSim are specific objects, created by Id.createId
//        Id<Link> id = Id.createLinkId("1");
//        Link link = network.getLinks().get(id);
//        link.setCapacity(0);
//
//        // This modifies the network such that link with id "1" is removed
//        network.removeLink(id);
//        Link l = network.getLinks().get(42);
//        l.getToNode().

        // This will clean the network, removing orphaned nodes and keeping only the biggest connected component.
        // It is good to run this after removing links.
        new NetworkCleaner().run(network);

        // This line simply writes our modified network to a new file.
        // The writer is able to decide on whether to compress the file or not based on the extension (.xml or .xml.gz)
        new NetworkWriter(scenario.getNetwork()).write("/Users/clemens/Documents/courses/abmt/projects/abmt_project/analysis/network_analysis/large_network_deleted_road_cleaned.xml.gz");
    }
}
