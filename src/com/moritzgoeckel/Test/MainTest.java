package com.moritzgoeckel.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.moritzgoeckel.Data.PositionType;
import com.moritzgoeckel.Indicators.RollingIndicator;
import com.moritzgoeckel.Indicators.SMA;
import com.moritzgoeckel.Market.Market;
import com.moritzgoeckel.Market.OandaMarket;
import com.oanda.v20.Context;
import com.oanda.v20.account.AccountID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Test collection")
class MainTest {

    @Test
    @DisplayName("Testtest")
    void myFirstTest() {
        assertEquals(2, 1 + 1);

        Context ctx = new Context("https://api-fxpractice.oanda.com", "3a9a4f4ce5b4c5838dbfc24a7706151b-dd15e44936e5c3a48fbae5277d024e07");

        Market m = new OandaMarket(ctx, new AccountID("101-004-2357917-002"));

        PositionType type = m.isPositionOpen("EUR_USD");

        System.out.println("Open position on EUR_USD -> " + type);

        if(type != PositionType.None) {
            System.out.println("Closing on EUR_USD -> " + type + " ...");
            m.closePosition("EUR_USD");
        }

        m.openPosition("EUR_USD", 1, PositionType.Sell);
    }

    void indicatorTest(){
        RollingIndicator sma = new SMA(10);
        for(int i = 0; i < 30; i++){
            String s = "NULL";
            try{
                sma.pushValue((double) i);
                s = Double.toString(sma.getIndicatorValue());
            }catch (Exception e){ e.printStackTrace(); }
            System.out.println(s);
        }
    }

}
