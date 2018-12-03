package ch.ethz.matsim.abmt.eventshandling;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.events.LinkEnterEvent;
import org.matsim.api.core.v01.events.VehicleEntersTrafficEvent;
import org.matsim.api.core.v01.events.handler.LinkEnterEventHandler;
import org.matsim.api.core.v01.events.handler.VehicleEntersTrafficEventHandler;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.events.EventsUtils;
import org.matsim.core.events.MatsimEventsReader;
import org.matsim.core.network.io.MatsimNetworkReader;
import org.matsim.core.network.io.NetworkWriter;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.core.utils.io.IOUtils;
import org.matsim.utils.objectattributes.attributable.Attributes;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * This class demonstrates how to generate statistics about the link volumes, and writes them to a file.
 * It is based on the understanding of the CountEvents class, and adds file writing and a more complicated data structure
 * to keep track of the information.
 */
public class WriteVolumesToFile {

    public static void main(String... args) throws IOException {
        // Read the network, to enrich the collected data with information from the network.
        Config config = ConfigUtils.createConfig();
        Scenario scenario = ScenarioUtils.createScenario(config);

        String scenarioPath = "/Users/clemens/Documents/courses/abmt/projects/abmt_project/java_src/scenarios/";
        String scenarioName = "original/";
        String path = scenarioPath + scenarioName;

        new MatsimNetworkReader(scenario.getNetwork()).readFile(path+"output_network.xml.gz");
        Network network = scenario.getNetwork();

        // create the events manager. See CountEvents.
        EventsManager events = EventsUtils.createEventsManager();

        // define the start and end time of the period for which volumes are computed.
        double startTime = 8 * 3600;
        double endTime = 10 * 3600;

        // create our handler and add it to the events manager
        VolumesEventHandler handler = new VolumesEventHandler(startTime, endTime);
        events.addHandler(handler);

        // read the file
        new MatsimEventsReader(events).readFile(path+"output_events.xml.gz");

        // now, the handler contains the information we need, and we want to write it to a file.
        // Files in Java are written by adding characters to a writer object that is responsible
        // for actually writing to the file.
        // MATSim provides the IOUtils class, that helps building that writer.
        // It can also decide on whether to compress the file using gzip based on the presence or absence of the
        // ".gz" extension
        BufferedWriter writer = IOUtils.getBufferedWriter(path+"volumes.csv");

        // write the first line, that will be the header of our CSV file.
        // Your IDE should complain about an unhandled exception: just accept the automatic resolution that says
        // "add exception to method signature". This will add the "throws IOException" at the to of the method.
        // In short, this means that the program will crash if writing to the file fails for one reason or another
        // (for instance if another program deletes the file we are writing to).
        writer.write("linkID,volume,capacity");

        // now write link volumes, line by line. This is done by iterating over the entries in the map where we stored
        // the volumes in the handler (see below for more details)
        for (Map.Entry<Id<Link>, Integer> entry : handler.volumes.entrySet()) {
            // extract the key (link id) and value (volume) stored by the entry.
            Id<Link> linkId = entry.getKey();
            Integer volume = entry.getValue();

            // add an additional field corresponding to the maximum number of vehicles that would have been allowed to
            // go through the link during the time period. To this end, retrieve the link object for the network and access
            // the relevant field.
            Link link = network.getLinks().get(linkId);
            double capacity = link.getFlowCapacityPerSec() * (endTime - startTime);

            // now write to the file: create a new line and add the fields, separated by commas.
            // String concatenation is done using the + operator
            writer.newLine();
            writer.write(linkId+","+volume+","+capacity);

            // Additionally, attach the computed value to the network
            final Attributes attributes = link.getAttributes();
            attributes.putAttribute("volume", volume);
            attributes.putAttribute("capacityRatio", volume / (0.01 * capacity));
        }

        // We are now done with writing the information: we can close the file.
        // This step is very important: to improve performance, the writter stores the characters in a buffer in RAM,
        // and only actually tranfers those characters to the file when the buffer is full (hence the "BufferedWriter"
        // name). Closing tells to the reader "I am ready, you can write everything to file and stop working").
        // calling write after that will result in an error.
        writer.close();

        // As we added some information to the network, we write the network to a file as well.
        new NetworkWriter(network).write(path+"/network_volumes.xml.gz");
    }
}

// this class computes the link volumes, for a given time period, for all links.
// Here, we understand link volumes as "the number of vehicles that entered the link during the time period".
// To this end, we do not only need to keep track of LinkEnterEvents, but also of VehicleEntersTrafficEvents,
// which correspond to the entry on the first link for a given trip.
class VolumesEventHandler implements LinkEnterEventHandler, VehicleEntersTrafficEventHandler {
    // Store the begining and end of our time interval. Those values are set at construction,
    // see below.
    double startTime;
    double endTime;

    // A Map is an object that stores mappings from keys to values (similar to Python dctionnaries)
    // We use it here to store the number of vehicles that entered each link during the period, one at a time
    Map<Id<Link>, Integer> volumes = new HashMap<>();

    // This is called a "constructor": a method, without a return type, that has the same name as the class,
    // can be used to define how to "construct" the object. Here, we ask for two parameters, the start and end time of
    // the period, that we store in the fields defined above
    public VolumesEventHandler(double startTime, double endTime) {
        this.endTime = endTime;
        this.startTime = startTime;
    }

    // The two methods for link enter and leave events are identical, so the actual logic is defined in a third method,
    // called from those methods
    @Override
    public void handleEvent(LinkEnterEvent linkEnterEvent) {
        double time = linkEnterEvent.getTime();
        Id<Link> linkId = linkEnterEvent.getLinkId();
        registerEntrance(linkId, time);
    }

    @Override
    public void handleEvent(VehicleEntersTrafficEvent vehicleEntersTrafficEvent) {
        double time = vehicleEntersTrafficEvent.getTime();
        Id<Link> linkId = vehicleEntersTrafficEvent.getLinkId();
        registerEntrance(linkId, time);
    }

    // take not of the entrance of a vehicle on the given link at the given time.
    void registerEntrance(Id<Link> linkId, double time) {
        // First check whether the time is in the required time period. If not, stop execution (return)
        if (time < startTime || time > endTime) return;

        // Updating the stored value is a two stage process: first, retrieve the current value associated to the link
        Integer volume = volumes.get(linkId);

        // Now, update it, handling the special case of the first observed value for a link
        if (volume == null) {
            // If the link was not yet observed, it has no value attached, represented by the special value "null",
            // which is *not equivalent to 0*. If this is the case, we know it is the first entrance, and the value is
            // thus 1
            volumes.put(linkId, 1);
        }
        else {
            // otherwise, increment the stored value
            volumes.put(linkId, volume + 1);
        }
    }
}
