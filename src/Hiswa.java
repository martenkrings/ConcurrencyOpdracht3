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
    private Condition newViewer, newBuyer, readyToEnter, finished, wait;

    private int consecutiveBuyers, waitingBuyers, waitingViewers, insideViewers, viewersEntering;
    private boolean enterReady = false, viewerAcces = false, buyerAccess = false;

    public Hiswa() {
        this.buyerLock = new ReentrantLock();
        this.lock = new ReentrantLock();
        this.viewerLock = new ReentrantLock();
        newViewer = viewerLock.newCondition();
        newBuyer = buyerLock.newCondition();
        readyToEnter = lock.newCondition();
        finished = lock.newCondition();
        wait = lock.newCondition();

        //thread for testing purposses
        Overwatch overwatch = new Overwatch();
        overwatch.start();
    }

    public synchronized boolean getViewerAccess(){
        return viewerAcces;
    }

    public synchronized  void setViewerAccess(boolean b){
        viewerAcces = b;
    }

    /**
     * Method HiswaEmployee calls to get the next customer
     */
    public void nextCustomer() {
        if (waitingBuyers < 1 && consecutiveBuyers < 4 && insideViewers < 100) {
            viewerLock.lock();
            try {
                //notify till no more viewers fit inside
                viewersEntering = waitingViewers;
                if (viewersEntering > MAX_VIEWERS - insideViewers){
                    viewersEntering = MAX_VIEWERS - insideViewers;
                }
                setViewerAccess(true);
                for (int x = 0; x < viewersEntering; x++){
                    newViewer.signal();
                }

                consecutiveBuyers = 0;
            } finally {
                viewerLock.unlock();
            }
        } else if (consecutiveBuyers > 3 && waitingBuyers > 0) {
            buyerLock.lock();
            try {
                consecutiveBuyers++;
                buyerAccess = true;
                newBuyer.signal();
                System.out.println("now letting a buyer enter");
            } finally {
                buyerLock.unlock();
            }
        }
    }

    public void viewerEnterHiswa() {
        viewerLock.lock();
        incrementWaitingViewers();
        try {
            while (getViewerAccess()) {
                newViewer.await();
            }
            decrementViewersEntering();
            //if I am the last to enter than close the door
            if (viewersEntering == 0) {
                setViewerAccess(false);
            }

            //no longer waiting
            decrementWaitingViewers();
            incrementInsideViewer();

        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            viewerLock.unlock();
        }

        lock.lock();
        try {
            readyToEnter.signal();

            //take the time to look around
            wait.await(1, TimeUnit.SECONDS);

            decrementInsideViewer();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
    }

    /**
     * method a buyer calls to enter the Hiswa
     */
    public void buyerEnterHiswa() {
        buyerLock.lock();
        try {
            incrementWaitingBuyer();

            //wait till i may enter
            while (buyerAccess) {
                newBuyer.await();
            }
            buyerAccess = false;
            decrementWaitingBuyer();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            buyerLock.unlock();
        }

        lock.lock();
        try {
            //take the time to buy a boat
            wait.await(1, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
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

    /**
     * locked method to increment viewersEntering
     */
    private void incrementViewersEntering() {
        viewerLock.lock();
        try {
            viewersEntering++;
        } finally {
            viewerLock.unlock();
        }
    }

    /**
     * locked method to decrement viewersEntering
     */
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

    /**
     * A class thats not part of the functional project, print info for testing purposes
     */
    private class Overwatch extends Thread {
        public void run() {
            while (true) {
                //a lock so we wont be spammed with data
                lock.lock();
                try {
                    wait.await(250, TimeUnit.MILLISECONDS);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    lock.unlock();
                }

                System.out.println("Er wachten nu " + waitingBuyers + " kopers en er zijn " + consecutiveBuyers + " consecutiveBuyers");
                System.out.println("Er zijn nu " + insideViewers + " kijkers binnen en " + waitingViewers + " wachtende kijkers");
                System.out.println();
            }
        }
    }
}