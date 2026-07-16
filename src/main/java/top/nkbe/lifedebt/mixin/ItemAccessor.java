package top.nkbe.lifedebt.mixin;

//? if <1.20.5 {
import net.minecraft.item.FoodComponent;
//?} else {
/*import net.minecraft.component.ComponentMap;
*///?}
import net.minecraft.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * 访问 {@link Item} 的食物组件字段，以便为已注册的原版物品补充食用属性。
 */
@Mixin(Item.class)
public interface ItemAccessor {

	//? if <1.20.5 {
	/**
	 * 设置该物品的食物组件。
	 *
	 * @param foodComponent 食物属性；{@code null} 表示不可食用
	 */
	@Mutable
	@Accessor("foodComponent")
	void setFoodComponent(FoodComponent foodComponent);
	//?} else {
	/*// >=1.20.5：Item 不再持有 foodComponent 字段，食物属性存放在数据组件表（components）中，
	// 通过整体替换 ComponentMap 来为原版物品追加 FOOD 组件。
	@Mutable
	@Accessor("components")
	void setComponents(ComponentMap components);
	*///?}
}
