package malex.argent.watcher.model;

public class EthLogData {

    private final String toAddress;

    private final String amount;

    private final String txHash;

    public EthLogData(String toAddress, String amount, String txHash) {
        this.toAddress = toAddress;
        this.amount = amount;
        this.txHash = txHash;
    }

    public String getToAddress() {
        return toAddress;
    }

    public String getAmount() {
        return amount;
    }

    public String getTxHash() {
        return txHash;
    }
}
