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
                hiswa.viewerEnterHiswa();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
