package fish;

import java.io.*;
import java.nio.file.*;
import java.text.*;
import java.time.*;
import java.time.format.*;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class Person {
    private static final String FILE_NAME = "persons.txt";
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    static class PersonData {
        String personID;
        String firstName;
        String lastName;
        String address;
        String birthDate;
        boolean isSuspended;
        List<Offense> offenses = new ArrayList<>();

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append(personID).append(",")
              .append(firstName).append(",")
              .append(lastName).append(",")
              .append(address).append(",")
              .append(birthDate).append(",")
              .append(isSuspended);
            for (Offense o : offenses) {
                sb.append(",").append(o.date).append(":").append(o.points);
            }
            return sb.toString();
        }

        static PersonData fromString(String line) {
            String[] parts = line.split(",");
            PersonData p = new PersonData();
            p.personID = parts[0];
            p.firstName = parts[1];
            p.lastName = parts[2];
            p.address = parts[3];
            p.birthDate = parts[4];
            p.isSuspended = Boolean.parseBoolean(parts[5]);

            for (int i = 6; i < parts.length; i++) {
                String[] offense = parts[i].split(":");
                if (offense.length == 2) {
                    p.offenses.add(new Offense(offense[0], Integer.parseInt(offense[1])));
                }
            }
            return p;
        }
    }

    static class Offense {
        String date;
        int points;

        Offense(String date, int points) {
            this.date = date;
            this.points = points;
        }
    }

    // 1. addPerson
    public boolean addPerson(String personID, String firstName, String lastName, String address, String birthDate) {
        if (!isValidPersonID(personID) || !isValidAddress(address) || !isValidDate(birthDate)) {
            return false;
        }

        List<PersonData> people = readAllPeople();
        for (PersonData p : people) {
            if (p.personID.equals(personID)) {
                return false; // duplicate ID
            }
        }

        PersonData newPerson = new PersonData();
        newPerson.personID = personID;
        newPerson.firstName = firstName;
        newPerson.lastName = lastName;
        newPerson.address = address;
        newPerson.birthDate = birthDate;
        newPerson.isSuspended = false;

        people.add(newPerson);
        writeAllPeople(people);
        return true;
    }


