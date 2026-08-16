package modernmods.modernfoundry.library.recipe.tinkerstation;

import modernmods.modernfoundry.library.recipe.ITinkerableContainer;

/**
 * Extension of {@link ITinkerStationContainer} to allow modifying inventory contents
 */
public interface IMutableTinkerStationContainer extends ITinkerStationContainer, ITinkerableContainer.Mutable {}
