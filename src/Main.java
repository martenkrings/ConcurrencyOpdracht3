import java.util.Random;
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

    private Lock buyerLock, viewerLock, lock;
    private Condition newViewer, newKoper, readyToEnter, finished;

    private int consecutiveBuyers = 0, waitingBuyers, waitingViewers, insideViewers;
    private boolean enterReady, isFinished, kijkerToegang, koperToegang = false;

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
    private synchronized void incrementWaitngViewers() {
        waitingViewers++;
    }

    /**
     * synchronized method to decrement waitingViewers
     */
    private synchronized void decrementWaitngViewers() {
        waitingViewers--;
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
     * de methode die de kijker aanroepen om de Main te bezoeken
     */
    private void nextViewers() throws InterruptedException {
        viewerLock.lock();
        try {
            //wacht tot kijkers naar binnen mogen
            while (!kijkerToegang) {
                newViewer.await();
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

            //wacht tot je gemeld wordt dat je klaar bent
            while (!isFinished) {
                finished.await();
            }

            //meld dat je buiten bent
            decrementInsideViewer();
        }finally {
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
            System.out.println("Een buyer gaat naar binnen");
        } finally {
            buyerLock.unlock();
        }

        lock.lock();
        try {
            //meld dat je naar binnen wil
            enterReady = true;
            readyToEnter.signal();

            //wacht tot je gemeld wordt dat je klaar bent
            while (!isFinished) {
                finished.await();
            }
            isFinished = false;
        } finally {
            lock.unlock();
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
    }

    private void getBuyer() {
        System.out.println("---------Een koper mag nu naar binnen---------");
        buyerLock.lock();
        try {
            koperToegang = true;
            consecutiveBuyers++;
            System.out.println("signal");
            newKoper.signal();
        } finally {
            buyerLock.unlock();
        }
        holdHiswa();

    }

    private void getViewer() {
        viewerLock.lock();
        try {
            kijkerToegang = true;
            consecutiveBuyers = 0;
            newViewer.signal();
        } finally {
            viewerLock.unlock();
        }
        holdHiswa();

    }

    private void holdHiswa() {
        lock.lock();
        try {
            //wacht tot de persoon klaar is om naar binnen te gaan
            while (!enterReady) {
                readyToEnter.await();
            }
            enterReady = false;

            //stuur de personen weg als deze klaar zijn
            isFinished = true;

            System.out.println("---------Hitswa is nu voorbij---------");
            finished.signalAll();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
    }

    private class Hiswa extends Thread {
        public void run() {
            //De Hiswa is oneindig
            while (true) {
                //als er wachtende kopers zijn dan mogen er geen kijkers meer naar binnen
                if (waitingBuyers != 0) {
                    System.out.println(waitingBuyers);

                    //als er niet 4 opeenvolgende kopers waren laat dan een koper naar binnen
                    if (consecutiveBuyers != 4) {
                        getBuyer();

                        //als er ruimte is stuur dan viewers naar binnen
                    } else if (insideViewers < 100) {
                        getViewer();
                    }

                    //als er ruimte is stuur dan viewers naar binnen
                } else if (insideViewers < 100 && waitingViewers != 0) {
                    getViewer();
                }
            }
        }
    }

    private class Koper extends Thread {
        public void run() {
            //het leven is oneindig
            while (true) {
                try {
                    //meld je op een willekeurig moment bij de HISWA
                    this.sleep(new Random().nextInt(12000));
                    System.out.println(this.toString() + " Gaat zig nu melden [KOPER]");
                    incrementWaitingBuyer();
                    nextBuyer();
                    decrementWaitingBuyer();
                    System.out.println(this.toString() + " Is nu klaar [KOPER]");
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
                    this.sleep(new Random().nextInt(15000));
                    System.out.println(this.toString() + " Gaat zig nu melden [VIEWER]");
                    incrementWaitngViewers();
                    nextViewers();
                    decrementWaitngViewers();
                    System.out.println(this.toString() + " Is nu klaar [VIEWER]");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
