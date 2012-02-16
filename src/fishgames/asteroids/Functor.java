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
public interface Functor {  
    
    public void dispatch(Object obj);
    public void visit(Debris obj);    
    public void visit(Explosion obj);
    public void visit(Photon obj);
    public void visit(Rock obj);
    public void visit(Starship obj);
}
