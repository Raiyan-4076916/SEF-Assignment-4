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

     // 2. updatePersonalDetails
    public boolean updatePersonalDetails(String oldPersonID, String newPersonID, String firstName, String lastName, String address, String birthDate) {
        List<PersonData> people = readAllPeople();
        for (PersonData p : people) {
            if (p.personID.equals(oldPersonID)) {
                LocalDate currentDob = LocalDate.parse(p.birthDate, DATE_FORMAT);
                LocalDate newDob = LocalDate.parse(birthDate, DATE_FORMAT);
                int currentAge = Period.between(currentDob, LocalDate.now()).getYears();
                boolean birthdayChanged = !birthDate.equals(p.birthDate);

                if (birthdayChanged && (!newPersonID.equals(oldPersonID) || !firstName.equals(p.firstName) || !lastName.equals(p.lastName) || !address.equals(p.address))) {
                    return false;
                }

                if (currentAge < 18 && !address.equals(p.address)) {
                    return false;
                }

                if (!newPersonID.equals(oldPersonID) && Character.getNumericValue(oldPersonID.charAt(0)) % 2 == 0) {
                    return false;
                }

                if (!isValidPersonID(newPersonID) || !isValidAddress(address) || !isValidDate(birthDate)) {
                    return false;
                }

                p.personID = newPersonID;
                p.firstName = firstName;
                p.lastName = lastName;
                p.address = address;
                p.birthDate = birthDate;

                writeAllPeople(people);
                return true;
            }
        }
        return false;
    }

    // 3. addDemeritPoints
    public String addDemeritPoints(String personID, String date, int points) {
        if (!isValidDate(date) || points < 1 || points > 6) {
            return "Failed";
        }

        List<PersonData> people = readAllPeople();
        for (PersonData p : people) {
            if (p.personID.equals(personID)) {
                p.offenses.add(new Offense(date, points));

                LocalDate offenseDate = LocalDate.parse(date, DATE_FORMAT);
                int age = Period.between(LocalDate.parse(p.birthDate, DATE_FORMAT), offenseDate).getYears();

                int total = 0;
                for (Offense o : p.offenses) {
                    LocalDate d = LocalDate.parse(o.date, DATE_FORMAT);
                    if (ChronoUnit.DAYS.between(d, offenseDate) <= 730) {
                        total += o.points;
                    }
                }

                if ((age < 21 && total > 6) || (age >= 21 && total > 12)) {
                    p.isSuspended = true;
                }

                writeAllPeople(people);
                return "Success";
            }
        }
        return "Failed";
    }



