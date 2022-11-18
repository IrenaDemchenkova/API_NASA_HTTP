import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpHeaders;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Paths;

public class Main {
    private static final String REMOTE_SERVICE_URI =
            "https://api.nasa.gov/planetary/apod?api_key=UvWyZV9CnUPDF8L3D5r7DA3jZU6CW5AikQRDe9Uz";

    public static void main(String[] args) throws IOException, URISyntaxException {
        CloseableHttpClient httpClient = HttpClientBuilder.create()
                .setDefaultRequestConfig(RequestConfig.custom()
                        .setConnectTimeout(5000)    // ������������ ����� �������� ����������� � �������
                        .setSocketTimeout(30000)    // ������������ ����� �������� ��������� ������
                        .setRedirectsEnabled(false) // ����������� ��������� ��������� � ������
                        .build())
                .build();

        // �������� ������� ������� � ������������� �����������
        HttpGet request = new HttpGet(REMOTE_SERVICE_URI);
        request.setHeader(HttpHeaders.ACCEPT, ContentType.APPLICATION_JSON.getMimeType());

        // �������� �������
        CloseableHttpResponse response = httpClient.execute(request);

        // �������������� json � java �������
        ObjectMapper mapper = new ObjectMapper();
        Post post = mapper.readValue(response.getEntity().getContent().readAllBytes(), new TypeReference<Post>() {
        });
        System.out.println(post.toString());

        //������ http-������
        String url = post.getUrl();
        HttpGet requestSecond = new HttpGet(url);
        requestSecond.setHeader(HttpHeaders.ACCEPT, ContentType.APPLICATION_JSON.getMimeType());
        CloseableHttpResponse responseSecond = httpClient.execute(requestSecond);
        byte[] content = EntityUtils.toByteArray(responseSecond.getEntity());

        // ���������� ��� �����
        String filename = Paths.get(new URI(url).getPath()).getFileName().toString();

        // �������� ������� File
        File resultFile = new File(filename);

        // �������� ������ �����
        try {
            if (resultFile.createNewFile()) {
                System.out.format("File %s was created and saved\n", filename);
            }
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }
        try (FileOutputStream fos = new FileOutputStream(filename)) {
            // ������ ������ � ����
            fos.write(content, 0, content.length);
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }
    }
}
