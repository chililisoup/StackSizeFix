package dev.chililisoup.stacksizefix.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.mojang.serialization.DataResult;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.util.Unit;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {
    @WrapMethod(method = "getMaxStackSize")
    public int allowBadStackSizes(Operation<Integer> original) {
        return Math.max(original.call(), ((ItemStack) (Object) this).getCount());
    }

    @Inject(method = "validateComponents", at = @At("HEAD"), cancellable = true)
    private static void validateBadStacks(DataComponentMap components, CallbackInfoReturnable<DataResult<Unit>> cir) {
        cir.setReturnValue(DataResult.success(Unit.INSTANCE));
    }

    @Redirect(method = "validateStrict", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;getMaxStackSize()I"))
    private static int validateBadStackSizes(ItemStack stack) {
        return stack.getCount();
    }
}
