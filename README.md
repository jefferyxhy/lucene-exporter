# lucene-exporter
A simple Java code to query and export Lucene index data with Lucene API


## Compile
```bash
javac -cp "lucene-jars/*" LuceneExporter.java
```

## Run
```bash
java -cp ".:lucene-jars/*" LuceneExporter /Path/to/your/index/dir /Path/to/your/desired/output.csv "your+lucene+query"
```
e.g.
```bash
java -cp ".:lucene-jars/*" LuceneExporter /Path/to/your/index/dir /Path/to/your/desired/output.csv "(macroName:status OR macroName:roadmap) AND contentStatus:current AND (type:page OR type:blogpost)"
```
