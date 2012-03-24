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
public class Server extends Entity {

    @Override
    public void dispatch(Functor func) {
        //func.visit(this);
    }

    @Override
    public void update(float delta) {
    }

    @Override
    public boolean isActive() {
        return true;
    }
    
}
