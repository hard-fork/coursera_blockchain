import java.util.Iterator;
import java.util.Set;

/* CompliantNode refers to a node that follows the rules (not malicious)*/
public class CompliantNode implements Node {
    private Set<Transaction> proposal; //final proposal
    private Set<Transaction> pending; // waiting to be sent
    private boolean[] followees;

    private int rounds;
    private int times;

    public CompliantNode(double p_graph, double p_malicious, double p_txDistribution, int numRounds) {
        // IMPLEMENT THIS
        this.rounds = numRounds;
        this.times = 0;
    }

    public void setFollowees(boolean[] followees) {
        // IMPLEMENT THIS
        this.followees = followees;
    }

    public void setPendingTransaction(Set<Transaction> pendingTransactions) {
        // IMPLEMENT THIS
        this.proposal = pendingTransactions;
        this.pending = pendingTransactions;
    }

    public Set<Transaction> sendToFollowers() {
        // IMPLEMENT THIS
        
        // at k round, txs in pending list are from a node that has a path to the current node
        // whose length is k.
        if(times ++ < rounds)
            return pending;

        return proposal;
    }

    public void receiveFromFollowees(Set<Candidate> candidates) {
        // IMPLEMENT THIS
        pending.clear();

        Iterator<Candidate> it = candidates.iterator();
        while(it.hasNext()){
            Candidate c = it.next();

            Transaction tx = c.tx;
            int sender = c.sender;

            // in case of malicious node
            if(followees[sender]){
                if(proposal.contains(tx))
                    pending.add(tx);
                else
                    proposal.add(tx);
            }
        }
    }
}
