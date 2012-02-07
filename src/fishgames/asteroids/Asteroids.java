/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fishgames.asteroids;

import org.lwjgl.LWJGLException;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.*;
import org.lwjgl.opengl.PixelFormat;
import org.lwjgl.util.vector.Vector3f;

public class Asteroids {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws LWJGLException {
        // TODO code application logic here       
        DisplayMode mode = new DisplayMode(800, 600);
        Display.setDisplayMode(mode);
        Display.setFullscreen(false);
        Display.setTitle("Asteroids");
        try {
            Display.create(new PixelFormat(8, 8, 8, 8, 4));
        } catch (LWJGLException ex) {
            Display.create(new PixelFormat(8, 8, 8, 8, 0));
        }
        
        Keyboard.create();
        Mouse.create();
        
        glClearColor(0.0f, 0, 0, 1.0f);
        glDisable(GL_DEPTH_TEST);
        glDisable(GL_LIGHTING);
        
        glMatrixMode(GL_PROJECTION);
        glLoadIdentity();
        glOrtho(0, mode.getWidth(), mode.getHeight(), 0.f, -1.f, 1.f);
        glMatrixMode(GL_MODELVIEW);
        
        Rock rock = new Rock(60, 16);
        //Starship ship = new Starship(new Vector3f(0.0f, .7f, 0.0f));
        Starship ship = new Starship(new Vector3f(1.0f, 0.f, 0.0f));
        glEnable(GL_LINE_SMOOTH);
        glEnable(GL_MULTISAMPLE);   
        
        while (!Display.isCloseRequested() && !Keyboard.isKeyDown(Keyboard.KEY_ESCAPE)) {
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
            if (Display.isVisible()) {
                rock.render();
                ship.render();
                Display.update();
            }
            Display.sync(60);
        }
        Display.destroy();
    }
}
