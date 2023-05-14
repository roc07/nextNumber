package com.number.next.controller;

import com.number.next.exception.InvalidListSize;
import com.number.next.exception.InvalidParametersException;
import com.number.next.model.Parameters;
import com.number.next.service.NumberService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.openMocks;

class NumberControllerTest {

    @Mock
    private NumberService numberServiceMock;

    @InjectMocks
    private NumberController numberController;

    @BeforeEach
    public void setUp() {
        openMocks(this);
    }

    @ParameterizedTest
    @MethodSource("getInvalidNumbers")
    void provideNumbers_tooFewValuesProvided_returnsBadRequest(List<Integer> invalidList) {
        ResponseEntity<String> result = numberController.provideNumbers(invalidList);

        verifyNoInteractions(numberServiceMock);
        assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
    }

    private static Stream<List<Integer>> getInvalidNumbers() {
        return Stream.of(null, List.of(), List.of(1));
    }

    @Test
    void provideNumbers_withProvidedNumbers_returnsOkAndChanceForEach() {
        List<Double> chance = getRandomChance();
        List<Integer> numbers = getNumbers();
        when(numberServiceMock.prepareNumbers(numbers)).thenReturn(chance);

        ResponseEntity<String> result = numberController.provideNumbers(numbers);

        verify(numberServiceMock, times(1)).prepareNumbers(numbers);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals("Expected chance for numbers to appear: [0.30743956507663706, 0.29577099069957985, 0.14176441287456293, 0.2550250313492202]",
                result.getBody());
    }

    @Test
    void provideNumbersAndProbabilities_withProvidedParameters_returnsOkAndChanceForEach() {
        List<Double> chance = getProvidedChance();
        Parameters parameters = Parameters
                .builder()
                .numbers(getNumbers())
                .probability(getProbabilities())
                .build();
        when(numberServiceMock.prepareProbabilities(parameters)).thenReturn(chance);

        ResponseEntity<String> result = numberController.provideNumbersAndProbabilities(parameters);

        verify(numberServiceMock, times(1)).prepareProbabilities(parameters);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals("Expected chance for numbers to appear: [0.20833333333333334, 0.0625, 0.625, 0.10416666666666667]",
                result.getBody());
    }

    @ParameterizedTest
    @MethodSource("getException")
    void provideNumbersAndProbabilities_numberServiceThrows_returnsBadRequest(Class<? extends RuntimeException> exceptionClass) {
        when(numberServiceMock.prepareProbabilities(any())).thenThrow(exceptionClass);

        ResponseEntity<String> result = numberController.provideNumbersAndProbabilities(any());

        verify(numberServiceMock, times(1)).prepareProbabilities(any());
        assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
    }

    @Test
    void getSingleNumber_void_returnsOkAndNumber() {
        when(numberServiceMock.getSingleNumber()).thenReturn(1);

        ResponseEntity<?> result = numberController.getSingleNumber();

        verify(numberServiceMock, times(1)).getSingleNumber();
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(1, result.getBody());
    }

    @Test
    void getSingleNumber_numberServiceThrows_returnsBadRequest() {
        when(numberServiceMock.getSingleNumber()).thenThrow(InvalidListSize.class);

        ResponseEntity<?> result = numberController.getSingleNumber();

        verify(numberServiceMock, times(1)).getSingleNumber();
        assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
        assertNull(result.getBody());
    }

    @Test
    void getMultipleNumbers_anyNumberOfRequests_returnsOkWithMap() {
        Map<Integer, Integer> numbersWithAppearances = getNumbersWithAppearances();
        when(numberServiceMock.getNumbersWithAppearances(2)).thenReturn(numbersWithAppearances);

        ResponseEntity<?> result = numberController.getMultipleNumbers(2);

        verify(numberServiceMock, times(1)).getNumbersWithAppearances(2);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(numbersWithAppearances, result.getBody());
    }

    @Test
    void getMultipleNumbers_numberServiceThrows_returnsBadRequest() {
        when(numberServiceMock.getNumbersWithAppearances(100)).thenThrow(InvalidListSize.class);

        ResponseEntity<?> result = numberController.getMultipleNumbers(100);

        verify(numberServiceMock, times(1)).getNumbersWithAppearances(100);
        assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
        assertNull(result.getBody());
    }

    private List<Integer> getNumbers() {
        return List.of(1, 2, 3, 4);
    }

    private List<Double> getProbabilities() {
        return List.of(20d, 30d, 100d, 50d);
    }

    private List<Double> getRandomChance() {
        return List.of(0.30743956507663706, 0.29577099069957985, 0.14176441287456293, 0.2550250313492202);
    }

    private List<Double> getProvidedChance() {
        return List.of(0.20833333333333334, 0.0625, 0.625, 0.10416666666666667);
    }

    private Map<Integer, Integer> getNumbersWithAppearances() {
        return Map.of(1, 1, 2, 1);
    }

    private static Stream<Class<? extends RuntimeException>> getException() {
        return Stream.of(
                InvalidParametersException.class,
                InvalidListSize.class
        );
    }
}