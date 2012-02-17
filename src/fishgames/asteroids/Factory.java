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
public class Factory {
    
    private Map<Class, Entity> cache = new HashMap<Class, Entity>();
    private short nextId;
    
    /**
     * Creates a new object, or retrieves it from the cache if necessary.
     * Objects are linked together in a linked list to save memory allocations
     * of entry types in a linked list.
     * @param <T>
     * @param type
     * @return 
     */
    public <T> T newEntity(Class type) {
        Entity ent = cache.get(type);
        if (ent == null) {
            try {
                ent = (Entity) type.newInstance();
                ent.setId(nextId++);
                Asteroids.addActiveObject(ent);
            } catch (InstantiationException ex) {
                throw new RuntimeException(ex);
            } catch (IllegalAccessException ex) {
                throw new RuntimeException(ex);
            }
        } else {
            cache.put(type, ent.getNext());
        } 
        ent.setActive(true);
        ent.setFactory(this);
        return (T) ent;
    }
    
    /**
     * Releases an entity, by adding it to the cache and disabling it.
     * @param object 
     */
    public void delEntity(Entity object) {
        Entity ent = cache.get(object.getClass());
        object.setNext(ent);      
        cache.put(object.getClass(), object);
    }
    
}
