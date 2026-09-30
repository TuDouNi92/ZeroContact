package net.zerocontact.compat;

import de.maxhenkel.voicechat.api.ForgeVoicechatPlugin;
import de.maxhenkel.voicechat.api.VoicechatApi;
import de.maxhenkel.voicechat.api.VoicechatConnection;
import de.maxhenkel.voicechat.api.VoicechatPlugin;
import de.maxhenkel.voicechat.api.VoicechatServerApi;
import de.maxhenkel.voicechat.api.audiochannel.StaticAudioChannel;
import de.maxhenkel.voicechat.api.events.EventRegistration;
import de.maxhenkel.voicechat.api.events.MicrophonePacketEvent;
import de.maxhenkel.voicechat.api.events.VoicechatServerStoppedEvent;
import de.maxhenkel.voicechat.api.opus.OpusDecoder;
import de.maxhenkel.voicechat.api.opus.OpusEncoder;
import de.maxhenkel.voicechat.api.opus.OpusEncoderMode;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.server.ServerLifecycleHooks;
import net.zerocontact.armor.modular.ModuleQuery;
import net.zerocontact.armor.modular.module.radio.api.MBITR;
import net.zerocontact.armor.modular.module.radio.model.RadioTransmission;
import net.zerocontact.armor.modular.module.radio.service.MBITRService;
import net.zerocontact.capability.CapabilityRegistries;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@ForgeVoicechatPlugin
public class RadioChatPlugin implements VoicechatPlugin {
    private static final int RELEASE_DELAY_TICKS = 6;
    private final Map<UUID, StaticAudioChannel> channels = new HashMap<>();
    private final Map<UUID, Integer> lastPacketTick = new HashMap<>();
    private final Map<UUID, RadioAudioStream> audioStreams = new HashMap<>();

    @Override
    public String getPluginId() {
        return "zerocontact_radio";
    }

    @Override
    public void initialize(VoicechatApi api) {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Override
    public void registerEvents(EventRegistration registration) {
        registration.registerEvent(MicrophonePacketEvent.class, this::onMicrophonePacket);
        registration.registerEvent(VoicechatServerStoppedEvent.class, event -> {
            MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
            if (server != null) server.execute(() -> {
                channels.clear();
                lastPacketTick.clear();
                audioStreams.values().forEach(RadioAudioStream::close);
                audioStreams.clear();
            });
        });
    }

    private void onMicrophonePacket(MicrophonePacketEvent event) {
        VoicechatConnection senderConnection = event.getSenderConnection();
        if (senderConnection == null) return;
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) return;
        UUID senderId = senderConnection.getPlayer().getUuid();
        VoicechatServerApi api = event.getVoicechat();
        byte[] opusData = event.getPacket().getOpusEncodedData().clone();
        // Voice chat dispatches microphone packets outside the Minecraft server thread.
        server.execute(() -> transmitAudio(server, api, senderId, opusData));
    }

    private void transmitAudio(MinecraftServer server, VoicechatServerApi api, UUID senderId, byte[] opusData) {
        ServerPlayer sender = server.getPlayerList().getPlayer(senderId);
        if (sender == null) return;
        var senderRadio = MBITRService.findActiveRadio(sender);
        if (senderRadio.isEmpty()) return;

        senderRadio.get().talk();
        lastPacketTick.put(senderId, sender.tickCount);
        StaticAudioChannel channel = channels.get(senderId);
        if (channel == null || channel.isClosed()) {
            channel = api.createStaticAudioChannel(UUID.randomUUID());
            if (channel == null) return;
            channels.put(senderId, channel);
        }
        channel.clearTargets();
        RadioTransmission transmission = new RadioTransmission(sender.position(), senderRadio.get().getRadioState());
        boolean hasTarget = false;
        for (ServerPlayer receiver : server.getPlayerList().getPlayers()) {
            if (receiver == sender || receiver.level() != sender.level()) continue;
            if (!MBITRService.canReceiveFromSender(receiver, transmission)) continue;
            VoicechatConnection connection = api.getConnectionOf(receiver.getUUID());
            if (connection == null) continue;
            channel.addTarget(connection);
            hasTarget = true;
        }
        RadioAudioStream stream = audioStreams.computeIfAbsent(senderId, ignored -> new RadioAudioStream(api));
        byte[] processedData = stream.process(opusData);
        if (hasTarget) channel.send(processedData);
    }

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.player instanceof ServerPlayer player)) return;
        ModuleQuery.streamMounted(player).forEach(ref -> ref.stack()
                .getCapability(CapabilityRegistries.RADIO).ifPresent(radio -> radio.scanChannel(player)));

        UUID playerId = player.getUUID();
        Integer lastTick = lastPacketTick.get(playerId);
        if (lastTick == null || player.tickCount - lastTick <= RELEASE_DELAY_TICKS) return;
        MBITRService.findActiveRadio(player).ifPresent(MBITR::endTalk);
        lastPacketTick.remove(playerId);
        StaticAudioChannel channel = channels.get(playerId);
        if (channel != null && !channel.isClosed()) channel.flush();
        closeAudioStream(playerId);
    }

    @SubscribeEvent
    public void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        UUID playerId = event.getEntity().getUUID();
        lastPacketTick.remove(playerId);
        StaticAudioChannel channel = channels.remove(playerId);
        if (channel != null && !channel.isClosed()) channel.flush();
        closeAudioStream(playerId);
    }

    private void closeAudioStream(UUID playerId) {
        RadioAudioStream stream = audioStreams.remove(playerId);
        if (stream != null) stream.close();
    }

    private static final class RadioAudioStream {
        private final OpusDecoder decoder;
        private final OpusEncoder encoder;
        private final RadioAudioProcessor processor = new RadioAudioProcessor();

        private RadioAudioStream(VoicechatServerApi api) {
            decoder = api.createDecoder();
            encoder = api.createEncoder(OpusEncoderMode.VOIP);
        }

        private byte[] process(byte[] opusData) {
            return encoder.encode(processor.process(decoder.decode(opusData)));
        }

        private void close() {
            decoder.close();
            encoder.close();
        }
    }
}
