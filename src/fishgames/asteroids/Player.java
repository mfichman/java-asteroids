/*
 * Copyright 2012 FishGames.
 * FishGames Proprietary and Confidential.
 *
 */
package fishgames.asteroids;

import org.lwjgl.input.Keyboard;

/**
 *
 * @author Matt Fichman <matt.fichman@gmail.com>
 */
public class Player extends Entity {

    private static byte FLAG_FORWARD = 0x1;
    private static byte FLAG_BACK = 0x2;
    private static byte FLAG_LEFT = 0x4;
    private static byte FLAG_RIGHT = 0x8;
    private static byte FLAG_FIRE = 0x10;
    private static byte FLAG_HYPERSPACE = 0x20;
    private Starship starship;
    private byte inputFlags;
    
    @Override
    public void setPeer(Peer peer) {
        super.setPeer(peer);
        // If there is no starship for this player yet, then create one.
        if (this.isRemote() || !this.isSerializable()) {
            this.starship = Asteroids.newEntity(Starship.class);
            this.starship.setActive(true);
        }
    }
            
    /**
     * Returns the input flags for the player, which describe which actions the
     * player is currently executing.
     *
     * @return
     */
    public byte getInputFlags() {
        byte flags = 0;
        if (Keyboard.isKeyDown(Keyboard.KEY_I)) {
            flags |= FLAG_FORWARD;
        }
        if (Keyboard.isKeyDown(Keyboard.KEY_K)) {
            flags |= FLAG_BACK;
        }
        if (Keyboard.isKeyDown(Keyboard.KEY_J)) {
            flags |= FLAG_LEFT;
        }
        if (Keyboard.isKeyDown(Keyboard.KEY_L)) {
            flags |= FLAG_RIGHT;
        }
        if (Keyboard.isKeyDown(Keyboard.KEY_H)) {
            flags |= FLAG_HYPERSPACE;
        }
        if (Keyboard.isKeyDown(Keyboard.KEY_SPACE)) {
            flags |= FLAG_FIRE;
        }
        return flags;
    }

    /**
     * Update the state of the starship.
     *
     * @param flags
     */
    public void setInputFlags(byte flags) {
        this.inputFlags = flags;
    }

    @Override
    public void dispatch(Functor func) {
        func.visit(this);
    }

    @Override
    public void update(float delta) {
        if (this.starship == null) {
            return;
        }
        if (!this.isRemote()) {
            this.inputFlags = getInputFlags();
        }
        
        if ((this.inputFlags & FLAG_FORWARD) != 0) {
            this.starship.setThrusterOn(true);
            this.starship.thrust();
        } else {
            this.starship.setThrusterOn(false);
        }
        if ((this.inputFlags & FLAG_BACK) != 0) {
            this.starship.brake();
        }
        if ((this.inputFlags & FLAG_LEFT) != 0 && (this.inputFlags & FLAG_RIGHT) == 0) {
            this.starship.rotateLeft();
        }
        if ((this.inputFlags & FLAG_RIGHT) != 0 && (this.inputFlags & FLAG_LEFT) == 0) {
            this.starship.rotateRight();
        }
        if ((this.inputFlags & FLAG_RIGHT) == 0 && (this.inputFlags & FLAG_LEFT) == 0) {
            this.starship.stopRotation();
        }
        if ((this.inputFlags & FLAG_HYPERSPACE) != 0) {
            this.starship.hyperjump();
        }
        if ((this.inputFlags & FLAG_FIRE) != 0) {
            this.starship.fire();
        }
    }
}
