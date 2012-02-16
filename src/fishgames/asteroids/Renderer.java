/*
 * Copyright 2012 FishGames.
 * FishGames Proprietary and Confidential.
 *
 */
package fishgames.asteroids;

import java.nio.IntBuffer;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.GL_MULTISAMPLE;
import static org.lwjgl.opengl.GL15.*;
import org.lwjgl.util.vector.Vector3f;

/**
 *
 * @author Matt Fichman <matt.fichman@gmail.com>
 */
public class Renderer implements Functor {

    private float interpolation; // Interpolation constant

    public Renderer() {
        DisplayMode mode = Display.getDisplayMode();
        glClearColor(0.0f, 0, 0, 1.0f);
        glDisable(GL_DEPTH_TEST);
        glDisable(GL_LIGHTING);
        glEnable(GL_LINE_SMOOTH);
        glEnable(GL_MULTISAMPLE);

        glMatrixMode(GL_PROJECTION);
        glLoadIdentity();
        glOrtho(0, mode.getWidth(), mode.getHeight(), 0.f, -1.f, 1.f);
        glMatrixMode(GL_MODELVIEW);
    }

    public void render(float alpha) {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        glLoadIdentity();
        glScalef(10.f, 10.f, 10.f);
        this.interpolation = alpha;

        for (Object object : Asteroids.getObjects()) {
            object.dispatch(this);
        }
        // DISPATCH
    }

    @Override
    public void dispatch(Object obj) {
        obj.dispatch(this);
    }

    @Override
    public void visit(Debris obj) {
        // Renders a debris object as a polygon with alpha
        Polygon poly = Debris.getPolygon(obj);
        float alpha = obj.getLife() / Debris.LIFE;

        glPushMatrix();
        setTransform(obj.getBody(), this.interpolation);
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glColor4f(.2f, .2f, .2f, alpha);
        drawSolid(poly);
        glColor4f(.8f, .8f, .8f, alpha);
        drawOutline(poly);
        glDisable(GL_BLEND);
        glPopMatrix();
    }

    @Override
    public void visit(Explosion obj) {
        // Renders an explosion as a circle with alpha
        Polygon poly = Explosion.getPolygon();
        Vector3f color = obj.getColor();
        float scale = obj.getSize() + 1.2f * (Explosion.LIFE - obj.getLife()) / Explosion.LIFE;
        float alpha = obj.getLife() / Explosion.LIFE;

        glPushMatrix();
        setTransform(obj.getBody(), this.interpolation);
        glScalef(scale, scale, scale);
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glColor4f(color.x, color.y, color.z, alpha);
        drawSolid(poly);
        glDisable(GL_BLEND);
        glPopMatrix();
    }

    @Override
    public void visit(Photon obj) {
        // Render a polygon as a small white square
        Polygon poly = Photon.getPolygon();

        glPushMatrix();
        setTransform(obj.getBody(), this.interpolation);
        glColor4f(1.f, 1.f, 1.f, 1.f);
        drawSolid(poly);
        glPopMatrix();
    }

    @Override
    public void visit(Rock obj) {
        // Render a rock.  Depending on the size and hash code of the rock, 
        // select a different polygon.
        Polygon poly;
        if (obj.getRadius() >= Rock.LARGE) {
            poly = Rock.getLargePolygon(obj);
        } else if (obj.getRadius() >= Rock.MEDIUM) {
            poly = Rock.getMediumPolygon(obj);
        } else {
            poly = Rock.getSmallPolygon(obj);
        }

        glPushMatrix();
        setTransform(obj.getBody(), this.interpolation);
        glColor3f(.2f, .2f, .2f);
        drawSolid(poly);
        glColor3f(.6f, .6f, .6f);
        drawOutline(poly);
        glPopMatrix();
    }

