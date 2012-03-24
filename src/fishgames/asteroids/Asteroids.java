/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fishgames.asteroids;

import java.awt.FontFormatException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.channels.Selector;
import java.util.*;
import org.jbox2d.collision.shapes.CircleShape;
import org.jbox2d.collision.shapes.Shape;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.*;
import org.jbox2d.dynamics.contacts.Contact;
import org.lwjgl.LWJGLException;

/**
 * Big huge static class to hold all the game-specific info, like world size,
 * pointers to useful activeEntities, utility methods, etc.
 *
 * @author Matt Fichman <matt.fichman@gmail.com>
 */
public class Asteroids {

    private static Selector selector;
    private static World world = new World(new Vec2(0, 0), true);
    private static Map<Class, Entity> cache = new HashMap<Class, Entity>();
    private static short nextId;
    private static Set<Entity> activeEntities = new HashSet<Entity>();
    private static ArrayList<Entity> workingEntities = new ArrayList<Entity>();
    private static PriorityQueue<Task> activeTasks = new PriorityQueue<Task>();
    private static ArrayList<Task> workingTasks = new ArrayList<Task>();
    private static long accumulator = 0;
    private static long last = System.nanoTime();
    private static float timestep = 1.f / 100.f;
    private static float frameRate = 1.f / 50.f;
    private static float frameInterpolation;
    private static boolean activeEntitiesDirty;
    private static boolean paused;
    private static Peer master;
    private static Random random = new Random();
    public static final int BUFSIZE = 4096;

    /**
     * Returns the selector used for game I/O operations.
     *
     * @return
     */
    public static Selector getSelector() {
        return selector;
    }
    
    public static Peer getMaster() {
        return master;
    }
    
    public static float getTimestep() {
        return timestep;
    }
    
    public static void setMaster(Peer peer) {
        master = peer;
    }

    /**
     * Creates a new object, or retrieves it from the cache if necessary.
     * Objects are linked together in a linked list to save memory allocations
     * of entry types in a linked list.
     *
     * @param <T>
     * @param type
     * @return
     */
    public static <T> T newEntity(Class type) {
        Entity ent = cache.get(type);
        if (ent == null) {
            try {
                ent = (Entity) type.newInstance();
                ent.setId(nextId++);
            } catch (InstantiationException ex) {
                throw new RuntimeException(ex);
            } catch (IllegalAccessException ex) {
                throw new RuntimeException(ex);
            }
        } else {
            cache.put(type, ent.getNext());
        }
        ent.setActive(true);
        return (T) ent;
    }

    /**
     * Releases this object so that it can be re-used later.
     *
     * @param object
     */
    public static void delEntity(Entity object) {
        Entity next = cache.get(object.getClass());
        object.setNext(next);
        cache.put(object.getClass(), object);
    }

    /**
     * Releases an entity, by adding it to the cache and disabling it. This
     * entity will no longer be active in the physical simulation, nor visible
     * to the renderer.
     *
     * @param object
     */
    public static void delActiveEntity(Entity object) {
        activeEntities.remove(object);
        activeEntitiesDirty = true;
        if (object.getPeer() == null) {
            delEntity(object);
        }
    }

