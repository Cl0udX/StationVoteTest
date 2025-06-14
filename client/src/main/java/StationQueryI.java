import com.zeroc.Ice.Current;

import Demo.QueryStation;
import Demo.VoteServicePrx;

public class StationQueryI implements QueryStation
{
    private final VoteServicePrx voteService;

    public StationQueryI(VoteServicePrx voteService) {
        this.voteService = voteService;
    }
    @Override
    public String query(String document, Current current) {
        String result = voteService.executeQuery(document);
        System.out.println("Query result: " + result);
        return result;
    }
}
