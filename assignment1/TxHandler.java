
import java.util.ArrayList;
import java.util.HashSet;

public class TxHandler {

    public UTXOPool pool;

    /**
     * Creates a public ledger whose current UTXOPool (collection of unspent transaction outputs) is
     * {@code utxoPool}. This should make a copy of utxoPool by using the UTXOPool(UTXOPool uPool)
     * constructor.
     */
    public TxHandler(UTXOPool utxoPool) {
        if(utxoPool == null){
            pool = new UTXOPool();
        }else{
            pool = new UTXOPool(utxoPool);
        }
    }

    /**
     * @return true if:
     * (1) all outputs claimed by {@code tx} are in the current UTXO pool, 
     * (2) the signatures on each input of {@code tx} are valid, 
     * (3) no UTXO is claimed multiple times by {@code tx},
     * (4) all of {@code tx}s output values are non-negative, and
     * (5) the sum of {@code tx}s input values is greater than or equal to the sum of its output
     *     values; and false otherwise.
     */
    public boolean isValidTx(Transaction tx) {
        ArrayList<Transaction.Input> inputs = tx.getInputs();
        ArrayList<Transaction.Output> outputs = tx.getOutputs();

        HashSet<UTXO> utxos = new HashSet<UTXO>();
        double inputSum = 0;
        for(int i = 0; i < inputs.size(); i ++){
            Transaction.Input input = inputs.get(i);
            UTXO utxo = new UTXO(input.prevTxHash, input.outputIndex);
            //cond 1
            if(! pool.contains(utxo))
                return false;
            
            //cond 2
            Transaction.Output output = pool.getTxOutput(utxo);
            byte[] rawDataToSign = tx.getRawDataToSign(i);
            if(! Crypto.verifySignature(output.address, rawDataToSign, input.signature))
                return false;

            //cond 3
            if(utxos.contains(utxo))
                return false;
            
            utxos.add(utxo);

            //cond 4
            if(output.value < 0)
                return false;
            
            //cond 5
            inputSum += output.value;
        }

        //cond 5
        double outputSum = 0;
        for(int i = 0; i < outputs.size(); i ++){
            double value = outputs.get(i).value;
            
            //cond 4
            if(value < 0)
                return false;
            
            outputSum += value;
        }

        if(inputSum < outputSum)
            return false;
        
        return true;
    }

    /**
     * Handles each epoch by receiving an unordered array of proposed transactions, checking each
     * transaction for correctness, returning a mutually valid array of accepted transactions, and
     * updating the current UTXO pool as appropriate.
     */
    public Transaction[] handleTxs(Transaction[] possibleTxs) {
        ArrayList<Transaction> finalTxs = new ArrayList<Transaction>();
        for(int i = 0; i < possibleTxs.length; i ++){
            Transaction tx = possibleTxs[i];
            ArrayList<Transaction.Input> inputs = tx.getInputs();
            ArrayList<Transaction.Output> outputs = tx.getOutputs();
            
            if(! isValidTx(tx))
                continue;
            
            // update UTXOPool intime to ensure no collision
            for(int j = 0; j < inputs.size(); j ++){
                Transaction.Input input = inputs.get(j);
                pool.removeUTXO(new UTXO(input.prevTxHash, input.outputIndex));
            }

            for(int k = 0; k < outputs.size(); k ++){
                pool.addUTXO(new UTXO(tx.getHash(), k), outputs.get(k));
            }

            finalTxs.add(tx);
        }

        Transaction[] result = new Transaction[finalTxs.size()];
        return finalTxs.toArray(result);
    }

}
