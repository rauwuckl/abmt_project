package org.matsim.abmt.filemanipulation;

import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.network.Node;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.network.algorithms.NetworkCleaner;
import org.matsim.core.network.io.MatsimNetworkReader;
import org.matsim.core.network.io.NetworkWriter;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.core.utils.io.IOUtils;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;




public class NetworkToCsv {

    public static void writeToFile(String path, List<LinkInfo> list_of_links) throws IOException {
        BufferedWriter writer = IOUtils.getBufferedWriter(path);

        String header ="id,length,capacity,freespeed,fromX,fromY,toX,toY";
        writer.write(header);

        for(LinkInfo l: list_of_links){
            String data = l.link_id+","+l.length+","+l.capacity+","+l.freespeed+","+l.fromX+","+l.fromY+","+l.toX+","+l.toY;
            writer.newLine();
            writer.write(data);
        }
        writer.close();
    }


    public static void main(String... args) throws IOException {
        // Those lines create the config object, which stores the information described in the config.xml file,
        // and the scenario object, which stores all data (population, network...)
        // To see what they give access to, in IntelliJ or Eclipse, type a dot after the variable name
        // (eg. "config.") to trigger code completion.
        Config config = ConfigUtils.createConfig();
        Scenario scenario = ScenarioUtils.createScenario(config);

        // this line creates a reader, that will store what it reads in the network object stored in the scenario.
        // adapt the path to the path on your filesystem.
        String path_to_network = "/Users/clemens/Documents/courses/abmt/projects/abmt_project/simulations/original/ile_de_france_network_simplified.xml.gz";
        String output_path = "/Users/clemens/Documents/courses/abmt/projects/abmt_project/analysis/Python/original_network.csv";
        new MatsimNetworkReader(scenario.getNetwork()).readFile(path_to_network);

        // this part gets the network object from the scenario,
        // iterates through all links, and for each link,
        // accesses the capacity of the link, multiplies it by 2,
        // and updates the link capacity with this new value. This shows how to edit all links in a network as a whole.
        // Again, typing "link." will give you access to a list of possible methods.
        // "capacity" in MATSim means flow capacity (vehicles per second)
        Network network = scenario.getNetwork();


        List<LinkInfo> list = new ArrayList<LinkInfo>();

        for(Link l: network.getLinks().values()){
            list.add(new LinkInfo(l));
        }
        writeToFile(output_path, list);

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
//        new NetworkCleaner().run(network);



        // This line simply writes our modified network to a new file.
        // The writer is able to decide on whether to compress the file or not based on the extension (.xml or .xml.gz)
//        NetworkWriter nw =  new NetworkWriter(scenario.getNetwork());
//        nw.write();
//        new NetworkWriter(scenario.getNetwork()).write("network.xml.gz");
    }
}

class LinkInfo{
    public Id<Link> link_id;
    public Double length;
    public Double fromX;
    public Double fromY;
    public Double toX;
    public Double toY;
    public Double capacity;
    public Double freespeed;

    LinkInfo(Link l){
        link_id = l.getId();
        length = l.getLength();
        capacity = l.getCapacity();
        freespeed = l.getFreespeed();

        Node fromNode = l.getFromNode();
        fromX = fromNode.getCoord().getX();
        fromY = fromNode.getCoord().getY();

        Coord toCoords = l.getToNode().getCoord();
        toX = toCoords.getX();
        toY = toCoords.getY();
    }



}