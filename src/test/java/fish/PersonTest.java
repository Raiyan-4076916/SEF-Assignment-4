package fish;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import java.io.File;

public class PersonTest {
    private Person registry;
    private final String TEST_FILE = "persons.txt";

    // This method runs before each test to reset the test environment
    @BeforeEach
    void setup() {
        registry = new Person(); // Create a new Person registry
        // Delete the file to start fresh for each test
        File file = new File(TEST_FILE);
        if (file.exists()) {
            file.delete();
        }
    }

    // -------------------- addPerson Tests ------------------------

    // Test adding a person with valid details
    @Test
    void testAddPerson_ValidData() {
        boolean result = registry.addPerson("56s_d%&fAB", "John", "Doe", "32|Main St|Melbourne|Victoria|Australia", "15-11-2000");
        assertTrue(result); // Expect successful addition
    }

    // Test with an invalid person ID (fails format validation)
    @Test
    void testAddPerson_InvalidPersonID() {
        boolean result = registry.addPerson("12abcdefXY", "Jane", "Smith", "32|Main St|Melbourne|Victoria|Australia", "15-11-2000");
        assertFalse(result); // Should fail due to invalid ID
    }

    // Test with an invalid address format (not 5 parts)
    @Test
    void testAddPerson_InvalidAddressFormat() {
        boolean result = registry.addPerson("56s_d%&fAB", "Alice", "Brown", "Melbourne|Victoria|Australia", "15-11-2000");
        assertFalse(result); // Should fail due to invalid address
    }

    // Test with invalid birthdate format (wrong pattern)
    @Test
    void testAddPerson_InvalidBirthdate() {
        boolean result = registry.addPerson("56s_d%&fAB", "Mark", "Lee", "32|Main St|Melbourne|Victoria|Australia", "2000-11-15");
        assertFalse(result); // Should fail due to wrong date format
    }

    // Test that duplicate person ID is not allowed
    @Test
    void testAddPerson_DuplicateID() {
        registry.addPerson("56s_d%&fAB", "Tom", "White", "32|Main St|Melbourne|Victoria|Australia", "15-11-2000");
        boolean result = registry.addPerson("56s_d%&fAB", "Tim", "White", "32|Main St|Melbourne|Victoria|Australia", "15-11-2000");
        assertFalse(result); // Should fail due to duplicate ID
    }

    // -------------------- updatePersonalDetails Tests ------------------------

    // Test a valid update to name while keeping other data same
    @Test
    void testUpdatePersonalDetails_ValidUpdate() {
        registry.addPerson("56s_d%&fAB", "John", "Doe", "32|Main St|Melbourne|Victoria|Australia", "15-11-2000");
        boolean result = registry.updatePersonalDetails("56s_d%&fAB", "56s_d%&fAB", "Johnny", "Doe", "32|Main St|Melbourne|Victoria|Australia", "15-11-2000");
        assertTrue(result); // Should succeed as it's a valid update
    }

    // Test changing only the birthdate (allowed)
    @Test
    void testUpdatePersonalDetails_BirthdayChangeOnly() {
        registry.addPerson("56s_d%&fAB", "Sam", "Green", "32|Main St|Melbourne|Victoria|Australia", "15-11-2000");
        boolean result = registry.updatePersonalDetails("56s_d%&fAB", "56s_d%&fAB", "Sam", "Green", "32|Main St|Melbourne|Victoria|Australia", "15-11-1999");
        assertTrue(result); // Allowed because only birthday is changed
    }

    // Test changing birthday along with other personal details (not allowed)
    @Test
    void testUpdatePersonalDetails_BirthdayAndOtherFieldsChanged() {
        registry.addPerson("56s_d%&fAB", "Lucy", "Gray", "32|Main St|Melbourne|Victoria|Australia", "15-11-2000");
        boolean result = registry.updatePersonalDetails("56s_d%&fAB", "56s_d%&fAB", "Lucia", "Gray", "45|King St|Melbourne|Victoria|Australia", "15-11-1999");
        assertFalse(result); // Should fail due to multiple fields changed along with birthday
    }

