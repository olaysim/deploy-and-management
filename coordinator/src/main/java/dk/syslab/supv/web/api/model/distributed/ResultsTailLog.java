package dk.syslab.supv.web.api.model.distributed;

import java.util.Comparator;
import java.util.SortedMap;
import java.util.TreeMap;

public class ResultsTailLog {
    private SortedMap<String, ResultTailLog> results;

    public ResultsTailLog() {
        results = new TreeMap<>(Comparator.naturalOrder());
    }

    public SortedMap<String, ResultTailLog> getResults() {
        return results;
    }

    public void setResults(SortedMap<String, ResultTailLog> results) {
        this.results = results;
    }

    public void add(String name, ResultTailLog log) {
        results.put(name, log);
    }
}


