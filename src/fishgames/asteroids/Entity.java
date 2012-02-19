/*
 * Copyright 2012 FishGames.
 * FishGames Proprietary and Confidential.
 *
 */
package fishgames.asteroids;

import org.jbox2d.dynamics.Body;

/**
 *
 * @author Matt Fichman <matt.fichman@gmail.com>
 */
public abstract class Entity {

    private Entity next;
    private short id;
    private boolean markedForRelease;
    private Peer peer;
    protected Body body;

    public abstract void dispatch(Functor func);
    public abstract void update(float delta);


    public boolean isMarkedForRelease() {
        return this.markedForRelease;
    }

    public short getId() {
        return this.id;
    }

    public Body getBody() {
        return this.body;
    }

    public Entity getNext() {
        return this.next;
    }
    
    public Peer getPeer() {
        return this.peer;
    }
    
    /**
     * Disables an object, and returns it to the object factory/pool/cache if
     * the object was created from a factory or cache.
     */
    public void setActive(boolean active) {
        if (getBody().isActive() == active) {
            return;
        }

        if (active) {
            this.getBody().setActive(true);
            Asteroids.addActiveEntity(this);
        } else {
            this.getBody().setActive(false);
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
}
