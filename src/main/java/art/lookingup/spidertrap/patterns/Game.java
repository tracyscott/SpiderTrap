package art.lookingup.spidertrap.patterns;

import static processing.core.PConstants.CLAMP;
import static processing.core.PConstants.P2D;

import art.lookingup.spidertrap.SpiderTrapApp;
import art.lookingup.spidertrap.SpiderTrapModel;
import heronarts.lx.LX;
import heronarts.lx.LXCategory;
import heronarts.lx.parameter.CompoundParameter;
import art.lookingup.pacman.PacmanBoard;
import art.lookingup.pacman.PacmanGame;
import art.lookingup.pacman.PacmanSprite;
import processing.core.PImage;

@LXCategory(LXCategory.FORM)
public class Game extends PGPixelPerfect {
    // Speed determines the overall speed of the entire pattern.
    public final CompoundParameter speedKnob =
        new CompoundParameter("Speed", 1, 0, 4).setDescription("Speed");

    public static final int BLOCK_PIXELS = PacmanBoard.BLOCK_PIXELS;
    public static final int BOARD_HEIGHT = PacmanBoard.BOARD_HEIGHT;
    public static final int BOARD_WIDTH = PacmanBoard.BOARD_WIDTH;

    public final static float HALF_WIDTH = BOARD_WIDTH / 2f;
    public final static float HALF_HEIGHT = BOARD_HEIGHT / 2f;

    public final static float FULL_HEIGHT = BOARD_HEIGHT;
    public final static float FULL_WIDTH = BOARD_WIDTH;

    // 10 minute game max.
    public final static float MAX_GAME_MILLIS = 600000;

    float telapsed;

    PacmanBoard board;
    PacmanGame game;
    PacmanSprite pac;
    PImage gboard;
    //PImage ctexture;

    float rotation;
    float rotSpeed;

    // float rainbowLX;
    // float rainbowLY;
    // float rainbowRX;
    // float rainbowRY;

    // float rainbowYOffset;
    // float rainbowLROffset;

    public Game(LX lx) {
        super(lx, "");

	this.board = new PacmanBoard(SpiderTrapApp.pApplet);
	this.telapsed = 0;
	this.board = new PacmanBoard(SpiderTrapApp.pApplet);
	this.pac = new PacmanSprite(SpiderTrapApp.pApplet);
	this.game = new PacmanGame(SpiderTrapApp.pApplet, this.board, this.pac);
	//this.ctexture = SpiderTrapApp.pApplet.loadImage("images/xyz-square-lookup.png");
	//this.ctexture.loadPixels();

	//pg.textureWrap(CLAMP);

        removeParameter(fpsKnob);
        addParameter(speedKnob);
    }

    void setControlPoints(float aX, float aY, float bX, float bY, float D) {
	// float minTheta = (float) Math.PI / 8;
	// float maxTheta = (float) Math.PI / 2;

	// float dFraction = D / PacmanBoard.MAX_DISTANCE;

	// float theta = minTheta + (maxTheta - minTheta) * dFraction;

	// float middleRadius = SpiderTrapModel.innerRadius + 15 * SpiderTrapModel.radiusInc;
	// float angle = (float) Math.PI / 2 - theta;

	// float sin = (float) Math.sin(angle);
	// float cos = (float) Math.cos(angle);

	// TODO vary the center-line angle based on a/b X/Y values
	// proximity to exterior border.

        // rainbowLX = canvas.map.subXi(-middleRadius * cos);
	// rainbowLY = canvas.map.subYi(+middleRadius * sin);
	// rainbowRX = canvas.map.subXi(+middleRadius * cos);
	// rainbowRY = canvas.map.subYi(+middleRadius * sin);

	// rainbowYOffset =
	//     (canvas.map.subYi(SpiderTrapModel.innerRadius + 30 * SpiderTrapModel.radiusInc) -
	//      canvas.map.subYi(SpiderTrapModel.innerRadius)) / 2;

	// float dX = rainbowLX - rainbowRX;
	// rainbowLROffset = (float) Math.sqrt(dX * dX);
    }

