package dev.codedsakura.blossom.tpa;

import dev.codedsakura.blossom.lib.TextUtils;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

class TpaRequest {
    public static final String TRANSLATION_KEY_TPA_TO = "blossom.tpa.to";
    public static final String TRANSLATION_KEY_TPA_HERE = "blossom.tpa.here";
    final ServerPlayerEntity teleportWho;
    final ServerPlayerEntity teleportTo;
    final ServerPlayerEntity initiator;
    final ServerPlayerEntity receiver;
    final boolean tpaHere;

    private Timer timer;

    TpaRequest(ServerPlayerEntity initiator, ServerPlayerEntity receiver, boolean tpaHere) {
        this.initiator = initiator;
        this.receiver = receiver;
        this.tpaHere = tpaHere;
        if (tpaHere) {
            this.teleportWho = receiver;
            this.teleportTo = initiator;
        } else {
            this.teleportWho = initiator;
            this.teleportTo = receiver;
        }
    }

    boolean similarTo(TpaRequest other) {
        return (this.teleportWho.equals(other.teleportWho) && this.teleportTo.equals(other.teleportTo)) ||
                (this.teleportWho.equals(other.teleportTo) && this.teleportTo.equals(other.teleportWho));
    }

    void startTimeout(Runnable onTimeout) {
        String translationKeyPrefix = tpaHere ? TRANSLATION_KEY_TPA_HERE : TRANSLATION_KEY_TPA_TO;
        timer = new Timer();
        timer.schedule(
            new TimerTask() {
                @Override
                public void run() {
                    BlossomTpa.LOGGER.info("{} timed out", this);
                    initiator.sendMessage(TextUtils.fTranslation(translationKeyPrefix + ".timeout.initiator", TextUtils.Type.ERROR, toArgs()), false);
                    receiver.sendMessage(TextUtils.fTranslation(translationKeyPrefix + ".timeout.receiver", TextUtils.Type.ERROR, toArgs()), false);
                    onTimeout.run();
                }
            },
            BlossomTpa.CONFIG.timeout * 1000L
        );
        initiator.sendMessage(TextUtils.translation(translationKeyPrefix + ".start.initiator", toArgs()), false);
        receiver.sendMessage(TextUtils.translation(translationKeyPrefix + ".start.receiver", toArgs()), false);
    }

    void cancelTimeout() {
        timer.cancel();
    }

    Object[] toArgs() {
        return new Object[]{
            BlossomTpa.CONFIG.timeout,
            initiator,
            receiver
        };
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (TpaRequest) obj;
        return Objects.equals(this.teleportWho, that.teleportWho) &&
            Objects.equals(this.teleportTo, that.teleportTo) &&
            Objects.equals(this.initiator, that.initiator) &&
            Objects.equals(this.receiver, that.receiver);
    }

    @Override
    public int hashCode() {
        return Objects.hash(teleportWho, teleportTo, initiator, receiver);
    }

    @Override
    public String toString() {
        return "TpaRequest[" +
            "teleportWho=" + teleportWho.getUuidAsString() + ", " +
            "teleportTo=" + teleportTo.getUuidAsString() + ", " +
            "initiator=" + initiator.getUuidAsString() + ", " +
            "receiver=" + receiver.getUuidAsString() + ']';
    }
}
