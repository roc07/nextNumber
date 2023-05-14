package com.number.next.service;

import com.number.next.exception.InvalidListSize;
import com.number.next.exception.InvalidParametersException;
import com.number.next.model.Parameters;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Getter
@Slf4j
public class NumberService {

    private static Double MAX_CHANCE = 1d;

    private Double probabilitySum;
    private List<Integer> randomNumbers;
    private List<Double> probabilities = new ArrayList<>();

    public Map<Integer, Integer> getNumbersWithAppearances(int numberOfRequests) {
        Map<Integer, Integer> results = new HashMap<>();
        for (int i = 0; i < numberOfRequests; i++) {
            int number = getSingleNumber();
            int timesNumberAppears = results.get(number) != null ? results.get(number) : 0;
            results.put(number, ++timesNumberAppears);
        }
        return results;
    }

    public int getSingleNumber() {
        MAX_CHANCE = 1d;
        validateLists();
        Random randomGenerator = new Random();
        Double currentChance;
        int result = 0;
        log.info(probabilities.toString());
        for (int i = 0; i < probabilities.size(); i++) {
            currentChance = randomGenerator.nextDouble() * MAX_CHANCE;
            if (probabilities.get(i) >= currentChance) {
                return randomNumbers.get(i);
            }
            MAX_CHANCE -= probabilities.get(i);
        }
        return result;
    }

    public List<Double> prepareNumbers(List<Integer> numbers) {
        this.randomNumbers = numbers;
        prepareRandomProbabilities(numbers.size());
        return probabilities;
    }

    public List<Double> prepareProbabilities(Parameters parameters) {
        if (parameters == null || parameters.getNumbers() == null || parameters.getProbability() == null) {
            throw new InvalidParametersException("Invalid parameters provided");
        }
        randomNumbers = parameters.getNumbers();
        probabilities = parameters.getProbability();
        validateLists();
        validateListSizes(randomNumbers, probabilities);
        DoubleSummaryStatistics stats = probabilities.stream().collect(Collectors.summarizingDouble(Double::doubleValue));
        probabilitySum = stats.getSum();
        probabilities = probabilityAsPercentageChance(probabilities);
        return probabilities;
    }

    private void prepareRandomProbabilities(int size) {
        probabilitySum = 0d;
        probabilities.clear();
        for (int i = 0; i < size; i++) {
            Double probability = getRandomProbability();
            probabilities.add(probability);
            probabilitySum += probability;
        }
        probabilities = probabilityAsPercentageChance(probabilities);
    }

    private List<Double> probabilityAsPercentageChance(List<Double> probabilities) {
        return probabilities
                .stream()
                .map(probability -> probability / probabilitySum)
                .collect(Collectors.toList());
    }

    private Double getRandomProbability() {
        Random randomGenerator = new Random();
        return randomGenerator.nextDouble();
    }

    private void validateLists() {
        if (randomNumbers == null || randomNumbers.isEmpty()) {
            throw new InvalidListSize("Initialize the list of numbers");
        }
    }

    private void validateListSizes(List<Integer> numbers, List<Double> probabilities) {
        if (numbers.size() != probabilities.size()) {
            throw new InvalidParametersException("Numbers list and probabilities list must have the same size");
        }
        if (CollectionUtils.containsInstance(numbers, null) || (CollectionUtils.containsInstance(probabilities,null))) {
            throw new InvalidParametersException("Provide valid numbers and probabilities");
        }
    }
}
