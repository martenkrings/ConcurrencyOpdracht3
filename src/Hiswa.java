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
    private Condition newCustomer, invitation, readyToBuy, finished, readyToView;

    private int numberOfCustomers;
    private boolean invited, isFinished, inside, finishedBuying, finishedViewing;

    private boolean noCustomers() {
        return numberOfCustomers == 0;
    }

    private Kijker nextViewer() {
        
    }

    public void run(){
        this.lock = new ReentrantLock();
        newCustomer = lock.newCondition();
        invitation = lock.newCondition();
        readyToBuy = lock.newCondition();
        finished = lock.newCondition();
        readyToView = lock.newCondition();

    }

    private class Koper extends Thread{

    }

    private class Kijker extends Thread{

    }
}
