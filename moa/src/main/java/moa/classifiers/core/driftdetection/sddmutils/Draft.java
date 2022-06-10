package moa.classifiers.core.driftdetection.sddmutils;

import moa.classifiers.core.driftdetection.Employee;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class Draft

{
    //this is iterator to create stream using iterator
    //example: Draft.iterate(stream,ArffFileStream::hasMoreInstances ,stream.nextInstance().getData());
    // this example doesn't work as nextInstance.getData() not from type ArffFileStream
    public static<T> Stream<T> iterate(T seed, Predicate<? super T> hasNext, UnaryOperator<T> next) {
        Objects.requireNonNull(next);
        Objects.requireNonNull(hasNext);
        Spliterator<T> spliterator = new Spliterators.AbstractSpliterator<T>(Long.MAX_VALUE,
                Spliterator.ORDERED | Spliterator.IMMUTABLE) {

            T prev;
            boolean started, finished;

            @Override
            public boolean tryAdvance(Consumer<? super T> action) {
                Objects.requireNonNull(action);
                if (finished)
                    return false;
                T t;
                if (started)
                    t = next.apply(prev);
                else {
                    t = seed;
                    started = true;
                }
                if (!hasNext.test(t)) {
                    prev = null;
                    finished = true;
                    return false;
                }
                action.accept(prev = t);
                return true;
            }

            @Override
            public void forEachRemaining(Consumer<? super T> action) {
                Objects.requireNonNull(action);
                if (finished)
                    return;
                finished = true;
                T t = started ? next.apply(prev) : seed;
                prev = null;
                while (hasNext.test(t)) {
                    action.accept(t);
                    t = next.apply(t);
                }
            }
        };
        return StreamSupport.stream(spliterator, false);
    }

//np range example
// List<Integer> list = IntStream.rangeClosed(20, 100).boxed().collect(Collectors.toList());
        /*List<Integer> result = IntStream
                .rangeClosed(6, 15)
                .filter(i -> (i - 6) % 2 == 0)
                .map(list::get)
                .boxed()
                .collect(Collectors.toList());
*/
        // group by using 2 elements pair
    // System.out.println(train.stream().collect(Collectors.groupingBy(instance -> new AbstractMap.SimpleEntry<>(instance.value(0),instance.value(1)))));
    // grouping by examples:
List<Employee> employees = Arrays.asList(
        new Employee(1, 10, "Chandra"), new Employee(1, 20, "Rajesh"),
        new Employee(1, 20, "Rahul"), new Employee(3, 20, "Rajesh"));

    //Map < Integer, List< Employee >> byDept = employees.stream().collect(
    //       Collectors.groupingBy(Employee::getDepartment));

    //Map < Integer, List< String >> byDept = employees.stream().collect(
    //        Collectors.groupingBy(Employee::getDepartment, Collectors.mapping(Employee::getName,Collectors.toList())));

//        Map<Integer, Map<String, Long>> byDept = employees.stream().collect(
    //              Collectors.groupingBy(Employee::getDepartment, Collectors.groupingBy(Employee::getName,Collectors.counting())));//Collectors.mapping(Employee::getName,Collectors.toList())));
    //  Map<String, Map<Integer, List<Person>>> map = people
    //        .collect(Collectors.groupingBy(Person::getName,
    //          Collectors.groupingBy(Person::getAge));
    //  List<String> fields = Arrays.asList(new String[]{"City", "Age"}); //This will be constructed as per user's wish
/*
    Map<Integer, Map<String, Long>> byDept = employees.stream().collect(
            Collectors.groupingBy(Employee::getDepartment, Collectors.groupingBy(Employee::getName,Collectors.counting())));//Collectors.mapping(Employee::getName,Collectors.toList())));
                (byDept).forEach((k, v) -> System.out.println("DeptId:" +k +"   " + v));
 */
/*
    private static List<String> buildClassificationFunction(Map<String,String> map, List<String> fields) {
        return fields.stream()
                .map(map::get)
                .collect(Collectors.toList());
    }



    Map<List<String>, List<Collection<String>>> city = aList.stream()
            .collect(Collectors.groupingBy(map ->
                            buildClassificationFunction(map, fields), //Call the function to build the list
                    Collectors.mapping(Map::values, Collectors.toList())));
*/
}
