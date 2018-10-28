package SymSpell;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import main.Main;

import java.util.List;

public class SymSpellTrigger {
    private  int maxEditDistanceLookup=3;
    private  static  SymSpell.Verbosity suggestionVerbosity = SymSpell.Verbosity.All;
    private  static  SymSpell symSpell= Main.getSymSpell();
    private List<SuggestItem> lookup(String input) {
        return symSpell.lookup(input, suggestionVerbosity, maxEditDistanceLookup);
    }
    public static  ObservableList<String>  lookUp(String word,ObservableList items)
    {
        ObservableList<String> suggestList = FXCollections.observableArrayList();
        List<SuggestItem> suggestions = symSpell.lookup(word, suggestionVerbosity);
        suggestions.stream().limit(10).forEach(suggestion ->
        {
            if (items.contains(suggestion.term))
            suggestList.add(suggestion.term);
        });
        return  suggestList;
    }
}
