import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by Marten on 10/7/2016.
 */
public class Hiswa {
    private static final int MAX_VIEWERS = 100;

    private Lock lock;
    private Condition newViewer, newBuyer, empty, wait;

    private int consecutiveBuyers, waitingBuyers, waitingViewers, insideViewers, viewersEntering;
    private boolean isEmpty = false, viewerAcces = false, buyerAccess = false, allViewersMayEnter = false;

    public Hiswa() {
        //locks
//        this.buyerLock = new ReentrantLock();
        this.lock = new ReentrantLock();
//        this.viewerLock = new ReentrantLock();

        //conditions
        newViewer = lock.newCondition();
        newBuyer = lock.newCondition();
        empty = lock.newCondition();
        wait = lock.newCondition();

//        //thread for testing purposses
//        Overwatch overwatch = new Overwatch();
//        overwatch.start();
    }

    /**
     * gets the insideViewers, locked by viewerLock
     *
     * @return insideViewer
     */
    public int getInsideViewers() {
        lock.lock();
        try {
            return insideViewers;
        } finally {
            lock.unlock();
        }
    }

    /**
     * gets the waitingViewers, locked by viewerLock
     *
     * @return waitingViewers
     */
    public int getWaitingViewers() {
        lock.lock();
        try {
            return waitingViewers;
        } finally {
            lock.unlock();
        }
    }

    //todo make it synchro xxxx

    /**
     * gets the waitingBuyers, locked by buyerLock
     *
     * @return waitingBuyers
     */
    public int getWaitingBuyers() {
        lock.lock();
        try {
            return waitingBuyers;
        } finally {
            lock.unlock();
        }
    }

//    /**
//     * Method HiswaEmployee calls to get the next customer(s)
//     */
//    public void nextCustomer() {
//        if (consecutiveBuyers < 4 && getWaitingBuyers() > 0 && allViewersMayEnter == false) {
//            //if a buyer may enter wait till all viewers have left before letting the buyer know
//            waitTurn();
//
//            //let a buyer know he/she can enter
//            buyerLock.lock();
//            try {
//                consecutiveBuyers++;
//                buyerAccess = true;
//                newBuyer.signal();
//            } finally {
//                buyerLock.unlock();
//            }
//        } else if (getInsideViewers() < MAX_VIEWERS && getWaitingViewers() > 0) {
//            //if the last to enter was a buyer than wait till he is done before letting viewers in
//            if (consecutiveBuyers != 0) {
//                waitTurn();
//            }
//
//            viewerLock.lock();
//            try {
//                //if there are 4 consecutiveBuyers than let all waiting viewers in
//                if (consecutiveBuyers == 4) {
//                    allViewersMayEnter = true;
//                    viewersEntering = waitingViewers;
//                }
//
//                //let a viewer in
//                viewerAcces = true;
//                newViewer.signal();
//
//                consecutiveBuyers = 0;
//            } finally {
//                viewerLock.unlock();
//            }
//        } else {
//            consecutiveBuyers = 0;
//        }
//    }

    /**
     * method that waits till the Hiswa room is empty
     */
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
        lock.lock();
        waitingViewers++;
        try {
            if (getWaitingBuyers() > 0 && allViewersMayEnter == false || getInsideViewers() >= MAX_VIEWERS) {
                while (viewerAcces == false) {
                    newViewer.await();
                }
                viewerAcces = false;
            }

            //if the last to enter was a buyer wait for that buyer to leave
            if (consecutiveBuyers > 0){
                waitTurn();
            }

            //no consecutiveBuyers anymore
            consecutiveBuyers = 0;

            //no longer waiting but going inside
            waitingViewers--;
            insideViewers++;

            System.out.println("inside viewers: " + insideViewers);

            //if allViewersMayEnter than take myself of the countdown
            if (allViewersMayEnter == true) {
                viewersEntering--;
            }

            //if I am the last of the viewers that could enter than let the rest know
            if (viewersEntering == 0) {
                allViewersMayEnter = false;
            }


        //can a new viewer enter?
        if (allViewersMayEnter == true || getWaitingBuyers() == 0){
            //is there room for a new viewer
            if (getInsideViewers() < MAX_VIEWERS){
                viewerAcces = true;
                newViewer.signal();
            }
        }

        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }

            //todo wait in a method
            //take the time to look around
        waitt();
//        try {
//            wait.await(100, TimeUnit.MILLISECONDS);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }

        //get the lock to savely adjust data
        lock.lock();
        try{
            //I'm leaving now
            insideViewers--;


            //if I am the last to leave than signal empty
            if (insideViewers == 0) {
                isEmpty = true;
                empty.signal();
            }

            //if a buyer may enter let him know
            if (allViewersMayEnter == false || getWaitingBuyers() > 0) {
                if (isEmpty) {
                    buyerAccess = true;
                    newBuyer.signal();
                }

                //else if there is a waiting viewer he can enter
            } else if (getWaitingViewers() > 0){
                viewerAcces = true;
                newViewer.signal();
            }
        }finally {
            lock.unlock();
        }
    }

    /**
     * Method a buyer calls to enter the Hiswa
     */
    public void buyerEnterHiswa() {
        lock.lock();
        try {

            waitingBuyers++;

            //if I cant enter wait
            if (allViewersMayEnter == true || isEmpty == false || getInsideViewers() > 0) {
                System.out.println(this.toString() + " now waiting");
                //wait till I may enter
                while (buyerAccess == false) {
                    newBuyer.await();
                }
            }
            buyerAccess = false;

            //the room is not empty anymore
            isEmpty = false;

            System.out.println(this.toString() + " now inside");
            System.out.println("|" + "InsideViewers: " + getInsideViewers() + "|");
            System.out.println("|" + "allviewersMayEnter: " + allViewersMayEnter + "|");

            //+1 the consecutive buyers
            consecutiveBuyers++;

            //no longer waiting
            waitingBuyers--;



            //todo do this in a method
            //take the time to buy a boat
            waitt();
//            wait.await(100, TimeUnit.MILLISECONDS);

            //leaving
            isEmpty = true;
            empty.signal();

            //if i am the 4th buyer in a row than I'll tell all viewers they can enter
            if (consecutiveBuyers == 4){
                //all viewers that are waiting may now enter
                allViewersMayEnter = true;
                viewersEntering = waitingViewers;

                System.out.println("I am the 4th buyer in a row so allViewersMayEnter" + allViewersMayEnter + " vieers that are entering" + viewersEntering);

                viewerAcces = true;
                newViewer.signal();

                //else let a new buyer in if there are any waiting
            } else if (getWaitingBuyers() > 0){
                buyerAccess = true;
                newBuyer.signal();

                //else if there are any viewers waiting let them in
            } else if (getWaitingViewers() > 0){
                viewerAcces = true;
                newViewer.signal();
            }

            System.out.println(this + "now outside again");
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
    }

    /**
     * method to make a thread wait(the simulate looking around/buying a boat)
     */
    private void waitt(){
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * A class that is not a part of the functional project, prints info for testing purposes
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