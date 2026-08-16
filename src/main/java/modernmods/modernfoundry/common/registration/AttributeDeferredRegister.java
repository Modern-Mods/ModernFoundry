package modernmods.modernfoundry.common.registration;

/** @deprecated use {@link modernmods.hilt.registration.deferred.AttributeDeferredRegister} */
@Deprecated(forRemoval = true)
public class AttributeDeferredRegister extends modernmods.hilt.registration.deferred.AttributeDeferredRegister {
  public AttributeDeferredRegister(String modID) {
    super(modID);
  }
}
