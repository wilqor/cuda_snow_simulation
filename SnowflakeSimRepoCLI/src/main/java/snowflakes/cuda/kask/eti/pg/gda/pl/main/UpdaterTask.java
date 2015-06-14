package snowflakes.cuda.kask.eti.pg.gda.pl.main;

import java.util.TimerTask;

/**
 * Created by Ariel on 2015-06-14.
 */
public class UpdaterTask extends TimerTask {
    @Override
    public void run() {
        UpdateManager.update();
    }
}
