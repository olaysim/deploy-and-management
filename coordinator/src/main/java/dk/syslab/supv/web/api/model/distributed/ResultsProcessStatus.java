package dk.syslab.supv.web.api.model.distributed;

import java.util.Comparator;
import java.util.SortedMap;
import java.util.TreeMap;

public class ResultsProcessStatus {
    private SortedMap<String, ResultProcessStatus> results;

    public ResultsProcessStatus() {
        results = new TreeMap<>(Comparator.naturalOrder());
    }

    public SortedMap<String, ResultProcessStatus> getResults() {
        return results;
    }

    public void setResults(SortedMap<String, ResultProcessStatus> results) {
        this.results = results;
    }

    public void add(String name, ResultProcessStatus status) {
        results.put(name, status);
    }
}


