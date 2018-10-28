package SymSpell;
//        MIT License
//Copyright (c) 2018 Hampus Londögård
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;


public class SuggestionStage {
    SuggestionStage(int initialCapacity) {
        deletes = new HashMap<>(initialCapacity);
        nodes = new ChunkArray<Node>(initialCapacity * 2);
    }

    public class Node {
        public String suggestion;
        public int next;
        public Node(String suggestion, int next) {
            this.suggestion = suggestion;
            this.next = next;
        }
    }
    public class Entry {
        public int count;
        public int first;
        Entry(int count, int first) {
            this.count = count;
            this.first = first;
        }
    }
    public Map<Integer, Entry> deletes; // {get; set; }
    public ChunkArray<Node> nodes;

    public int deleteCount() { return deletes.size(); }

    public int nodeCount() { return nodes.count; }

    public void clear() {
        deletes.clear();
        nodes.clear();
    }

    void add(int deleteHash, String suggestion) {
        Entry entry = deletes.getOrDefault(deleteHash, new Entry(0, -1));
        int next = entry.first;
        entry.count++;
        entry.first = nodes.count;
        deletes.put(deleteHash, entry);
        nodes.add(new Node(suggestion, next));
    }

    void commitTo(Map<Integer, String[]> permanentDeletes) {
        deletes.forEach((key, value) -> {
            int i;
            String[] suggestions;
            if (permanentDeletes.containsKey(key)) {
                suggestions = permanentDeletes.get(key);
                i = suggestions.length;
                String[] newSuggestion = Arrays.copyOf(suggestions, i + value.count);

                permanentDeletes.put(key, newSuggestion);
                suggestions = newSuggestion;
            } else {
                i = 0;
                suggestions = new String[value.count];
                permanentDeletes.put(key, suggestions);
            }
            int next = value.first;
            Node node;
            while (next >= 0) {
                node = nodes.getValues(next);
                suggestions[i] = node.suggestion;
                next = node.next;
                i++;
            }
        });
    }
}