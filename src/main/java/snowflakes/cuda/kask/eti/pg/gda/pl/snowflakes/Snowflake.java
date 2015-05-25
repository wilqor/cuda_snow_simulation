package snowflakes.cuda.kask.eti.pg.gda.pl.snowflakes;

/**
 * Created by Ariel on 2015-05-11.
 */
public class Snowflake {
    private int id;
    private float posX;
    private float posY;
    private float sizeModulator;
    public Snowflake(int id, float posX, float posY, float sizeModulator) {
        this.id = id;
        this.posX = posX;
        this.posY = posY;
        this.sizeModulator = sizeModulator;
    }

    public float getPosX() {
        return posX;
    }

    public void setPosX(float posX) {
        this.posX = posX;
    }

    public float getPosY() {
        return posY;
    }

    public void setPosY(float posY) {
        this.posY = posY;
    }

    public float getSizeModulator() {
        return sizeModulator;
    }

    public int getId() {
        return id;
    }
    public void incPosY(float value){
        this.posY += value;
    }

    public void setSizeModulator(float sizeModulator) {
        this.sizeModulator = sizeModulator;
    }
}
