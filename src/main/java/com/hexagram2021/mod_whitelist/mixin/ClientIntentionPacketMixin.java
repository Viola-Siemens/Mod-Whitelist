package com.hexagram2021.mod_whitelist.mixin;

import com.hexagram2021.mod_whitelist.client.ModWhitelistClient;
import com.hexagram2021.mod_whitelist.common.network.IPacketWithModIds;
import com.hexagram2021.mod_whitelist.common.utils.MWLogger;
import io.netty.handler.codec.DecoderException;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.handshake.ClientIntent;
import net.minecraft.network.protocol.handshake.ClientIntentionPacket;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(ClientIntentionPacket.class)
public class ClientIntentionPacketMixin implements IPacketWithModIds {
	@Shadow @Final
	private ClientIntent intention;

	@Unique @Nullable
	private List<String> modWhitelist$modIds = null;

	@Override @Nullable
	public List<String> getModIds() {
		return this.modWhitelist$modIds;
	}

	@Override
	public void setModIds(@Nullable List<String> modIds) {
		this.modWhitelist$modIds = modIds;
	}

	@Inject(method = "<init>(ILjava/lang/String;ILnet/minecraft/network/protocol/handshake/ClientIntent;)V", at = @At(value = "TAIL"))
	private void getModIdsFromInit(int protocolVersion, String hostName, int port, ClientIntent intention, CallbackInfo ci) {
		if(intention.equals(ClientIntent.LOGIN)) {
			this.modWhitelist$modIds = ModWhitelistClient.mods;
		}
	}

	@Inject(method = "<init>(Lnet/minecraft/network/FriendlyByteBuf;)V", at = @At(value = "TAIL"))
	private void getModIdsFromNetwork(FriendlyByteBuf friendlyByteBuf, CallbackInfo ci) {
		if(this.intention.equals(ClientIntent.LOGIN)) {
			try {
				this.modWhitelist$modIds = friendlyByteBuf.readList(FriendlyByteBuf::readUtf);
			} catch (DecoderException e) {
				MWLogger.LOGGER.warn("Decoder exception occurs when parsing ClientIntentionPacket: ", e);
			}
		}
	}

	@Inject(method = "write", at = @At(value = "TAIL"))
	private void writeModIdsToNetwork(FriendlyByteBuf friendlyByteBuf, CallbackInfo ci) {
		if(this.modWhitelist$modIds != null) {
			friendlyByteBuf.writeCollection(this.modWhitelist$modIds, FriendlyByteBuf::writeUtf);
		}
	}
}
