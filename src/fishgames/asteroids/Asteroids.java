/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fishgames.asteroids;

import java.awt.FontFormatException;
import java.io.IOException;
import java.util.*;
import org.jbox2d.collision.shapes.CircleShape;
import org.jbox2d.collision.shapes.Shape;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.*;
import org.jbox2d.dynamics.contacts.Contact;
import org.lwjgl.LWJGLException;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.PixelFormat;
import org.lwjgl.util.vector.Vector3f;

/**
 * Big huge static class to hold all the game-specific info, like world size,
 * pointers to useful objects, utility methods, etc.
 *
 * @author Matt Fichman <matt.fichman@gmail.com>
 */
public class Asteroids {

    private static World world = new World(new Vec2(0, 0), true);
    private static Renderer renderer;
    private static Set<Object> objects = new HashSet<Object>();
    private static ArrayList<Object> working = new ArrayList<Object>();
    private static long accum = 0;
    private static long last = System.nanoTime();
    private static float timestep = 1.f / 100.f;
    private static boolean objectsDirty = false;
    private static Random random = new Random();

    /**
     * Returns a random number between min and max.
     * @param min
     * @param max
     * @return 
     */
    public static int random(int min, int max) {
        return (Math.abs(random.nextInt()) % (max - min + 1)) + min;
    }

    /**
     * Adds an object to the update and render list for the game.
     * @param obj 
     */
    public static void add(Object obj) {
        objects.add(obj);
        objectsDirty = true;
    }

    /**
     * Removes an object from the update and render list for the game.  The
     * change to the list will be reflected at the next frame.
     * @param obj 
     */
    public static void remove(Object obj) {
        objects.remove(obj);
        objectsDirty = true;
    }

    /**
     * Returns all objects in the update and render list.
     * @return 
     */
    public static ArrayList<Object> getObjects() {
        return working;
    }

    /**
     * Returns the size of the world in world units (kind-of-meters).
     * @return
     */
    public static Vec2 getWorldSize() {
        return new Vec2(80, 80);
    }

    /**
     * Returns a random position on the map.
     * @return
     */
    public static Vec2 getRandomPos() {
        float x = (float) (Math.random() * Asteroids.getWorldSize().x);
        float y = (float) (Math.random() * Asteroids.getWorldSize().x);
        return new Vec2(x, y);
    }

    /**
     * Returns the world, used to carry out physics calculations (jBox2d).
     * @return 
     */
    public static World getWorld() {
        return world;
    }

    /**
     * Returns a Body that was created with a fixture for each shape in polygon.
     * Also sets various other properties of Body all at once.
     * @param polygon
     * @param type
     * @param mask
     * @param density
     * @return 
     */
    public static Body getBody(Polygon polygon, int type, int mask, float density) {
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyType.DYNAMIC;
        Body body = Asteroids.getWorld().createBody(bodyDef);
        if (polygon.getShapes() != null) {
            for (Shape shape : polygon.getShapes()) {
                Fixture fixture = body.createFixture(shape, density);
                Filter filter = new Filter();
                filter.categoryBits = type;
                filter.maskBits = mask;
                fixture.setFilterData(filter);
            }
        }
        body.setLinearDamping(0.f);
        return body;
    }

    /**
     * Returns a body with a single circle shape fixture.  Also sets various
     * other properties of Body all at once.
     * @param radius
     * @param type
     * @param mask
     * @param density
     * @return 
     */
    public static Body getBody(float radius, int type, int mask, float density) {
        CircleShape shape = new CircleShape();
        shape.m_radius = radius;
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyType.DYNAMIC;
        Body body = Asteroids.getWorld().createBody(bodyDef);
        Fixture fixture = body.createFixture(shape, density);
        Filter filter = new Filter();
        filter.categoryBits = type;
        filter.maskBits = mask;
        fixture.setFilterData(filter);
        return body;
    }

    /**
     * Wraps the transform to the size of the map.
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
     * Steps the physics simulation forward once, by running jBox2d forward a
     * timestep, and then checking the contact list.  Also calls update() for
     * each object in the update list.
     */
    public static void step() {
        getWorld().step(timestep, 8, 3);
        for (Contact c = getWorld().getContactList(); c != null; c = c.getNext()) {
            java.lang.Object a = c.getFixtureA().getBody().getUserData();
            java.lang.Object b = c.getFixtureB().getBody().getUserData();
            if (a != null && b != null && c.isTouching()) {
                Functor ca = (Functor) a;
                Functor cb = (Functor) b;
                ca.dispatch((Object) cb);
                cb.dispatch((Object) ca);
            }
        }
        for (Object object : working) {
            object.update(timestep);
        }
    }

    /**
     * Updates all tasks. Find any tasks whose deadline has been reached, and
     * executes those tasks.
     */
    public static void updateTasks() {
        long time = System.nanoTime();

        // Update all current tasks
        PriorityQueue<Task> tasks = Task.getActiveTasks();
        while (!tasks.isEmpty() && tasks.peek().getDeadline() <= time) {
            Task task = tasks.remove();
            if (task.update()) {
                task.setDeadline();
                tasks.add(task);
            } else {
                task.setDeadline(0);
            }
        }
    }

    /**
     * Update the game state if necessary, and render to the screen.  Update the
     * working object array if the object set was dirty.
     */
    public static void updateGame() {
        long increment = (long) (timestep * 1000000000);
        long time = System.nanoTime();

        if (!Display.isVisible()) {
            last = time;
            return;
        }

        if (objectsDirty) {
            objectsDirty = false;
            working.clear();
            working.addAll(Asteroids.objects);
        }

        // Update all objects and tasks in the task queue.
        accum += time - last;
        while (accum >= increment) {
            step();
            accum -= increment;
        }

        // Render all objects; be sure to pass the 'frame' remainder'
        renderer.render((float) accum / (float) increment);

        Display.update();
        Display.sync(50);
        last = time;
    }

    /**
     * Runs the game.
     * @param args the command line arguments
     */
    public static void main(String[] args) throws LWJGLException, IOException, FontFormatException {
        // TODO code application logic here
        int width = (int) (Asteroids.getWorldSize().x * 10);
        int height = (int) (Asteroids.getWorldSize().y * 10);
        DisplayMode mode = new DisplayMode(width, height);
        Display.setDisplayMode(mode);
        Display.setFullscreen(false);
        Display.setTitle("Asteroids");
        try {
            Display.create(new PixelFormat(8, 8, 8, 8, 4));
        } catch (LWJGLException ex) {
            Display.create(new PixelFormat(8, 8, 8, 8, 0));
        }

        renderer = new Renderer();

        Keyboard.create();
        Mouse.create();

        for (int i = 0; i < 8; i++) {
            Rock.getRock(Rock.LARGE);
        }
        add(new Starship(new Vector3f(1.0f, 0.f, 0.0f)));
        while (!Display.isCloseRequested()) {
            updateTasks();
            updateGame();
        }
        Display.destroy();
    }
}

/*
 * static TrueTypeFont font; URL url =
 * Asteroids.class.getResource("/fishgames/asteroids/fonts/Russel.ttf"); Font
 * awtFont = Font.createFont(Font.TRUETYPE_FONT, url.openStream()); awtFont =
 * awtFont.deriveFont(Font.PLAIN, 60.f); font = new TrueTypeFont(awtFont, true);
 */

/*
 * glLoadIdentity(); //Color.white.bind(); glEnable(GL_BLEND);
 * glEnable(GL_TEXTURE_2D); glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
 * font.drawString(20, 300, "extreme asteroids", Color.white);
 * glDisable(GL_TEXTURE_2D); glDisable(GL_BLEND);
 */
