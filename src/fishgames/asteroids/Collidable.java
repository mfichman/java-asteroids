/*
 * Copyright 2012 FishGames.
 * FishGames Proprietary and Confidential.
 *
 */
package fishgames.asteroids;

/**
 *
 * @author Matt Fichman <matt.fichman@gmail.com>
 */
public interface Collidable {
    
    /**
     * Collide this object with another object.
     * @param other 
     */
    public void dispatch(Collidable other);
    
    public void collide(Projectile other);
    public void collide(Rock other);
    public void collide(Starship other);
}