    public void draw(double deltaMs) {

	double speed = speedKnob.getValue();
	telapsed += (float) (deltaMs * speed);

        if (game.finished() || this.telapsed > (float)(speed * MAX_GAME_MILLIS)) {
            this.board.reset();
            this.telapsed = (float)(deltaMs * speed);
            game = new PacmanGame(SpiderTrapApp.pApplet, this.board, this.pac);
        }
        game.render(telapsed, null);
        gboard = game.get();

        if (game.pacTicks == 0) {
            draw1(deltaMs);
            return;
        }
        pg.background(0);

	pg.translate(256, 256);
	pg.rotate(rotation);
	pg.translate(-256, -256);
	
        boolean pacIsRight = false;

        float aX = game.pacX();
        float aY = game.pacY();

        float bX = game.redX();
        float bY = game.redY();

        if (aX > bX || (aX == bX && bY > aY)) {
            float tX, tY;

            tX = bX;
            tY = bY;

            bX = aX;
            bY = aY;

            aX = tX;
            aY = tY;

            pacIsRight = true;
        }

        float aD = (float) Math.sqrt(aX * aX + aY * aY);
        float bD = (float) Math.sqrt(bX * bX + bY * bY);

        float dX = aX - bX;
        float dY = aY - bY;

        float dAB = (float)Math.sqrt(dX * dX + dY * dY);


	float dRatio;
	if (dAB < 24) {
	    dRatio = 256f/24f;
	} else if (dAB < 256) {
	    dRatio = 256f/dAB;
	} else {
	    dRatio = 0.75F * PacmanBoard.MAX_DISTANCE / dAB;
	}

        //setControlPoints(aX, aY, bX, bY, dAB);

	// Render 

	pg.translate(128, 256);
        // pg.translate(rainbowLX, rainbowLY);

        // Note: this determines whether Pac is on the right or the left side.
        if (!pacIsRight) {
            float xoffset = 256 / 2;
            pg.translate(xoffset, 0);
            pg.rotate((float) -Math.PI);
            pg.translate(-xoffset, 0);
        }

	if (pacIsRight) {
	    rotSpeed += Math.PI/100000f;
	    rotSpeed = Math.min(rotSpeed, (float)Math.PI/10000f);
	} else {
	    rotSpeed -= Math.PI/100000f;
	    rotSpeed = Math.max(rotSpeed, -(float)Math.PI/10000f);
	}

	rotation += rotSpeed * (float)deltaMs;

        float rr;
        if (dX == 0) {
            rr = (float) Math.PI * 3 / 2;
        } else {
            rr = (float) Math.atan(dY / dX);
        }

        pg.rotate((float)Math.PI * 2f - rr);

        pg.scale(dRatio, dRatio);

	// pg.translate(128, 0);

	// pg.translate(-128, 0);

        pg.translate(-aX, -aY);

        // if (game.collision()) {
        //     pg.scale(((float)gboard.width / (float)ctexture.width),
        //              ((float)gboard.height / (float)ctexture.height));
        //     pg.image(ctexture, 0, 0);
        // } else {
            pg.image(gboard, 0, 0);
        // }
    }

    // Pacman stays on screen w/ this draw()
    public void draw1(double deltaMs) {
        pg.background(0);

        float pacX = game.pacX();
        float pacY = game.pacY();

        // float redX = game.redX();
        // float redY = game.redY();

        // float dX = pacX - pacX;
        // float dY = pacY - pacY;
        // float dAB = (float)Math.sqrt(dX * dX + dY * dY);
        
        // setControlPoints(pacX, pacY, redX, redY, dAB);
        
        float xratio = (float) 512 / (float) gboard.width;

        pg.translate(0, -256);
        pg.scale(xratio, xratio);
        pg.translate(0, -pacY);

        // the READY! text is 6 blocks above the start position, pan the camera down.
        if (telapsed < PacmanGame.STANDSTILL_MILLIS) {
            if (telapsed < PacmanGame.READY_MILLIS) {
                pg.translate(0, PacmanBoard.BLOCK_PIXELS * 6f);
            } else {
                float ratio = (telapsed - PacmanGame.READY_MILLIS) /
                    (PacmanGame.STANDSTILL_MILLIS - PacmanGame.READY_MILLIS);
                pg.translate(0, (1 - ratio) * PacmanBoard.BLOCK_PIXELS * 6f);
            }
        }

        pg.image(gboard, 0, 0);
    }
}
