package SymSpell;//        MIT License
//Copyright (c) 2018 Hampus Londögård
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SymSpell {
    public enum Verbosity{
        Top,
        Closest,
        All
    }
    private static int defaultMaxEditDistance = 2;
    private static int  defaultPrefixLength = 7;
    private static int  defaultCountThreshold = 1;
    private static int  defaultInitialCapacity = 16;
    private static int  defaultCompactLevel = 5;
    private int initialCapacity;
    private int maxDictionaryEditDistance;
    private int prefixLength;
    private long countThreshold;
    private int compactMask;
    private EditDistance.DistanceAlgorithm distanceAlgorithm = EditDistance.DistanceAlgorithm.Damerau;
    private int maxLength;
    private Map<Integer, String[]> deletes;
    private Map<String, Long> words;

    private Map<String, Long> belowThresholdWords = new HashMap<>();
    public SymSpell(int initialCapacity, int maxDictionaryEditDistance, int prefixLength, int countThreshold)//,

    {
        if (initialCapacity < 0) initialCapacity = defaultInitialCapacity;
        if (maxDictionaryEditDistance < 0) maxDictionaryEditDistance = defaultMaxEditDistance;
        if (prefixLength < 1 || prefixLength <= maxDictionaryEditDistance) prefixLength = defaultPrefixLength;
        if (countThreshold < 0) countThreshold = defaultCountThreshold;

        this.initialCapacity = initialCapacity;
        this.words = new HashMap<>(initialCapacity);
        this.maxDictionaryEditDistance = maxDictionaryEditDistance;
        this.prefixLength = prefixLength;
        this.countThreshold = countThreshold;

        this.compactMask = (0xffffffff >> (3 + defaultCompactLevel)) << 2;
    }

    public boolean createDictionaryEntry(String key, long count, SuggestionStage staging) {
        if (count <= 0) {
            if (this.countThreshold > 0) return false;
            count = 0;
        }
        long countPrevious;

        if (countThreshold > 1 && belowThresholdWords.containsKey(key)) {
            countPrevious = belowThresholdWords.get(key);

            count = (Long.MAX_VALUE - countPrevious > count) ? countPrevious + count : Long.MAX_VALUE;

            if (count >= countThreshold) {
                belowThresholdWords.remove(key);
            } else {
                belowThresholdWords.put(key, count);
                return false;
            }
        }
        else if (words.containsKey(key)) {
            countPrevious = words.get(key);
            count = (Long.MAX_VALUE - countPrevious > count) ? countPrevious + count : Long.MAX_VALUE;
            words.put(key, count);
            return false;
        } else if (count < countThreshold) {
            belowThresholdWords.put(key, count);
            return false;
        }
        words.put(key, count);
        if(key.equals("can't")) System.out.println("Added to words..!");

        if (key.length() > maxLength) maxLength = key.length();

        HashSet<String> edits = editsPrefix(key);

        if (staging != null){
            edits.forEach(delete -> staging.add(getStringHash(delete), key));
        } else {
            if (deletes == null) this.deletes = new HashMap<>(initialCapacity);

            edits.forEach(delete -> {
                int deleteHash = getStringHash(delete);
                String[] suggestions;
                if (deletes.containsKey(deleteHash)){
                    suggestions = deletes.get(deleteHash);
                    String[] newSuggestions = Arrays.copyOf(suggestions, suggestions.length + 1);
                    deletes.put(deleteHash, newSuggestions);
                    suggestions = newSuggestions;
                } else {
                    suggestions = new String[1];
                    deletes.put(deleteHash, suggestions);
                }
                suggestions[suggestions.length - 1] = key;
            });
        }
        return true;
    }

    public boolean loadDictionary(String corpus, int termIndex, int countIndex) {
        File file = new File(corpus);
        if (!file.exists()) return false;

        BufferedReader br = null;
        try {
            br = Files.newBufferedReader(Paths.get(corpus), StandardCharsets.UTF_8);
        }catch (IOException ex){
            System.out.println(ex.getMessage());
        }
        if (br == null) { return false; }
        return loadDictionary(br, termIndex, countIndex);
    }

    public boolean loadDictionary(InputStream corpus, int termIndex, int countIndex) {
        if (corpus == null) return false;
        BufferedReader br = new BufferedReader(new InputStreamReader(corpus, StandardCharsets.UTF_8));
        return loadDictionary(br, termIndex, countIndex);
    }

    public boolean loadDictionary(BufferedReader br, int termIndex, int countIndex) {
        if (br == null) return false;
        
        SuggestionStage staging = new SuggestionStage(16384);
        try {
            for(String line; (line = br.readLine()) != null;){
                String[] lineParts = line.split("\\s");
                if (lineParts.length >= 2) {
                    String key = lineParts[termIndex];
                    long count;
                    try{
                        count = Long.parseLong(lineParts[countIndex]);
                        createDictionaryEntry(key, count, staging);
                    }catch (NumberFormatException ex){
                        System.out.println(ex.getMessage());
                    }
                }
            }
        }catch (IOException ex){
            System.out.println(ex.getMessage());
        }
        if (this.deletes == null) this.deletes = new HashMap<>(staging.deleteCount());
        commitStaged(staging);
        return true;
    }

    public boolean createDictionary(String corpus) {
        File file = new File(corpus);
        if (!file.exists()) return false;

        SuggestionStage staging = new SuggestionStage(16384);
        try (BufferedReader br = Files.newBufferedReader(Paths.get(corpus))) {
            for (String line; (line = br.readLine()) != null; ) {
                Arrays.stream(parseWords(line)).forEach(key -> createDictionaryEntry(key, 1, staging));
            }
        }catch (IOException ex){
            System.out.println(ex.getMessage());
        }

        if (this.deletes == null) this.deletes = new HashMap<>(staging.deleteCount());
        commitStaged(staging);
        return true;
    }

    public void purgeBelowThresholdWords() {
        belowThresholdWords = new HashMap<String, Long>();
    }
    public void commitStaged(SuggestionStage staging) {
        staging.commitTo(deletes);
    }
    public List<SuggestItem> lookup(String input, Verbosity verbosity) {
        return lookup(input, verbosity, maxDictionaryEditDistance);
    }
    public List<SuggestItem> lookup(String input, Verbosity verbosity, int maxEditDistance) {
        if (maxEditDistance > maxDictionaryEditDistance) throw new IllegalArgumentException("Dist to big: " + maxEditDistance);

        List<SuggestItem> suggestions = new ArrayList<>();
        int inputLen = input.length();

        if (inputLen - maxEditDistance > maxLength) return suggestions;

        HashSet<String> consideredDeletes = new HashSet<>();

        HashSet<String> consideredSuggestions = new HashSet<>();
        long suggestionCount;

        if (words.containsKey(input)) {
            suggestionCount = words.get(input);
            suggestions.add(new SuggestItem(input, 0, suggestionCount));
            if (verbosity != Verbosity.All) return suggestions;
        }
        consideredSuggestions.add(input);

        int maxEditDistance2 = maxEditDistance;
        int candidatePointer = 0;
        List<String> candidates = new ArrayList<>();


        int inputPrefixLen = inputLen;
        if (inputPrefixLen > prefixLength) {
            inputPrefixLen = prefixLength;
            candidates.add(input.substring(0, inputPrefixLen));
        } else {
            candidates.add(input);
        }

        EditDistance distanceComparer = new EditDistance(input, this.distanceAlgorithm);
        while (candidatePointer < candidates.size()) {
            String candidate = candidates.get(candidatePointer++);
            int candidateLen = candidate.length();
            int lengthDiff = inputPrefixLen - candidateLen;


            if (lengthDiff > maxEditDistance2) {

                if (verbosity == Verbosity.All) continue;
                break;
            }


            if (deletes.containsKey(getStringHash(candidate))) {
                String[] dictSuggestions = deletes.get(getStringHash(candidate));
                for (String suggestion : dictSuggestions) {
                    if (suggestion.equals(input)) continue;
                    int suggestionLen = suggestion.length();

                    if ((Math.abs(suggestionLen - inputLen) > maxEditDistance2)
                            || (suggestionLen < candidateLen)
                            || (suggestionLen == candidateLen && !suggestion.equals(candidate)))
                        continue;

                    int suggPrefixLen = Math.min(suggestionLen, prefixLength);
                    if (suggPrefixLen > inputPrefixLen && (suggPrefixLen - candidateLen) > maxEditDistance2) continue;

                    int distance;
                    int min = 0;
                    if (candidateLen == 0) {
                        distance = Math.max(inputLen, suggestionLen);
                        if (distance > maxEditDistance2 || !consideredSuggestions.add(suggestion)) continue;
                    } else if (suggestionLen == 1) {
                        if (input.indexOf(suggestion.charAt(0)) < 0) distance = inputLen;
                        else distance = inputLen - 1;
                        if (distance > maxEditDistance2 || !consideredSuggestions.add(suggestion)) continue;
                    } else
                        if ((prefixLength - maxEditDistance == candidateLen)
                                && (((min = Math.min(inputLen, suggestionLen) - prefixLength) > 1)
                                && !(input.substring(inputLen + 1 - min).equals(suggestion.substring(suggestionLen + 1 - min))))
                                || ((min > 0) && (input.charAt(inputLen - min) != suggestion.charAt(suggestionLen - min))
                                && ((input.charAt(inputLen - min - 1) != suggestion.charAt(suggestionLen - min))
                                || (input.charAt(inputLen - min) != suggestion.charAt(suggestionLen - min - 1))))) {
                            continue;
                        } else {
                            if ((verbosity != Verbosity.All && !deleteInSuggestionPrefix(candidate, candidateLen, suggestion, suggestionLen))
                                    || !consideredSuggestions.add(suggestion)) continue;
                            distance = distanceComparer.compare(suggestion, maxEditDistance2);
                            if (distance < 0) continue;
                        }
                    if (distance <= maxEditDistance2) {
                        suggestionCount = words.get(suggestion);
                        SuggestItem si = new SuggestItem(suggestion, distance, suggestionCount);
                        if (suggestions.size() > 0) {
                            switch (verbosity) {
                                case Closest:
                                    if (distance < maxEditDistance2) suggestions.clear();
                                    break;
                                case Top:
                                    if (distance < maxEditDistance2 || suggestionCount > suggestions.get(0).count) {
                                        maxEditDistance2 = distance;
                                        suggestions.set(0, si);
                                    }
                                    continue;
                            }
                        }
                        if (verbosity != Verbosity.All) maxEditDistance2 = distance;
                        suggestions.add(si);
                    }
                }
            }
            if ((lengthDiff < maxEditDistance) && (candidateLen <= prefixLength))
            {
                if (verbosity != Verbosity.All && lengthDiff >= maxEditDistance2) continue;

                for (int i = 0; i < candidateLen; i++)
                {
                    StringBuilder sb = new StringBuilder(candidate);
                    sb.deleteCharAt(i);
                    String delete = sb.toString();

                    if (consideredDeletes.add(delete)) { candidates.add(delete); }
                }
            }
        }

        if (suggestions.size() > 1) Collections.sort(suggestions);
        return suggestions;
    }



    private boolean deleteInSuggestionPrefix(String delete, int deleteLen, String suggestion, int suggestionLen) {
        if (deleteLen == 0) return true;
        if (prefixLength < suggestionLen) suggestionLen = prefixLength;
        int j = 0;
        for (int i = 0; i < deleteLen; i++)
        {
            char delChar = delete.charAt(i);
            while (j < suggestionLen && delChar != suggestion.charAt(j)) j++;
            if (j == suggestionLen) return false;
        }
        return true;
    }

    private String[] parseWords(String text) {
        Pattern pattern = Pattern.compile("['’\\p{L}-[_]]+");
        Matcher match = pattern.matcher(text.toLowerCase());
        List<String> matches = new ArrayList<>();
        while(match.find()){
            matches.add(match.group());
        }
        String[] toreturn = new String[matches.size()];
        matches.toArray(toreturn);
        return toreturn;
    }

    private HashSet<String> edits(String word, int editDistance, HashSet<String> deleteWords) {
        editDistance++;
        if (word.length() > 1) {
            for (int i = 0; i < word.length(); i++) {
                StringBuilder sb = new StringBuilder(word);
                sb.deleteCharAt(i);
                String delete = sb.toString();
                if (deleteWords.add(delete)) {
                    if (editDistance < maxDictionaryEditDistance) edits(delete, editDistance, deleteWords);
                }
            }
        }
        return deleteWords;
    }

    private HashSet<String> editsPrefix(String key) {
        HashSet<String> hashSet = new HashSet<>();
        if (key.length() <= maxDictionaryEditDistance) hashSet.add("");
        if (key.length() > prefixLength) key = key.substring(0, prefixLength);
        hashSet.add(key);
        return edits(key, 0, hashSet);
    }

    @SuppressWarnings("unchecked")
    private int getStringHash(String s) {
        int len = s.length();
        int lenMask = len;
        if (lenMask > 3) lenMask = 3;

        long hash = 2166136261L;
        for (int i = 0; i < len; i++) {
                hash ^= s.charAt(i);
                hash *= 16777619;
        }

        hash &= this.compactMask;
        hash |= (long)lenMask;
        return (int)hash;
    }
}

