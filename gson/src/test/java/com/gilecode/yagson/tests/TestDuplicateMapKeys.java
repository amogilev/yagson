package com.gilecode.yagson.tests;

import com.gilecode.yagson.YaGson;
import com.google.gson.reflect.TypeToken;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TestDuplicateMapKeys {


    @Test
    public void verifyMapKeySerialization(){
        YaGson gson = new YaGson();

        HashMap<Integer, Double> first = new HashMap<Integer, Double>();
        first.put(1, 0.0);
        first.put(2, 0.0);

        HashMap<Integer, Double> second = new HashMap<Integer, Double>();
        second.put(1, 0.0);
        second.put(2, 0.0);

        List<HashMap<Integer, Double>> list = new ArrayList<HashMap<Integer, Double>>();
        list.add(first);
        list.add(second);

        String json = gson.toJson(list, new TypeToken<ArrayList<HashMap<Integer,Double>>>(){}.getType());

        System.out.println(json);

        List<Map<Integer, Double>> roundtrip = gson.fromJson(json, new TypeToken<ArrayList<HashMap<Integer,Double>>>(){}.getType());
    }

}
