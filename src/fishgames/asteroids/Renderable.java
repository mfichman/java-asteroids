/*
 * Copyright 2012 FishGames.
 * FishGames Proprietary and Confidential.
 *
 */
package fishgames.asteroids;

/**
 * Simple interface for a renderable object.
 * 
 * @author Matt Fichman <matt.fichman@gmail.com>
 */
public interface Renderable {
    
    public void render(float alpha);
    public void update();
}
