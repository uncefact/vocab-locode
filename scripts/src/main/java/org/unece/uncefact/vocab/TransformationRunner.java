package org.unece.uncefact.vocab;

import org.apache.commons.cli.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.unece.uncefact.vocab.transformers.*;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.unece.uncefact.vocab.Transformer.*;

public class TransformationRunner {
    final static Options options = new Options();

    public static void main(String[] args) throws ParseException, IOException, InvalidFormatException {
        Attributes mainAttributes = readProperties();
        String version = mainAttributes.getValue("Implementation-Version");

        Option transformationTypeOption = new Option("t", true, "transformation type.\n" +
                String.format("Allowed values: %s, %s.\n", "json-ld", "split") +
                String.format("Default value: %s.", "json-ld"));
        Option dirOption = new Option("d", true, "directory with csv files for split option.");
        Option prettyPrintOption = new Option("p", "pretty-print",false, "an output file to be created as a result of transformation. Default value: output.jsonld.");
        Option versionOption = new Option("?", "version", false, "display this help.");

        options.addOption(transformationTypeOption);
        options.addOption(prettyPrintOption);
        options.addOption(dirOption);
        options.addOption(versionOption);

        HelpFormatter formatter = new HelpFormatter();

        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = parser.parse(options, args);

        if (cmd.hasOption(versionOption.getOpt())) {
            formatter.printHelp(String.format("java -jar vocab-transformer-%s.jar", version), options);
            return;
        }

        Set<String> inputFileNames = new TreeSet<>();
        boolean prettyPrint = false;
        String transformationType = UNECE_NS;
        String dir = "";
        Transformer transformer = null;
        Iterator<Option> optionIterator = cmd.iterator();
        while (optionIterator.hasNext()) {
            Option option = optionIterator.next();
            String opt = StringUtils.defaultIfEmpty(option.getOpt(), "");
            if (opt.equals(prettyPrintOption.getOpt())) {
                prettyPrint = Boolean.TRUE;
            } else if (opt.equals(transformationTypeOption.getOpt())) {
                transformationType = option.getValue();
            } else if (opt.equals(dirOption.getOpt())) {
                dir = option.getValue();
            }
        }
        Set<String> defaultInputNames = new TreeSet<>();

        try (Stream<Path> walk = Files.walk(Paths.get(dir))) {
            // We want to find only regular files
            List<String> result = walk.filter(Files::isRegularFile)
                    .map(x -> x.toString()).collect(Collectors.toList());
            System.out.println(result.size());
            for(String r:result){
                System.out.println(r);
            }

            inputFileNames = new TreeSet<>(result);
        } catch (IOException e) {
            System.err.println(dir);
            e.printStackTrace();
        }
        
        switch (transformationType.toLowerCase()) {
            case "json-ld":
            default:
                transformer = new UNLOCODEToJSONLDVocabulary(inputFileNames, defaultInputNames, prettyPrint);
                break;

            case "split":
                transformer = new UNLOCODEToFilesByCode(inputFileNames, defaultInputNames, prettyPrint);
                break;
        }
        if (transformer!=null)
            transformer.transform();
    }

    public static Attributes readProperties() throws IOException {
        final InputStream resourceAsStream = ClassLoader.getSystemResourceAsStream("META-INF/MANIFEST.MF");
        final Manifest manifest = new Manifest(resourceAsStream);
        final Attributes mainAttributes = manifest.getMainAttributes();
        return mainAttributes;
    }
}
