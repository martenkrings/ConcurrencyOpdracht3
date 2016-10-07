/**
 * Created by Marten on 10/7/2016.
 */
public class World {
    Hiswa hiswa;

    private Viewer[] kijkers = new Viewer[1000];
    private Buyer[] kopers = new Buyer[10];

    public static void main(String[] args) {
        new Main().run();
    }

    public void run() {
        hiswa = new Hiswa();

        //De Hiswa is oneindig
        while (true) {
            //als er wachtende kopers zijn dan mogen er geen kijkers meer naar binnen
            if (hiswa.waitingBuyers != 0) {
                System.out.println("there is a waiting buyer");
                //als er niet 4 opeenvolgende kopers waren laat dan een koper naar binnen
                if (consecutiveBuyers < 4) {
                    System.out.println("a buyer wil try to enter because there haven't been 4 in a row");
                    waitForEmpty();
                    System.out.println("The room is now empty so the buyer can enter");

                    getBuyer();

                    //als er ruimte is stuur dan viewers naar binnen
                } else if (insideViewers < maxViewers) {
//                            waitForEmpty();

                    getViewer();
                }

                //als er ruimte is stuur dan viewers naar binnen
            } else if (insideViewers < maxViewers && waitingViewers != 0) {
                System.out.println("no waiting buyer so try to fit in some more viewers");
//                        waitForEmpty();
                getViewer();
            }
            System.out.println("Nothing happend");
        }
    }
}
