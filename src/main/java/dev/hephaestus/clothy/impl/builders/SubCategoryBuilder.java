package dev.hephaestus.clothy.impl.builders;

import com.google.common.collect.Lists;
import dev.hephaestus.clothy.api.AbstractConfigListEntry;
import dev.hephaestus.clothy.api.EntryContainer;
import dev.hephaestus.clothy.impl.gui.entries.SubCategoryListEntry;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

import java.util.*;

@Environment(EnvType.CLIENT)
public class SubCategoryBuilder extends FieldBuilder<Object, SubCategoryListEntry> implements List<AbstractConfigListEntry<?>>, EntryContainer {
    private final List<AbstractConfigListEntry<?>> entries;
    private boolean expanded = false;
    
    public SubCategoryBuilder(Text resetButtonKey, Text fieldNameKey) {
        super(resetButtonKey, fieldNameKey);
        this.entries = Lists.newArrayList();
    }

    @Override
    protected SubCategoryListEntry withValue(Object value) {
        SubCategoryListEntry entry = new SubCategoryListEntry(getFieldNameKey(), entries, expanded);
        entry.setTooltipSupplier(o -> tooltipSupplier.apply(entry.getValue()));
        return entry;
    }

    public SubCategoryBuilder setExpanded(boolean expanded) {
        this.expanded = expanded;
        return this;
    }
    
    @Override
    public int size() {
        return entries.size();
    }
    
    @Override
    public boolean isEmpty() {
        return entries.isEmpty();
    }
    
    @Override
    public boolean contains(Object o) {
        return entries.contains(o);
    }
    
    @Override
    public @NotNull Iterator<AbstractConfigListEntry<?>> iterator() {
        return entries.iterator();
    }
    
    @Override
    public Object[] toArray() {
        return entries.toArray();
    }
    
    @Override
    public <T> T[] toArray(T[] a) {
        return entries.toArray(a);
    }
    
    @Override
    public boolean add(AbstractConfigListEntry abstractConfigListEntry) {
        return entries.add(abstractConfigListEntry);
    }
    
    @Override
    public boolean remove(Object o) {
        return entries.remove(o);
    }
    
    @Override
    public boolean containsAll(@NotNull Collection<?> c) {
        return entries.containsAll(c);
    }
    
    @Override
    public boolean addAll(@NotNull Collection<? extends AbstractConfigListEntry<?>> c) {
        return entries.addAll(c);
    }
    
    @Override
    public boolean addAll(int index, @NotNull Collection<? extends AbstractConfigListEntry<?>> c) {
        return entries.addAll(index, c);
    }
    
    @Override
    public boolean removeAll(Collection<?> c) {
        return entries.removeAll(c);
    }
    
    @Override
    public boolean retainAll(Collection<?> c) {
        return entries.retainAll(c);
    }
    
    @Override
    public void clear() {
        entries.clear();
    }
    
    @Override
    public AbstractConfigListEntry get(int index) {
        return entries.get(index);
    }
    
    @Override
    public AbstractConfigListEntry set(int index, AbstractConfigListEntry element) {
        return entries.set(index, element);
    }
    
    @Override
    public void add(int index, AbstractConfigListEntry element) {
        entries.add(index, element);
    }
    
    @Override
    public AbstractConfigListEntry remove(int index) {
        return entries.remove(index);
    }
    
    @Override
    public int indexOf(Object o) {
        return entries.indexOf(o);
    }
    
    @Override
    public int lastIndexOf(Object o) {
        return entries.lastIndexOf(o);
    }
    
    @Override
    public @NotNull ListIterator<AbstractConfigListEntry<?>> listIterator() {
        return entries.listIterator();
    }
    
    @Override
    public @NotNull ListIterator<AbstractConfigListEntry<?>> listIterator(int index) {
        return entries.listIterator(index);
    }
    
    @Override
    public @NotNull List<AbstractConfigListEntry<?>> subList(int fromIndex, int toIndex) {
        return entries.subList(fromIndex, toIndex);
    }

    @Override
    public EntryContainer addEntry(AbstractConfigListEntry<?> entry) {
        this.add(entry);
        return this;
    }
}
