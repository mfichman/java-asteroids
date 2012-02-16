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
public interface Object {
    public void dispatch(Functor func);
    public void update(float delta);
}
