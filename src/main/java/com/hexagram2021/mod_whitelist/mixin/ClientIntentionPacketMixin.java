package com.hexagram2021.mod_whitelist.mixin;

import com.hexagram2021.mod_whitelist.client.ModWhitelistClient;
import com.hexagram2021.mod_whitelist.common.network.IPacketWithModIds;
import com.hexagram2021.mod_whitelist.common.utils.MWLogger;
import io.netty.handler.codec.DecoderException;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.handshake.ClientIntentionPacket;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(ClientIntentionPacket.class)
public class ClientIntentionPacketMixin implements IPacketWithModIds {
	@Shadow @Final
	private ConnectionProtocol intention;

	@Nullable
	private List<String> modIds = null;

	@Override @Nullable
	public List<String> getModIds() {
		return this.modIds;
	}

	@Override
	public void setModIds(@Nullable List<String> modIds) {
		this.modIds = modIds;
	}

	@Inject(method = "<init>(Ljava/lang/String;ILnet/minecraft/network/ConnectionProtocol;)V", at = @At(value = "TAIL"))
	private void getModIdsFromInit(String string, int i, ConnectionProtocol connectionProtocol, CallbackInfo ci) {
		if(connectionProtocol.equals(ConnectionProtocol.LOGIN)) {
			this.modIds = ModWhitelistClient.mods;
		}
	}

	@Inject(method = "<init>(Lnet/minecraft/network/FriendlyByteBuf;)V", at = @At(value = "TAIL"))
	private void getModIdsFromNetwork(FriendlyByteBuf friendlyByteBuf, CallbackInfo ci) {
		if(this.intention.equals(ConnectionProtocol.LOGIN)) {
			try {
				this.modIds = friendlyByteBuf.readList(FriendlyByteBuf::readUtf);
			} catch (DecoderException e) {
				MWLogger.LOGGER.debug("Decoder exception occurs when parsing ClientIntentionPacket: ", e);
			}
		}
	}

	@Inject(method = "write", at = @At(value = "TAIL"))
	private void writeModIdsToNetwork(FriendlyByteBuf friendlyByteBuf, CallbackInfo ci) {
		if(this.modIds != null) {
			friendlyByteBuf.writeCollection(this.modIds, FriendlyByteBuf::writeUtf);
		}
	}
}
