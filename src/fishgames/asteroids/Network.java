/*
 * Copyright 2012 FishGames.
 * FishGames Proprietary and Confidential.
 *
 */
package fishgames.asteroids;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Matt Fichman <matt.fichman@gmail.com>
 */
public class Network extends Task {
    private Serializer serializer = new Serializer();
    private Deserializer deserializer = new Deserializer();
    private Selector selector;
    private SocketAddress address;
    private Map<SocketAddress, Peer> peerByAddress = new HashMap<SocketAddress, Peer>();
    private byte[] tempArray = new byte[1024];
    public static final byte MESSAGE_DATA = 2;
    public static final byte MESSAGE_NEW = 3;
    
    /**
     * Creates a new network subsystem and registers it with the game.
     */
    public Network() throws IOException {
        super(0);
        this.selector = Selector.open();
    }
    
    /**
     * Returns a peer by the address the peer is connecting from.
     * @param address
     * @return 
     */
    public Peer getPeerByAddress(SocketAddress address) {
        Peer peer = peerByAddress.get(address);
        if (peer == null) {
            peer = new Peer();
            peerByAddress.put(address, peer);
        }
        return peer;
    }
    
    /**
     * Reads a packet, which may contain one or more messages.
     *
     * @param channel
     * @throws IOException
     */
    public void readPacket(DatagramChannel channel) throws IOException {
        ByteBuffer buffer = deserializer.getBuffer();
        buffer.clear();
        SocketAddress addr = channel.receive(buffer);
        if (addr == null) {
            return;
        } else {
            buffer.flip();
        }

        while (buffer.hasRemaining()) {
            readMessage(buffer);
        }
    }

    /**
     * Reads a single message from a byte buffer. A single message may have one
     * or more segments per object in the message.  The format of a message is:
     * 
     * type [1] length [2] payload [n]
     *
     * @param buffer
     */
    public void readMessage(ByteBuffer buffer) {
        byte type = buffer.get();
        short length = buffer.getShort(buffer.position());
        int start = buffer.position() + 2; // +2 for the length field itself.
        
        // Select the message to parse by using the 'type' field of the message.
        switch(type) {
            case MESSAGE_DATA:
                readDataMessage(buffer);
                break;
            case MESSAGE_NEW:
                readNewMessage(buffer);
                break;
        }
        
        if (buffer.position() != (start+length)) {
            System.err.printf("Invalid message length.  Expected message "
                + "length: %d.  Actual message length: %d", length, 
                buffer.position() - start);
        }
        if (buffer.position() > (start+length)) {
            throw new RuntimeException("Invalid message length");
        }
        
        // Fast-forward to the end of the message if the message length was
        // incorrect.
        if (buffer.position() < (start+length)) {
            buffer.position(start+length);
        }
    }
    
    /**
     * De-serializes an array of object state.  Each object consists of one or
     * more attributes.  The serializer de-serializes an object.  The format
     * of a data message is:
     * 
     * length [2] (id [2] payload [n])+
     * 
     * Note that the 'type' message field is already parsed at this point.
     * 
     * @param buffer 
     */
    public void readDataMessage(ByteBuffer buffer) {
        // Before reading the message, mark all entities for release.  If any
        // entity doesn't appear in the message, consider it deactivated.
        Peer peer = getPeerByAddress(address);
        for (Entity ent : peer.getEntities()) {
            ent.setMarkedForRelease(true);
        }
        
        short length = buffer.getShort();
        while(buffer.position() < length) {
            short id = buffer.getShort();
            Entity ent = peer.getEntity(id);
            if (ent == null) {
                deserializer.dispatch(ent);
            }
            ent.setActive(true);
            ent.setMarkedForRelease(false);
        }
        
        for (Entity ent : peer.getEntities()) {
            if (ent.isMarkedForRelease()) {
                ent.setActive(false);
            }
        }
    }
    
    /**
     * De-serializes a request to create a new object.  The object was created
     * on the remote side; this adds the object on the local side.   The format 
     * of a "new" message is: 
     *
     * length [2] (id [2] length [2] name [n])+
     * 
     * Note that the 'type' message field is already parsed at this point.
     * @param buffer 
     */
    public void readNewMessage(ByteBuffer buffer) {
        Peer peer = getPeerByAddress(address);
        short length = buffer.getShort();
        
        while(buffer.position() < length) {
            short id = buffer.getShort();
            short typeNameLength = buffer.getShort();
            buffer.get(tempArray, 0, typeNameLength);
            String typeName = new String(tempArray, 0, typeNameLength);
            peer.newEntity(id, typeName);
        }
    }

    /**
     * Updates the network by polling for read/write events. Serializes any
     * active active that are deemed necessary for serialization. Also,
     * de-multiplexes received messages.
     */
    public boolean update() {
        try {
            selector.selectNow();
            Iterator<SelectionKey> i = selector.selectedKeys().iterator();
            while (i.hasNext()) {
                SelectionKey key = i.next();
                i.remove();

                if (key.isValid() && key.isReadable()) {
                    readPacket((DatagramChannel) key.channel());
                }
                if (key.isValid() && key.isWritable()) {
                    //DatagramChannel channel = (DatagramChannel) key.channel();
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(Network.class.getName()).log(Level.SEVERE, null, ex);
        }
        return true;
    }
}
