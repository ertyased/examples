package info.kgeorgiy.ja.shchetinin.student;

import info.kgeorgiy.java.advanced.student.AdvancedQuery;
import info.kgeorgiy.java.advanced.student.Group;
import info.kgeorgiy.java.advanced.student.GroupName;
import info.kgeorgiy.java.advanced.student.Student;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class StudentDB implements AdvancedQuery {
    // :NOTE: private static final
    static final private Comparator<Student> COMPARATOR_STUDENT =
            Comparator.comparing(Student::getLastName, String.CASE_INSENSITIVE_ORDER)
                    .thenComparing(Student::getFirstName, String.CASE_INSENSITIVE_ORDER)
                    .thenComparingInt(student -> -student.getId());
    static final private Comparator<Map.Entry<String, Set<GroupName>>> ENTRY_STUDENT_COMPARATOR
            = Comparator.comparingInt(entry -> entry.getValue().size());
    static final private Comparator<Group> GROUP_BY_NAME =
            Comparator.comparing((group) -> group.getName().name(), String.CASE_INSENSITIVE_ORDER);
    static final private Comparator<Map.Entry<GroupName, List<Student>>> ENTRY_BY_GROUP_NAME =
            Comparator.comparing(entry -> entry.getKey().toString(), String.CASE_INSENSITIVE_ORDER);
    static final private Comparator<Student> STUDENT_BY_ID = Comparator.comparingInt(Student::getId);

    static final private Comparator<Map.Entry<GroupName, List<Student>>> GROUP_BY_SIZE
            = Comparator.comparingInt(entry -> entry.getValue().size());

    static final Comparator<Map.Entry<GroupName, Integer>> GROUP_BY_SIZE_NAME =
            Comparator.<Map.Entry<GroupName, Integer>>comparingInt(Map.Entry::getValue)
                    .thenComparing(entry -> entry.getKey().toString(), (s1, s2) -> -s1.compareTo(s2));

    private String getFirstSortedName(Collection<Student> students,
                                      BiFunction<Stream<Map.Entry<String, Set<GroupName>>>,
                                              Comparator<Map.Entry<String, Set<GroupName>>>,
                                              Optional<Map.Entry<String, Set<GroupName>>>> func,
                                      Comparator<String> comp) {
        return func.apply(students.stream()
                                .collect(Collectors.groupingBy(Student::getFirstName))
                                .entrySet()
                                .stream()
                                .map(entry -> Map.entry(entry.getKey(), entry.getValue().stream()
                                        .map(Student::getGroup)
                                        .collect(Collectors.toSet())
                                ))
                        , ENTRY_STUDENT_COMPARATOR.thenComparing(Map.Entry::getKey, comp)
                )
                .map(Map.Entry::getKey)
                .orElse("");

    }

    @Override
    public String getMostPopularName(Collection<Student> students) {
        return getFirstSortedName(students, Stream::max,
                String.CASE_INSENSITIVE_ORDER.reversed());
    }

    @Override
    public String getLeastPopularName(Collection<Student> students) {
        return getFirstSortedName(students, Stream::min, String.CASE_INSENSITIVE_ORDER);
    }

    private <T> List<T> getByIndex(Collection<Student> students, int[] indices, Function<Student, T> func) {
        return Arrays.stream(indices)
                .mapToObj(index -> students.stream()
                        .skip(index)
                        .findFirst()
                        .map(func)
                        .orElseThrow())
                .toList();
    }

    @Override
    public List<String> getFirstNames(Collection<Student> students, int[] indices) {
        return getByIndex(students, indices, Student::getFirstName);
    }

    @Override
    public List<String> getLastNames(Collection<Student> students, int[] indices) {
        return getByIndex(students, indices, Student::getLastName);
    }

    @Override
    public List<GroupName> getGroups(Collection<Student> students, int[] indices) {
        return getByIndex(students, indices, Student::getGroup);
    }

    @Override
    public List<String> getFullNames(Collection<Student> students, int[] indices) {
        return getByIndex(students, indices, student -> student.getFirstName() + " " + student.getLastName());
    }

    private Stream<Map.Entry<GroupName, List<Student>>> collectByGroups(Collection<Student> students) {
        return students.stream()
                .collect(Collectors.groupingBy(Student::getGroup))
                .entrySet()
                .stream();
    }

    private List<Group> getGroups(Collection<Student> students, Comparator<Student> comp) {
        return collectByGroups(students)
                .map(entry -> new Group(entry.getKey(),
                        entry.getValue().stream().sorted(comp).toList())
                )
                .sorted(GROUP_BY_NAME)
                .toList();
    }

    @Override
    public List<Group> getGroupsByName(Collection<Student> students) {
        return getGroups(students, COMPARATOR_STUDENT);
    }

    @Override
    public List<Group> getGroupsById(Collection<Student> students) {
        return getGroups(students, Comparator.comparingInt(Student::getId));
    }

    private GroupName getLargestGroupComp(Collection<Student> students, Comparator<Map.Entry<GroupName, List<Student>>> comp) {
        return collectByGroups(students)
                .max(comp.thenComparing(ENTRY_BY_GROUP_NAME)) // :NOTE: reuse
                .map(Map.Entry::getKey)
                .orElse(null);
    }

    @Override
    public GroupName getLargestGroup(Collection<Student> students) {
        return getLargestGroupComp(students, GROUP_BY_SIZE);
    }

    @Override
    public GroupName getLargestGroupFirstName(Collection<Student> students) {
        return collectByGroups(students)
                .map(entry -> Map.entry(entry.getKey(), new HashSet<>(entry.getValue().
                        stream()
                        .map(Student::getFirstName)
                        .toList()).size()))
                .max(GROUP_BY_SIZE_NAME)
                .map(Map.Entry::getKey)
                .orElse(null);
    }

    private <T> List<T> getStudentInfo(List<Student> students, Function<Student, T> func) {
        return students
                .stream()
                .map(func)
                .toList();
    }

    @Override
    public List<String> getFirstNames(List<Student> students) {
        return getStudentInfo(students, Student::getFirstName);
    }

    @Override
    public List<String> getLastNames(List<Student> students) {
        return getStudentInfo(students, Student::getLastName);
    }

    @Override
    public List<GroupName> getGroups(List<Student> students) {
        return getStudentInfo(students, Student::getGroup);
    }

    @Override
    public List<String> getFullNames(List<Student> students) {
        return getStudentInfo(students, student -> student.getFirstName() + " " + student.getLastName());
    }

    @Override
    public Set<String> getDistinctFirstNames(List<Student> students) {
        return new HashSet<>(getFirstNames(students));
    }

    @Override
    public String getMaxStudentFirstName(List<Student> students) {
        return students.stream()
                .max(STUDENT_BY_ID)
                .map(Student::getFirstName)
                .orElse("");
    }

    private List<Student> sortBy(Collection<Student> students, Comparator<Student> comp) {
        return students.stream()
                .sorted(comp)
                .toList();
    }

    @Override
    public List<Student> sortStudentsById(Collection<Student> students) {
        return sortBy(students, STUDENT_BY_ID);
    }

    @Override
    public List<Student> sortStudentsByName(Collection<Student> students) {
        return sortBy(students, COMPARATOR_STUDENT);
    }

    private List<Student> findStudentsBy(Collection<Student> students, Predicate<Student> predicate) {
        return students.stream()
                .filter(predicate)
                .sorted(COMPARATOR_STUDENT)
                .toList();
    }

    @Override
    public List<Student> findStudentsByFirstName(Collection<Student> students, String name) {
        return findStudentsBy(students, student -> student.getFirstName().equals(name));
    }

    @Override
    public List<Student> findStudentsByLastName(Collection<Student> students, String name) {
        return findStudentsBy(students, student -> student.getLastName().equals(name));
    }

    @Override
    public List<Student> findStudentsByGroup(Collection<Student> students, GroupName group) {
        return findStudentsBy(students, student -> student.getGroup().equals(group));
    }

    @Override
    public Map<String, String> findStudentNamesByGroup(Collection<Student> students, GroupName group) {
        return students.stream()
                .filter(student -> student.getGroup().equals(group))
                .collect(Collectors.toMap(Student::getLastName, Student::getFirstName, (s1, s2) -> s1.compareTo(s2) > 0 ? s2 : s1));
    }
}
