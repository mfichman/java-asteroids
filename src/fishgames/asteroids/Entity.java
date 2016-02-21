/*
 * Copyright 2012 FishGames.
 * FishGames Proprietary and Confidential.
 *
 */
package fishgames.asteroids;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Matt Fichman <matt.fichman@gmail.com>
 */
public class Entity {

    private static Map<Class, Byte> typeId = new HashMap<Class, Byte>();
    private static Map<Byte, Class> type = new HashMap<Byte, Class>();
    private static int nextTypeId;
    private Entity next;
    private short id;
    private boolean markedForRelease;
    private boolean serializable = true;
    private boolean initialized = false;
    private boolean active = false;
    private Peer peer;

    public void dispatch(Functor func) {
    }
    
    public void update(float delta) {
    }
    
    public boolean isActive() {
        return active;
    }

    public static void addType(Class clazz) {
        if (nextTypeId == 255) {
            throw new RuntimeException("Too many types");
        }
        byte id = (byte) nextTypeId++;
        typeId.put(clazz, id);
        type.put(id, clazz);
    }
    
    public static Class getType(int id) {
        return type.get((byte) id);
    }
    
    public static byte getTypeId(Class clazz) {
        return typeId.get(clazz);
    }
    
    public boolean isSerializable() {
        return this.serializable;
    }
    
    public boolean isRemote() {
        return this.peer != null;
    }
    
    public byte getTypeId() {
       if (!typeId.containsKey(getClass())) {
           throw new RuntimeException("Unknown type ID: " + getClass().toString());
       }
       return typeId.get(getClass()); 
    }

    public boolean isMarkedForRelease() {
        return this.markedForRelease;
    }

    public short getId() {
        return this.id;
    }

    public Entity getNext() {
        return this.next;
    }
    
    public Peer getPeer() {
        return this.peer;
    }
    
    public void setSerializable(boolean serializable) {
        this.serializable = serializable;
    }
    
    /**
     * Disables an object, and returns it to the object factory/pool/cache if
     * the object was created from a factory or cache.
     */
    public void setActive(boolean active) {
        if (isActive() == active) {
            return;
        }
        this.active = active;

        if (active) {
            Asteroids.addActiveEntity(this);
        } else {
            Asteroids.delActiveEntity(this);
        }
    }


    public void setMarkedForRelease(boolean release) {
        this.markedForRelease = release;
    }

    public void setId(short id) {
        this.id = id;
    }

    public void setNext(Entity next) {
        this.next = next;
    }

    public void setPeer(Peer peer) {
        this.peer = peer;
    }

    /**
     * @return the initialized
     */
    public boolean isInitialized() {
        return initialized;
    }

    /**
     * @param initialized the initialized to set
     */
    public void setInitialized(boolean initialized) {
        this.initialized = initialized;
    }
}
