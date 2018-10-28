package SymSpell;//        MIT License
//Copyright (c) 2018 Hampus Londögård
import java.util.Arrays;
import SymSpell.SuggestionStage.Node;
public class ChunkArray<T>
{
    private static int chunkSize = 4096;
    private static int divShift = 12;
    public Node[][] values;
    public int count;

    ChunkArray(int initialCapacity)
    {
        int chunks = (initialCapacity + chunkSize - 1) / chunkSize;
        values = new Node[chunks][];
        for (int i = 0; i < values.length; i++) values[i] = new Node[chunkSize];
    }

    public int add(Node value)
    {
        if (count == capacity()) {
            Node[][] newValues = Arrays.copyOf(values, values.length + 1);
            newValues[values.length] = new Node[chunkSize];
            values = newValues;
        }

        values[row(count)][col(count)] = value;
        count++;
        return count - 1;
    }

    public void clear()
    {
        count = 0;
    }

    public Node getValues(int index) {
        return values[row(index)][col(index)];
    }
    public void setValues(int index, Node value){
        values[row(index)][col(index)] = value;
    }
    public void setValues(int index, Node value, Node[][] list){
        list[row(index)][col(index)] = value;
    }

    private int row(int index) { return index >> divShift; }
    private int col(int index) { return index & (chunkSize - 1); }
    private int capacity() { return values.length * chunkSize; }
}