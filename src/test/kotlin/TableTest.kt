import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import java.util.concurrent.CountDownLatch
import kotlin.test.Test
import kotlin.test.assertEquals

class TableTest {
    @Test
    fun naiveDinnerTest() = runTest {
        var table = Table()
        var eatenPhilosophers = 0
        table.dinner(false)
        for (i: Int in 0..table.size - 1) {
            eatenPhilosophers += if (table.philosophers[i].eaten) 1 else 0
        }
        assertEquals(0, eatenPhilosophers)
    }

    @Test
    fun realDinnerTest() = runTest {
        var table = Table()
        var eatenPhilosophers = 0
        table.dinner(true)
        for (i: Int in 0..table.size - 1) {
            eatenPhilosophers += if (table.philosophers[i].eaten) 1 else 0
        }
        assertEquals(5, eatenPhilosophers)
    }
}