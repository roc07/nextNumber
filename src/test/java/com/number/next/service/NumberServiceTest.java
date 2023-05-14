package com.number.next.service;

import com.number.next.exception.InvalidListSize;
import com.number.next.exception.InvalidParametersException;
import com.number.next.model.Parameters;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class NumberServiceTest {

    @InjectMocks
    private NumberService numberService;

    @Test
    void getSingleNumber_withValidNumbers_willAlwaysReturn() {
        List<Integer> numbers = getNumbers();
        numberService.prepareNumbers(numbers);

        int number = numberService.getSingleNumber();

        assertTrue(number > 0);
        assertEquals(numbers.size(), numberService.getProbabilities().size());
    }

    @Test
    void getSingleNumber_withNoNumbersProvided_willThrow() {
        numberService.prepareNumbers(List.of());

        assertThrows(InvalidListSize.class, () -> numberService.getSingleNumber());
    }

    @Test
    void prepareProbabilities_withNumbersAndProbabilities_willPreparePercentages() {
        Parameters parameters = Parameters
                .builder()
                .numbers(getNumbers())
                .probability(getProbabilities())
                .build();
        List<Double> percentages = numberService.prepareProbabilities(parameters);

        assertEquals(0.10, percentages.get(0));
        assertEquals(0.15, percentages.get(1));
        assertEquals(0.50, percentages.get(2));
        assertEquals(0.25, percentages.get(3));
    }

    @Test
    void prepareProbabilities_withDifferentListSizes_willThrow() {
        Parameters parameters = Parameters
                .builder()
                .numbers(getNumbers())
                .probability(List.of(20d, 30d, 100d, 50d, 20d))
                .build();
        assertThrows(InvalidParametersException.class, () -> numberService.prepareProbabilities(parameters));
    }

    @ParameterizedTest
    @MethodSource("getInvalidParameters")
    void prepareProbabilities_withNoNumbersProvided_willThrow(Parameters parameters) {
        assertThrows(InvalidParametersException.class, () -> numberService.prepareProbabilities(parameters));
    }

    private static Stream<Parameters> getInvalidParameters() {
        List<Integer> invalidIntList = Collections.singletonList(null);
        List<Double> invalidDoubleList = Collections.singletonList(null);
        return Stream.of(
                null,
                Parameters.builder().build(),
                Parameters.builder().numbers(invalidIntList).probability(List.of(1.0)).build(),
                Parameters.builder().numbers(List.of(1)).probability(invalidDoubleList).build(),
                Parameters.builder().numbers(null).probability(getProbabilities()).build(),
                Parameters.builder().numbers(getNumbers()).probability(null).build());
    }

    @Test
    void multipleCallsToGetSingleNumber_withNumbersAndProbabilities_willReturnWithinExpectedRanges() {
        Parameters parameters = Parameters
                .builder()
                .numbers(getNumbers())
                .probability(getProbabilities())
                .build();
        numberService.prepareProbabilities(parameters);

        Map<Integer, Integer> results = new HashMap<>();
        for (int i = 0; i < 10000; i++) {
            int num = numberService.getSingleNumber();
            int end = results.get(num) != null ? results.get(num) : 0;
            results.put(num, ++end);
        }

        assertTrue(results.get(1) > 900 && results.get(1) < 1100);
        assertTrue(results.get(2) > 1400 && results.get(1) < 1600);
        assertTrue(results.get(3) > 4900 && results.get(1) < 5100);
        assertTrue(results.get(4) > 2400 && results.get(1) < 2600);
    }

    @Test
    void getNumbersWithAppearances_withNumbersAndProbabilities_willReturnMap() {
        Parameters parameters = Parameters
                .builder()
                .numbers(getNumbers())
                .probability(getProbabilities())
                .build();
        numberService.prepareProbabilities(parameters);
        Map<Integer, Integer> results = numberService.getNumbersWithAppearances(10000);

        assertTrue(results.get(1) > 900 && results.get(1) < 1100);
        assertTrue(results.get(2) > 1400 && results.get(1) < 1600);
        assertTrue(results.get(3) > 4850 && results.get(1) < 5150);
        assertTrue(results.get(4) > 2400 && results.get(1) < 2600);
    }

    private static List<Integer> getNumbers() {
        return List.of(1, 2, 3, 4);
    }

    private static List<Double> getProbabilities() {
        return List.of(20d, 30d, 100d, 50d);
    }

}