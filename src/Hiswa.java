import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by Marten on 10/7/2016.
 */
public class Hiswa {
    private static final int MAX_VIEWERS = 100;

    private Lock buyerLock, viewerLock, lock;
    private Condition newViewer, newKoper, readyToEnter, finished, wait;

    private int consecutiveBuyers, waitingBuyers, waitingViewers, insideViewers, viewersEntering;
    private boolean enterReady = false, kijkerToegang = false, koperToegang = false;

    public Hiswa(){
        this.buyerLock = new ReentrantLock();
        this.lock = new ReentrantLock();
        this.viewerLock = new ReentrantLock();
        newViewer = viewerLock.newCondition();
        newKoper = buyerLock.newCondition();
        readyToEnter = lock.newCondition();
        finished = lock.newCondition();
        wait = lock.newCondition();
    }

    private void waitForEmpty() {
        //wacht tot je naar binnen mag
        lock.lock();
        try {
            while (insideViewers != 0) {
                finished.await();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
    }

    /**
     * Methode die de Hiswa aanroept om een koper naar binnen laten
     */
    private void getBuyer() {
        buyerLock.lock();
        try {
            koperToegang = true;
            consecutiveBuyers++;
            newKoper.signal();
        } finally {
            buyerLock.unlock();
        }
        enterHiswa();
    }

    /**
     * Signal viewers to enter
     */
    private void getViewer() {
//        System.out.println("---------Een viewer mag nu naar binnen---------");
        viewerLock.lock();
        try {
            consecutiveBuyers = 0;
//            newViewer.signalAll();
            //Let all waitingviewers inside untill the limit has been reached or there are no more waitng viewers
            while (insideViewers < MAX_VIEWERS && waitingViewers > 0) {
                newViewer.signal();
                incrementViewersEntering();
            }
        } finally {
            viewerLock.unlock();
        }
        enterHiswa();
    }

    /**
     * let a person enter the Hiswa
     */
    private void enterHiswa() {
        lock.lock();
        try {
            //wacht tot de persoon klaar is om naar binnen te gaan
            while (!enterReady) {
                readyToEnter.await();
            }
            enterReady = false;
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
    }

    /**
     * de methode die de kijker aanroepen om de Main te bezoeken
     */
    public void nextViewers(Thread thread) throws InterruptedException {
        viewerLock.lock();
        try {
            //wacht tot kijkers naar binnen mogen
            while (!kijkerToegang) {
                newViewer.await();
            }

            decrementViewersEntering();
            //if I enter last close the door behind me
            if (viewersEntering == 0) {
                kijkerToegang = false;
            }

        } finally {
            viewerLock.unlock();
        }

        lock.lock();
        try {
            //meld dat je door de poort bent
            incrementInsideViewer();

            //meld dat je naar binnen wil
            enterReady = true;
            readyToEnter.signal();

            //neem de tijd om rond te kijken
            wait.await(1, TimeUnit.SECONDS);
            System.out.println(insideViewers);

            //meld dat je buiten bent
            decrementInsideViewer();

            //als je de laatste bent die weg gaat meld de wachtende koper
            if (insideViewers == 0) {
                finished.signal();
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * De methode die de kopers aanroepen om de Main te bezoeken
     */
    public void nextBuyer() throws InterruptedException {
        buyerLock.lock();
        try {
            //een koper gaat nu wachten
            incrementWaitingBuyer();

            //wacht tot kopers naar binnen mogen
            while (!koperToegang) {
                newKoper.await();
            }
            koperToegang = false;
            decrementWaitingBuyer();
//            System.out.println("Een buyer gaat naar binnen");
        } finally {
            buyerLock.unlock();
        }

        lock.lock();
        try {
            //meld dat je naar binnen wil
            enterReady = true;
            readyToEnter.signal();

            //neem de tijd om rond te kijken
            wait.await(1, TimeUnit.SECONDS);
            System.out.println("Een Koper is nu klaar");
            finished.signal();
        } finally {
            lock.unlock();
        }
    }

    /**
     * locked method to increment waitingBuyers
     */
    private void incrementWaitingBuyer() {
        buyerLock.lock();
        try {
            waitingBuyers++;
        } finally {
            buyerLock.unlock();
        }
    }

    /**
     * locked method to decrement waiting buyers
     */
    private void decrementWaitingBuyer() {
        buyerLock.lock();
        try {
            waitingBuyers--;
        } finally {
            buyerLock.unlock();
        }
    }

    /**
     * synchronized method to increment waitingViewers
     */
    private synchronized void incrementWaitingViewers() {
        waitingViewers++;
    }

    /**
     * synchronized method to decrement waitingViewers
     */
    private synchronized void decrementWaitingViewers() {
        waitingViewers--;
    }

    private void incrementViewersEntering() {
        viewerLock.lock();
        try {
            viewersEntering++;
        } finally {
            viewerLock.unlock();
        }
    }

    private void decrementViewersEntering() {
        viewerLock.lock();
        try {
            viewersEntering--;
        } finally {
            viewerLock.unlock();
        }
    }

    /**
     * locked method to increment insideViewers
     */
    private void incrementInsideViewer() {
        viewerLock.lock();
        try {
            insideViewers++;
        } finally {
            viewerLock.unlock();
        }
    }

    /**
     * locked method to decrement insideViewers
     */
    private void decrementInsideViewer() {
        viewerLock.lock();
        try {
            insideViewers--;
        } finally {
            viewerLock.unlock();
        }
    }
}
