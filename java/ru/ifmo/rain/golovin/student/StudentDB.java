package ru.ifmo.rain.golovin.student;

import info.kgeorgiy.java.advanced.student.Group;
import info.kgeorgiy.java.advanced.student.Student;
import info.kgeorgiy.java.advanced.student.StudentGroupQuery;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class StudentDB implements StudentGroupQuery {

    // simple map methods

    private List<String> getSomething(final List<Student> students, Function<Student, String> mapFunc) {
        return students.stream()
                .map(mapFunc)
                .collect(Collectors.toList());
    }

    /**
     * Returns student {@link Student#getFirstName() first names}.
     */
    @Override
    public List<String> getFirstNames(final List<Student> students) {
        return getSomething(students, Student::getFirstName);
    }

    /**
     * Returns student {@link Student#getLastName() last names}.
     */
    @Override
    public List<String> getLastNames(final List<Student> students) {
        return getSomething(students, Student::getLastName);
    }

    /**
     * Returns student {@link Student#getGroup() groups}.
     */
    @Override
    public List<String> getGroups(final List<Student> students) {
        return getSomething(students, Student::getGroup);
    }

    /**
     * Returns student {@link Student#getGroup() groups}.
     */
    @Override
    public List<String> getFullNames(final List<Student> students) {
        return getSomething(students, student -> student.getFirstName() + " " + student.getLastName());
    }


    // sorted methods

    private List<Student> sortStudentsBy(Collection<Student> students, Comparator<Student> comparator) {
        return students.stream()
                .sorted(comparator)
                .collect(Collectors.toList());
    }

    /**
     * Returns list of students sorted by {@link Student#getId() id}.
     */
    @Override
    public List<Student> sortStudentsById(Collection<Student> students) {
        return sortStudentsBy(students, Comparator.comparing(Student::getId));
    }

    /**
     * Returns list of students sorted by name
     * (students are ordered by {@link Student#getLastName() lastName},
     * students with equal last names are ordered by {@link Student#getFirstName() firstName},
     * students having equal both last and first names are ordered by {@link Student#getId() id}.
     */
    @Override
    public List<Student> sortStudentsByName(Collection<Student> students) {
        return sortStudentsBy(students, COMPARATOR_BY_NAME);
    }

    // find methods

    private static final Comparator<Student> COMPARATOR_BY_NAME =
            Comparator.comparing(Student::getLastName)
                    .thenComparing(Student::getFirstName)
                    .thenComparing(Student::getId);

    private List<Student> findStudentBy(Collection<Student> students, Function<Student, String> mapFunc, String name) {
        return students.stream()
                .filter(student -> mapFunc.apply(student).equals(name))
                .sorted(COMPARATOR_BY_NAME)
                .collect(Collectors.toList());
    }

    /**
     * Returns list of students having specified first name. Students are ordered by name.
     */
    @Override
    public List<Student> findStudentsByFirstName(Collection<Student> students, String name) {
        return findStudentBy(students, Student::getFirstName, name);
    }

    /**
     * Returns list of students having specified last name. Students are ordered by name.
     */
    @Override
    public List<Student> findStudentsByLastName(Collection<Student> students, String name) {
        return findStudentBy(students, Student::getLastName, name);
    }

    /**
     * Returns list of students having specified groups. Students are ordered by name.
     */
    @Override
    public List<Student> findStudentsByGroup(Collection<Student> students, String group) {
        return findStudentBy(students, Student::getGroup, group);
    }

    // group's methods
    private Stream<Map.Entry<String, List<Student>>> getGroupEntryStream(Collection<Student> students) {
        return students.stream()
                .collect(Collectors.groupingBy(Student::getGroup))
                .entrySet().stream();
    }

    private List<Group> getGroupsBy(Collection<Student> students, Comparator<Student> comparator) {
        return getGroupEntryStream(students)
                .map(mapEntry -> new Group(mapEntry.getKey(), sortStudentsBy(mapEntry.getValue(), comparator)))
                .sorted(Comparator.comparing(Group::getName))
                .collect(Collectors.toList());
    }

    /**
     * Returns student groups, where both groups and students within a group are ordered by name.
     */
    @Override
    public List<Group> getGroupsByName(Collection<Student> students) {
        return getGroupsBy(students, COMPARATOR_BY_NAME);
    }

    /**
     * Returns student groups, where groups are ordered by name, and students within a group are ordered by id.
     */
    @Override
    public List<Group> getGroupsById(Collection<Student> students) {
        return getGroupsBy(students, Comparator.comparing(Student::getId));
    }

    private String getLargestGroupBy(Collection<Student> students, Comparator<Group> comparator) {
        return getGroupEntryStream(students)
                .map(entry -> new Group(entry.getKey(), entry.getValue()))
                .max(comparator)
                .map(Group::getName).orElse("");
    }

    /**
     * Returns name of the group containing maximum number of students.
     * If there are more than one largest group, the one with smallest name is returned.
     */
    @Override
    public String getLargestGroup(Collection<Student> students) {
        return getLargestGroupBy(students, Comparator.<Group, Integer>comparing(group -> group.getStudents().size())
                .thenComparing(Comparator.comparing(Group::getName).reversed()));
    }

    /**
     * Returns name of the group containing maximum number of students with distinct first names.
     * If there are more than one largest group, the one with smallest name is returned.
     */
    @Override
    public String getLargestGroupFirstName(Collection<Student> students) {
        return getLargestGroupBy(students, Comparator.<Group, Integer>comparing(group -> getDistinctFirstNames(group.getStudents()).size())
                .thenComparing(Comparator.comparing(Group::getName).reversed()));
    }

    // ...

    /**
     * Returns map of group's student last names mapped to minimal first name.
     */
    @Override
    public Map<String, String> findStudentNamesByGroup(final Collection<Student> students, final String group) {
        return students.stream()
                .filter(student -> student.getGroup().equals(group))
                .collect(Collectors.toMap(Student::getLastName, Student::getFirstName, BinaryOperator.minBy(String::compareTo)));
    }

    /**
     * Returns distinct student {@link Student#getFirstName() first names} in alphabetical order.
     */
    @Override
    public Set<String> getDistinctFirstNames(final List<Student> students) {
        return students.stream()
                .map(Student::getFirstName)
                .collect(Collectors.toCollection(TreeSet::new));
    }

    /**
     * Returns name of the student with minimal {@link Student#getId() id}.
     */
    @Override
    public String getMinStudentFirstName(final List<Student> students) {
        return students.stream()
                .min(Comparator.comparing(Student::getId))
                .map(Student::getFirstName)
                .orElse("");
    }
}
//NB почему методы не статические?