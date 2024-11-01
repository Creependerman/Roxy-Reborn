package net.minecraft.network.play.client;

import java.io.IOException;

import lombok.Getter;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayServer;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;

@Getter
public class C07PacketPlayerDigging implements Packet<INetHandlerPlayServer>
{
    private BlockPos position;
    private EnumFacing facing;
    private boolean half = false;

    /** Status of the digging (started, ongoing, broken). */
    private C07PacketPlayerDigging.Action status;

    public C07PacketPlayerDigging()
    {
    }

    public C07PacketPlayerDigging(C07PacketPlayerDigging.Action statusIn, BlockPos posIn, EnumFacing facingIn)
    {
        this.status = statusIn;
        this.position = posIn;
        this.facing = facingIn;
    }
    public C07PacketPlayerDigging(C07PacketPlayerDigging.Action statusIn)
    {
        this.status = statusIn;
        half = true;
    }

    /**
     * Reads the raw packet data from the data stream.
     */
    public void readPacketData(PacketBuffer buf) throws IOException
    {
        this.status = buf.readEnumValue(Action.class);
        this.position = buf.readBlockPos();
        this.facing = EnumFacing.getFront(buf.readUnsignedByte());
    }

    /**
     * Writes the raw packet data to the data stream.
     */
    public void writePacketData(PacketBuffer buf) throws IOException
    {
        buf.writeEnumValue(this.status);
        if (this.half) {
            return;
        }
        buf.writeBlockPos(this.position);
        buf.writeByte(this.facing.getIndex());
    }

    /**
     * Passes this Packet on to the NetHandler for processing.
     */
    public void processPacket(INetHandlerPlayServer handler)
    {
        handler.processPlayerDigging(this);
    }

    public enum Action
    {
        START_DESTROY_BLOCK,
        ABORT_DESTROY_BLOCK,
        STOP_DESTROY_BLOCK,
        DROP_ALL_ITEMS,
        DROP_ITEM,
        RELEASE_USE_ITEM;
    }
}
