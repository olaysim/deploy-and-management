package dk.syslab.supv;

import dk.syslab.supv.client.RelativePathBuilder;
import org.junit.Test;

import java.io.File;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.Assert.assertNotNull;

public class RelativePathBuilderTest {

    @Test
    public void buildPathWithStrings() throws Exception {
        RelativePathBuilder builder = RelativePathBuilder.create();
        builder.add("test.txt", "folder/haps");
        List<String> list = builder.build();
        assertNotNull(list);
    }

    @Test
    public void buildPathWithFile() throws Exception {
        String file1 = "C:\\Users\\user\\Documents\\test\\somedocument.txt";
        String file2 = "C:\\Users\\user\\Documents\\test\\haps\\someotherdocument.txt";
        String file3 = "C:\\Users\\user\\Documents\\test\\somethirddocument.txt";
        File f1 = Paths.get(file1).toFile();
        File f2 = Paths.get(file2).toFile();
        File f3 = Paths.get(file3).toFile();

        RelativePathBuilder builder = RelativePathBuilder.create();
        builder.setProgramDir(f1.getParent());
        builder.add(f1);
        builder.add(f2);
        builder.add(f3);
        List<String> list = builder.build();
        assertNotNull(list);
    }
}
