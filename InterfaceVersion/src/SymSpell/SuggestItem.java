package SymSpell;//        MIT License
//Copyright (c) 2018 Hampus Londögård

import java.util.Comparator;

public class SuggestItem implements Comparator<SuggestItem>, Comparable<SuggestItem>
{

    public String term;

    public int distance;

    public long count;

    public SuggestItem(String term, int distance, long count) {
        this.term = term;
        this.distance = distance;
        this.count = count;
    }

    @Override
    public int compare(SuggestItem suggestItem, SuggestItem t1) {
        return suggestItem.compareTo(t1);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof SuggestItem && term.equals(((SuggestItem) obj).term);
    }

    @Override
    public int hashCode()
    {
        return term.hashCode();
    }

    @Override
    public String toString()
    {
        return "{" + term + ", " + distance + ", " + count + "}";
    }

    @Override
    public int compareTo(SuggestItem other) {
        if (this.distance == other.distance) return Long.compare(other.count, this.count);
        return Integer.compare(this.distance, other.distance);
    }

    public SuggestItem clone(){
        return new SuggestItem(this.term, this.distance, this.count);
    }
}
