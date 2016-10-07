/**
 * Class of an employee that signal when peiple can enter.
 */
public class HiswaEmployee extends Thread {
    private Hiswa hiswa;

    public HiswaEmployee(Hiswa hiswa){
        this.hiswa = hiswa;
    }

    public void run(){
        //Work never stops
        while (true){
            hiswa.nextCustomer();
        }
    }
}
