import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by Marten on 9/27/2016.
 */
public class Hiswa {
    public static void main(String[] args){
        new Hiswa().run();
    }

    private Lock lock;
    private Condition newViewer, newKoper, invitation, readyToEnter, finished;

    private int consecutiveBuyers = 0;
    private boolean enterReady, isFinished, kijkerToegang, koperToegang = false;

    /**
     * de methode die de kijker aanroepen om de Hiswa te bezoeken
     */
    private void nextViewers() throws InterruptedException {
        //wacht tot kijkers naar binnen mogen
        while (!kijkerToegang){
            newViewer.await();
        }
        //meld dat je naar binnen wil
        enterReady = true;
        readyToEnter.signal();
        //wacht tot je gemeld wordt dat je klaar bent
        while (!isFinished) {
            finished.await();
        }
    }

    /**
     * De methode die de kopers aanroepen om de Hiswa te bezoeken
     */
    private void nextBuyer() throws InterruptedException {
        lock.lock();
        try {
            //wacht tot kopers naar binnen mogen
            while (!koperToegang) {
                newKoper.await();
            }
            //meld dat je naar binnen wil
            enterReady = true;
            readyToEnter.signal();
            //wacht tot je gemeld wordt dat je klaar bent
            while (!isFinished) {
                finished.await();
            }
        } finally {
            lock.unlock();
        }
    }

    public void run(){
        this.lock = new ReentrantLock();
        newViewer = lock.newCondition();
        newKoper = lock.newCondition();
        invitation = lock.newCondition();
        readyToEnter = lock.newCondition();
        finished = lock.newCondition();

        //De Hiswa is oneindig
        while (true){
            //als er niet 4 opeenvolgende kopers waren laat dan een koper naar binnen
            if (consecutiveBuyers != 4){
                koperToegang = true;
                consecutiveBuyers++;
                newKoper.signal();
            } else {
                kijkerToegang = true;
                consecutiveBuyers = 0;
                newViewer.signal();
            }
            try {
                //wacht tot de persoon klaar is om naar binnen te gaan
                while (!enterReady)
                readyToEnter.await();
                //stuur de persoon weg als deze klaar is
                isFinished = true;
                finished.signal();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

    private class Koper extends Thread{

    }

    private class Kijker extends Thread{

    }
}
