import java.util.Random;

/**
 * A thread that tries to enter the Hiswa to buy a boat
 */
public class Buyer extends Thread {
    private Hiswa hiswa;

    public Buyer(Hiswa hiswa) {
        this.hiswa = hiswa;
    }

    public void run() {
        //life never ends
        while (true) {
            try {
                //go to the Hiswa at a random time
                this.sleep(new Random().nextInt(12000));
                hiswa.buyerEnterHiswa();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
