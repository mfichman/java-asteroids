/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fishgames.asteroids;

import java.awt.FontFormatException;
import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
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
 * pointers to useful active, utility methods, etc.
 *
 * @author Matt Fichman <matt.fichman@gmail.com>
 */
public class Asteroids {

    private static World world = new World(new Vec2(0, 0), true);
    private static Factory factory = new Factory();
    private static Renderer renderer;
    private static Serializer serializer = new Serializer();
    private static Deserializer deserializer = new Deserializer();
    private static Selector selector;
    private static SocketAddress address;
    private static Map<SocketAddress, Peer> peerByAddress = new HashMap<SocketAddress, Peer>();
    private static Set<Entity> active = new HashSet<Entity>();
    private static ArrayList<Entity> working = new ArrayList<Entity>();
    private static long accum = 0;
    private static long last = System.nanoTime();
    private static float timestep = 1.f / 100.f;
    private static boolean activeDirty = false;
    private static Random random = new Random();
    private static byte[] tempArray = new byte[1024];
    public static final int BUFSIZE = 4096;
    public static final byte MESSAGE_DATA = 2;
    public static final byte MESSAGE_NEW = 3;

    /**
     * Returns a random number between min and max.
     *
     * @param min
     * @param max
     * @return
     */
    public static int random(int min, int max) {
        return (Math.abs(random.nextInt()) % (max - min + 1)) + min;
    }
    
    /**
     * Creates a new entity with the given class.
     * @param <T>
     * @param type
     * @return 
     */
    public static <T> T newEntity(Class type) {
        return factory.newEntity(type);
    }

    /**
     * Adds an object to the update and render list for the game.
     *
     * @param obj
     */
    public static void addActiveObject(Entity obj) {
        active.add(obj);
        activeDirty = true;
    }

    /**
     * Removes an object from the update and render list for the game. The
     * change to the list will be reflected at the next frame.
     *
     * @param obj
     */
    public static void removeActiveObject(Entity obj) {
        active.remove(obj);
        activeDirty = true;
    }

    /**
     * Returns the size of the world in world units (kind-of-meters).
     *
     * @return
     */
    public static Vec2 getWorldSize() {
        return new Vec2(80, 80);
    }
    
    public static Vec2 getRandomVel(float min, float max) {
        float speed = (float) (Math.random() * (max - min)) + min;
        float angle = (float) (2 * Math.PI * Math.random());
        float dx = (float) (speed * Math.cos(angle));
        float dy = (float) (speed * Math.sin(angle));
        return new Vec2(dx, dy);
    }

