import java.util.Random;

/**
 * Created by Marten on 10/7/2016.
 */
public class Viewer extends Thread{
    private Hiswa hiswa;

    public Viewer(Hiswa hiswa) {
        this.hiswa = hiswa;
    }

    public void run() {
        //het leven is oneindig
        while (true) {
            try {
                //meld je op een willekeurig moment bij de HISWA
                this.sleep(new Random().nextInt(12000));
//                    System.out.println(this.toString() + " Gaat zig nu melden [VIEWER]");
                hiswa.incrementWaitingViewers();
                hiswa.nextViewers(this);
                hiswa.decrementWaitingViewers();
//                    System.out.println(this.toString() + " Is nu klaar [VIEWER]");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
