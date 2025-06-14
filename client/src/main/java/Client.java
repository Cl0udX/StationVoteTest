import com.zeroc.Ice.Communicator;
import com.zeroc.Ice.ObjectAdapter;
import com.zeroc.Ice.Util;

import Demo.VoteServicePrx;


public class Client
{
    public static void main(String[] args)
    {
        // String name = "Default1-vote";
        // for (int i = 0; i < args.length; i++) {
        //     if ("-vote".equals(args[i]) && i + 1 < args.length) {
        //         name = args[i + 1];
        //     }
        // } 

        try(Communicator communicator = Util.initialize(args, "properties.cfg"))
        {
            VoteServicePrx printer = VoteServicePrx.checkedCast(communicator.propertyToProxy("vote.proxy"));
            if(printer == null)
            {
                throw new Error("Invalid proxy");
            }

            ObjectAdapter adapter = communicator.createObjectAdapter("services");
            StationI stationObject = new StationI(printer);
            StationQueryI queryObject = new StationQueryI(printer);
            adapter.add(stationObject, Util.stringToIdentity("Station"));
            adapter.add(queryObject, Util.stringToIdentity("StationQuery"));
            adapter.activate();

            communicator.waitForShutdown();
        }
    }
}