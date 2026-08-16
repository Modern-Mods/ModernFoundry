package modernmods.modernfoundry.library.json.variable.block;

import net.minecraft.world.level.block.state.BlockState;
import modernmods.hilt.data.loadable.record.RecordLoadable;
import modernmods.hilt.data.registry.GenericLoaderRegistry;
import modernmods.hilt.data.registry.GenericLoaderRegistry.IHaveLoader;
import modernmods.modernfoundry.library.json.variable.ToFloatFunction;
import modernmods.modernfoundry.library.json.variable.VariableLoaderRegistry;

import static modernmods.hilt.data.loadable.record.SingletonLoader.singleton;

/** Variable that fetches a property from a block state */
public interface BlockVariable extends IHaveLoader {
  GenericLoaderRegistry<BlockVariable> LOADER = new VariableLoaderRegistry<>("Block Variable", Constant::new);

  /** Gets a value from the given block state */
  float getValue(BlockState state);

  @Override
  RecordLoadable<? extends BlockVariable> getLoader();

  /* Singletons */

  /** Creates a new singleton variable getter */
  static BlockVariable simple(ToFloatFunction<BlockState> getter) {
    return singleton(loader -> new BlockVariable() {
      @Override
      public float getValue(BlockState state) {
        return getter.apply(state);
      }

      @Override
      public RecordLoadable<? extends BlockVariable> getLoader() {
        return loader;
      }
    });
  }

  /** Gets the block's blast resistance */
  BlockVariable BLAST_RESISTANCE = simple(state -> state.getBlock().getExplosionResistance());
  /** Gets the block's hardness */
  BlockVariable HARDNESS = simple(state -> state.getBlock().defaultDestroyTime());


  /** Constant value instance for this object */
  record Constant(float value) implements VariableLoaderRegistry.ConstantFloat, BlockVariable {
    public static final RecordLoadable<Constant> LOADER = VariableLoaderRegistry.constantLoader(Constant::new);

    @Override
    public float getValue(BlockState state) {
      return value;
    }

    @Override
    public RecordLoadable<Constant> getLoader() {
      return LOADER;
    }
  }
}
