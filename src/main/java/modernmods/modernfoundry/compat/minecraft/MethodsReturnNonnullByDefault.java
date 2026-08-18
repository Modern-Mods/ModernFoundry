package modernmods.modernfoundry.compat.minecraft;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import javax.annotation.Nonnull;
import javax.annotation.meta.TypeQualifierDefault;

/**
 * Restored copy of the vanilla annotation that was removed in 26.1.2.
 * Tinkers' package-info files reference it pervasively; keeping a source-level
 * copy avoids touching 170+ files.
 */
@Nonnull
@TypeQualifierDefault({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface MethodsReturnNonnullByDefault {
}
