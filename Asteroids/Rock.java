import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.StrokeLineJoin;

import java.util.ArrayList;
import java.util.Random;

public class Rock extends Polygon {
    private int randomSpeedX, randomSpeedY;
    private double width, height;
    private static Random random = new Random();
    private boolean alive = true;
    //Color colors[] = {Color.ORANGE, Color.ALICEBLUE, Color.SNOW, Color.ANTIQUEWHITE, Color.CHARTREUSE};

    Rock() {
        super(6, 0, 14, 0, 15, 5, 20, 6, 20, 14, 15, 15, 14, 20, 6, 20, 5, 15, 0, 14, 0, 6, 5, 5);

        setSmooth(true);
        setRotate(((int)(Math.random()*10+1))*9);
        setStroke(Color.web("e6825b"));
        setFill(Color.web("511b05"));
        //setFill(colors[random.nextInt(5)]);
        setStrokeWidth(2);
        setStrokeLineJoin(StrokeLineJoin.ROUND);

        width = getLayoutBounds().getWidth();
        height = getLayoutBounds().getWidth();

        // give random speed in the range -4 to +4
        randomSpeedX = random.nextInt(9) - 4;
        randomSpeedY = random.nextInt(9) - 4;

        // give random spawn location
        setTranslateX(random.nextInt(901) - width);
        setTranslateY(random.nextInt(601) - height);
    }

    void move(ArrayList<Rock> rocks) {
        double newPositionX = getTranslateX() + randomSpeedX;
        double newPositionY = getTranslateY() + randomSpeedY;

        if(newPositionX <= 0) {
            newPositionX = 0;
            randomSpeedX = -randomSpeedX;
        } else if(newPositionX+width >= 900) {
            newPositionX = 900 - width;
            randomSpeedX = -randomSpeedX;
        }

        if(newPositionY <= 0) {
            newPositionY = 0;
            randomSpeedY = -randomSpeedY;
        } else if(newPositionY+height >= 600) {
            newPositionY = 600 - height;
            randomSpeedY = -randomSpeedY;
        }

        setTranslateX(newPositionX);
        setTranslateY(newPositionY);

        for(Rock rock: rocks) {
            if(rock == this)
                continue;

            if(this.getBoundsInParent().intersects(rock.getBoundsInParent())) {
                this.randomSpeedX = this.randomSpeedX + rock.randomSpeedX - (rock.randomSpeedX = this.randomSpeedX);
                this.randomSpeedY = this.randomSpeedY + rock.randomSpeedY - (rock.randomSpeedY = this.randomSpeedY);

            }
        }
    }

    int getSpeedX() {
        return randomSpeedX;
    }

    int getSpeedY() {
        return randomSpeedY;
    }

    void setSpeedX(int speed) {
        randomSpeedX = speed;
    }

    void setSpeedY(int speed) {
        randomSpeedY = speed;
    }

    boolean isAlive() {
        return alive;
    }

    void setDead() {
        alive = false;
    }
}
