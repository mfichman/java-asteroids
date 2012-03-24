/*
 * Copyright 2012 FishGames.
 * FishGames Proprietary and Confidential.
 *
 */
package fishgames.asteroids;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
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
    private SocketAddress address;
    private Map<SocketAddress, Peer> peers = new HashMap<SocketAddress, Peer>();
    public static final float SEND_RATE = .1f; // 10 packets per second
    public static int nextPeer = 0;
    public DatagramChannel channel;
    public Task sendTask = new Task(SEND_RATE) {

        @Override
        public boolean update() {
            return false;
        }
    };

    /**
     * Creates a new network subsystem and registers it with the game.
     */
    public Network(boolean server) throws IOException {
        super(0);
        this.channel = DatagramChannel.open();
        InetSocketAddress local = new InetSocketAddress("localhost", 12345);
        if (server) {
            this.channel.socket().bind(new InetSocketAddress(12345));
        } else {
            //this.channel.socket().bind(new InetSocketAddress(12345));
            Asteroids.setMaster(new Peer(local));
            this.peers.put(local, Asteroids.getMaster());
        }
        this.channel.configureBlocking(false);
        this.channel.register(Asteroids.getSelector(), SelectionKey.OP_READ | SelectionKey.OP_WRITE);
    }

    /**
     * Returns a peer by the address the peer is connecting from.
     *
     * @param address
     * @return
     */
    public Peer getPeerByAddress(SocketAddress address) {
        Peer peer = peers.get(address);
        if (peer == null) {
            peer = new Peer(address);
            peers.put(address, peer);
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
        address = channel.receive(buffer);
        if (address == null) {
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
     * or more segments per object in the message. The format of a message is:
     *
     * length [2] (id [2] payload [n])+
     *
     * @param buffer
     */
    public void readMessage(ByteBuffer buffer) {
        // Before reading the message, mark all entities for release.  If any
        // entity doesn't appear in the message, consider it deactivated.

        //System.out.println("Reading: " + System.currentTimeMillis());
        Peer peer = getPeerByAddress(address);
        for (Entity ent : peer.getEntities()) {
            ent.setMarkedForRelease(true);
        }

        while (buffer.hasRemaining()) {
            short id = buffer.getShort();
            byte type = buffer.get();
            Entity entity = peer.getEntity(id, type);
            deserializer.dispatch(entity);
            entity.setActive(true);
            entity.setMarkedForRelease(false);
        }

        for (Entity ent : peer.getEntities()) {
            if (ent.isMarkedForRelease()) {
                ent.setActive(false);
                ent.setInitialized(false);
            }
        }
    }

    /**
     * Writes a packet to the channel. This method takes a snapshot of the
     * current state of active entities, and writes it to a buffer.
     *
     * @param channel
     */
    public void writePacket(DatagramChannel channel) throws IOException {

        ByteBuffer buffer = serializer.getBuffer();
        buffer.clear();

        int num = 0;

        // Serialize all non-remote, active entities 
        for (Entity entity : Asteroids.getActiveEntities()) {
            if (!entity.isRemote() && entity.isSerializable()) {
                buffer.putShort(entity.getId());
                buffer.put(entity.getTypeId());
                serializer.dispatch(entity);
                num++;

            }
        }
        if (buffer.limit() <= 0) {
            return; // Nothing to send
        }
        buffer.flip();

        Iterator<Peer> iter = peers.values().iterator();
        for (int i = 0; i < nextPeer; i++) {
            iter.next();
        }
        if (!peers.isEmpty()) {
            Peer peer = iter.next();
            channel.send(buffer, peer.getAddress());
            nextPeer = (nextPeer + 1) % peers.size();
        }

        if (nextPeer == 0) {
            channel.register(Asteroids.getSelector(), SelectionKey.OP_READ);
            sendTask.setActive(true);
        }
    }

    /**
     * Updates the network by polling for read/write events. Serializes any
     * active active that are deemed necessary for serialization. Also,
     * de-multiplexes received messages.
     */
    @Override
    public boolean update() {
        try {
            Iterator<SelectionKey> i = Asteroids.getSelector().selectedKeys().iterator();
            while (i.hasNext()) {
                SelectionKey key = i.next();
                i.remove();

                if (key.isValid() && key.isReadable()) {
                    readPacket((DatagramChannel) key.channel());
                }
                if (key.isValid() && key.isWritable()) {
                    writePacket((DatagramChannel) key.channel());
                }
            }
            if (!sendTask.isActive()) {
                channel.register(Asteroids.getSelector(), SelectionKey.OP_READ | SelectionKey.OP_WRITE);
            }
        } catch (IOException ex) {
            Logger.getLogger(Network.class.getName()).log(Level.SEVERE, null, ex);
        }


        //System.out.printf("Objects serialized: %d\n", num);

        // Send the data to all peers
        //for (Peer peer : peers.values()) {
        //    int sent = channel.send(buffer, peer.getAddress());
        //    System.out.println(peer.getAddress()+"SENT: "+sent);

        //}

        return true;
    }
}
