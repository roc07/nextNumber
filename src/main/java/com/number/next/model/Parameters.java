package com.number.next.model;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class Parameters {

    private List<Integer> numbers;
    private List<Double> probability;
}

