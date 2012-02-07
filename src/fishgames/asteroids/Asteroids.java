/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fishgames.asteroids;

import java.util.HashSet;
import java.util.Set;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.World;
import org.lwjgl.LWJGLException;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.*;
import org.lwjgl.opengl.PixelFormat;
import org.lwjgl.util.vector.Vector3f;

public class Asteroids {

    public static World world = new World(new Vec2(0, 0), true);
    public static Set<Renderable> renderable = new HashSet<Renderable>();
    public static long accum = 0;
    public static long last = System.nanoTime();
    public static float timestep = 1.f / 60.f;

    /**
     * Returns the size of the world in meters.
     *
     * @return
     */
    public static Vec2 getWorldSize() {
        return new Vec2(80, 80);
    }

    /**
     * Sets the model-view transform to be equal to the transform of the body,
     * with interpolation by alpha.
     *
     * @param body
     * @param alpha
     */
    public static void setTransform(Body body, float alpha) {
        float angle = body.getAngle();
        float angularVel = body.getAngularVelocity();
        ///float angle1 = angle * (1 - alpha);
        //float angle2 = (angle + angularVel) * (alpha);
        //float finalAngle = angle1 + angle2;
        float finalAngle = angle;

        Vec2 pos = body.getPosition();
        Vec2 linearVel = body.getLinearVelocity();
        Vec2 pos1 = pos.mul(1 - alpha);
        Vec2 pos2 = (pos.add(linearVel.mul(1.f / 60.f))).mul(alpha);
        Vec2 finalPos = pos1.add(pos2);
        glTranslatef(finalPos.x, finalPos.y, 0.f);
        glRotatef((float) (finalAngle * 180.f / Math.PI), 0, 0, 1.f);
        // Rotate around z-axis
    }

    /**
     * Wraps the transform to the size of the map.
     *
     * @param body
     */
    public static void wrapTransform(Body body) {
        Vec2 pos = body.getPosition();
        Vec2 size = getWorldSize();
        if (pos.x > size.x) {
            pos.x = pos.x - size.x;
        } else if (pos.x < 0) {
            pos.x = size.x + pos.x;
        }
        if (pos.y > size.y) {
            pos.y = pos.y - size.y;
        } else if (pos.y < 0) {
            pos.y = size.y + pos.y;
        }
        body.setTransform(pos, body.getAngle());
    }

    /**
     * Update the game state if necessary, and render to the screen.
     */
    public static void update() {
        long increment = (long) (timestep * 1000000000);
        long current = System.nanoTime();

        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        if (Display.isVisible()) {
            accum += current - last;
            int num = 0;
            while (accum >= increment) {
                world.step(timestep, 10, 10);
                accum -= increment;
                num++;
                for (Renderable object : Asteroids.renderable) {
                    object.update();
                }
            }
            float interp = (float) accum / (float) increment;
            for (Renderable object : Asteroids.renderable) {
                object.render(interp);
            }
            Display.update();
        }
        last = current;
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws LWJGLException {
        // TODO code application logic here 
        int width = (int) (getWorldSize().x * 10);
        int height = (int) (getWorldSize().y * 10);
        DisplayMode mode = new DisplayMode(width, height);
        Display.setDisplayMode(mode);
        Display.setFullscreen(false);
        Display.setTitle("Asteroids");
        try {
            Display.create(new PixelFormat(8, 8, 8, 8, 4));
        } catch (LWJGLException ex) {
            Display.create(new PixelFormat(8, 8, 8, 8, 0));
        }

        Keyboard.create();
        Mouse.create();

        glClearColor(0.0f, 0, 0, 1.0f);
        glDisable(GL_DEPTH_TEST);
        glDisable(GL_LIGHTING);

        glMatrixMode(GL_PROJECTION);
        glLoadIdentity();
        glOrtho(0, mode.getWidth(), mode.getHeight(), 0.f, -1.f, 1.f);
        glMatrixMode(GL_MODELVIEW);
        glScalef(10.f, 10.f, 10.f);

        for (int i = 0; i < 8; i++) {
            renderable.add(Rock.getRock(6.f));
        }
        renderable.add(new Starship(new Vector3f(1.0f, 0.f, 0.0f)));
        glEnable(GL_LINE_SMOOTH);
        glEnable(GL_MULTISAMPLE);

        while (!Display.isCloseRequested()) {
            update();
        }
        Display.destroy();
    }
}
