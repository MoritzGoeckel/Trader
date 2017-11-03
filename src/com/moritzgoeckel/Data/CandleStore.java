package com.moritzgoeckel.Data;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.model.*;
import com.oanda.v20.instrument.CandlestickData;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;

public class CandleStore {

    private AmazonDynamoDB client;

    public CandleStore(AmazonDynamoDB client){
        this.client = client;
    }

    public AmazonDynamoDB getClient() {
        return client;
    }

    public List<Candle> loadCandles(String instrument, String granularity, LocalDateTime from, LocalDateTime to) {

        List<Candle> candles = new LinkedList<>();
        long lastTimestamp;

        lastTimestamp = loadAddCandlesToListAndGetLastTimestamp(instrument, granularity, from.toEpochSecond(ZoneOffset.UTC), to.toEpochSecond(ZoneOffset.UTC), candles);

        while (lastTimestamp != -1) {
            lastTimestamp = loadAddCandlesToListAndGetLastTimestamp(instrument, granularity, lastTimestamp + 1L, to.toEpochSecond(ZoneOffset.UTC), candles);
        }

        return candles;
    }

    private long loadAddCandlesToListAndGetLastTimestamp(String instrument, String granularity, long from, long to, List<Candle> candlesList){
        Map<String, AttributeValue> values = new HashMap<>();
        values.put(":instrGran", new AttributeValue(instrument + "_" + granularity));
        values.put(":tsTo", new AttributeValueShort(to));
        values.put(":tsFrom", new AttributeValueShort(from));

        QueryRequest queryRequest = new QueryRequest()
                .withTableName("Candles")
                .withKeyConditionExpression("instrument_granularity = :instrGran AND timestampSeconds BETWEEN :tsFrom AND :tsTo")
                .withExpressionAttributeValues(values);

        QueryResult result = client.query(queryRequest);


        for (Map<String, AttributeValue> item : result.getItems()){
            Candle candle = new Candle();

            if(item.containsKey("ask_c")) {
                CandlestickData ask = new CandlestickData();
                ask.setC(item.get("ask_c").getN());
                ask.setO(item.get("ask_o").getN());
                ask.setH(item.get("ask_h").getN());
                ask.setL(item.get("ask_l").getN());
                candle.setAsk(ask);
            }

            if(item.containsKey("bid_c")) {
                CandlestickData bid = new CandlestickData();
                bid.setC(item.get("bid_c").getN());
                bid.setO(item.get("bid_o").getN());
                bid.setH(item.get("bid_h").getN());
                bid.setL(item.get("bid_l").getN());
                candle.setBid(bid);
            }

            if(item.containsKey("mid_c")) {
                CandlestickData mid = new CandlestickData();
                mid.setC(item.get("mid_c").getN());
                mid.setO(item.get("mid_o").getN());
                mid.setH(item.get("mid_h").getN());
                mid.setL(item.get("mid_l").getN());
                candle.setMid(mid);
            }

            candle.setComplete(item.get("complete").getBOOL());
            candle.setVolume(Integer.parseInt(item.get("volume").getN()));
            candle.setTime(item.get("time").getS());

            candlesList.add(candle);
        }

        Map<String,AttributeValue> lastKey = result.getLastEvaluatedKey();

        if(lastKey != null)
            return  Long.parseLong(lastKey.get("timestampSeconds").getN());
        else
            return -1;
    }

    public void saveCandle(List<Candle> candles) {
        List<WriteRequest> requests = new LinkedList<>();

        for(Candle candle : candles) {
            HashMap<String, AttributeValue> values = convertCandleToDynamoDBItem(candle, candle.getInstrument(), candle.getGranularity());
            client.putItem("Candles", values);

            requests.add(new WriteRequest().withPutRequest(new PutRequest().withItem(values)));

            if(requests.size() > 20) {
                client.batchWriteItem(new BatchWriteItemRequest().addRequestItemsEntry("Candles", requests));
                requests.clear();
            }
        }

        if(requests.size() > 0)
            client.batchWriteItem(new BatchWriteItemRequest().addRequestItemsEntry("Candles", requests));
    }

    private static HashMap<String, AttributeValue> convertCandleToDynamoDBItem(Candle candle, String instrument, String granularity){
        HashMap<String,AttributeValue> itemValues = new HashMap<>();

        itemValues.put("instrument_granularity", new AttributeValue(instrument + "_" + granularity));

        itemValues.put("timestampSeconds", new AttributeValueShort(candle.getLocalDateTime().toEpochSecond(ZoneOffset.UTC)));
        itemValues.put("time",  new AttributeValueShort(candle.getTime().toString()));
        itemValues.put("complete",  new AttributeValueShort(candle.getComplete()));
        itemValues.put("volume", new AttributeValueShort(candle.getVolume()));

        if(candle.getMid() == null && candle.getAsk() == null && candle.getBid() == null)
            throw new RuntimeException("Need at least one of mid/bid/ask");

        if(candle.getMid() != null) {
            itemValues.put("mid_o", new AttributeValueShort(candle.getMid().getO().doubleValue()));
            itemValues.put("mid_c", new AttributeValueShort(candle.getMid().getC().doubleValue()));
            itemValues.put("mid_h", new AttributeValueShort(candle.getMid().getH().doubleValue()));
            itemValues.put("mid_l", new AttributeValueShort(candle.getMid().getL().doubleValue()));
        }

        if(candle.getBid() != null) {
            itemValues.put("bid_o", new AttributeValueShort(candle.getBid().getO().doubleValue()));
            itemValues.put("bid_c", new AttributeValueShort(candle.getBid().getC().doubleValue()));
            itemValues.put("bid_h", new AttributeValueShort(candle.getBid().getH().doubleValue()));
            itemValues.put("bid_l", new AttributeValueShort(candle.getBid().getL().doubleValue()));
        }

        if(candle.getAsk() != null) {
            itemValues.put("ask_o", new AttributeValueShort(candle.getAsk().getO().doubleValue()));
            itemValues.put("ask_c", new AttributeValueShort(candle.getAsk().getC().doubleValue()));
            itemValues.put("ask_h", new AttributeValueShort(candle.getAsk().getH().doubleValue()));
            itemValues.put("ask_l", new AttributeValueShort(candle.getAsk().getL().doubleValue()));
        }

        return itemValues;
    }

    public void createCandleTable() {
        List<KeySchemaElement> keySchema = new ArrayList<>();
        keySchema.add(new KeySchemaElement().withAttributeName("instrument_granularity").withKeyType(KeyType.HASH));
        keySchema.add(new KeySchemaElement().withAttributeName("timestampSeconds").withKeyType(KeyType.RANGE));

        List<AttributeDefinition> attributeDefinitions= new ArrayList<>();
        attributeDefinitions.add(new AttributeDefinition().withAttributeName("instrument_granularity").withAttributeType(ScalarAttributeType.S));
        attributeDefinitions.add(new AttributeDefinition().withAttributeName("timestampSeconds").withAttributeType(ScalarAttributeType.N));

        CreateTableRequest request = new CreateTableRequest()
                .withTableName("Candles")
                .withKeySchema(keySchema)
                .withAttributeDefinitions(attributeDefinitions)
                .withProvisionedThroughput(new ProvisionedThroughput()
                        .withReadCapacityUnits(5L)
                        .withWriteCapacityUnits(6L)
                );

        client.createTable(request);
    }
}
