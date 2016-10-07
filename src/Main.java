import javax.management.monitor.Monitor;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by Marten on 9/27/2016.
 */
public class Main {
    public static void main(String[] args) {
        new Main().run();
    }

    private Kijker[] kijkers = new Kijker[1000];
    private Koper[] kopers = new Koper[10];

    private int maxViewers = 100;
    //this is used so the last viewer to enter can close the door
    private int viewersEntering = 0;

    private Lock buyerLock, viewerLock, lock;
    private Condition newViewer, newKoper, readyToEnter, finished, wait;

    private int consecutiveBuyers = 0, waitingBuyers, waitingViewers, insideViewers;
    private boolean enterReady = false, kijkerToegang = false, koperToegang = false;

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


    public synchronized void run() {
        this.buyerLock = new ReentrantLock();
        this.lock = new ReentrantLock();
        this.viewerLock = new ReentrantLock();
        newViewer = viewerLock.newCondition();
        newKoper = buyerLock.newCondition();
        readyToEnter = lock.newCondition();
        finished = lock.newCondition();
        wait = lock.newCondition();

        //maak alle kopers en kijkers
        for (int i = 0; i < kijkers.length; i++) {
            kijkers[i] = new Kijker();
            kijkers[i].start();
        }
        for (int j = 0; j < kopers.length; j++) {
            kopers[j] = new Koper();
            kopers[j].start();
        }

        //start de Hiswa
        Hiswa hiswa = new Hiswa();
        hiswa.start();
        Overwatch overwatch = new Overwatch();
        overwatch.start();
    }

    /**
     * Een classe die regelmatig data uitprint voor een overview
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


    private class Hiswa extends Thread {
        public void run() {
            //De Hiswa is oneindig
            while (true) {
                //als er wachtende kopers zijn dan mogen er geen kijkers meer naar binnen
                if (waitingBuyers != 0) {
                    System.out.println("there is a waiting buyer");
                    //als er niet 4 opeenvolgende kopers waren laat dan een koper naar binnen
                    if (consecutiveBuyers < 4) {
                        System.out.println("a buyer wil try to enter because there haven't been 4 in a row");
                        waitForEmpty();
                        System.out.println("The room is now empty so the buyer can enter");

                        getBuyer();

                        //als er ruimte is stuur dan viewers naar binnen
                    } else if (insideViewers < maxViewers) {
//                            waitForEmpty();

                        getViewer();
                    }

                    //als er ruimte is stuur dan viewers naar binnen
                } else if (insideViewers < maxViewers && waitingViewers != 0) {
                    System.out.println("no waiting buyer so try to fit in some more viewers");
//                        waitForEmpty();
                    getViewer();
                }
                System.out.println("Nothing happend");
            }
        }
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
            while (insideViewers < maxViewers && waitingViewers > 0) {
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

//    /**
//     * finish the Hiswa
//     */
//    private void finishHiswa(){
//        lock.lock();
//        try {
//            //stuur de personen weg als deze klaar zijn
//            System.out.println("---------Hitswa is nu voorbij---------");
//            wait.await(1, TimeUnit.SECONDS);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        } finally {
//            lock.unlock();
//        }
//    }


    private class Koper extends Thread {
        public void run() {
            //het leven is oneindig
            while (true) {
                try {
                    //meld je op een willekeurig moment bij de HISWA
                    this.sleep(new Random().nextInt(15000));
//                    System.out.println(this.toString() + " Gaat zich nu melden [KOPER]");
                    incrementWaitingBuyer();
                    nextBuyer();
//                    System.out.println(this.toString() + " Is nu klaar [KOPER]");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private class Kijker extends Thread {
        public void run() {
            //het leven is oneindig
            while (true) {
                try {
                    //meld je op een willekeurig moment bij de HISWA
                    this.sleep(new Random().nextInt(12000));
//                    System.out.println(this.toString() + " Gaat zig nu melden [VIEWER]");
                    incrementWaitingViewers();
                    nextViewers(this);
                    decrementWaitingViewers();
//                    System.out.println(this.toString() + " Is nu klaar [VIEWER]");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
 * de methode die de kijker aanroepen om de Main te bezoeken
 */
private void nextViewers(Thread thread) throws InterruptedException {
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
    private void nextBuyer() throws InterruptedException {
        buyerLock.lock();
        try {
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
}