    /**
     * Returns a random position on the map.
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
     * Returns a peer by the address the peer is connecting from.
     * @param address
     * @return 
     */
    public static Peer getPeerByAddress(SocketAddress address) {
        Peer peer = peerByAddress.get(address);
        if (peer == null) {
            peer = new Peer();
            peerByAddress.put(address, peer);
        }
        return peer;
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
            java.lang.Object a = c.getFixtureA().getBody().getUserData();
            java.lang.Object b = c.getFixtureB().getBody().getUserData();
            if (a != null && b != null && c.isTouching()) {
                Functor ca = (Functor) a;
                Functor cb = (Functor) b;
                ca.dispatch((Entity) cb);
                cb.dispatch((Entity) ca);
            }
        }
        for (Entity object : working) {
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
     * Reads a packet, which may contain one or more messages.
     *
     * @param channel
     * @throws IOException
     */
    public static void readPacket(DatagramChannel channel) throws IOException {
        ByteBuffer buffer = deserializer.getBuffer();
        buffer.clear();
        SocketAddress addr = channel.receive(buffer);
        if (addr == null) {
            return;
        } else {
            buffer.flip();
        }

        while (buffer.hasRemaining()) {
            readMessage(buffer);
        }
    }

    /**
     * Reads a single message from a byte buffer. A single message may have one
     * or more segments per object in the message.  The format of a message is:
     * 
     * type [1] length [2] payload [n]
     *
     * @param buffer
     */
    public static void readMessage(ByteBuffer buffer) {
        byte type = buffer.get();
        short length = buffer.getShort(buffer.position());
        int start = buffer.position() + 2; // +2 for the length field itself.
        
        // Select the message to parse by using the 'type' field of the message.
        switch(type) {
            case MESSAGE_DATA:
                readDataMessage(buffer);
                break;
            case MESSAGE_NEW:
                readNewMessage(buffer);
                break;
        }
        
        if (buffer.position() != (start+length)) {
            System.err.printf("Invalid message length.  Expected message "
                + "length: %d.  Actual message length: %d", length, 
                buffer.position() - start);
        }
        if (buffer.position() > (start+length)) {
            throw new RuntimeException("Invalid message length");
        }
        
        // Fast-forward to the end of the message if the message length was
        // incorrect.
        if (buffer.position() < (start+length)) {
            buffer.position(start+length);
        }
    }
    
    /**
     * De-serializes an array of object state.  Each object consists of one or
     * more attributes.  The serializer de-serializes an object.  The format
     * of a data message is:
     * 
     * length [2] (id [2] payload [n])+
     * 
     * Note that the 'type' message field is already parsed at this point.
     * 
     * @param buffer 
     */
    public static void readDataMessage(ByteBuffer buffer) {
        // Before reading the message, mark all entities for release.  If any
        // entity doesn't appear in the message, consider it deactivated.
        Peer peer = getPeerByAddress(address);
        for (Entity ent : peer.getEntities()) {
            ent.setMarkedForRelease(true);
        }
        
        short length = buffer.getShort();
        while(buffer.position() < length) {
            short id = buffer.getShort();
            Entity ent = peer.getEntity(id);
            if (ent == null) {
                deserializer.dispatch(ent);
            }
            ent.setActive(true);
            ent.setMarkedForRelease(false);
        }
        
        for (Entity ent : peer.getEntities()) {
            if (ent.isMarkedForRelease()) {
                ent.setActive(false);
            }
        }
    }
    
    /**
     * De-serializes a request to create a new object.  The object was created
     * on the remote side; this adds the object on the local side.   The format 
     * of a "new" message is: 
     *
     * length [2] (id [2] length [2] name [n])+
     * 
     * Note that the 'type' message field is already parsed at this point.
     * @param buffer 
     */
    public static void readNewMessage(ByteBuffer buffer) {
        Peer peer = getPeerByAddress(address);
        short length = buffer.getShort();
        
        while(buffer.position() < length) {
            short id = buffer.getShort();
            short typeNameLength = buffer.getShort();
            buffer.get(tempArray, 0, typeNameLength);
            String typeName = new String(tempArray, 0, typeNameLength);
            peer.newEntity(id, typeName);
        }
    }

    /**
     * Updates the network by polling for read/write events. Serializes any
     * active active that are deemed necessary for serialization. Also,
     * de-multiplexes received messages.
     */
    public static void updateNetwork() throws IOException {
        selector.selectNow();

        Iterator<SelectionKey> i = selector.selectedKeys().iterator();
        while (i.hasNext()) {
            SelectionKey key = i.next();
            i.remove();

            if (key.isValid() && key.isReadable()) {
                readPacket((DatagramChannel) key.channel());
            }
            if (key.isValid() && key.isWritable()) {
                //DatagramChannel channel = (DatagramChannel) key.channel();
            }
        }
    }

    /**
     * Update the game state if necessary, and render to the screen. Update the
     * working object array if the object set was dirty.
     */
    public static void updateGame() {
        long increment = (long) (timestep * 1000000000);
        long time = System.nanoTime();

        if (!Display.isVisible()) {
            last = time;
            return;
        }

        if (activeDirty) {
            activeDirty = false;
            working.clear();
            working.addAll(Asteroids.active);
        }

        // Update all active and tasks in the task queue.
        accum += time - last;
        while (accum >= increment) {
            step();
            accum -= increment;
        }

        // Render all active; be sure to pass the 'frame' remainder'
        renderer.render(active, (float) accum / (float) increment);

        Display.update();
        Display.sync(50);
        last = time;
    }

    /**
     * Runs the game.
     *
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
        selector = Selector.open();

        Keyboard.create();
        Mouse.create();

        for (int i = 0; i < 8; i++) {
            Rock.Large.getRock(Asteroids.getRandomPos());
        }
        addActiveObject(new Starship(new Vector3f(1.0f, 0.f, 0.0f)));
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
