package com.oierbravo.createsifter.content.contraptions.components.sifter;

import com.oierbravo.createsifter.content.contraptions.components.meshes.BaseMesh;
import com.oierbravo.createsifter.register.ModShapes;
import com.oierbravo.createsifter.register.ModTiles;
import com.simibubi.create.content.contraptions.base.KineticBlock;
import com.simibubi.create.content.contraptions.relays.elementary.ICogWheel;
import com.simibubi.create.foundation.block.ITE;
import com.simibubi.create.foundation.item.ItemHelper;
import com.simibubi.create.foundation.utility.Iterate;
import io.github.fabricators_of_create.porting_lib.transfer.TransferUtil;
import io.github.fabricators_of_create.porting_lib.transfer.item.ItemStackHandler;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class SifterBlock  extends KineticBlock implements ITE<SifterTileEntity>, ICogWheel {
    public SifterBlock(Properties properties) {
        super(properties);
        registerDefaultState(super.defaultBlockState().setValue(BlockStateProperties.WATERLOGGED, false));
    }


    @Override
    public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
        return ModShapes.SIFTER;
    }

    @Override
    public boolean hasShaftTowards(LevelReader world, BlockPos pos, BlockState state, Direction face) {
        return face == Direction.DOWN;
    }

    @Override
    public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player player, InteractionHand handIn,
                                 BlockHitResult hit) {
        SifterTileEntity sifterTileEntity = (SifterTileEntity) worldIn.getBlockEntity(pos);
        ItemStack handInStack = player.getItemInHand(handIn);

        if (worldIn.isClientSide)
            return InteractionResult.SUCCESS;
        //if(handInStack.is())
        if(handInStack.getItem() instanceof BaseMesh){
        //if(handInStack.is(ModTags.ModItemTags.MESHES.tag)){
            sifterTileEntity.insertMesh(handInStack, player);
      //  }
      //  if(handInStack.sameItem(new ItemStack(ModItems.ANDESITE_MESH.get(),1))){
        //if(handInStack.sameItem(new ItemStack(ModItems.ANDESITE_MESH.get(),1))){

        }
        if(handInStack.isEmpty() && sifterTileEntity.hasMesh() && player.isShiftKeyDown()){
            sifterTileEntity.removeMesh(player);
        }
        if (!handInStack.isEmpty())
            return InteractionResult.PASS;
       //if(player.getItemInHand(handIn).isEmpty() &&){

        //}

        withTileEntityDo(worldIn, pos, sifter -> {
            boolean emptyOutput = true;
            ItemStackHandler inv = sifter.outputInv;
            for (int slot = 0; slot < inv.getSlots(); slot++) {
                ItemStack stackInSlot = inv.getStackInSlot(slot);
                if (!stackInSlot.isEmpty())
                    emptyOutput = false;
                player.getInventory()
                        .placeItemBackInInventory(stackInSlot);
                inv.setStackInSlot(slot, ItemStack.EMPTY);
            }

            if (emptyOutput) {
                inv = sifter.inputInv;
                for (int slot = 0; slot < inv.getSlots(); slot++) {
                    player.getInventory()
                            .placeItemBackInInventory(inv.getStackInSlot(slot));
                    inv.setStackInSlot(slot, ItemStack.EMPTY);
                }
            }

            sifter.setChanged();
            sifter.sendData();
        });

        return InteractionResult.SUCCESS;
    }

    @Override
    public void updateEntityAfterFallOn(BlockGetter worldIn, Entity entityIn) {
        super.updateEntityAfterFallOn(worldIn, entityIn);

        if (entityIn.level.isClientSide)
            return;
        if (!(entityIn instanceof ItemEntity))
            return;
        if (!entityIn.isAlive())
            return;

        SifterTileEntity sifter = null;
        for (BlockPos pos : Iterate.hereAndBelow(entityIn.blockPosition()))
            if (sifter == null)
                sifter = getTileEntity(worldIn, pos);

        if (sifter == null)
            return;

        ItemEntity itemEntity = (ItemEntity) entityIn;
        Storage<ItemVariant> handler = TransferUtil.getItemStorage(sifter);
        if (handler == null)
            return;

        ItemStack stack = itemEntity.getItem();
        long remainder = stack.getCount() - TransferUtil.insertItem(handler, stack);
        if (remainder <= 0)
            itemEntity.discard();
        if (remainder < itemEntity.getItem()
                .getCount()) {
            ItemStack cloned = stack.copy();
            cloned.setCount((int) remainder);
            itemEntity.setItem(cloned);
        }
    }

    @Override
    public void onRemove(BlockState state, Level worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
        if (state.hasBlockEntity() && state.getBlock() != newState.getBlock()) {
            withTileEntityDo(worldIn, pos, te -> {
                ItemHelper.dropContents(worldIn, pos, te.inputInv);
                ItemHelper.dropContents(worldIn, pos, te.meshInv);
                ItemHelper.dropContents(worldIn, pos, te.outputInv);
            });

            worldIn.removeBlockEntity(pos);
        }
    }

    @Override
    public Direction.Axis getRotationAxis(BlockState state) {
        return Direction.Axis.Y;
    }

    @Override
    public Class<SifterTileEntity> getTileEntityClass() {
        return SifterTileEntity.class;
    }

    @Override
    public BlockEntityType<? extends SifterTileEntity> getTileEntityType() {
        return ModTiles.SIFTER.get();
    }

    @Override
    public boolean isPathfindable(BlockState state, BlockGetter reader, BlockPos pos, PathComputationType type) {
        return false;
    }
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        FluidState FluidState = context.getLevel().getFluidState(context.getClickedPos());
        return super.getStateForPlacement(context).setValue(BlockStateProperties.WATERLOGGED, Boolean.valueOf(FluidState.getType() == Fluids.WATER));
    }
    @Override
    public FluidState getFluidState(BlockState state) {
        return state.getValue(BlockStateProperties.WATERLOGGED) ? Fluids.WATER.getSource(false) : Fluids.EMPTY.defaultFluidState();
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(BlockStateProperties.WATERLOGGED);
        super.createBlockStateDefinition(builder);
    }
}
