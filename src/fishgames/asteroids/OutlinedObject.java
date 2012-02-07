/*
 * Copyright 2012 FishGames.
 * FishGames Proprietary and Confidential.
 *
 */
package fishgames.asteroids;

import static org.lwjgl.opengl.GL11.*;
import org.lwjgl.util.vector.Vector3f;

/**
 *
 * @author Matt Fichman <matt.fichman@gmail.com>
 */
class OutlinedObject implements Renderable {
    
    protected Polygon polygon;
    protected Vector3f fillColor;
    protected Vector3f outlineColor;
    protected Vector3f outlineScale;
    
    public void render() {
        if (this.outlineColor != null) {
            glEnable(GL_POLYGON_OFFSET_FILL);
            glPolygonOffset(-20f, -20f);        
            glPushMatrix();
            glScalef(this.outlineScale.x, this.outlineScale.y, this.outlineScale.z);       
            glColor3f(this.outlineColor.x, this.outlineColor.y, this.outlineColor.z);
            this.polygon.render();
            glPopMatrix();
            glDisable(GL_POLYGON_OFFSET_FILL);
        }
        
        glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
        glColor3f(this.fillColor.x, this.fillColor.y, this.fillColor.z);
        this.polygon.render();
    }
}
