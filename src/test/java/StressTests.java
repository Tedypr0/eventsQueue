import org.example.Helper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.mockito.Mock;

public class StressTests {
    @Mock
    Helper helper;

    @BeforeEach
    public void initialize() {
        helper = new Helper();
    }

    @RepeatedTest(25000)
    public void stressTest_shouldAlways_EndProgram_gracefully() {
        helper.eventCreation();
        helper.threadCreation();
    }
}
