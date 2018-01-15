package blue.origami.chibi;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Collectors;

public class NezCC {

  public static final String loadFile(String fileName) throws IOException {
    String path = new File(".").getAbsoluteFile().getParent() + "/" + fileName;
    try {
      return Files.lines(Paths.get(path), Charset.forName("UTF-8")).collect(Collectors.joining(System.getProperty("line.separator")));
    } catch (IOException e) {
      return "";
    }
  }

  public static final double time(double offset) {
    double start = ((double)(System.nanoTime()) / 1000.0);
    return start - offset;
  }

  public static final double difTime(double start) {
    double end = ((double)(System.nanoTime()) / 1000.0);
    return end - start;
  }
}