    @Override
    public void visit(Starship obj) {
        // Draws a starship.  Conditionally draws the hull, thruster, and main
        // deflector shield.
        if (!obj.isFlickerOn()) {
            return;
        }
        Polygon hull = Starship.getHullPolygon();
        Polygon thruster = Starship.getMainThrusterPolygon();
        Polygon shield = Starship.getShieldPolygon();
        Vector3f color = obj.getColor();
        glPushMatrix();
        setTransform(obj.getBody(), this.interpolation);
        if (obj.isThrusterOn()) {
            glColor3f(1.f, .85f, .2f);
            drawSolid(thruster);
        }
        glColor3f(color.x, color.y, color.z);
        drawSolid(hull);
        glColor3f(1.f, 1.f, 1.f);
        drawOutline(hull);
        if (obj.isShieldVisible()) {
            float alpha = obj.getShieldLife() / Starship.SHIELD_LIFE * 0.5f;
            glEnable(GL_BLEND);
            glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
            glColor4f(.2f, .2f, 1.f, alpha);
            drawSolid(shield);
            glColor4f(.4f, .4f, 1.f, alpha);
            drawOutline(shield);
            glDisable(GL_BLEND);
        }
        glPopMatrix();
    }

    private void drawOutline(Polygon obj) {
        // Draws a polygon outline using GL_LINE_LOOP     
        loadPolygon(obj);
        glLineWidth(1.5f);
        glEnableClientState(GL_VERTEX_ARRAY);
        glBindBuffer(GL_ARRAY_BUFFER, obj.getVertexBuffer());
        glVertexPointer(2, GL_FLOAT, 0, 0);
        if (obj.getNumIndices() > 0) {
            glDrawArrays(GL_LINE_LOOP, 1, obj.getNumVertices() - 1);
        } else {
            glDrawArrays(GL_LINE_LOOP, 0, obj.getNumVertices());
        }
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glDisableClientState(GL_VERTEX_ARRAY);
    }

    private void drawSolid(Polygon obj) {
        // Draws a solid polygon, either using GL_TRIANGLES if the polygon is
        // concave, or GL_POLYGON if it's convex.
        loadPolygon(obj);
        glEnableClientState(GL_VERTEX_ARRAY);
        glBindBuffer(GL_ARRAY_BUFFER, obj.getVertexBuffer());
        glVertexPointer(2, GL_FLOAT, 0, 0);
        if (obj.getNumIndices() > 0) {
            glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, obj.getIndexBuffer());
            glDrawElements(GL_TRIANGLES, obj.getNumIndices(), GL_UNSIGNED_INT, 0);
            glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
        } else {
            glDrawArrays(GL_POLYGON, 0, obj.getNumVertices());
        }
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glDisableClientState(GL_VERTEX_ARRAY);
    }

    /**
     * Loads the polygon into a hardware vertex buffer and possibly a hardware
     * index buffer, if not already loaded.
     *
     * @param obj
     */
    private void loadPolygon(Polygon obj) {
        if (obj.getVertexBuffer() != 0) {
            return;
        }

        IntBuffer intBuf = BufferUtils.createIntBuffer(1);
        glGenBuffers(intBuf);
        obj.setVertexBuffer(intBuf.get(0));
        glBindBuffer(GL_ARRAY_BUFFER, obj.getVertexBuffer());
        glBufferData(GL_ARRAY_BUFFER, obj.getVertexArray(), GL_STATIC_DRAW);
        glBindBuffer(GL_ARRAY_BUFFER, 0);

        if (obj.getIndexArray() != null) {
            glGenBuffers(intBuf);
            obj.setIndexBuffer(intBuf.get(0));
            glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, obj.getIndexBuffer());
            glBufferData(GL_ELEMENT_ARRAY_BUFFER, obj.getIndexArray(), GL_STATIC_DRAW);
            glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
        }
    }

    /**
     * Sets the model-view transform to be equal to the transform of the body,
     * with interpolation by alpha.
     *
     * @param body
     * @param alpha
     */
    private void setTransform(Body body, float alpha) {
        float angle = body.getAngle();
        //float angularVel = body.getAngularVelocity();
        //float angle1 = angle * (1 - alpha);
        //float angle2 = (angle + angularVel) * (alpha);
        //float finalAngle = angle1 + angle2;
        float finalAngle = angle;

        Vec2 pos = body.getPosition();
        Vec2 linearVel = body.getLinearVelocity();
        Vec2 pos1 = pos.mul(1 - alpha);
        Vec2 pos2 = (pos.add(linearVel.mul(1.f / 60.f))).mul(alpha);
        Vec2 finalPos = pos1.add(pos2);
        glTranslatef(finalPos.x, finalPos.y, 0.f);
        glRotatef((float) (finalAngle * 180.f / Math.PI), 0, 0, 1.f);
        // Rotate around z-axis
    }
}
