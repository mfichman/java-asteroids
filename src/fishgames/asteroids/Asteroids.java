/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fishgames.asteroids;

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
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.GL_MULTISAMPLE;
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
    private static Set<Renderable> renderables = new HashSet<Renderable>();
    private static ArrayList<Renderable> working = new ArrayList<Renderable>();
    private static long accum = 0;
    private static long last = System.nanoTime();
    private static float timestep = 1.f / 100.f;
    private static boolean renderablesDirty = false;
    private static Random random = new Random();
    
    public static int random(int min, int max) {
        return (Math.abs(random.nextInt()) % (max - min + 1)) + min;
    }
    
    /**
     * 
     * @param renderable 
     */
    public static void add(Renderable renderable) {
        renderables.add(renderable);
        renderablesDirty = true;
    }
    
    /*
     * 
     */
    public static void remove(Renderable renderable) {
        renderables.remove(renderable);
        renderablesDirty = true;
    }
    
    /**
     * Returns the size of the world in world units (kind-of-meters).
     *
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
     * Creates a new body using this shape.
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
     * Returns a circle body.
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
        long time = System.nanoTime();

        if (!Display.isVisible()) {
            last = time;
            return;
        }
        
        if (renderablesDirty) {
            renderablesDirty = false;
            working.clear();
            working.addAll(Asteroids.renderables);
        }

        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        // Update all objects and tasks in the task queue.
        accum += time - last;
        while (accum >= increment) {
            getWorld().step(timestep, 8, 3);
            accum -= increment;
            for (Contact c = getWorld().getContactList(); c != null; c = c.getNext()) {
                Object a = c.getFixtureA().getBody().getUserData();
                Object b = c.getFixtureB().getBody().getUserData();
                if (a != null && b != null && c.isTouching()) {
                    Collidable ca = (Collidable) a;
                    Collidable cb = (Collidable) b;
                    ca.dispatch(cb);
                    cb.dispatch(ca);
                }
            }
            for (Renderable object : working) {
                object.update(timestep);
            }

        }

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

        // Render all objects; be sure to pass the 'frame' remainder'
        float interp = (float) accum / (float) increment;
        for (Renderable object : working) {
            object.render(interp);
        }

        Display.update();
        Display.sync(50);
        
        last = time;
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
            Rock.getRock(Rock.LARGE);
        }
        add(new Starship(new Vector3f(1.0f, 0.f, 0.0f)));
        glEnable(GL_LINE_SMOOTH);
        glEnable(GL_MULTISAMPLE);

        while (!Display.isCloseRequested()) {
            update();
        }
        Display.destroy();
    }

    /**
     * @return the world
     */
    public static World getWorld() {
        return world;
    }
}
