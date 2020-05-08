/**
 *
 *  @author Jaworski Maciej S18239
 *
 */

package S_PASSTIME_SERVER1;


import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

public class Tools {
    public static Options createOptionsFromYaml(String fileName) throws IOException {
        Path filePath = Paths.get(fileName);
        try(InputStream stream = Files.newInputStream(filePath)) {
            Yaml yamlParser = new Yaml();
            Map<String, Object> yamlMap = yamlParser.load(stream);
            return toOptions(yamlMap);
        }
    }

    @SuppressWarnings("unchecked")
    private static Options toOptions(Map<String, Object> yamlMap) {
        String host = (String) yamlMap.get("host");
        Integer port = (Integer) yamlMap.get("port");
        Boolean concurMode = (Boolean) yamlMap.get("concurMode");
        Boolean showSendRes = (Boolean) yamlMap.get("showSendRes");
        Map<String, List<String>> clientsMap = (Map<String, List<String>>) yamlMap.get("clientsMap");
        return new Options(host, port, concurMode, showSendRes, clientsMap);
    }

}
