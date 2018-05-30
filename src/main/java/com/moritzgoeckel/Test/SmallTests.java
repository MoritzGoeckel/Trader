package com.moritzgoeckel.Test;

import com.moritzgoeckel.Data.PositionType;
import com.moritzgoeckel.Indicators.RollingIndicator;
import com.moritzgoeckel.Indicators.SMA;
import com.moritzgoeckel.Market.Market;
import com.moritzgoeckel.Market.OandaMarket;
import com.moritzgoeckel.Util.Formatting;
import com.oanda.v20.Context;
import com.oanda.v20.account.AccountID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("Test collection")
class SmallTests {

    @Test
    @DisplayName("Round test")
    void myFirstTest() {
        assertEquals(7.123, Formatting.round(7.123456, 3));
        assertEquals(7.12, Formatting.round(7.123456, 2));
        assertEquals(7.1, Formatting.round(7.123456, 1));
    }
}