    // Test changing address for someone under 18 (not allowed)
    @Test
    void testUpdatePersonalDetails_AddressChangeUnder18() {
        registry.addPerson("56s_d%&fAB", "Tom", "Kid", "32|Main St|Melbourne|Victoria|Australia", "15-11-2010"); // under 18
        boolean result = registry.updatePersonalDetails("56s_d%&fAB", "56s_d%&fAB", "Tom", "Kid", "45|Other St|Melbourne|Victoria|Australia", "15-11-2010");
        assertFalse(result); // Should fail because minors can't change address
    }

    // Test ID change is disallowed if old ID starts with an even digit
    @Test
    void testUpdatePersonalDetails_IDChangeNotAllowed() {
        registry.addPerson("42s_d%&fAB", "Tom", "Even", "32|Main St|Melbourne|Victoria|Australia", "15-11-2000");
        boolean result = registry.updatePersonalDetails("42s_d%&fAB", "43s_d%&fAB", "Tom", "Even", "32|Main St|Melbourne|Victoria|Australia", "15-11-2000");
        assertFalse(result); // Not allowed since ID begins with an even digit
    }

    // -------------------- addDemeritPoints Tests ------------------------

    // Test adding valid demerit points
    @Test
    void testAddDemeritPoints_Valid() {
        registry.addPerson("56s_d%&fAB", "John", "Doe", "32|Main St|Melbourne|Victoria|Australia", "15-11-2000");
        String result = registry.addDemeritPoints("56s_d%&fAB", "01-01-2024", 3);
        assertEquals("Success", result); // Should succeed
    }

    // Test failure due to invalid date format
    @Test
    void testAddDemeritPoints_InvalidDateFormat() {
        registry.addPerson("56s_d%&fAB", "John", "Doe", "32|Main St|Melbourne|Victoria|Australia", "15-11-2000");
        String result = registry.addDemeritPoints("56s_d%&fAB", "2024-01-01", 3);
        assertEquals("Failed", result); // Should fail due to wrong date format
    }

    // Test failure due to invalid demerit points (greater than max allowed)
    @Test
    void testAddDemeritPoints_InvalidPoints() {
        registry.addPerson("56s_d%&fAB", "John", "Doe", "32|Main St|Melbourne|Victoria|Australia", "15-11-2000");
        String result = registry.addDemeritPoints("56s_d%&fAB", "01-01-2024", 7); // points out of valid range (1–6)
        assertEquals("Failed", result);
    }

    // Test suspension logic for person under 21 who exceeds 6 points
    @Test
    void testAddDemeritPoints_SuspensionUnder21() {
        registry.addPerson("56s_d%&fAB", "Young", "Driver", "32|Main St|Melbourne|Victoria|Australia", "15-11-2005"); // under 21
        registry.addDemeritPoints("56s_d%&fAB", "01-01-2023", 4); // 4 points
        String result = registry.addDemeritPoints("56s_d%&fAB", "01-01-2024", 3); // Total becomes 7, triggers suspension
        assertEquals("Success", result); // Action still succeeds, suspension applied internally
    }

    // Test suspension logic for person over 21 who exceeds 12 points
    @Test
    void testAddDemeritPoints_SuspensionOver21() {
        registry.addPerson("56s_d%&fAB", "Adult", "Driver", "32|Main St|Melbourne|Victoria|Australia", "15-11-1990"); // over 21
        registry.addDemeritPoints("56s_d%&fAB", "01-01-2023", 6); // Total = 6
        registry.addDemeritPoints("56s_d%&fAB", "01-01-2023", 6); // Total = 12
        String result = registry.addDemeritPoints("56s_d%&fAB", "01-01-2024", 5); // Total = 17 → should be suspended
        assertEquals("Success", result); // Still returns success, suspension logic is internal
    }
}
