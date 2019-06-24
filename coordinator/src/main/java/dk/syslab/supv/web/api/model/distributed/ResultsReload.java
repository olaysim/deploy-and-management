package dk.syslab.supv.web.api.model.distributed;

import java.util.Comparator;
import java.util.SortedMap;
import java.util.TreeMap;

public class ResultsReload {
    private SortedMap<String, ResultReload> results;

    public ResultsReload() {
        results = new TreeMap<>(Comparator.naturalOrder());
    }

    public SortedMap<String, ResultReload> getResults() {
        return results;
    }

    public void setResults(SortedMap<String, ResultReload> results) {
        this.results = results;
    }

    public void add(String name, ResultReload info) {
        results.put(name, info);
    }
}


