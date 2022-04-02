package io.gitlab.chaver.mining.patterns.measure.operand;

import io.gitlab.chaver.mining.patterns.measure.Measure;
import io.gitlab.chaver.mining.patterns.measure.MeasureFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MeasureOperandTest {

    @Test
    public void testConvertArea() {
        Assertions.assertEquals(new HashSet<>(List.of(MeasureFactory.freq())), MeasureFactory.area().maxConvert());
        Assertions.assertEquals(new HashSet<>(List.of(MeasureFactory.length())), MeasureFactory.area().minConvert());
    }

    @Test
    public void testConvertFreqAreaGr() {
        List<Measure> mList = Arrays.asList(MeasureFactory.freq(), MeasureFactory.area(), MeasureFactory.growthRate());
        Assertions.assertEquals(new HashSet<>(List.of(MeasureFactory.freq(), MeasureFactory.freq1())), MeasureOperand.maxConvert(mList));
        Assertions.assertEquals(new HashSet<>(List.of(MeasureFactory.length(), MeasureFactory.freq2())), MeasureOperand.minConvert(mList));
    }

    @Test
    public void testConvertMean() {
        Assertions.assertEquals(new HashSet<>(List.of(MeasureFactory.min(0))), MeasureFactory.mean(0).maxConvert());
        Assertions.assertEquals(new HashSet<>(List.of(MeasureFactory.max(0))), MeasureFactory.mean(0).minConvert());
    }

    @Test
    public void testConvertAllConf() {
        Assertions.assertEquals(new HashSet<>(List.of(MeasureFactory.freq(), MeasureFactory.maxFreq())), MeasureFactory.allConf().maxConvert());
        Assertions.assertEquals(new HashSet<>(List.of()), MeasureFactory.allConf().minConvert());
    }

    @Test
    public void testConvertAnyConf() {
        Assertions.assertEquals(new HashSet<>(List.of(MeasureFactory.freq())), MeasureFactory.anyConf().maxConvert());
        Assertions.assertEquals(new HashSet<>(List.of(MeasureFactory.minFreq())), MeasureFactory.anyConf().minConvert());
    }

}
