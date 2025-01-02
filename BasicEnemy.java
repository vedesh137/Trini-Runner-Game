import javax.swing.JFrame;

public class BasicEnemy extends Enemy {
    public BasicEnemy(JFrame window, TileMap tm) {
        super(window, tm);
        tileMap = tm;	
        this.window = window;

        //Sets the animations.
        loadAnimations();

        //Sets the current animations.
        currAnimation = getcurrAnimation();

        //enemy attributes
        x = 0;
        y = 0;
        Speed = 2;
        currSpeed = Speed;
        knockbackDistance = 5;
        detectionRange = 300;
        ENTITY_DEFAULT_HEALTH = 200;
        health = ENTITY_DEFAULT_HEALTH;
        
    }
}
