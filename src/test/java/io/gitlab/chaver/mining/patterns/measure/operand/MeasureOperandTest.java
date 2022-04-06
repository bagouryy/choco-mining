package io.gitlab.chaver.mining.patterns.measure.operand;

import io.gitlab.chaver.mining.patterns.measure.Measure;
import io.gitlab.chaver.mining.patterns.measure.MeasureFactory;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MeasureOperandTest {

    @Test
    public void testConvertArea() {
        assertEquals(new HashSet<>(Arrays.asList(MeasureFactory.freq())), MeasureFactory.area().maxConvert());
        assertEquals(new HashSet<>(Arrays.asList(MeasureFactory.length())), MeasureFactory.area().minConvert());
    }

    @Test
    public void testConvertFreqAreaGr() {
        List<Measure> mList = Arrays.asList(MeasureFactory.freq(), MeasureFactory.area(), MeasureFactory.growthRate());
        assertEquals(new HashSet<>(Arrays.asList(MeasureFactory.freq(), MeasureFactory.freq1())), MeasureOperand.maxConvert(mList));
        assertEquals(new HashSet<>(Arrays.asList(MeasureFactory.length(), MeasureFactory.freq2())), MeasureOperand.minConvert(mList));
    }

    @Test
    public void testConvertMean() {
        assertEquals(new HashSet<>(Arrays.asList(MeasureFactory.min(0))), MeasureFactory.mean(0).maxConvert());
        assertEquals(new HashSet<>(Arrays.asList(MeasureFactory.max(0))), MeasureFactory.mean(0).minConvert());
    }

    @Test
    public void testConvertAllConf() {
        assertEquals(new HashSet<>(Arrays.asList(MeasureFactory.freq(), MeasureFactory.maxFreq())), MeasureFactory.allConf().maxConvert());
        assertEquals(new HashSet<>(Arrays.asList()), MeasureFactory.allConf().minConvert());
    }

    @Test
    public void testConvertAnyConf() {
        assertEquals(new HashSet<>(Arrays.asList(MeasureFactory.freq())), MeasureFactory.anyConf().maxConvert());
        assertEquals(new HashSet<>(Arrays.asList(MeasureFactory.minFreq())), MeasureFactory.anyConf().minConvert());
    }

}
