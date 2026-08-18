package modernmods.modernfoundry.library.materials.json;

import net.neoforged.neoforge.common.conditions.ICondition;
import org.jetbrains.annotations.ApiStatus.Internal;
import modernmods.modernfoundry.library.json.JsonRedirect;

import javax.annotation.Nullable;

@Internal
public class MaterialJson {
  @Nullable
  private final ICondition condition;
  @Nullable
  private final Boolean craftable;
  @Nullable
  private final Integer tier;
  @Nullable
  private final Integer sortOrder;
  @Nullable
  private final Boolean hidden;
  @Nullable
  private final JsonRedirect[] redirect;

  public MaterialJson(@Nullable ICondition condition, @Nullable Boolean craftable, @Nullable Integer tier, @Nullable Integer sortOrder, @Nullable Boolean hidden, @Nullable JsonRedirect[] redirect) {
    this.condition = condition;
    this.craftable = craftable;
    this.tier = tier;
    this.sortOrder = sortOrder;
    this.hidden = hidden;
    this.redirect = redirect;
  }

  @Nullable
  public ICondition getCondition() {
    return condition;
  }

  @Nullable
  public Boolean getCraftable() {
    return craftable;
  }

  @Nullable
  public Integer getTier() {
    return tier;
  }

  @Nullable
  public Integer getSortOrder() {
    return sortOrder;
  }

  @Nullable
  public Boolean getHidden() {
    return hidden;
  }

  @Nullable
  public JsonRedirect[] getRedirect() {
    return redirect;
  }
}
