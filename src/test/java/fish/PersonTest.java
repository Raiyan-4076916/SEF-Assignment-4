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
