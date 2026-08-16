package modernmods.modernfoundry.library.modifiers.fluid;

import modernmods.hilt.data.loadable.primitive.EnumLoadable;

/** Helper for effects with time that wish to either add or set */
public enum TimeAction {
  /** Adds the time to the current time */
  ADD,
  /** Sets the time */
  SET;

  public static final EnumLoadable<TimeAction> LOADABLE = new EnumLoadable<>(TimeAction.class);
}
