import java.util.Random;

/**
 * A thread that tries to enter the Hiswa to look around
 */
public class Viewer extends Thread{
    private Hiswa hiswa;

    public Viewer(Hiswa hiswa) {
        this.hiswa = hiswa;
    }

    public void run() {
        //life is endless
        while (true) {
            try {
                //go to the Hiswa at a random time
                this.sleep(new Random().nextInt(12000));
                hiswa.viewerEnterHiswa();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
