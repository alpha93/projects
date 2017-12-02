import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.effect.Glow;
import javafx.scene.effect.Light;
import javafx.scene.effect.Lighting;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.media.AudioClip;
import javafx.scene.paint.Color;
import javafx.scene.shape.Shape;
import javafx.scene.text.*;
import javafx.stage.Stage;


import java.util.ArrayList;

public class Main extends Application {
    private ArrayList<Rock> rocks = new ArrayList<>();
    private ArrayList<Beam> beams = new ArrayList<>();
    private SpaceShip spaceShip = new SpaceShip();;
    private Group group;
    private Text score, gameOver, playerWon;
    private ImageView playAgain = new ImageView(Main.class.getResource("replay1.png").toString());
    private ImageView exit = new ImageView(Main.class.getResource("exit1.png").toString());
    private FlowPane icons;
    private final int SCENE_WIDTH = 900, SCENE_HEIGHT = 600;
    private final int ROCK_COUNT = 20;
    private boolean upKeyPressed, upKeyReleased, zKeyPressed, leftKeyPressed, rightKeyPressed;
    private boolean skip = true;
    private int bulletsFired = 0, skipCount = 10, scoreCount = 0;
    private SimpleIntegerProperty crashCount = new SimpleIntegerProperty(0);
    private SimpleIntegerProperty rocksDestroyed = new SimpleIntegerProperty(0);
    private AudioClip explosion = new AudioClip(Main.class.getResource("explosion.wav").toString());
    private AudioClip destroy = new AudioClip(Main.class.getResource("destroy.wav").toString());
    private AudioClip beamFire = new AudioClip(Main.class.getResource("beam_fire.wav").toString());
    private long currentTime;
    private int respawnBlinkCount;
    private boolean flip, showRespawnAnimation;

