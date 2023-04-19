package academy.prog;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

// DB -> E(20) -> R -> S -> DTO <- C -> View / JSON (5)

@Service
public class UrlService {
    private final UrlRepository urlRepository;

    public UrlService(UrlRepository urlRepository) {
        this.urlRepository = urlRepository;
    }

    @Transactional
    public long saveUrl(UrlDTO urlDTO) {
        var urlRecord = urlRepository.findByUrl(urlDTO.getUrl());
        if (urlRecord == null) {
            urlRecord = UrlRecord.of(urlDTO);
            urlRepository.save(urlRecord);
        }

        return urlRecord.getId();
    }

    @Transactional
    public String getUrl(long id) {
        var urlOpt = urlRepository.findById(id);
        if (urlOpt.isEmpty())
            return null;

        var urlRecord = urlOpt.get();
        urlRecord.setCount(urlRecord.getCount() + 1);
        urlRecord.setLastAccess(new Date());

        return urlRecord.getUrl();
    }

    //функція видалення скороченних посилань
    @Transactional
    public String delUrl(long id) {
        var urlOpt = urlRepository.findById(id);
        if (urlOpt.isEmpty())
            return "URL for deleting not found";

        var urlRecord = urlOpt.get();
        urlRepository.delete(urlRecord);
        return "URL deleted";
    }

    //функція видалення старіючих посилань
    @Transactional
    @Scheduled(cron = "${interval-in-cron}")
    public void delOldUrl() {
        var records = urlRepository.findAll();
        for (UrlRecord record : records) {
            Date now = new Date();
            Duration d = Duration.ofMinutes((now.getTime() - record.getLastAccess().getTime()));
            if (d.toMinutes() > 1) {
                urlRepository.delete(record);
            }
        }
    }


    @Transactional(readOnly = true)
    public List<UrlStatDTO> getStatistics() {
        var records = urlRepository.findAll();
        var result = new ArrayList<UrlStatDTO>();

        records.forEach(x -> result.add(x.toStatDTO()));

        return result;
    }
}
