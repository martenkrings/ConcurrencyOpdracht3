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
    private Condition newViewer, newBuyer, readyToEnter, empty, wait;

    private int consecutiveBuyers, waitingBuyers, waitingViewers, insideViewers, viewersEntering;
    private boolean isEmpty = false, viewerAcces = false, buyerAccess = false, allViewersMayEnter = false;

    public Hiswa() {
        this.buyerLock = new ReentrantLock();
        this.lock = new ReentrantLock();
        this.viewerLock = new ReentrantLock();
        newViewer = viewerLock.newCondition();
        newBuyer = buyerLock.newCondition();
        readyToEnter = lock.newCondition();
        empty = lock.newCondition();
        wait = lock.newCondition();

        //thread for testing purposses
        Overwatch overwatch = new Overwatch();
        overwatch.start();
    }

    public int getInsideViewers() {
        viewerLock.lock();
        try {
            return insideViewers;
        } finally {
            viewerLock.unlock();
        }
    }

    public int getWaitingViewers() {
        viewerLock.lock();
        try {
            return waitingViewers;
        } finally {
            viewerLock.unlock();
        }
    }

    public int getWaitingBuyers() {
        buyerLock.lock();
        try {
            return waitingBuyers;
        } finally {
            buyerLock.unlock();
        }
    }
//
//    public synchronized boolean getViewerAccess() {
//        return viewerAcces;
//    }
//
//    public synchronized void setViewerAccess(boolean b) {
//        viewerAcces = b;
//    }

    /**
     * Method HiswaEmployee calls to get the next customer(s)
     */
    public void nextCustomer() {
        if (consecutiveBuyers < 4 && getWaitingBuyers() > 0 && allViewersMayEnter == false) {
            //if a buyer may enter wait till all viewers have left before letting the buyer know
            waitTurn();

            //let a buyer know he/she can enter
            buyerLock.lock();
            try {
                consecutiveBuyers++;
                buyerAccess = true;
                newBuyer.signal();
            } finally {
                buyerLock.unlock();
            }
        } else if (getInsideViewers() < MAX_VIEWERS && getWaitingViewers() > 0) {
            //if the last to enter was a buyer than wait till he is done before letting viewers in
            if (consecutiveBuyers != 0) {
                waitTurn();
            }

            viewerLock.lock();
            try {
                if (consecutiveBuyers == 4){
                    allViewersMayEnter = true;
                    viewersEntering = waitingViewers;
                }

                //let a viewer in
                viewerAcces = true;
                newViewer.signal();

                //if consecutiveBuyer equals 4 than all waiting viewers should enter
//                if (consecutiveBuyers == 4){
//                    viewersEntering = waitingViewers;
//                    viewerAcces = true;
//                    newViewer.signalAll();
//                } else {
//                    //notify till no more viewers fit inside
//                    viewersEntering = waitingViewers;
//                    if (viewersEntering > MAX_VIEWERS - insideViewers) {
//                        viewersEntering = MAX_VIEWERS - insideViewers;
//                    }
//                    viewerAcces = true;
//                    for (int x = 0; x < viewersEntering; x++) {
//                        newViewer.signal();
//                    }
//                }


                consecutiveBuyers = 0;
            } finally {
                viewerLock.unlock();
            }
        } else {
            consecutiveBuyers = 0;
        }
    }

    public void waitTurn() {
        lock.lock();
        try {
            while (isEmpty == false) {
                empty.await();
            }
            //reset isEmpty
            isEmpty = false;
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
    }

    public void viewerEnterHiswa() {
        viewerLock.lock();
        waitingViewers++;
        try {
            while (viewerAcces == false) {
                newViewer.await();
            }
            viewerAcces = false;

            //if allViewersMayEnter than take myself of the countdown
            if (allViewersMayEnter == true) {
                viewersEntering--;
            }

            //if I am the last of the viewers that could enter than let the rest know
            if (viewersEntering == 0) {
                allViewersMayEnter = false;
            }

            //no longer waiting but going inside
            waitingViewers--;
            insideViewers++;

        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            viewerLock.unlock();
        }

        lock.lock();
        try {
            //take the time to look around
            wait.await(100, TimeUnit.MILLISECONDS);
            //get the lock to savely change insideViewers
            viewerLock.lock();
            try {
                //I'm leaving now
                insideViewers--;
            } finally {
                viewerLock.unlock();
            }

            if (insideViewers == 0) {
                isEmpty = true;
                empty.signal();
            }
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
            waitingBuyers++;

            //wait till I may enter
            while (buyerAccess == false) {
                newBuyer.await();
            }

            buyerAccess = false;
            waitingBuyers--;
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            buyerLock.unlock();
        }

        lock.lock();
        try {
            //take the time to buy a boat
            wait.await(1, TimeUnit.SECONDS);

            //leaving
            isEmpty = true;
            empty.signal();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
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
                    wait.await(5, TimeUnit.MILLISECONDS);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    lock.unlock();
                }

                System.out.println("Er wachten nu " + getWaitingBuyers() + " kopers en er zijn " + consecutiveBuyers + " consecutiveBuyers");
                System.out.println("AllViewerMayEnter: " + allViewersMayEnter + " viewersEntering: " + viewersEntering);
                System.out.println("Er zijn nu " + getInsideViewers() + " kijkers binnen en " + waitingViewers + " wachtende kijkers");
                System.out.println();
            }
        }
    }
}