    public static void main(String args[]) {
        launch();
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        ImageView spaceBackground = new ImageView("space.jpg");
        spaceBackground.setFitHeight(SCENE_HEIGHT);
        spaceBackground.setFitWidth(SCENE_WIDTH);

        group = new Group(spaceBackground);
        Scene scene = new Scene(group, SCENE_WIDTH, SCENE_HEIGHT);

        // add the space ship to the scene
        group.getChildren().add(spaceShip);

        // initialize all the game objects
        initializeGameObjects();

        AnimationTimer updater = new AnimationTimer() {
            @Override
            public void handle(long now) {
                updateGameObjects();
            }
        };

        // add event listeners for the spaceShip controls
        scene.setOnKeyPressed((keyEvent) -> {
            switch(keyEvent.getCode()) {
                case UP:
                    upKeyPressed = true;
                    break;
                case Z:
                    zKeyPressed = true;
                    break;
                case LEFT:
                    leftKeyPressed = true;
                    break;
                case RIGHT:
                    rightKeyPressed = true;
            }
        });

        scene.setOnKeyReleased((keyEvent) -> {
            switch(keyEvent.getCode()) {
                case UP:
                    upKeyPressed = false;
                    upKeyReleased = true;
                    break;
                case Z:
                    zKeyPressed = false;
                    break;
                case LEFT:
                    leftKeyPressed = false;
                    break;
                case RIGHT:
                    rightKeyPressed = false;
            }
        });

        // listener to check if the game is over depending on the space ship crash count
        crashCount.addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                if((Integer)newValue == 3) {
                    gameOver.setVisible(true);
                    icons.setVisible(true);
                    updater.stop();
                }
            }
        });

        // listener to check if the player has won
        rocksDestroyed.addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                if((Integer)newValue == 20) {
                    playerWon.setVisible(true);
                    icons.setVisible(true);
                    updater.stop();
                }
            }
        });

        // apply hover effect to the icons
        EventHandler<MouseEvent> hoverEffect = new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                ImageView clickedIcon = (ImageView)event.getSource();

                if(event.getEventType().getName().equals("MOUSE_ENTERED"))
                    clickedIcon.setEffect(new Glow(5));
                else if(event.getEventType().getName().equals("MOUSE_EXITED"))
                    clickedIcon.setEffect(null);
            }
        };
        playAgain.setOnMouseEntered(hoverEffect);
        exit.setOnMouseEntered(hoverEffect);
        playAgain.setOnMouseExited(hoverEffect);
        exit.setOnMouseExited(hoverEffect);

        // add listener to the play again icon
        playAgain.setOnMouseClicked((mouseEvent) -> {
            cleanUp();
            initializeGameObjects();
            System.gc();    // run the garbage collector
            updater.start();
        });

        // add listener to the exit icon
        exit.setOnMouseClicked((mouseEvent) -> {
            primaryStage.close();
        });

        primaryStage.setScene(scene);
        primaryStage.setTitle("Asteroids");
        primaryStage.setResizable(false);
        primaryStage.sizeToScene(); // the absence of this method would cause the extra space problem on the right and bottom that Stage.setResizable(false) causes in jre-8
        primaryStage.getIcons().add(new Image(Main.class.getResource("icon.png").toString()));
        primaryStage.setAlwaysOnTop(true);
        primaryStage.show();

        updater.start();
    }

    private void initializeGameObjects() {
        // reset crashCount and rocksDestroyed
        crashCount.set(0);
        rocksDestroyed.set(0);

        // reposition  the spaceship to the center
        spaceShip.reInitialize();

        // initialize variables for respawning the spaceShip
        showRespawnAnimation = true;
        flip = true;
        respawnBlinkCount = 0;
        currentTime = System.currentTimeMillis();

        // initialize the Rock ArrayList
        for(int i=0; i<ROCK_COUNT; i++) {
            Rock rock = new Rock();
            rocks.add(rock);
            group.getChildren().add(rock);
        }

        // add the score Text
        score = new Text("00");
        score.setOpacity(0.5);
        score.setFont(Font.font("consolas", FontWeight.BOLD, FontPosture.REGULAR, 30));
        score.setFontSmoothingType(FontSmoothingType.LCD);
        score.setTextOrigin(VPos.TOP);
        score.setX(10);
        score.setY(10);
        score.setFill(Color.WHITE);

        // game over Text
        gameOver = new Text("Game Over");
        gameOver.setEffect(new Glow(35));
        gameOver.setTextOrigin(VPos.TOP);
        gameOver.setFont(Font.font("helvetica", FontWeight.EXTRA_BOLD, 120));
        gameOver.setX(450 - gameOver.getLayoutBounds().getWidth()/2);
        gameOver.setY(150);
        gameOver.setFill(Color.ORANGE);
        gameOver.setVisible(false);

        // player won Text
        playerWon = new Text("You Won");
        playerWon.setEffect(new Glow(35));
        playerWon.setTextOrigin(VPos.TOP);
        playerWon.setFont(Font.font("helvetica", FontWeight.EXTRA_BOLD, 120));
        playerWon.setX(450 - playerWon.getLayoutBounds().getWidth()/2);
        playerWon.setY(150);
        playerWon.setFill(Color.ORANGE);
        playerWon.setVisible(false);


        // customize the play again and exit icons and add them to a Group
        playAgain.setOpacity(0.7);
        exit.setOpacity(0.7);
        icons = new FlowPane();
        icons.setHgap(40);
        icons.getChildren().addAll(playAgain, exit);
        icons.setLayoutX(450 - 140 - 20);
        icons.setLayoutY(300);
        icons.setVisible(false);

        // add everything to the root node
        group.getChildren().addAll(score, gameOver, playerWon, icons);
    }

    private void cleanUp() {
        // remove the rocks
        for(Rock rock: rocks)
            rock.setVisible(false);

        rocks.clear();

        //remove the beams
        for(Beam beam: beams)
            beam.setVisible(false);

        beams.clear();

        // remove the score board and reset the scoreCount
        group.getChildren().remove(score);
        scoreCount = 0;

        // remove the game over and player won Texts
        group.getChildren().remove(gameOver);
        group.getChildren().remove(playerWon);
        group.getChildren().remove(icons);
    }

    private void updateGameObjects() {
        //long start = System.currentTimeMillis();
        // move the rocks
        for(Rock rock: rocks) {
            rock.move(rocks);
        }

        // check for collision among rocks
        for(int i=0; i<rocks.size(); i++) {
            for(int j=i+1; j<rocks.size(); j++) {
                Rock rock1 = rocks.get(i), rock2 = rocks.get(j);

                // if two rocks collide, interchange their speeds
                if(rock1.getBoundsInParent().intersects(rock2.getBoundsInParent())) {
                    int tmpSpeedX = rock1.getSpeedX();
                    int tmpSpeedY = rock1.getSpeedY();

                    rock1.setSpeedX(rock2.getSpeedX());
                    rock1.setSpeedY(rock2.getSpeedY());

                    rock2.setSpeedX(tmpSpeedX);
                    rock2.setSpeedY(tmpSpeedY);
                }
            }
        }

        // control the spaceShip
        if(upKeyPressed) {
            spaceShip.accelerate();
            //System.out.println(spaceShip.getSpeed());
        }
        else if(upKeyReleased) {
            if(spaceShip.getSpeed() > 0)
                spaceShip.decelerate();
            else {
                spaceShip.nullifySpeed();
                upKeyReleased = false;
            }
            //System.out.println(spaceShip.getSpeed());
        }

        if(leftKeyPressed)
            spaceShip.rotateLeft();
        if(rightKeyPressed)
            spaceShip.rotateRight();
        if(zKeyPressed) {
            if(bulletsFired < 4  &&  !skip) {
                beams = spaceShip.fire(group);
                // play the beam fire sound
                beamFire.play(0.04, 0, 1.5, 0, 1);
                bulletsFired++;
                skipCount = 15;
            } else {
                skipCount--;

                if(skipCount == 0)
                    bulletsFired = 0;
            }

            skip = !skip;
        }

        // move the beams
        for(int i=0; i<beams.size(); i++) {
            Beam beam = beams.get(i);

            if(!beam.isAlive()) {
                beams.remove(beam);
                continue;
            }

            beam.move();
        }

        // check if the ship hits a rock only if the ship isn't just respawned
        if(!showRespawnAnimation) {
            for(int i=0; i<rocks.size(); i++) {
                Rock rock = rocks.get(i);
                Bounds spaceShipBounds = spaceShip.getBoundsInParent();

                if(rock.getBoundsInParent().intersects(spaceShipBounds.getMinX()+6,
                        spaceShipBounds.getMinY()+6, 16, 16)) {
                    rock.setVisible(false);
                    rocks.remove(rock);

                    crashCount.set(crashCount.get() + 1);
                    rocksDestroyed.set(rocksDestroyed.get() + 1);

                    currentTime = System.currentTimeMillis();
                    showRespawnAnimation = true;

                    explosion.play(0.04, 0, 1.5, 0, 2);
                }
            }
        }

        // if the spaceShip hits a rock, show respawn animation depending on the value of showRespawnAnimation
        if(showRespawnAnimation  &&  respawnBlinkCount <= 11) {
            if(System.currentTimeMillis() - currentTime < 200) {
                if(flip)
                    spaceShip.setOpacity(0.5);
                else
                    spaceShip.setOpacity(1);
            } else {
                flip = ! flip;
                currentTime = System.currentTimeMillis();
                respawnBlinkCount++;
            }
        } else {
            showRespawnAnimation = false;
            respawnBlinkCount = 0;
        }

        // check if a beam hits a rock
        for(int i=0; i<beams.size(); i++) {
            Beam beam = null;
            Rock rock = null;

            for(int j=0; j<rocks.size(); j++) {
                try {
                    beam = beams.get(i);
                    rock = rocks.get(j);
                } catch(IndexOutOfBoundsException e) {
                    continue;
                }

                if(beam.getBoundsInParent().intersects(rock.getBoundsInParent())) {
                    rock.setVisible(false);
                    rocks.remove(rock);
                    beam.setVisible(false);
                    beams.remove(beam);

                    scoreCount += 10;
                    score.setText(scoreCount + "");

                    rocksDestroyed.set(rocksDestroyed.get() + 1);

                    destroy.play(0.06, 0, 1, 0, 1);
                }
            }
        }

        //System.out.println(System.currentTimeMillis() - start);
    }
}
