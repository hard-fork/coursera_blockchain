import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

// Block Chain should maintain only limited block nodes to satisfy the functions
// You should not have all the blocks added to the block chain in memory 
// as it would cause a memory overflow.

public class BlockChain {
    public static final int CUT_OFF_AGE = 10;

    public class Node<T> {
        public T data;
        public Node<T> parent;
        public List<Node<T>> children;

        public Node(T data, Node<T> parent){
            this.data = data;
            this.parent = parent;
            this.children = new ArrayList<Node<T>>();
        }
    }

    public class BlockInfo {
        public Block block;
        public UTXOPool pool;
        public int height;

        public BlockInfo(Block block, UTXOPool pool, int height){
            this.block = block;
            this.pool = pool;
            this.height = height;
        }

        // return null if this block contains invalid txs
        // otherwise return updated pool
        public UTXOPool getNextUTXOPool(Block block){
            TxHandler handler = new TxHandler(new UTXOPool(pool));
            ArrayList<Transaction> tx_list = block.getTransactions();
            Transaction[] txs = tx_list.toArray(new Transaction[tx_list.size()]);
            Transaction[] h_txs = handler.handleTxs(txs);
            if(h_txs.length < txs.length)
                return null;
            
            UTXOPool new_pool= handler.getUTXOPool();
            Transaction coin_base = block.getCoinbase();
            
            new_pool.addUTXO(new UTXO(coin_base.getHash(), 0), coin_base.getOutput(0));
            
            return new_pool;
        }

        // return null if this block contains invalid txs
        public BlockInfo getNextBlockInfo(Block block){
            UTXOPool pool = getNextUTXOPool(block);
            if(pool == null)
                return null;

            return new BlockInfo(block, pool, height + 1);
        }
    }

    private Node<BlockInfo> root;
    private HashMap<ByteArrayWrapper, Node<BlockInfo>> block_pool;
    private BlockInfo highest_block;

    private TransactionPool transaction_pool;

    /**
     * create an empty block chain with just a genesis block. Assume {@code genesisBlock} is a valid
     * block
     */
    public BlockChain(Block genesisBlock) {
        // IMPLEMENT THIS
        BlockInfo genesisBlockInfo = new BlockInfo(genesisBlock, new UTXOPool(), 1);
        genesisBlockInfo.pool = genesisBlockInfo.getNextUTXOPool(genesisBlock);

        root = new Node<BlockInfo>(genesisBlockInfo, null);

        block_pool = new HashMap<ByteArrayWrapper, Node<BlockInfo>>();
        block_pool.put(new ByteArrayWrapper(genesisBlock.getHash()), root);

        highest_block = genesisBlockInfo;

        transaction_pool = new TransactionPool();
    }

    /** Get the maximum height block */
    public Block getMaxHeightBlock() {
        // IMPLEMENT THIS
        return highest_block.block;
    }

    /** Get the UTXOPool for mining a new block on top of max height block */
    public UTXOPool getMaxHeightUTXOPool() {
        // IMPLEMENT THIS
        return new UTXOPool(highest_block.pool);
    }

    /** Get the transaction pool to mine a new block */
    public TransactionPool getTransactionPool() {
        // IMPLEMENT THIS
        return new TransactionPool(transaction_pool);
    }

    /**
     * Add {@code block} to the block chain if it is valid. For validity, all transactions should be
     * valid and block should be at {@code height > (maxHeight - CUT_OFF_AGE)}.
     * 
     * <p>
     * For example, you can try creating a new block over the genesis block (block height 2) if the
     * block chain height is {@code <=
     * CUT_OFF_AGE + 1}. As soon as {@code height > CUT_OFF_AGE + 1}, you cannot create a new block
     * at height 2.
     * 
     * @return true if block is successfully added
     */
    public boolean addBlock(Block block) {
        // IMPLEMENT THIS
        // check validity
        if(block.getPrevBlockHash() == null)
            return false;
        
        ByteArrayWrapper prev_hash = new ByteArrayWrapper(block.getPrevBlockHash());
        if(!block_pool.containsKey(prev_hash))
            return false;
        
        Node<BlockInfo> prev_node = block_pool.get(prev_hash);
        BlockInfo prev_block = prev_node.data;

        if((prev_block.height + 1) <= (highest_block.height - CUT_OFF_AGE))
            return false;
        
        BlockInfo current_block = prev_block.getNextBlockInfo(block);
        if(current_block == null)
            return false;

        // insert into blockchain
        Node<BlockInfo> current_node = new Node<BlockInfo>(current_block, prev_node);
        prev_node.children.add(current_node);

        block_pool.put(new ByteArrayWrapper(block.getHash()), current_node);

        // remove transactions ...
        ArrayList<Transaction> txs = block.getTransactions();
        for(int i = 0; i < txs.size(); i ++){
            transaction_pool.removeTransaction(txs.get(i).getHash());
        }
        
        // check if this block is the highest block
        if(current_block.height > highest_block.height)
            highest_block = current_block;
        
        if(current_block.height <= CUT_OFF_AGE)
            return true;
        
        // TODO: delete those nodes that are out of range

        return true;   
    }

    /** Add a transaction to the transaction pool */
    public void addTransaction(Transaction tx) {
        // IMPLEMENT THIS
        transaction_pool.addTransaction(tx);
    }
}