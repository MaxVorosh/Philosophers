import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import java.util.concurrent.atomic.AtomicInteger
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.util.concurrent.CountDownLatch

class Table {
    val forks: MutableList<AtomicInteger> = mutableListOf()
    val philosophers: MutableList<Philosopher> = mutableListOf()
    val size: Int = 5
    var forksCount: AtomicInteger = AtomicInteger(size)

    init {
        for (i: Int in 0..size - 1) {
            forks.add(AtomicInteger(1))
            philosophers.add(Philosopher(100, 10, 50))
        }
    }

    suspend fun action(philosopher: Int, isWaiter: Boolean, counter: Int) {
        if (counter > 1000) {
            return
        }
        val waiterPermission = (!isWaiter || forksCount.get() > 1)
        val timeLimit = philosophers[philosopher].thoughts >= 20
        if (forks[philosopher].get() == 1 && philosophers[philosopher].stage == 0 && waiterPermission && !timeLimit) {
            forks[philosopher].decrementAndGet()
            forksCount.decrementAndGet()
            philosophers[philosopher].takeLeftFork()
        } else if (forks[(philosopher + 1) % forks.size].get() == 1 && philosophers[philosopher].stage == 1 && waiterPermission && !timeLimit) {
            forks[(philosopher + 1) % forks.size].decrementAndGet()
            forksCount.decrementAndGet()
            philosophers[philosopher].takeRightFork()
        } else if (philosophers[philosopher].prepareToEat() && philosophers[philosopher].stage == 2) {
            philosophers[philosopher].eat()
        } else if (forks[philosopher].get() == 0 && (philosophers[philosopher].stage == 3 || timeLimit)) {
            forks[philosopher].incrementAndGet()
            forksCount.incrementAndGet()
            philosophers[philosopher].putLeftFork()
        } else if (forks[(philosopher + 1) % forks.size].get() == 0 && (philosophers[philosopher].stage == 4 || timeLimit)) {
            forks[(philosopher + 1) % forks.size].incrementAndGet()
            forksCount.incrementAndGet()
            philosophers[philosopher].putRightFork()
        } else {
            philosophers[philosopher].think()
        }
        action(philosopher, isWaiter, counter + 1)
    }

    suspend fun dinner(isNaive: Boolean) = coroutineScope {
        for (i: Int in 0..size - 1) {
            launch {
                action(i, isNaive, 0)
            }
        }
    }

}

class Philosopher(
    val eatTime: Long,
    val takeForkTime: Long,
    val thinkTime: Long,
    var leftFork: Boolean = false,
    var rightFork: Boolean = false,
) {
    var eaten = false
    var stage = 0
    var thoughts = 0

    fun prepareToEat(): Boolean {
        return leftFork && rightFork
    }

    suspend fun eat() {
        delay(eatTime)
        stage = 3
        eaten = true
    }

    suspend fun takeLeftFork() {
        leftFork = true
        stage = 1
        delay(takeForkTime)
    }

    suspend fun takeRightFork() {
        rightFork = true
        stage = 2
        delay(takeForkTime)
    }

    suspend fun putLeftFork() {
        leftFork = false
        thoughts = 0
        stage = 4
        delay(takeForkTime)
    }

    suspend fun putRightFork() {
        rightFork = false
        thoughts = 0
        stage = 0
        delay(takeForkTime)
    }

    suspend fun think() {
        delay(thinkTime)
        if (leftFork || rightFork) {
            thoughts++
        }
    }
}