    /**
     * Interpolation using the spinor approach for two angles (a spinor is the
     * 2D analog of a quaternion).
     *
     * @param angle1
     * @param angle2
     * @return
     */
    public static float slerp(float angle1, float angle2, float alpha) {
        angle1 = (float) (angle1 % (Math.PI * 2));
        angle2 = (float) (angle2 % (Math.PI * 2));
        double d1 = Math.abs(angle1 - angle2);
        double d2 = Math.abs(angle1 + Math.PI * 2 - angle2);
        double d3 = Math.abs(angle1 - Math.PI * 2 - angle2);
        
        if (d1 < d2) {
            if (d1 < d3) {
                // Do nothing
            } else {
                // Case D3
                angle2 += Math.PI * 2;
            }
        } else {
            if (d2 < d3) {
                // Case D2
                angle1 += Math.PI * 2;
            } else {
                // Case D3
                angle2 += Math.PI * 2;
            }
        }
        
        return (1 - alpha) * angle1 + (alpha) * angle2;
        
        
        /*
        double a1Real = Math.cos(angle1);
        double a1Complex = Math.sin(angle1);
        double a2Real = Math.cos(angle2);
        double a2Complex = Math.sin(angle2);
        
        double cosom = a1Real * a2Real + a1Complex * a2Complex;
        double tc;
        double tr;
        if (cosom < 0) {
            cosom = -cosom;
            tc = -a2Complex;
            tr = -a2Real;
        } else {
            tc = a2Complex;
            tr = a2Real;
        }
        
        // Use linear interpolation if the angles are too close
        Quaternion q;
        double scale0;
        double scale1;
        if ((1 - cosom) > 0.001) {
            double omega = Math.acos(cosom);
            double sinom = Math.sin(omega);
            scale0 = Math.sin((1 - alpha) * omega) / sinom;
            scale1 = Math.sin(alpha * omega) / sinom;
        } else {
            scale0 = 1 - alpha;
            scale1 = alpha;
        }
        double resultComplex = scale0 * a1Complex + scale1 * tc;
        double resultReal = scale0 * a1Real + scale1 * tr;
        return (float) Math.atan2(resultComplex, resultReal) * 2.f;*/
    }

    /**
     * Adds an object to the update and render list for the game. This entity
     * will no longer be active in the physical simulation, nor visible to the
     * renderer.
     *
     * @param obj
     */
    public static void addActiveEntity(Entity object) {
        activeEntities.add(object);
        activeEntitiesDirty = true;
    }

    /**
     * Removes a task from the task list.
     *
     * @param task
     */
    public static void delTask(Task task) {
        activeTasks.remove(task);
    }

    /**
     * Adds a task to the task list. The tasks will be checked and executed each
     * frame if they've reached the deadline.
     */
    public static void addTask(Task task) {
        activeTasks.add(task);
    }

    /**
     * Returns a list of activeEntities entities.
     *
     * @return
     */
    public static Collection<Entity> getActiveEntities() {
        return activeEntities;
    }

    public static PriorityQueue<Task> getActiveTasks() {
        return activeTasks;
    }

    /*
     * Returns the interpolation factor, as a value between 0 and 1. The
     * interpolation factor describes how close the current time is to the next
     * physics timestep, allowing for very smooth interpolated animation.
     */
    public static float getFrameInterpolation() {
        return frameInterpolation;
    }

    /**
     * Returns true if the game is paused.
     *
     * @return
     */
    public static boolean isPaused() {
        return paused;
    }

