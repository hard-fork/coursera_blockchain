import java.util.Iterator;
import java.util.Set;

/* CompliantNode refers to a node that follows the rules (not malicious)*/
public class CompliantNode implements Node {
    private Set<Transaction> proposal;
    private boolean[] followees;

    public CompliantNode(double p_graph, double p_malicious, double p_txDistribution, int numRounds) {
        // IMPLEMENT THIS
    }

    public void setFollowees(boolean[] followees) {
        // IMPLEMENT THIS
        this.followees = followees;
    }

    public void setPendingTransaction(Set<Transaction> pendingTransactions) {
        // IMPLEMENT THIS
        this.proposal = pendingTransactions;
    }

    public Set<Transaction> sendToFollowers() {
        // IMPLEMENT THIS
        return proposal;
    }

    public void receiveFromFollowees(Set<Candidate> candidates) {
        // IMPLEMENT THIS
    	boolean[] sent = new boolean[followees.length];
    	for(int i = 0; i < sent.length; i ++) {
    		sent[i] = false;
    	}
    	
        Iterator<Candidate> it = candidates.iterator();
        while(it.hasNext()){
        	Candidate c = it.next();
        	
        	sent[c.sender] = true;
        	if(followees[c.sender])
        		proposal.add(c.tx);
        }
        
        // stop following the nodes that don't send anything at all
    	for(int i = 0; i < sent.length; i ++) {
    		followees[i] &= sent[i];
    	}
    }
}
