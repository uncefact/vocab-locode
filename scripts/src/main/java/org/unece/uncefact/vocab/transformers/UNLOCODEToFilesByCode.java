package org.unece.uncefact.vocab.transformers;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.unece.uncefact.vocab.FileGenerator;
import org.unece.uncefact.vocab.Transformer;

import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class UNLOCODEToFilesByCode extends Transformer {
    public UNLOCODEToFilesByCode(Set<String> inputFiles, Set<String> defaultInputFiles, boolean prettyPrint) {
        super(null);
        setInputFiles(inputFiles);
        setDefaultInputFiles(defaultInputFiles);

    }

    protected JsonObjectBuilder getContext() {
        JsonObjectBuilder result = super.getMinimalContext();
        for (String ns :Arrays.asList(UNLOCODE_VOCAB_NS, RDF_NS)){
            result.add(ns, NS_MAP.get(ns));
        }
        return result;
    }


    public void transform() throws IOException {
        Map<String, Set<CSVRecord>> locodesByCountries = new TreeMap<>();
        if (inputFiles.isEmpty()){
            for (String file : defaultInputFiles) {
                InputStream in = getClass().getResourceAsStream(file);
                BufferedReader reader = new BufferedReader(new InputStreamReader(in, Charset.forName("ISO-8859-1")));
                CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT);
                List<CSVRecord> records = csvParser.getRecords();
                if (records.get(0).size() > 4) {
                    for (int i = 0; i < records.size(); i++) {
                        CSVRecord record = records.get(i);
                        String country = record.get(1);
                        Set<CSVRecord> locodesSet;
                        if (locodesByCountries.containsKey(country)){
                            locodesSet = locodesByCountries.get(country);
                        } else {
                            locodesSet  = new HashSet<>();
                        }
                        locodesSet.add(record);
                        locodesByCountries.put(country, locodesSet);
                    }
                }
            }
        }
        else {
            for (String file : inputFiles) {
                BufferedReader reader = Files.newBufferedReader(Paths.get(file), Charset.forName("ISO-8859-1"));
                CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT);
                List<CSVRecord> records = csvParser.getRecords();
                if (records.get(0).size() > 4) {
                    for (int i = 0; i < records.size(); i++) {
                        CSVRecord record = records.get(i);
                        String country = record.get(1);
                        Set<CSVRecord> locodesSet;
                        if (locodesByCountries.containsKey(country)){
                            locodesSet = locodesByCountries.get(country);
                        } else {
                            locodesSet  = new HashSet<>();
                        }
                        locodesSet.add(record);
                        locodesByCountries.put(country, locodesSet);
                    }
                }
            }
        }

        for (String country:locodesByCountries.keySet()){
            Set<CSVRecord> records = locodesByCountries.get(country);
            Map<String, String> sortedLines = new HashMap<>();
            Iterator<CSVRecord> iterator = records.iterator();
            while (iterator.hasNext()){
                String line = "";
                CSVRecord record = iterator.next();
                for (String value:record.toList()){
                    if (value.isEmpty())
                        line += ",";
                    else
                        line += String.format("\"%s\",", value);
                }
                String key = record.get(1).concat(record.get(2));
                if (sortedLines.containsKey(key)){
                    if (!record.get(2).isEmpty()) {
                        System.err.println(sortedLines.get(key));
                        System.err.println(line);
                    }
                    key += record.get(3);
                }
                sortedLines.put(key, line.substring(0,line.length()-1));
            }

            new FileGenerator().generateTextFile(sortedLines,
                    String.format("%s.csv", country));

        }
    }

    @Override
    protected void readInputFileToGraphArray(Object object) {

    }
}
