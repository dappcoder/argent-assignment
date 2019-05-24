package malex.argent.watcher.model;

import java.math.BigInteger;

public class TokenData {

    private final String symbol;

    private final String name;

    private final BigInteger decimals;

    public TokenData(String symbol, String name, BigInteger decimals) {
        this.symbol = symbol;
        this.name = name;
        this.decimals = decimals;
    }

    public String getSymbol() {
        return symbol;
    }

    public String getName() {
        return name;
    }

    public BigInteger getDecimals() {
        return decimals;
    }
}
