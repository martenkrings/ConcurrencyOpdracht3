/**
 * Created by Marten on 10/7/2016.
 */
public class World {
    Hiswa hiswa;

    private Viewer[] kijkers = new Viewer[1000];
    private Buyer[] kopers = new Buyer[10];

    public static void main(String[] args) {
        new World().run();
    }

    public void run() {
        hiswa = new Hiswa();

        //start up the world
        for (int i = 0; i < kijkers.length; i++) {
            kijkers[i] = new Viewer(hiswa);
            kijkers[i].start();
        }
        for (int j = 0; j < kopers.length; j++) {
            kopers[j] = new Buyer(hiswa);
            kopers[j].start();
        }
        HiswaEmployee hiswaEmployee = new HiswaEmployee(hiswa);
        hiswaEmployee.start();
    }
}
