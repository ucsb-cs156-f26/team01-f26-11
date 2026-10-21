package main.java.edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.controllers.ApiController;
import edu.ucsb.cs156.example.repositories.ArticlesRepository;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Articles")
@RequestMapping("/api/ucsbdates")
@RestController
@Slf4j
public class UCSBArticlesController extends ApiController {

  @Autowired ArticlesRepository articlesRepository;
}
