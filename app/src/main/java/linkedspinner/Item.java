package linkedspinner;

import java.util.List;

public class Item<Value, Name> {
    Value value;
    Name name;
    Item superItem;
    List<Item> subItems;

    public Item(Value value, Name name) {
        this.value = value;
        this.name = name;
    }

    public Item(Value value, Name name, Item superItem) {
        this.value = value;
        this.name = name;
        this.superItem = superItem;
    }

    public Item(Name name, Value value, List<Item> subItems) {
        this.name = name;
        this.value = value;
        this.subItems = subItems;
    }

    public Value getValue() {
        return value;
    }

    public Name getName() {
        return name;
    }

    public List<Item> getSubItems() {
        return subItems;
    }

    public void setSubItems(List<Item> subItems) {
        this.subItems = subItems;
    }

    public Item getSuperItem() {
        return superItem;
    }

    public void setSuperItem(Item superItem) {
        this.superItem = superItem;
    }
}
