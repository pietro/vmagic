/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.upb.hni.vmagic.parser;

import de.upb.hni.vmagic.VhdlFile;
import de.upb.hni.vmagic.output.VhdlOutput;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import static org.junit.Assert.*;

@RunWith(Parameterized.class)
public class ParserTest {

    private static final String RESOURCES_PATH = "test/de/upb/hni/vmagic/parser/resources";

    @Parameterized.Parameters
    public static Collection files() throws IOException {
        List<File[]> files = new ArrayList<File[]>();

        File dir = new File(RESOURCES_PATH);
        for (File file : dir.listFiles()) {
            if (file.isFile()) {
                files.add(new File[]{file});
            }
        }

        return files;
    }
    private final File file;

    public ParserTest(File file) {
        this.file = file;
    }

    @Test
    //TODO: add check for parse errors
    public void test() throws IOException, VhdlParserException {
        System.out.println("Parsing " + file.getName());

        VhdlFile parsed1 = VhdlParser.parseFile(file.getPath());
        String parsedString1 = VhdlOutput.toVhdlString(parsed1);

        System.out.println("Reparsing output");

        VhdlFile parsed2 = VhdlParser.parseString(parsedString1);
        String parsedString2 = VhdlOutput.toVhdlString(parsed2);

        assertEquals(parsedString1, parsedString2);

        System.out.println();
    }
}
