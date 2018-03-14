package ru.ifmo.rain.golovin.student;

import info.kgeorgiy.java.advanced.student.Student;
import info.kgeorgiy.java.advanced.student.StudentQuery;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

public class StudentDB implements StudentQuery {
    /** Returns student {@link Student#getFirstName() first names}. */
    @Override
    public List<String> getFirstNames(final List<Student> students) {
        return students.stream()
                .map(Student::getFirstName)
                .collect(Collectors.toList());
    }

    /** Returns student {@link Student#getLastName() last names}. */
    @Override
    public List<String> getLastNames(final List<Student> students) {
         return students.stream()
                .map(Student::getLastName)
                .collect(Collectors.toList());
    }

    /** Returns student {@link Student#getGroup() groups}. */
    @Override
    public List<String> getGroups(final List<Student> students) {
        return students.stream()
                .map(Student::getGroup)
                .collect(Collectors.toList());

    }

    /** Returns student {@link Student#getGroup() groups}. */
    @Override
    public List<String> getFullNames(final List<Student> students) {
        return students.stream()
                .map(student -> student.getFirstName() + student.getLastName())
                .collect(Collectors.toList());
    }

    /** Returns distinct student {@link Student#getFirstName() first names} in alphabetical order. */
    @Override
    public Set<String> getDistinctFirstNames(final List<Student> students) {
        return students.stream()
                .map(Student::getFirstName)
                .collect(Collectors.toCollection(TreeSet::new));
    }

    /** Returns name of the student with minimal {@link Student#getId() id}. */
    @Override
    public String getMinStudentFirstName(final List<Student> students) {
        return students.stream()
                .map(Student::getFirstName)
                .min(String::compareTo)
                .get();
    }

    /** Returns list of students sorted by {@link Student#getId() id}. */
    @Override
    public List<Student> sortStudentsById(Collection<Student> students) {
        return students.stream()
                .sorted(Comparator.comparingInt(Student::getId))
                .collect(Collectors.toList());
    }

    /**
     * Returns list of students sorted by name
     * (students are ordered by {@link Student#getLastName() lastName},
     * students with equal last names are ordered by {@link Student#getFirstName() firstName},
     * students having equal both last and first names are ordered by {@link Student#getId() id}.
     */
    @Override
    public List<Student> sortStudentsByName(Collection<Student> students) {
        return students.stream()
                .sorted(Comparator.comparing(Student::getLastName)
                        .thenComparing(Student::getFirstName)
                        .thenComparing(Student::getId))
                .collect(Collectors.toList());
    }

    /** Returns list of students having specified first name. Students are ordered by name. */
    @Override
    public List<Student> findStudentsByFirstName(Collection<Student> students, String name) {
        return students.stream()
                .filter(student -> student.getFirstName().equals(name))
                .sorted(Comparator.comparing(Student::getLastName)
                        .thenComparing(Student::getFirstName)
                        .thenComparing(Student::getId))
                .collect(Collectors.toList());
    }

    /** Returns list of students having specified last name. Students are ordered by name. */
    @Override
    public List<Student> findStudentsByLastName(Collection<Student> students, String name) {
        return students.stream()
                .filter(student -> student.getLastName().equals(name))
                .sorted(Comparator.comparing(Student::getLastName)
                        .thenComparing(Student::getFirstName)
                        .thenComparing(Student::getId))
                .collect(Collectors.toList());
    }

    /** Returns list of students having specified groups. Students are ordered by name. */
    @Override
    public List<Student> findStudentsByGroup(Collection<Student> students, String group) {
        return students.stream()
                .filter(student -> student.getGroup().equals(group))
                .sorted(Comparator.comparing(Student::getLastName)
                        .thenComparing(Student::getFirstName)
                        .thenComparing(Student::getId))
                .collect(Collectors.toList());

    }

    /** Returns map of group's student last names mapped to minimal first name. */
    @Override
    public Map<String, String> findStudentNamesByGroup(final Collection<Student> students, final String group) {
        return students.stream()
                .filter(student -> student.getGroup().equals(group))
                .sorted(Comparator.comparing(Student::getLastName)
                        .thenComparing(Student::getFirstName)
                        .thenComparing(Student::getId))
                .collect(Collectors.groupingBy(Student::getLastName, Collectors))
    }

}


//NB почему методы не статические?