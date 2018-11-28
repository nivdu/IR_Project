package sample;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class ReadFile {
    private Parse parse;
    private String path;

    /**
     * Constructor
     * @param path - path of the corpus file
     */
    public ReadFile(String path) {
        this.parse = new Parse();
        this.path=path;
    }

    /**
     *
     *
     */
    public void SpiltFileIntoSeparateDocs(){
        long Stime = System.currentTimeMillis();
        final File folder = new File(path);
        for (final File fileEntry : folder.listFiles()) {
            for (final File fileOfDocs: fileEntry.listFiles()){
                String wholeFileString = file2String(fileOfDocs);
                //file split by <Doc>
                String[] splitByDoc = splitBySpecificString(wholeFileString,"<DOC>\n");
                //file split by <TEXT> //todo need to delete the <</TEXT> at parse.
                for (String currentDoc:splitByDoc){
                    if(currentDoc.equals(""))
                        continue;
                    String[] splitByText = splitBySpecificString(currentDoc,"<TEXT>\n");
                    if(splitByText.length<2)
                        continue;
                        String[] splitByEndText = splitBySpecificString(splitByText[1], "</TEXT>\n");
                        String docSplitBySpaces[] = splitByEndText[0].split(" |\\\n|\\--");
                        parse.parseDoc(docSplitBySpaces);
                }
            }
        }
        long Ftime = System.currentTimeMillis();
        System.out.println(Ftime-Stime);
        int temp=-10;
    }


    /**
     * from url: https://howtodoinjava.com/java/io/java-read-file-to-string-examples/
     * read file to String line by line
     * @param file - The path of the file to read into String
     * @return - String of the context of the file
     */
    private String file2String(File file)
    {
        StringBuilder contentBuilder = new StringBuilder();
        {
            String content = "";

            try
            {
                content = new String ( Files.readAllBytes( Paths.get(file.toPath().toString()) ) );
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }

            return content;
        }
    }

    /**
     * split file by specificString
     * @param file2Split - file to split
     * @return - String array of the split text file
     */
    private String[] splitBySpecificString(String file2Split,String splitBy){
        String[] docs = file2Split.split(splitBy);
        return docs;
    }
}
