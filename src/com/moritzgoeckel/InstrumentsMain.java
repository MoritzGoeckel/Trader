package com.moritzgoeckel;

import com.moritzgoeckel.Data.CandleDownloader;
import com.oanda.v20.Context;
import com.oanda.v20.ExecuteException;
import com.oanda.v20.RequestException;
import com.oanda.v20.account.AccountID;
import com.oanda.v20.primitives.Instrument;
import com.oanda.v20.primitives.InstrumentType;

import java.util.List;

public class InstrumentsMain {
    public static void main(String... args) throws ExecuteException, RequestException {
        Context ctx = new Context("https://api-fxpractice.oanda.com", "3a9a4f4ce5b4c5838dbfc24a7706151b-dd15e44936e5c3a48fbae5277d024e07");
        CandleDownloader downloader = new CandleDownloader(ctx);

        List<Instrument> instruments = ctx.account.instruments(new AccountID("101-004-2357917-002")).getInstruments();

        for(Instrument i : instruments){
            if((i.getName().toString().contains("USD") || i.getName().toString().contains("EUR")) && (i.getType() == InstrumentType.CFD || i.getType() == InstrumentType.METAL))
                System.out.print(i.getName() + "|");
        }
    }
}
