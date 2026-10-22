package edu.ucsb.cs156.example.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import edu.ucsb.cs156.example.entities.MenuItemReview;
import edu.ucsb.cs156.example.errors.EntityNotFoundException;
import edu.ucsb.cs156.example.repositories.MenuItemReviewRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.LocalDateTime;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** This is a REST controller for MenuItemReviews */
@Tag(name = "MenuItemReview")
@RequestMapping("/api/menuitemreview")
@RestController
@Slf4j
public class MenuItemReviewController extends ApiController {

  @Autowired MenuItemReviewRepository MenuItemReviewRepository;

  /**
   * List all menu item reviews
   *
   * @return an iterable of MenuItemReviews
   */
  @Operation(summary = "List all menu item reviews")
  @PreAuthorize("hasRole('ROLE_USER')")
  @GetMapping("/all")
  public Iterable<MenuItemReview> allMenuItemReviews() {
    Iterable<MenuItemReview> reviews = MenuItemReviewRepository.findAll();
    return reviews;
  }

  /**
   * Get a single review by id
   *
   * @param id the id of the review
   * @return a MenuItemReview
   */
  @Operation(summary = "Get a single review")
  @PreAuthorize("hasRole('ROLE_USER')")
  @GetMapping("")
  public MenuItemReview getById(@Parameter(name = "id") @RequestParam Long id) {
    MenuItemReview menuItemReview =
        MenuItemReviewRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException(MenuItemReview.class, id));

    return menuItemReview;
  }

  /**
   * Create a new menu item review
   *
   * @param itemid the id of the item being reviewed
   * @param reviewerEmail the email of whomever submitted the review
   * @param stars the star rating given
   * @param dateReviewed the date of the review
   * @param comments any comments on the review
   * @return the saved menuItemReview
   */
  @Operation(summary = "Create a new date")
  @PreAuthorize("hasRole('ROLE_ADMIN')")
  @PostMapping("/post")
  public MenuItemReview postMenuItemReview(
      @Parameter(name = "itemid") @RequestParam long itemid,
      @Parameter(name = "reviewerEmail") @RequestParam String reviewerEmail,
      @Parameter(name = "stars") @RequestParam int stars,
      @Parameter(
              name = "dateReviewed",
              description =
                  "date (in iso format, e.g. YYYY-mm-ddTHH:MM:SS; see https://en.wikipedia.org/wiki/ISO_8601)")
          @RequestParam("dateReviewed")
          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
          LocalDateTime dateReviewed,
      @Parameter(name = "comments") @RequestParam String comments)
      throws JsonProcessingException {

    // For an explanation of @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    // See: https://www.baeldung.com/spring-date-parameters

    log.info("dateReviewed={}", dateReviewed);

    MenuItemReview menuItemReview = new MenuItemReview();
    menuItemReview.setItemid(itemid);
    menuItemReview.setReviewerEmail(reviewerEmail);
    menuItemReview.setStars(stars);
    menuItemReview.setDateReviewed(dateReviewed);
    menuItemReview.setComments(comments);

    MenuItemReview savedMenuItemReview = MenuItemReviewRepository.save(menuItemReview);

    return savedMenuItemReview;
  }

  /**
   * Delete a MenuItemReview
   *
   * @param id the id of the review to delete
   * @return a message indicating the review was deleted
   */
  @Operation(summary = "Delete a MenuItemReview")
  @PreAuthorize("hasRole('ROLE_ADMIN')")
  @DeleteMapping("")
  public Object deleteUCSBDate(@Parameter(name = "id") @RequestParam Long id) {
    MenuItemReview menuItemReview =
        MenuItemReviewRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException(MenuItemReview.class, id));

    MenuItemReviewRepository.delete(menuItemReview);
    return genericMessage("MenuItemReview with id %s deleted".formatted(id));
  }

  /**
   * Update a single review
   *
   * @param id id of the review to update
   * @param incoming the new review
   * @return the updated date object
   */
  @Operation(summary = "Update a single review")
  @PreAuthorize("hasRole('ROLE_ADMIN')")
  @PutMapping("")
  public MenuItemReview updateMenuItemReview(
      @Parameter(name = "id") @RequestParam Long id, @RequestBody @Valid MenuItemReview incoming) {

    MenuItemReview menuItemReview =
        MenuItemReviewRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException(MenuItemReview.class, id));

    menuItemReview.setItemid(incoming.getItemid());
    menuItemReview.setReviewerEmail(incoming.getReviewerEmail());
    menuItemReview.setStars(incoming.getStars());
    menuItemReview.setDateReviewed(incoming.getDateReviewed());
    menuItemReview.setComments(incoming.getComments());

    MenuItemReviewRepository.save(menuItemReview);

    return menuItemReview;
  }
}
