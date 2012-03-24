/*
 * Copyright 2012 FishGames.
 * FishGames Proprietary and Confidential.
 *
 */
package fishgames.asteroids;

import java.net.SocketAddress;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * A Peer object contains the state of a remote peer. The state of a remote peer
 * includes a list of entities that the peer has asked us to create.
 *
 * @author Matt Fichman <matt.fichman@gmail.com>
 */
public class Peer {

    private Map<Short, Entity> entities = new HashMap<Short, Entity>();
    private SocketAddress address;
    
    /**
     * Creates a new peer and sets the peer's socket address.
     * @param address 
     */
    public Peer(SocketAddress address) {
        this.address = address;
    }

    /**
     * Creates a new entity, if it doesn't already exist. If the entity exists,
     * but has a different class name, then throws an exception.
     * 
     * @param id
     * @return
     */
    public Entity getEntity(short id, int typeId) {
        Entity entity = entities.get(id);
        if (entity != null) {
            if (entity.getTypeId() == typeId) {
                return entity;
            }
            entity.setActive(false);
            entities.remove(id);
        }
        try {
            Class type = Entity.getType(typeId);
            if (type == null) {
                throw new RuntimeException("Unkown entity type: "+typeId);
            }
            entity = (Entity) type.newInstance();
            entity.setId(id);
            entity.setPeer(this);
            entities.put(id, entity);
            return entity;
        } catch (InstantiationException ex) {
            throw new RuntimeException(ex);
        } catch (IllegalAccessException ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Returns a collection of all entities on the peer.
     */
    public Collection<Entity> getEntities() {
        return entities.values();
    }

    /**
     * @return the address
     */
    public SocketAddress getAddress() {
        return address;
    }
}
