import org.example.SideStuff;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.mockito.Mock;

public class StressTests {
    @Mock
    SideStuff stuff;

    @BeforeEach
    public void initialize(){
        stuff = new SideStuff();
    }

    @RepeatedTest(20000)
    public void stressTest_shouldWork_normally() throws InterruptedException {
        stuff.eventReferencesCreationAndThreadStartup();
        stuff.eventCreation();
    }
}
