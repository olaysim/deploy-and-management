package dk.syslab.supv.web.api.model;

import java.util.ArrayList;
import java.util.List;

public class Group {
    String name;
    List<String> programs;

//    public Group() {
//        programs = new ArrayList<>();
//    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getPrograms() {
        return programs;
    }

    public void setPrograms(List<String> programs) {
        this.programs = programs;
    }
}
