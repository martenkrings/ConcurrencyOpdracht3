import java.util.Random;

/**
 * Created by Marten on 10/7/2016.
 */
public class Buyer extends Thread {
    private Hiswa hiswa;

    public Buyer(Hiswa hiswa) {
        this.hiswa = hiswa;
    }

    public void run() {
        //het leven is oneindig
        while (true) {
            try {
                //meld je op een willekeurig moment bij de HISWA
                this.sleep(new Random().nextInt(15000));
//                    System.out.println(this.toString() + " Gaat zich nu melden [KOPER]");
                hiswa.nextBuyer();
//                    System.out.println(this.toString() + " Is nu klaar [KOPER]");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
