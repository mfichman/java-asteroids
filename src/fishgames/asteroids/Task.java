/*
 * Copyright 2012 FishGames.
 * FishGames Proprietary and Confidential.
 *
 */
package fishgames.asteroids;


/**
 * A task is some periodic activity that runs every frame, or when scheduled.
 * 
 * @author Matt Fichman <matt.fichman@gmail.com>
 */
public abstract class Task implements Comparable<Task> {

    private float timeout;
    private long deadline;
    
    public Task(float timeout) {
        this.timeout = timeout;
    }
    
    /**
     * Updates the task.  Returns true if the task should be continued; false
     * otherwise.
     */
    public abstract boolean update();

    /**
     * @return the timeout
     */
    public float getTimeout() {
        return timeout;
    }

    /**
     * @param timeout the timeout to set
     */
    public void setTimeout(float timeout) {
        this.timeout = timeout;
    }

    /**
     * @return the deadline
     */
    public long getDeadline() {
        return deadline;
    }
    
    /**
     */
    public void setDeadline() {
        this.deadline = (long) (getTimeout() * 1000000000.) + System.nanoTime();
    }
    
    /**
     * @param deadline the deadline to set
     */
    public void setDeadline(long deadline) {
        this.deadline = deadline;
    }
    
    /**
     * Enables this task and adds it to the task queue.
     */
    public void setActive(boolean active) {
        if (active && this.deadline == 0) {
            this.setDeadline();
            Asteroids.getActiveTasks().add(this);
        } else if (!active && this.deadline != 0) {
            this.deadline = 0;
            Asteroids.getActiveTasks().remove(this);
        }
    }
    
    /**
     * 
     * @return whether or not this task is active
     */
    public boolean isActive() {
        return this.deadline != 0;
    }
        
    /**
     * Compares the time remaining until this task must run.
     */
    @Override
    public int compareTo(Task other) {
        return (int)(this.deadline - other.deadline);
    }
}
