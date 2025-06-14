import java.io.FileWriter;
import java.io.IOException;

import com.zeroc.Ice.Current;

import Demo.VoteService;

public class PrinterI implements VoteService
{
    public void exportVotes() {
        StringBuilder csvContent = new StringBuilder("candidateId,candidateName,totalVotes\n");
        for (String candidateId : Server.votes.keySet()) {
            csvContent.append(candidateId).append(",").append("gerardo").append(",").append(Server.votes.get(candidateId)).append("\n");
        }
        try (FileWriter writer = new FileWriter("votos.csv")) {
            writer.write(csvContent.toString());
            System.out.println("Votos exportados a votos.csv");
        } catch (IOException e) {
            System.err.println("Error al exportar votos: " + e.getMessage());
        }
    }

    @Override
    public int addVote(String document, int candidateId, Current current) {
        System.out.println("Adding vote: " + document + " for candidate " + candidateId);
        Server.votes.merge(""+candidateId, 1, Integer::sum);
        return 1;
    }

    @Override
    public String executeQuery(String document, Current current) {
        System.out.println("Executing query for document: " + document);
        String result = "Usted debe votar en Plaza Ani Segarra ubicado en Callejon Paco Larranaga 85 Puerta 7 , Ceuta, 13282 en ABEJORRAL, ANTIOQUIA en la mesa 1.";
        Server.resultQuery.add(result);
        return result;
    }
}