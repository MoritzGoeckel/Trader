package com.moritzgoeckel.Data;

import com.amazonaws.services.dynamodbv2.model.AttributeValue;

public class AttributeValueShort extends AttributeValue {
    public AttributeValueShort(Integer n){
        setN(n.toString());
    }
    public AttributeValueShort(Long n){
        setN(n.toString());
    }
    public AttributeValueShort(String s){
        setS(s);
    }
    public AttributeValueShort(Double n){
        setN(n.toString());
    }
    public AttributeValueShort(Boolean b){
        setBOOL(b);
    }
}
