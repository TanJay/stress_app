package com.tanushaj.element;

import com.robinhood.spark.SparkAdapter;
import com.tanushaj.element.models.HRVDto;

import java.util.List;

public class HeartRateAdapter extends SparkAdapter {

    private List<HRVDto> dto = null;

    public HeartRateAdapter(List<HRVDto> hrvDto) {
        this.dto = hrvDto;
    }

    @Override
    public int getCount() {
        return this.dto.size();
    }

    @Override
    public Object getItem(int index) {
        return this.dto.get(index);
    }

    @Override
    public float getY(int index) {
        return this.dto.get(index).getHR();
    }
}
