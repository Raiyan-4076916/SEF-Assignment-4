package fish;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import java.io.File;

public class PersonTest {
    private Person registry;
    private final String TEST_FILE = "persons.txt";

    @BeforeEach
    void setup() {
        registry = new Person();
        // Clear the file before each test
        File file = new File(TEST_FILE);
        if (file.exists()) {
            file.delete();
        }
    }

    // -------------------- addPerson Tests ------------------------

    @Test
    void testAddPerson_ValidData() {
        boolean result = registry.addPerson("56s_d%&fAB", "John", "Doe", "32|Main St|Melbourne|Victoria|Australia", "15-11-2000");
        assertTrue(result);
    }

    @Test
    void testAddPerson_InvalidPersonID() {
        boolean result = registry.addPerson("12abcdefXY", "Jane", "Smith", "32|Main St|Melbourne|Victoria|Australia", "15-11-2000");
        assertFalse(result);
    }

    @Test
    void testAddPerson_InvalidAddressFormat() {
        boolean result = registry.addPerson("56s_d%&fAB", "Alice", "Brown", "Melbourne|Victoria|Australia", "15-11-2000");
        assertFalse(result);
    }

    @Test
    void testAddPerson_InvalidBirthdate() {
        boolean result = registry.addPerson("56s_d%&fAB", "Mark", "Lee", "32|Main St|Melbourne|Victoria|Australia", "2000-11-15");
        assertFalse(result);
    }

    @Test
    void testAddPerson_DuplicateID() {
        registry.addPerson("56s_d%&fAB", "Tom", "White", "32|Main St|Melbourne|Victoria|Australia", "15-11-2000");
        boolean result = registry.addPerson("56s_d%&fAB", "Tim", "White", "32|Main St|Melbourne|Victoria|Australia", "15-11-2000");
        assertFalse(result);
    }

     // -------------------- updatePersonalDetails Tests ------------------------

    @Test
    void testUpdatePersonalDetails_ValidUpdate() {
        registry.addPerson("56s_d%&fAB", "John", "Doe", "32|Main St|Melbourne|Victoria|Australia", "15-11-2000");
        boolean result = registry.updatePersonalDetails("56s_d%&fAB", "56s_d%&fAB", "Johnny", "Doe", "32|Main St|Melbourne|Victoria|Australia", "15-11-2000");
        assertTrue(result);
    }

    @Test
    void testUpdatePersonalDetails_BirthdayChangeOnly() {
        registry.addPerson("56s_d%&fAB", "Sam", "Green", "32|Main St|Melbourne|Victoria|Australia", "15-11-2000");
        boolean result = registry.updatePersonalDetails("56s_d%&fAB", "56s_d%&fAB", "Sam", "Green", "32|Main St|Melbourne|Victoria|Australia", "15-11-1999");
        assertTrue(result);
    }

    @Test
    void testUpdatePersonalDetails_BirthdayAndOtherFieldsChanged() {
        registry.addPerson("56s_d%&fAB", "Lucy", "Gray", "32|Main St|Melbourne|Victoria|Australia", "15-11-2000");
        boolean result = registry.updatePersonalDetails("56s_d%&fAB", "56s_d%&fAB", "Lucia", "Gray", "45|King St|Melbourne|Victoria|Australia", "15-11-1999");
        assertFalse(result);
    }

    @Test
    void testUpdatePersonalDetails_AddressChangeUnder18() {
        registry.addPerson("56s_d%&fAB", "Tom", "Kid", "32|Main St|Melbourne|Victoria|Australia", "15-11-2010");
        boolean result = registry.updatePersonalDetails("56s_d%&fAB", "56s_d%&fAB", "Tom", "Kid", "45|Other St|Melbourne|Victoria|Australia", "15-11-2010");
        assertFalse(result);
    }

    @Test
    void testUpdatePersonalDetails_IDChangeNotAllowed() {
        registry.addPerson("42s_d%&fAB", "Tom", "Even", "32|Main St|Melbourne|Victoria|Australia", "15-11-2000");
        boolean result = registry.updatePersonalDetails("42s_d%&fAB", "43s_d%&fAB", "Tom", "Even", "32|Main St|Melbourne|Victoria|Australia", "15-11-2000");
        assertFalse(result);
    }

    // -------------------- addDemeritPoints Tests ------------------------

    @Test
    void testAddDemeritPoints_Valid() {
        registry.addPerson("56s_d%&fAB", "John", "Doe", "32|Main St|Melbourne|Victoria|Australia", "15-11-2000");
        String result = registry.addDemeritPoints("56s_d%&fAB", "01-01-2024", 3);
        assertEquals("Success", result);
    }

    @Test
    void testAddDemeritPoints_InvalidDateFormat() {
        registry.addPerson("56s_d%&fAB", "John", "Doe", "32|Main St|Melbourne|Victoria|Australia", "15-11-2000");
        String result = registry.addDemeritPoints("56s_d%&fAB", "2024-01-01", 3);
        assertEquals("Failed", result);
    }

    @Test
    void testAddDemeritPoints_InvalidPoints() {
        registry.addPerson("56s_d%&fAB", "John", "Doe", "32|Main St|Melbourne|Victoria|Australia", "15-11-2000");
        String result = registry.addDemeritPoints("56s_d%&fAB", "01-01-2024", 7);
        assertEquals("Failed", result);
    }

    @Test
    void testAddDemeritPoints_SuspensionUnder21() {
        registry.addPerson("56s_d%&fAB", "Young", "Driver", "32|Main St|Melbourne|Victoria|Australia", "15-11-2005");
        registry.addDemeritPoints("56s_d%&fAB", "01-01-2023", 4);
        String result = registry.addDemeritPoints("56s_d%&fAB", "01-01-2024", 3); // total = 7
        assertEquals("Success", result);
    }

    @Test
    void testAddDemeritPoints_SuspensionOver21() {
        registry.addPerson("56s_d%&fAB", "Adult", "Driver", "32|Main St|Melbourne|Victoria|Australia", "15-11-1990");
        registry.addDemeritPoints("56s_d%&fAB", "01-01-2023", 6);
        registry.addDemeritPoints("56s_d%&fAB", "01-01-2023", 6);
        String result = registry.addDemeritPoints("56s_d%&fAB", "01-01-2024", 5); // total = 17
        assertEquals("Success", result);
    }
}
