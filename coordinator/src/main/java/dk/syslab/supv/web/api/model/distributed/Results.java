package dk.syslab.supv.web.api.model.distributed;

import java.util.*;

public class Results {
    private SortedMap<String, ResultStatus> results;

    public Results() {
        results = new TreeMap<>(Comparator.naturalOrder());
    }

    public SortedMap<String, ResultStatus> getResults() {
        return results;
    }

    public void setResults(SortedMap<String, ResultStatus> results) {
        this.results = results;
    }

    public void add(String name, ResultStatus status) {
        results.put(name, status);
    }
}


