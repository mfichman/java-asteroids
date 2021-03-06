/*
 * Copyright 2012 FishGames.
 * FishGames Proprietary and Confidential.
 *
 */
package fishgames.asteroids;

import org.jbox2d.common.Vec2;

/**
 *
 * @author Matt Fichman <matt.fichman@gmail.com>
 */
public interface Projectile {
    
    public static int TYPE = 0x4;
    public static int MASK = Starship.TYPE | Rock.TYPE;
    
    /**
     * Sets the launch vector and also activates the projectile.
     * @param direction 
     */
    public void setLaunchVector(Vec2 direction, Vec2 velocity);
  
    /**
     * Sets the position of the projectile
     * @param position 
     */
    public void setPosition(Vec2 position);
    
    /**
     * Returns the damage inflicted by this projectile.
     */
    public float getDamage(); 
    
    /**
     * Returns the reload time.
     */
    public float getRearmTime();
}
