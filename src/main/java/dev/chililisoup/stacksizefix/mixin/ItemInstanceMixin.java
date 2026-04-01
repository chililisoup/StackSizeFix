//? if >= 26 {
package dev.chililisoup.stacksizefix.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.world.item.ItemInstance;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ItemInstance.class)
public interface ItemInstanceMixin {
    @WrapMethod(method = "getMaxStackSize")
    default int allowBadStackSizes(Operation<Integer> original) {
        return Math.max(original.call(), ((ItemInstance) this).count());
    }
}
//?}