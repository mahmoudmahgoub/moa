package moa.classifiers.core.driftdetection.sddmutils;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class Reader {

    //np_Ranges similiar to Python: np.arange(start_index, end_index, length_drift)
    // example: System.out.println( npRange(1,7,2));
    public List<Integer> npRange(int start, int end, int step) {
        return
                IntStream
                        .range(start, end)
                        .filter(i -> (i - start) % step == 0)
                        .boxed()
                        .collect(Collectors.toList());
    }
    //np Range that return stream so that I can use Map on it instead of for loop:
    //example:
    public static Stream<Integer> npRangeStream(int start, int end, int step) {
        return
                IntStream
                        .range(start, end)
                        .filter(i -> (i - start) % step == 0)
                        .boxed();
    }
}

//file_data.stream().skip(2).limit(5); //end-start;