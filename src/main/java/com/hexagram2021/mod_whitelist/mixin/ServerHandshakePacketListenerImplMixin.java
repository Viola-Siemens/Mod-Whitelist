package com.hexagram2021.mod_whitelist.mixin;

import com.hexagram2021.mod_whitelist.common.network.IPacketWithModIds;
import com.hexagram2021.mod_whitelist.server.config.MWServerConfig;
import com.hexagram2021.mod_whitelist.server.config.MismatchType;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.handshake.ClientIntentionPacket;
import net.minecraft.network.protocol.login.ClientboundLoginDisconnectPacket;
import net.minecraft.server.network.ServerHandshakePacketListenerImpl;
import org.apache.commons.lang3.tuple.Pair;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(ServerHandshakePacketListenerImpl.class)
public class ServerHandshakePacketListenerImplMixin {
	@Shadow @Final
	private Connection connection;

	@Inject(method = "handleIntention", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/Connection;setClientboundProtocolAfterHandshake(Lnet/minecraft/network/protocol/handshake/ClientIntent;)V", shift = At.Shift.AFTER, ordinal = 0), cancellable = true)
	private void tryDisconnectPlayersIfModlistNotMatches(ClientIntentionPacket clientIntentionPacket, CallbackInfo ci) {
		MutableComponent reason = null;
		IPacketWithModIds packetWithModIds = (IPacketWithModIds)(Object)clientIntentionPacket;
		if(packetWithModIds.getModIds() != null) {
			List<Pair<String, MismatchType>> mismatches = MWServerConfig.test(packetWithModIds.getModIds());
			if(!mismatches.isEmpty()) {
				reason = Component.translatable("multiplayer.disconnect.mod_whitelist.modlist_mismatch");
				for (Pair<String, MismatchType> mod: mismatches) {
					switch (mod.getRight()) {
						case UNINSTALLED_BUT_SHOULD_INSTALL -> reason.append(Component.translatable("multiplayer.disconnect.mod_whitelist.misc.to_install", mod.getLeft()));
						case INSTALLED_BUT_SHOULD_NOT_INSTALL -> reason.append(Component.translatable("multiplayer.disconnect.mod_whitelist.misc.to_uninstall", mod.getLeft()));
					}
				}
			}
		} else {
			reason = Component.translatable("multiplayer.disconnect.mod_whitelist.packet_corruption");
		}

		if(reason != null) {
			this.connection.send(new ClientboundLoginDisconnectPacket(reason));
			this.connection.disconnect(reason);
			ci.cancel();
		}
	}
}
