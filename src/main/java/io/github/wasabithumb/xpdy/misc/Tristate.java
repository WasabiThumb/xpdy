package io.github.wasabithumb.xpdy.misc;

import org.intellij.lang.annotations.MagicConstant;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.function.BooleanSupplier;

/**
 * An object with 3 valid states:
 * <ul>
 *     <li>{@link #UNSET} - Neither {@link #TRUE} or {@link #FALSE}</li>
 *     <li>{@link #TRUE} - Opposite of {@link #FALSE}, canonically maps to {@link Boolean#TRUE}</li>
 *     <li>{@link #FALSE} - Opposite of {@link #TRUE}, canonically maps to {@link Boolean#FALSE}</li>
 * </ul>
 * @see #isSet()
 * @see #value()
 * @see #valueOr(boolean)
 * @see #valueOr(BooleanSupplier)
 */
public final class Tristate {

    private static final int F_UNSET = 0b00;
    private static final int F_SET = 0b10;
    private static final int F_TRUE = 0b01;

    private static final String[] NAMES = new String[] {
            "unset", "", "false", "true"
    };

    public static Tristate UNSET = new Tristate(F_UNSET);
    public static Tristate TRUE = new Tristate(F_SET | F_TRUE);
    public static Tristate FALSE = new Tristate(F_SET);

    public static @NotNull Tristate of(boolean value) {
        return value ? TRUE : FALSE;
    }

    public static @NotNull Tristate of(@Nullable Boolean value) {
        if (value == null) return UNSET;
        return of(value.booleanValue());
    }

    //

    private final int mask;
    private Tristate(
            @MagicConstant(flagsFromClass = Tristate.class) int mask
    ) {
        this.mask = mask;
    }

    //

    private boolean is(
            @MagicConstant(flagsFromClass = Tristate.class) int flag
    ) {
        return (this.mask & flag) != 0;
    }

    public boolean isSet() {
        return this.is(F_SET);
    }

    public boolean value() throws UnsupportedOperationException {
        if (!this.isSet())
            throw new UnsupportedOperationException("Cannot get value of UNSET");
        return this.is(F_TRUE);
    }

    public boolean valueOr(@NotNull BooleanSupplier fn) {
        if (!this.isSet()) return fn.getAsBoolean();
        return this.is(F_TRUE);
    }

    public boolean valueOr(boolean fallback) {
        if (!this.isSet()) return fallback;
        return this.is(F_TRUE);
    }

    public @NotNull Optional<Boolean> toOptional() {
        if (this.isSet())
            return Optional.of(this.is(F_TRUE));
        return Optional.empty();
    }

    //


    @Override
    public @NotNull String toString() {
        return NAMES[this.mask];
    }

    @Override
    public int hashCode() {
        return this.mask;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (obj instanceof Tristate other) {
            return this.mask == other.mask;
        }
        return super.equals(obj);
    }

}
