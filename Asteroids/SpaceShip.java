import javafx.geometry.Bounds;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.StrokeLineJoin;

import java.util.ArrayList;

public class SpaceShip extends Polygon {
    private double currentAngle = 0, movingAngle = 0;
    private double speed = 0;
    private int exponent = -1;
    private ArrayList<Beam> beams = new ArrayList<>();

    SpaceShip() {
        super(14, 2, 20, 16, 24, 19, 26, 28, 16, 21, 12, 21, 2, 28, 4, 19, 8, 16);
        setStrokeWidth(3);
        setFill(Color.SNOW);
        setStroke(Color.web("0eb8ff"));
        setStrokeLineJoin(StrokeLineJoin.ROUND);

        setTranslateX(450-28/2);
        setTranslateY(300-30/2);
    }

    void accelerate() {
        if(speed < 5)
            speed += 0.04;

        if(exponent < -1)
            exponent++;

        if(Math.abs(currentAngle - movingAngle) < 60) {
            if(currentAngle > movingAngle)
                movingAngle += 2.2;
            else if(currentAngle < movingAngle)
                movingAngle -= 2.2;
        } else {
            if(currentAngle > movingAngle)
                movingAngle += 3;
            else if(currentAngle < movingAngle)
                movingAngle -= 3;
        }

//        if(currentAngle > movingAngle)
//            movingAngle = currentAngle - 15;
//        else if(currentAngle < movingAngle)
//            movingAngle = currentAngle + 15;

        setTranslateX(getTranslateX() + speed*Math.sin(Math.toRadians(movingAngle)));
        setTranslateY(getTranslateY() - speed*Math.cos(Math.toRadians(movingAngle)));

        boundsCheckAndMaintain();

        //System.out.println("ACCELERATE -> Current Angle: " + currentAngle + " Moving Angle: " + movingAngle + " exponent: " + exponent);
    }

    void decelerate() {
        if(speed > 0) {
            speed -= speed*Math.exp(exponent--) + 0.05;   // the -0.05 is to make sure the value of speed reaches 0 in finite time
        }

        if(currentAngle > movingAngle)
            movingAngle += 2.2;
        else if(currentAngle < movingAngle)
            movingAngle -= 2.2;

        setTranslateX(getTranslateX() + speed*Math.sin(Math.toRadians(movingAngle)));
        setTranslateY(getTranslateY() - speed*Math.cos(Math.toRadians(movingAngle)));

        boundsCheckAndMaintain();

        //System.out.println("DECELERATE -> Current Angle: " + currentAngle + " Moving Angle: " + movingAngle + " exponent: " + exponent);
    }

    private void boundsCheckAndMaintain() {
        if(getTranslateY() < 0)
            setTranslateY(600);
        else if(getTranslateY() > 600)
            setTranslateY(0);

        if(getTranslateX() < 0)
            setTranslateX(900);
        else if(getTranslateX() > 900)
            setTranslateX(0);
    }

    ArrayList<Beam> fire(Group group) {
        // getTranslateX()+14 gives centerX and getTranslateY()+15 gives centerY of the ship
        double noseOfShipX = getTranslateX()+14 + 15*Math.sin(Math.toRadians(currentAngle));
        double noseOfShipY = getTranslateY()+15 - 15*Math.cos(Math.toRadians(currentAngle));

        Beam beam = new Beam(noseOfShipX, noseOfShipY, currentAngle);

        group.getChildren().add(beam);
        beams.add(beam);

        return beams;
    }

    void rotateRight() {
        currentAngle += 3;
        setRotate(currentAngle);

        if(speed == 0)
            movingAngle = currentAngle;
    }

    void rotateLeft() {
        currentAngle -= 3;
        setRotate(currentAngle);

        if(speed == 0)
            movingAngle = currentAngle;
    }

    double getSpeed() {
        return speed;
    }

    void nullifySpeed() {
        speed = 0;
    }

    void reInitialize() {
        setTranslateX(450-28/2);
        setTranslateY(300-30/2);
        setRotate(0);

        currentAngle = 0;
        movingAngle = 0;
        speed = 0;
        exponent = -1;
    }
}
