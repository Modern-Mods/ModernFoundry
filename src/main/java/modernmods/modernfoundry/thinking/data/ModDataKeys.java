package modernmods.modernfoundry.thinking.data;

import modernmods.modernfoundry.TConstruct;
import modernmods.modernfoundry.library.tools.capability.TinkerDataCapability;

import static modernmods.modernfoundry.library.tools.capability.TinkerDataKeys.INTEGER_REGISTRY;

public interface ModDataKeys {
    static void init() {}
    TinkerDataCapability.TinkerDataKey<Integer>  SculkCatalyse = intKey("sculk_catalyse");
    TinkerDataCapability.TinkerDataKey<Integer>  Antibrute = intKey("antibrute");
    TinkerDataCapability.TinkerDataKey<Integer>  Reburning = intKey("reburning");
    TinkerDataCapability.TinkerDataKey<Integer>  SculkStruggle = intKey("sculk_struggle");
    TinkerDataCapability.TinkerDataKey<Integer>  Retransit = intKey("retransit");
    TinkerDataCapability.TinkerDataKey<Integer>  Remisdirection = intKey("remisdirection");
    TinkerDataCapability.TinkerDataKey<Integer>  SculkBoost = intKey("sculk_boost");
    private static TinkerDataCapability.TinkerDataKey<Integer> intKey(String name) {
        return INTEGER_REGISTRY.register(TConstruct.createKey(name));
    }
}
