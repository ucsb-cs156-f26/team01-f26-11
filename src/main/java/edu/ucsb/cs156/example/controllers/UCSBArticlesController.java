package main.java.edu.ucsb.cs156.example.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import edu.ucsb.cs156.example.controllers.ApiController;
import edu.ucsb.cs156.example.entities.Articles;
import edu.ucsb.cs156.example.repositories.ArticlesRepository;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDateTime;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Articles")
@RequestMapping("/api/ucsbdates")
@RestController
@Slf4j
public class UCSBArticlesController extends ApiController {

  @Autowired ArticlesRepository articlesRepository;

  @GetMapping("/all")
  public Iterable<Articles> allArticles() {
    Iterable<Articles> articles = articlesRepository.findAll();
    return articles;
  }

  @PostMapping("/post")
  public Articles postArticles(
      @Parameter(name = "quarterYYYYQ") @RequestParam String quarterYYYYQ,
      @Parameter(name = "name") @RequestParam String name,
      @Parameter(
              name = "localDateTime",
              description =
                  "date (in iso format, e.g. YYYY-mm-ddTHH:MM:SS; see https://en.wikipedia.org/wiki/ISO_8601)")
          @RequestParam("localDateTime")
          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
          LocalDateTime localDateTime)
      throws JsonProcessingException {

    // For an explanation of @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    // See: https://www.baeldung.com/spring-date-parameters

    log.info("localDateTime={}", localDateTime);

    Articles articles = new Articles();
    // articles.setQuarterYYYYQ(quarterYYYYQ);
    // articles.setName(name);
    // articles.setLocalDateTime(localDateTime);

    // Articles savedArticles = ArticlesRepository.save(articles);

    return articles;
  }
}
