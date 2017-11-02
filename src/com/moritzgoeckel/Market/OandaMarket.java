package com.moritzgoeckel.Market;

import com.oanda.v20.Context;
import com.oanda.v20.account.AccountID;
import com.oanda.v20.account.AccountListResponse;
import com.oanda.v20.account.AccountProperties;
import com.oanda.v20.order.MarketOrderRequest;
import com.oanda.v20.order.OrderCreateRequest;
import com.oanda.v20.order.OrderType;
import com.oanda.v20.position.Position;
import com.oanda.v20.position.PositionCloseRequest;
import com.oanda.v20.position.PositionListOpenResponse;
import com.oanda.v20.position.PositionSide;
import com.oanda.v20.primitives.InstrumentName;
import com.sun.javaws.exceptions.InvalidArgumentException;

import java.util.List;

public class OandaMarket implements Market {

    private Context ctx;
    private AccountID account;

    public OandaMarket(String url, String token, AccountID account){
        this(new Context(url, token), account);
    }

    public OandaMarket(Context ctx, AccountID account){
        this.ctx = ctx;
        setAccount(account);

        /*AccountID accountId = new AccountID("101-004-2357917-002"); //AutoTest -> 1000€ 10/1
        List<String> instruments = new ArrayList<>(
                Arrays.asList("EUR_USD", "USD_JPY", "GBP_USD", "USD_CHF"));*/

        /*AccountListResponse list = ctx.account.list();
        for(AccountProperties p : list.getAccounts())
            System.out.println(p.getId());*/

        // AccountID accountId = new AccountID("101-004-2357917-002");
    }

    public void setAccount(AccountID s){
        this.account = s;
    }

    @Override
    public void openPosition(String instrument, double units, PositionType type) {

        if(units <= 0)
            throw new RuntimeException("Units cant be negative, was " + units);

        if(type == PositionType.Sell)
            units = -units;

        if(type == PositionType.None)
            throw new RuntimeException("PositionType cant be NONE");

        try {
            ctx.order.create(new OrderCreateRequest(this.account).setOrder(new MarketOrderRequest().setInstrument(instrument).setUnits(units)));
        }catch (Exception e) { System.out.println(e.getMessage()); e.printStackTrace(); }
    }

    @Override
    public void closePosition(String instrument) {
        try {
            Position p = getOpenPosition(instrument);

            if(p != null) {
                PositionCloseRequest request = new PositionCloseRequest(this.account, new InstrumentName(instrument));
                if(p.getShort().getUnits().doubleValue() < 0)
                    request.setShortUnits(p.getShort().getUnits().toString().substring(1));

                if(p.getLong().getUnits().doubleValue() > 0)
                    request.setLongUnits(p.getLong().getUnits().toString());

                ctx.position.close(request);
            }
        }catch (Exception e) { System.out.println(e.getMessage()); e.printStackTrace(); }
    }

    @Override
    public PositionType isPositionOpen(String instrument) {
        Position p = getOpenPosition(instrument);

        if(p != null) {
            //System.out.println(instrumentPosition);
            if (p.getLong().getUnits().doubleValue() > 0)
                return PositionType.Buy;
            else if (p.getShort().getUnits().doubleValue() < 0)
                return PositionType.Sell;
        }

        return PositionType.None;
    }

    private Position getOpenPosition(String instrument){
        try {
            List<Position> positions = ctx.position.listOpen(this.account).getPositions();
            for(Position p : positions)
                if(p.getInstrument().equals(instrument))
                    return p;
        }
        catch (Exception e) { System.out.println(e.getMessage()); e.printStackTrace(); }

        return null;
    }
}
