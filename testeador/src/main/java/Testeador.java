import com.zeroc.Ice.Communicator;
import com.zeroc.Ice.ObjectAdapter;
import com.zeroc.Ice.ObjectPrx;
import com.zeroc.Ice.Util;

import Demo.ObserverPrx;
import Demo.SubjectPrx;

public class Testeador {

    public static Communicator mainCommunicator = null;

    public static void main(String[] args) {
        System.out.println("Ingrese el id del host: ");
        String hostId = null;
        try(Communicator communicator = Util.initialize(args, "properties.cfg"))
        {
            mainCommunicator = communicator;
            hostId = communicator.getProperties().getProperty("hostId");
            SubjectPrx subject = SubjectPrx.checkedCast(communicator.propertyToProxy("subject.proxy"));
            if(subject == null)
            {
                throw new Error("Invalid proxy");
            }

            Worker observer = new Worker(subject);
            ObjectAdapter adapter = communicator.createObjectAdapter("Observer");
            ObjectPrx proxyObs = adapter.add(observer, Util.stringToIdentity("notNecessaryName"));
            adapter.activate();
            ObserverPrx observerPrx = ObserverPrx.checkedCast(proxyObs);
            
            
            subject.registerObserver(hostId,observerPrx);

            communicator.waitForShutdown();
        }
    }
}
