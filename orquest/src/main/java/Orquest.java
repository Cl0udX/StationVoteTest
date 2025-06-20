import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import com.zeroc.Ice.Communicator;
import com.zeroc.Ice.ObjectAdapter;
import com.zeroc.Ice.Util;

public class Orquest {

    public static Map<Integer, List<String>> documentos = new HashMap<>();

    public static void main(String[] args) {

        System.out.println("¿Cuántos ciudadanos desea cargar?");
        System.out.println("Ingrese un número o escriba 'todos' para cargar los 100000000:");
        Scanner scanner = new Scanner(System.in);
        String input = scanner.nextLine();

        int maxToLoad;
        if (input.equalsIgnoreCase("todos")) {
            maxToLoad = Integer.MAX_VALUE;
        } else {
            try {
                maxToLoad = Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("Entrada inválida. Se cargarán todos los ciudadanos.");
                maxToLoad = Integer.MAX_VALUE;
            }
        }

        System.out.println("Cargando ciudadanos desde el archivo ciudadanos.csv...");
        try (BufferedReader br = new BufferedReader(new FileReader("ciudadanos.csv"))) {
            String line = br.readLine();
            int count = 0;
            while ((line = br.readLine()) != null && count < maxToLoad) {
                String[] fields = line.split(",");
                if (fields.length > 1) {
                    String documento = fields[1].trim();
                    Integer mesaId = Integer.parseInt(fields[4].trim());
                    List<String> docs = documentos.getOrDefault(mesaId, new ArrayList<>());
                    docs.add(documento);
                    documentos.put(mesaId, docs);
                    count++;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            scanner.close();
            return;
        }
        System.out.println("Ciudadanos cargados: " + documentos.size());

        try (Communicator communicator = Util.initialize(args, "properties.cfg")) {

            ObjectAdapter adapter = communicator.createObjectAdapter("services");
            OrcheService service = new OrcheService();
            adapter.add(service, Util.stringToIdentity("Subject"));

            adapter.activate();
            String n = null;
            
            do {
                
                service.loadClientsProxies();
                System.out.println("Para evaluar las estacions:\n" +
                "1. Para evaluar las estaciones de votación\n" +
                "2. Para evaluar las estaciones de consulta\n" +
                "3. Ingrese 'exit' para salir");
                n = scanner.nextLine();
                if (n.equals("1")) {
                    service.startEvaluationStationVote();
                } else if (n.equals("2")) {
                    service.startEvaluationStationQuery();
                } else if (!n.equals("exit")) {
                    System.out.println("Opción no válida. Intente de nuevo.");
                }
            } while (!n.equals("exit"));
            scanner.close();

            communicator.waitForShutdown();
        }
    }
}
