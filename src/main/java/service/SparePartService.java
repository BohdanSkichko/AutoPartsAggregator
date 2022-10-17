package service;

import dto.Response;
import entity.SparePart;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;
import java.util.Set;

@Service
public interface SparePartService {
    Set<Response> getSparePartBySerialNumber(String serialNumber);

/*    void loadContents() throws MalformedURLException, IOException;

    List<String> listAuthors();

    List<SparePart> searchArticlesByAuthor(String sparePartSerialNumber);

    List<SparePart> searchSparePartBySerialNumber(String serialNumber);*/
}
