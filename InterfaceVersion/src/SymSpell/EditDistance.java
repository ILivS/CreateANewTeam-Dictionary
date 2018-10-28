package SymSpell;//        MIT License
//Copyright (c) 2018 Hampus Londögård
public class EditDistance {
    public enum DistanceAlgorithm{
        Damerau
    }
    private String baseString;
    private DistanceAlgorithm algorithm;
    private int[] v0;
    private int[] v2;
    EditDistance(String baseString, DistanceAlgorithm algorithm)
    {
        this.baseString = baseString;
        this.algorithm = algorithm;
        if (this.baseString.isEmpty()) {
            this.baseString = null;
            return;
        }
        if (algorithm == DistanceAlgorithm.Damerau) {
            v0 = new int[baseString.length()];
            v2 = new int[baseString.length()];
        }
    }
    public int compare(String string2, int maxDistance) {
        switch (algorithm) {
            case Damerau: return DamerauLevenshteinDistance(string2, maxDistance);
        }
        throw new IllegalArgumentException("unknown DistanceAlgorithm");
    }
    public int DamerauLevenshteinDistance(String string2, int maxDistance) {
        if (baseString == null) return string2 == null ? 0 : string2.length();
        if (string2 == null || string2.isEmpty()) return baseString.length();
        String string1;
        if (baseString.length() > string2.length()) {
            string1 = string2;
            string2 = baseString;
        } else {
            string1 = baseString;
        }
        int sLen = string1.length();
        int tLen = string2.length();

        while ((sLen > 0) && (string1.charAt(sLen - 1) == string2.charAt(tLen - 1))) { sLen--; tLen--; }

        int start = 0;
        if ((string1.charAt(0) == string2.charAt(0)) || (sLen == 0)) {

            while ((start < sLen) && (string1.charAt(start) == string2.charAt(start))) start++;
            sLen -= start;
            tLen -= start;

            if (sLen == 0) return tLen;

            string2 = string2.substring(start, start + tLen);
        }
        int lenDiff = tLen - sLen;
        if ((maxDistance < 0) || (maxDistance > tLen)) {
            maxDistance = tLen;
        } else if (lenDiff > maxDistance) return -1;

        if (tLen > v0.length)
        {
            v0 = new int[tLen];
            v2 = new int[tLen];
        } else {
            for(int i = 0; i < tLen; i++) v2[i] = 0;
        }
        int j;
        for (j = 0; j < maxDistance; j++) v0[j] = j + 1;
        for (; j < tLen; j++) v0[j] = maxDistance + 1;

        int jStartOffset = maxDistance - (tLen - sLen);
        boolean haveMax = maxDistance < tLen;
        int jStart = 0;
        int jEnd = maxDistance;
        char sChar = string1.charAt(0);
        int current = 0;
        for (int i = 0; i < sLen; i++) {
            char prevsChar = sChar;
            sChar = string1.charAt(start+i);
            char tChar = string2.charAt(0);
            int left = i;
            current = left + 1;
            int nextTransCost = 0;

            jStart += (i > jStartOffset) ? 1 : 0;
            jEnd += (jEnd < tLen) ? 1 : 0;
            for (j = jStart; j < jEnd; j++) {
                int above = current;
                int thisTransCost = nextTransCost;
                nextTransCost = v2[j];
                v2[j] = current = left;
                left = v0[j];
                char prevtChar = tChar;
                tChar = string2.charAt(j);
                if (sChar != tChar) {
                    if (left < current) current = left;
                    if (above < current) current = above;
                    current++;
                    if ((i != 0) && (j != 0)
                            && (sChar == prevtChar)
                            && (prevsChar == tChar)) {
                        thisTransCost++;
                        if (thisTransCost < current) current = thisTransCost;
                    }
                }
                v0[j] = current;
            }
            if (haveMax && (v0[i + lenDiff] > maxDistance)) return -1;
        }
        return (current <= maxDistance) ? current : -1;
    }
}
