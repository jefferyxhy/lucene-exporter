import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.TotalHitCountCollector;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class LuceneExporter {
    public static void main(String[] args) throws IOException, ParseException {
        String indexDir = args[0];
        String outputFile = args[1];
        String queryString = args[2];

        FSDirectory dir = FSDirectory.open(new File(indexDir));
        DirectoryReader reader = DirectoryReader.open(dir);
        IndexSearcher searcher = new IndexSearcher(reader);
        QueryParser parser = new QueryParser(Version.LUCENE_44, "defaultField", new StandardAnalyzer(Version.LUCENE_44));
        Query query = parser.parse(queryString);

        // count total hits
        TotalHitCountCollector counter = new TotalHitCountCollector();
        searcher.search(query, counter);
        int totalHits = counter.getTotalHits();
        System.out.println("------------------------------------------------------------ Total hits: " + totalHits);

        // fetch and output hits in batch
        ScoreDoc lastScoreDoc = null;
        List<Document> allDocs = new ArrayList<>();
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile))) {
            writer.write("macroName, contentId, title, spaceName, spaceKey, author, lastModifier, lastModified\n");
            while (true) {
                TopDocs topDocs = searcher.searchAfter(lastScoreDoc, query, 1000); // batch size
                if (topDocs.scoreDocs.length == 0) break;

                for (ScoreDoc sd : topDocs.scoreDocs) {
                    Document doc = searcher.doc(sd.doc);
                    String macroName = doc.get("macroName");
                    String contentId = doc.get("content-id");
                    String title = doc.get("title");
                    String spaceName = doc.get("space-name");
                    String spaceKey = doc.get("spacekey");
                    String author = doc.get("authorContributions");
                    String lastModifier = doc.get("lastModifiers");
                    String lastModified = doc.get("modified");

                    System.out.println("write content: " + contentId + " with macro: " + macroName);

                    writer.write(String.format("\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"\n",
                            escapeCsv(macroName), escapeCsv(contentId), escapeCsv(title), escapeCsv(spaceName)
                            , escapeCsv(spaceKey), escapeCsv(author), escapeCsv(lastModifier), escapeCsv(lastModified)));
                }

                lastScoreDoc = topDocs.scoreDocs[topDocs.scoreDocs.length - 1];

                totalHits = totalHits - topDocs.scoreDocs.length;

                System.out.println("------------------------------------------------------------ remaining hits: " + totalHits);
            }
        }

        reader.close();
        dir.close();
        System.out.println("Search and export complete.");
    }

    // Simple CSV escape (wrap with double quotes and escape inner quotes)
    private static String escapeCsv(String input) {
        if (input == null) return "";
        return input.replace("\"", "\"\"");
    }
}
