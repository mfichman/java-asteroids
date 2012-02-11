/*
 * Copyright 2012 FishGames.
 * FishGames Proprietary and Confidential.
 *
 */
package fishgames.asteroids;

import static org.lwjgl.opengl.GL11.*;
import org.lwjgl.util.vector.Vector3f;

/**
 * Object with a nice outline, created by drawing the primitives once using the
 * outline color (and scaled up slightly) and a second time with the fill color
 * (normal scaling). The scale factor requires tweaking depending on the object
 * size. Ideally, this would not be necessary, but it's easier just to tweak the
 * outline for each object (and it adds some 'approximation' to the look of the
 * game, so it's not so manufactured-looking).
 *
 * @author Matt Fichman <matt.fichman@gmail.com>
 */
public class OutlinedObject implements Renderable {

    protected Polygon polygon;
    protected Vector3f fillColor;
    protected Vector3f outlineColor;
    protected Vector3f outlineScale;

    /**
     * Updates the object (unused, no animation for this object type).
     * 
     * @param delta
     */
    @Override
    public void update(float delta) {
    }

    /**
     * Renders this outlined object with interpolation constant alpha (unused,
     * no animation for this one).
     *
     * @param alpha
     */
    @Override
    public void render(float alpha) {
        if (this.outlineColor != null) {
            glEnable(GL_POLYGON_OFFSET_FILL);
            glPolygonOffset(-20f, -20f);
            glPushMatrix();
            glScalef(this.outlineScale.x, this.outlineScale.y, this.outlineScale.z);
            glColor3f(this.outlineColor.x, this.outlineColor.y, this.outlineColor.z);
            this.polygon.render(alpha);
            glPopMatrix();
            glDisable(GL_POLYGON_OFFSET_FILL);
        }

        glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
        glColor3f(this.fillColor.x, this.fillColor.y, this.fillColor.z);
        this.polygon.render(alpha);
    }
}
