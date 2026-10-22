package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.entities.UCSBDiningCommonsMenuItem;
import edu.ucsb.cs156.example.errors.EntityNotFoundException;
import edu.ucsb.cs156.example.repositories.UCSBDiningCommonsMenuItemRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** This is a REST controller for Restaurants */
@Tag(name = "UCSBDiningCommonsMenuItem")
@RequestMapping("/api/ucsbdiningcommonsmenuitem")
@RestController
public class UCSBDiningCommonsMenuItemController extends ApiController {

  @Autowired UCSBDiningCommonsMenuItemRepository ucsbdiningcmmonsmenuitemrepository;

  /**
   * This method returns a list of all restaurants.
   *
   * @return a list of all restaurants
   */
  @Operation(summary = "List all UCSBDiningCommonsMenuItems")
  @PreAuthorize("hasRole('ROLE_USER')")
  @GetMapping("/all")
  public Iterable<UCSBDiningCommonsMenuItem> allUCSBDiningCommonsMenuItem() {
    Iterable<UCSBDiningCommonsMenuItem> ucsbdiningcommonsmenuitem =
        ucsbdiningcmmonsmenuitemrepository.findAll();
    return ucsbdiningcommonsmenuitem;
  }

  /**
   * This method returns a single restaurant.
   *
   * @param id id of the restaurant to get
   * @return a single restaurant
   */
  @Operation(summary = "Get a single UCSBDiningCommonsMenuItem")
  @PreAuthorize("hasRole('ROLE_USER')")
  @GetMapping("")
  public UCSBDiningCommonsMenuItem getById(@Parameter(name = "id") @RequestParam Long id) {
    UCSBDiningCommonsMenuItem ucsbdiningcommonsmenuitem =
        ucsbdiningcmmonsmenuitemrepository
            .findById(id)
            .orElseThrow(() -> new EntityNotFoundException(UCSBDiningCommonsMenuItem.class, id));

    return ucsbdiningcommonsmenuitem;
  }

  /**
   * This method creates a new restaurant. Accessible only to users with the role "ROLE_ADMIN".
   *
   * @param diningCommonsCode
   * @param name name of the restaurant
   * @param station description of the restaurant
   * @return the save restaurant (with it's id field set by the database)
   */
  @Operation(summary = "Create a new UCSBDiningCommonsMenuItem")
  @PreAuthorize("hasRole('ROLE_ADMIN')")
  @PostMapping("/post")
  public UCSBDiningCommonsMenuItem postUCSBDiningCommonsMenuItem(
      @Parameter(name = "diningCommonsCode") @RequestParam String diningCommonsCode,
      @Parameter(name = "name") @RequestParam String name,
      @Parameter(name = "station") @RequestParam String station) {
    UCSBDiningCommonsMenuItem ucsbdiningcommonsmenuitem = new UCSBDiningCommonsMenuItem();

    ucsbdiningcommonsmenuitem.setDiningCommonsCode(diningCommonsCode);
    ucsbdiningcommonsmenuitem.setName(name);
    ucsbdiningcommonsmenuitem.setStation(station);

    UCSBDiningCommonsMenuItem saveducsbdiningcommonsmenuitem =
        ucsbdiningcmmonsmenuitemrepository.save(ucsbdiningcommonsmenuitem);
    return saveducsbdiningcommonsmenuitem;
  }

  /**
   * Deletes a restaurant. Accessible only to users with the role "ROLE_ADMIN".
   *
   * @param id id of the restaurant to delete
   * @return a message indicating that the restaurant was deleted
   */
  @Operation(summary = "Delete a UCSBDiningCommonsMenuItem")
  @PreAuthorize("hasRole('ROLE_ADMIN')")
  @DeleteMapping("")
  public Object deleteUCSBDiningCommonsMenuItem(@Parameter(name = "id") @RequestParam Long id) {
    UCSBDiningCommonsMenuItem ucsbdiningcommonsmenuitem =
        ucsbdiningcmmonsmenuitemrepository
            .findById(id)
            .orElseThrow(() -> new EntityNotFoundException(UCSBDiningCommonsMenuItem.class, id));

    ucsbdiningcmmonsmenuitemrepository.delete(ucsbdiningcommonsmenuitem);
    return genericMessage("UCSBDiningCommonsMenuItem with id %s deleted".formatted(id));
  }

  /**
   * Update a single restaurant. Accessible only to users with the role "ROLE_ADMIN".
   *
   * @param id id of the restaurant to update
   * @param incoming the new restaurant contents
   * @return the updated restaurant object
   */
  @Operation(summary = "Update a single UCSBDiningCommonsMenuItem")
  @PreAuthorize("hasRole('ROLE_ADMIN')")
  @PutMapping("")
  public UCSBDiningCommonsMenuItem updateUCSBDiningCommonsMenuItem(
      @Parameter(name = "id") @RequestParam Long id,
      @RequestBody @Valid UCSBDiningCommonsMenuItem incoming) {

    UCSBDiningCommonsMenuItem ucsbdiningcommonsmenuitem =
        ucsbdiningcmmonsmenuitemrepository
            .findById(id)
            .orElseThrow(() -> new EntityNotFoundException(UCSBDiningCommonsMenuItem.class, id));

    ucsbdiningcommonsmenuitem.setDiningCommonsCode(incoming.getDiningCommonsCode());
    ucsbdiningcommonsmenuitem.setName(incoming.getName());
    ucsbdiningcommonsmenuitem.setStation(incoming.getStation());

    ucsbdiningcmmonsmenuitemrepository.save(ucsbdiningcommonsmenuitem);

    return ucsbdiningcommonsmenuitem;
  }
}
