/**
 * Created by Marten on 9/27/2016.
 */
public class Hiswa {
    public static void main(String[] args){
        new Hiswa().run();
    }

    private synchronized void bezoek(Object bezoeker){
        //bekijk de boot

        //als er een koper is koop de boot
        if (bezoeker instanceof Koper){
            //koop een boot
        }
    }

    /**
     * stuurt mensen naar binnen
     */
    public void run(){

    }

    private class Koper extends Thread{

    }

    private class Kijker extends Thread{

    }
}
