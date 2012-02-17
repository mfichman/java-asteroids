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
    private Factory factory;
    private short id;
    private boolean markedForRelease;
    protected Body body;

    public abstract void dispatch(Functor func);

    public abstract void update(float delta);

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
            Asteroids.addActiveObject(this);
        } else {
            this.getBody().setActive(false);
            Asteroids.removeActiveObject(this);
            if (this.factory != null) {
                this.factory.delEntity(this);
            }
        }
    }

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

    public void setMarkedForRelease(boolean release) {
        this.markedForRelease = release;
    }

    public void setId(short id) {
        this.id = id;
    }

    public void setNext(Entity next) {
        this.next = next;
    }

    public void setFactory(Factory factory) {
        this.factory = factory;
    }
}