    /**
     * Pauses or un-pauses the game.
     *
     * @param flag
     */
    public static void setPaused(boolean flag) {
        paused = flag;
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
     * Returns a getRandomInt number between min and max.
     *
     * @param min
     * @param max
     * @return
     */
    public static int getRandomInt(int min, int max) {
        return (Math.abs(random.nextInt()) % (max - min + 1)) + min;
    }

    /**
     * Returns a getRandomInt velocity with magnitude between min and max.
     *
     * @param min
     * @param max
     * @return
     */
    public static Vec2 getRandomVel(float min, float max) {
        float speed = (float) (Math.random() * (max - min)) + min;
        float angle = (float) (2 * Math.PI * Math.random());
        float dx = (float) (speed * Math.cos(angle));
        float dy = (float) (speed * Math.sin(angle));
        return new Vec2(dx, dy);
    }

    /**
     * Returns a getRandomInt position on the map.
     *
     * @return
     */
    public static Vec2 getRandomPos() {
        float x = (float) (Math.random() * Asteroids.getWorldSize().x);
        float y = (float) (Math.random() * Asteroids.getWorldSize().x);
        return new Vec2(x, y);
    }

    /**
     * Returns the world, used to carry out physics calculations (jBox2d).
     *
     * @return
     */
    public static World getWorld() {
        return world;
    }

    /**
     * Returns a Body that was created with a fixture for each shape in polygon.
     * Also sets various other properties of Body all at once.
     *
     * @param polygon
     * @param type
     * @param mask
     * @param density
     * @return
     */
    public static Body getBody(Polygon polygon, int type, int mask, float density) {
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyType.DYNAMIC;
        bodyDef.active = false;
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
     * Returns a body with a single circle shape fixture. Also sets various
     * other properties of Body all at once.
     *
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
        bodyDef.active = false;
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
     * Steps the physics simulation forward once, by running jBox2d forward a
     * timestep, and then checking the contact list. Also calls update() for
     * each object in the update list.
     */
    public static void step() {
        getWorld().step(timestep, 8, 3);
        for (Contact c = getWorld().getContactList(); c != null; c = c.getNext()) {
            Object a = c.getFixtureA().getBody().getUserData();
            Object b = c.getFixtureB().getBody().getUserData();
            if (a != null && b != null && c.isTouching()) {
                Functor ca = (Functor) a;
                Functor cb = (Functor) b;
                ca.dispatch((Entity) cb);
                cb.dispatch((Entity) ca);
            }
        }
        for (Entity object : workingEntities) {
            object.update(timestep);
        }
    }

    /**
     * Updates all activeTasks. Find any activeTasks whose deadline has been
     * reached, and executes those activeTasks.
     */
    public static void update() throws InterruptedException, IOException {
        long increment = (long) (timestep * 1000000000);
        long time = System.nanoTime();
        long nextFrame = (long) (frameRate * 1000000000) + time;
        if (isPaused()) {
            last = time;
            return;
        }

        // Update the workingEntities set.  If the set is dirty, then clear it and
        // rebuild the list of objects.
        if (activeEntitiesDirty) {
            activeEntitiesDirty = false;
            workingEntities.clear();
            workingEntities.addAll(Asteroids.activeEntities);
        }

        // Update all activeEntities and activeTasks in the task queue.
        accumulator += time - last;
        while (accumulator >= increment) {
            step();
            accumulator -= increment;
        }
        last = time;
        frameInterpolation = (float) accumulator / (float) increment;


        // Update all current activeTasks 
        long nextDeadline = Long.MAX_VALUE;
        while (!activeTasks.isEmpty() && activeTasks.peek().getDeadline() <= time) {
            Task task = activeTasks.remove();


            if (task.update()) {
                task.setDeadline();
                workingTasks.add(task);
                if (task.getTimeout() != 0.f) {
                    nextDeadline = Math.min(nextDeadline, task.getDeadline());
                }
            } else {
                task.setDeadline(0);
            }
        }
        if (!activeTasks.isEmpty()) {
            nextDeadline = Math.min(nextDeadline, activeTasks.peek().getDeadline());
        }

        // Add any tasks that ran in this frame and were re-scheduled to the
        // priority queue.
        for (Task task : workingTasks) {
            activeTasks.add(task);
        }
        workingTasks.clear();

        // Sleep until the next task
        long wakeTime = Math.min(nextFrame, nextDeadline);
        long sleepTime = (wakeTime - System.nanoTime()) / 1000000;
        if (sleepTime > 0) {
            //Thread.sleep(sleepTime / 1000000);
            selector.select(sleepTime);
        }
    }

    /**
     * Runs the game.
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) throws LWJGLException, IOException, FontFormatException, InterruptedException {
        selector = Selector.open();
        Entity.addType(Rock.Large.class);
        Entity.addType(Rock.Medium.class);
        Entity.addType(Rock.Small.class);
        Entity.addType(Starship.class);
        Entity.addType(Photon.class);
        Entity.addType(Player.class);
        //Entity.addType(Explosion.class);
        //Entity.addType(Debris.class);


        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        System.out.print("Master or client? ");
        String answer = reader.readLine();


        if (answer.equalsIgnoreCase("master") || answer.equalsIgnoreCase("m")) {
            //addActiveEntity(new Starship(new Vector3f(1.0f, 0.f, 0.0f)));

            addTask(new Network(true));
            for (int i = 0; i < 8; i++) {
                Rock.Large.getRock(Asteroids.getRandomPos());
            }

        } else {
            Player player = Asteroids.newEntity(Player.class);
            player.setActive(true);
            addTask(new Renderer());
            addTask(new Network(false));
        }

        //Starship starship = new Starship(new Vector3f(1.0f, 0.f, 0.0f));
        //starship.setActive(true);
        //Player player = Asteroids.newEntity(Player.class);
        //player.setActive(true);
        //player.setSerializable(false);

        //addTask(new Renderer());

        while (true) {
            //player.setInputFlags(player.getInputFlags());
            update();
        }
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
