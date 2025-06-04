package fish;

import java.io.*;
import java.time.*;
import java.time.format.*;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class Person {
    private static final String FILE_NAME = "persons.txt"; // File where person data is stored
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd-MM-yyyy"); // Date format used throughout the code

    // Inner class to represent a person's data
    static class PersonData {
        String personID;
        String firstName;
        String lastName;
        String address;
        String birthDate;
        boolean isSuspended;
        List<Offense> offenses = new ArrayList<>(); // List of offenses committed by the person

        // Converts a PersonData object to a comma-separated string for file storage
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

        // Converts a string line from the file to a PersonData object
        static PersonData fromString(String line) {
            String[] parts = line.split(",");
            PersonData p = new PersonData();
            p.personID = parts[0];
            p.firstName = parts[1];
            p.lastName = parts[2];
            p.address = parts[3];
            p.birthDate = parts[4];
            p.isSuspended = Boolean.parseBoolean(parts[5]);

            // Parse offenses if present
            for (int i = 6; i < parts.length; i++) {
                String[] offense = parts[i].split(":");
                if (offense.length == 2) {
                    p.offenses.add(new Offense(offense[0], Integer.parseInt(offense[1])));
                }
            }
            return p;
        }
    }

    // Inner class to represent a traffic offense
    static class Offense {
        String date;
        int points;

        Offense(String date, int points) {
            this.date = date;
            this.points = points;
        }
    }

    // 1. Add a new person to the system
    public boolean addPerson(String personID, String firstName, String lastName, String address, String birthDate) {
        // Validate all input fields
        if (!isValidPersonID(personID) || !isValidAddress(address) || !isValidDate(birthDate)) {
            return false;
        }

        List<PersonData> people = readAllPeople();
        // Check for duplicate person ID
        for (PersonData p : people) {
            if (p.personID.equals(personID)) {
                return false;
            }
        }

        // Create and add the new person
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

    // 2. Update personal details with validation constraints
    public boolean updatePersonalDetails(String oldPersonID, String newPersonID, String firstName, String lastName, String address, String birthDate) {
        List<PersonData> people = readAllPeople();
        for (PersonData p : people) {
            if (p.personID.equals(oldPersonID)) {
                // Parse birthdates for age calculation
                LocalDate currentDob = LocalDate.parse(p.birthDate, DATE_FORMAT);
                LocalDate newDob = LocalDate.parse(birthDate, DATE_FORMAT);
                int currentAge = Period.between(currentDob, LocalDate.now()).getYears();

                boolean birthdayChanged = !birthDate.equals(p.birthDate);

                // Ensure changes are limited when changing the birthday
                if (birthdayChanged && (!newPersonID.equals(oldPersonID) || !firstName.equals(p.firstName) || !lastName.equals(p.lastName) || !address.equals(p.address))) {
                    return false;
                }

                // Restrict address change if under 18
                if (currentAge < 18 && !address.equals(p.address)) {
                    return false;
                }

                // If changing ID, check if the old ID's first digit is even
                if (!newPersonID.equals(oldPersonID) && Character.getNumericValue(oldPersonID.charAt(0)) % 2 == 0) {
                    return false;
                }

                // Validate new data
                if (!isValidPersonID(newPersonID) || !isValidAddress(address) || !isValidDate(birthDate)) {
                    return false;
                }

                // Update fields
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

    // 3. Add demerit points and suspend if threshold exceeded
    public String addDemeritPoints(String personID, String date, int points) {
        // Validate date and points
        if (!isValidDate(date) || points < 1 || points > 6) {
            return "Failed";
        }

        List<PersonData> people = readAllPeople();
        for (PersonData p : people) {
            if (p.personID.equals(personID)) {
                p.offenses.add(new Offense(date, points));

                LocalDate offenseDate = LocalDate.parse(date, DATE_FORMAT);
                int age = Period.between(LocalDate.parse(p.birthDate, DATE_FORMAT), offenseDate).getYears();

                // Calculate total points in the last 2 years
                int total = 0;
                for (Offense o : p.offenses) {
                    LocalDate d = LocalDate.parse(o.date, DATE_FORMAT);
                    if (ChronoUnit.DAYS.between(d, offenseDate) <= 730) { // 2 years = 730 days
                        total += o.points;
                    }
                }

                // Apply suspension based on age
                if ((age < 21 && total > 6) || (age >= 21 && total > 12)) {
                    p.isSuspended = true;
                }

                writeAllPeople(people);
                return "Success";
            }
        }
        return "Failed";
    }


// ---------------------- Helper Methods -----------------------

    // Validates the person ID format
    private boolean isValidPersonID(String id) {
        if (id.length() != 10) return false;
        // First two digits must be 2-9
        if (!id.substring(0, 2).matches("[2-9]{2}")) return false;
        // Next six must contain at least two special characters
        if (!id.substring(2, 8).replaceAll("[^!@#$%^&*()_+=\\[\\]{};':\"\\\\|,.<>/?~-]", "").matches(".*[!@#$%^&*()_+=\\[\\]{};':\"\\\\|,.<>/?~-].*[!@#$%^&*()_+=\\[\\]{};':\"\\\\|,.<>/?~-].*")) return false;
        // Last two characters must be uppercase letters
        return id.substring(8).matches("[A-Z]{2}");
    }

    // Validates address format and checks state must be 'Victoria'
    private boolean isValidAddress(String address) {
        String[] parts = address.split("\\|");
        return parts.length == 5 && parts[3].equals("Victoria");
    }

    // Validates date using defined format
    private boolean isValidDate(String date) {
        try {
            LocalDate.parse(date, DATE_FORMAT);
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    // Reads all persons from the file
    private List<PersonData> readAllPeople() {
        List<PersonData> list = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(FILE_NAME))) {
            String line;
            while ((line = reader.readLine()) != null) {
                list.add(PersonData.fromString(line));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return list;
    }

    // Writes all persons to the file (overwrites file)
    private void writeAllPeople(List<PersonData> people) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_NAME, false))) {
            for (PersonData p : people) {
                writer.write(p.toString());
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

