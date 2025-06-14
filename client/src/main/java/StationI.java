import com.zeroc.Ice.Current;

import Demo.VoteServicePrx;
import Demo.VoteStation;

public class StationI implements VoteStation
{
    private final VoteServicePrx voteService;

    public StationI(VoteServicePrx voteService) {
        this.voteService = voteService;
    }

    @Override
    public int vote(String document, int candidateId, Current current) {
        return voteService.addVote(document, candidateId);
    }   
}
