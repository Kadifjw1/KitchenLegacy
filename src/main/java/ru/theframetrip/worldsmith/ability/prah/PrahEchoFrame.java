package ru.theframetrip.worldsmith.ability.prah;

/**
 * One server-tick snapshot used by the active ability «След их праха».
 */
public record PrahEchoFrame(
        double x,
        double y,
        double z,
        float yRot,
        float xRot,
        boolean onGround,
        boolean crouching,
        boolean sprinting,
        boolean swinging
) {
}
