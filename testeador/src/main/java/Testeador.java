import java.util.Scanner;

import com.zeroc.Ice.Communicator;
import com.zeroc.Ice.ObjectAdapter;
import com.zeroc.Ice.ObjectPrx;
import com.zeroc.Ice.Util;

import Demo.ObserverPrx;
import Demo.SubjectPrx;

public class Testeador {

    public static Communicator mainCommunicator = null;

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Ingrese el id del host: ");
        String hostId = null;
        do{
            hostId = scanner.nextLine();
            if(hostId != null && !hostId.isEmpty())
            {
                break;
            }else
            {
                System.out.println("El id del host no puede estar vacio. Ingrese nuevamente: ");
            }     
        }while(true);
        scanner.close();
        try(Communicator communicator = Util.initialize(args, "properties.cfg"))
        {
            mainCommunicator = communicator;
            Worker observer = new Worker();
            ObjectAdapter adapter = communicator.createObjectAdapter("Observer");
            ObjectPrx proxyObs = adapter.add(observer, Util.stringToIdentity("notNecessaryName"));
            adapter.activate();
            ObserverPrx observerPrx = ObserverPrx.checkedCast(proxyObs);
            
            SubjectPrx subject = SubjectPrx.checkedCast(communicator.propertyToProxy("subject.proxy"));
            if(subject == null)
            {
                throw new Error("Invalid proxy");
            }
            
            subject.registerObserver(hostId,observerPrx);

            communicator.waitForShutdown();
        }
    }
}
