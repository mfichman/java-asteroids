/*
 * Copyright 2012 FishGames.
 * FishGames Proprietary and Confidential.
 *
 */
package fishgames.asteroids;

import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.Collection;

/**
 * A Peer object contains the state of a remote peer.  The state of a remote
 * peer includes a list of entities that the peer has asked us to create.
 * @author Matt Fichman <matt.fichman@gmail.com>
 */
public class Peer {

    private ArrayList<Entity> entity;
    private SocketAddress address;

    /**
     * Returns the entity with the given ID.  If the entity does not exist, 
     * then return null.
     * @param id
     * @return 
     */
    public Entity getEntity(int id) {
        if (entity.size() <= id) {
            return null;
        } else {
            return entity.get(id);
        }
    }
    
    /**
     * Returns a collection of all entities on the peer.
     */
    public Collection<Entity> getEntities() {
        return entity;
    }

    /**
     * Creates a new entity, if it doesn't already exist. If the entity exists,
     * but has a different class name, then throws an exception.
     *
     * @param typeName
     * @return
     */
    public Entity newEntity(int id, String typeName) {
        try {
            Class type = Class.forName(typeName);
            Entity obj = getEntity(id);
            if (obj != null && obj.getClass() == type) {
                return obj;
            } else {
                obj = (Entity) type.newInstance();
                Asteroids.addActiveEntity(obj);
                entity.add(obj);
                return obj;
            }
        } catch (InstantiationException ex) {
            throw new RuntimeException(ex);
        } catch (IllegalAccessException ex) {
            throw new RuntimeException(ex);
        } catch (ClassNotFoundException ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * @return the address
     */
    public SocketAddress getAddress() {
        return address;
    }

    /**
     * @param address the address to set
     */
    public void setAddress(SocketAddress address) {
        this.address = address;
    }
}
