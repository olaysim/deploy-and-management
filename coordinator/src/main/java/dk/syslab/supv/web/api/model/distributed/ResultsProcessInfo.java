package dk.syslab.supv.web.api.model.distributed;

import java.util.Comparator;
import java.util.SortedMap;
import java.util.TreeMap;

public class ResultsProcessInfo {
    private SortedMap<String, ResultProcessInfo> results;

    public ResultsProcessInfo() {
        results = new TreeMap<>(Comparator.naturalOrder());
    }

    public SortedMap<String, ResultProcessInfo> getResults() {
        return results;
    }

    public void setResults(SortedMap<String, ResultProcessInfo> results) {
        this.results = results;
    }

    public void add(String name, ResultProcessInfo info) {
        results.put(name, info);
    }
}


