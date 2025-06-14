import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;

import com.zeroc.Ice.Communicator;
import com.zeroc.Ice.ObjectAdapter;
import com.zeroc.Ice.Util;

public class Server
{

    public static Map<String, Integer> votes = new ConcurrentHashMap<>();
    public static ArrayList<String> resultQuery = new ArrayList<>();
    public static void main(String[] args)
    {
        votes = new HashMap<>();
        try(Communicator communicator = Util.initialize(args, "properties.cfg"))
        {
            ObjectAdapter adapter = communicator.createObjectAdapter("services");
            PrinterI object = new PrinterI();
            adapter.add(object, Util.stringToIdentity("SimpleVote"));
            adapter.activate();

            Scanner scanner = new Scanner(System.in);
            System.out.println("Enter 'exit' to finish and export votes.");
            String n = scanner.nextLine();
            while (!n.equals("exit")) {
                n = scanner.nextLine();
            }
            System.out.println("Exportando registro de votos...");
            object.exportVotes();
            scanner.close();

            communicator.waitForShutdown();
        }
    }
}