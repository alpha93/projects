import javafx.scene.effect.Bloom;
import javafx.scene.effect.Glow;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

public class Beam extends Circle {
    private double movingAngle;
    private boolean alive = true;

    Beam(double spawnPositionX, double spawnPositionY, double movingAngle) {
        super(1.2);
        setFill(Color.web("0eb8ff"));
        this.movingAngle = movingAngle;

        setCenterX(spawnPositionX);
        setCenterY(spawnPositionY);
    }

    void move() {
        setCenterX(getCenterX() + 10*Math.sin(Math.toRadians(movingAngle)));
        setCenterY(getCenterY() - 10*Math.cos(Math.toRadians(movingAngle)));

        if(getCenterX() < 0  ||  getCenterX() > 900  ||  getCenterY() < 0  ||  getCenterY() > 600)
            alive = false;
    }

    boolean isAlive() {
        return alive;
    }

    void setDead() {
        alive = false;
    }
}