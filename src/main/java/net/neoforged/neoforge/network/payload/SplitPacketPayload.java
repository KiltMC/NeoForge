package net.neoforged.neoforge.network.payload;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.reading.PayloadReadingContext;

public record SplitPacketPayload(byte[] payload) implements CustomPacketPayload {
    
    public static final ResourceLocation ID = new ResourceLocation("neoforge", "split");
    
    public SplitPacketPayload(FriendlyByteBuf buf) {
        this(buf.readBytes(buf.readVarInt()).array());
    }
    
    public SplitPacketPayload(FriendlyByteBuf buf, PayloadReadingContext context) {
        this(buf);
    }
    
    @Override
    public void write(FriendlyByteBuf p_294947_) {
        p_294947_.writeVarInt(payload.length);
        p_294947_.writeBytes(payload);
    }
    
    @Override
    public ResourceLocation id() {
        return ID;
    }
}
