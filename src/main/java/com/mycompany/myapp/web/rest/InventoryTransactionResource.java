package com.mycompany.myapp.web.rest;

import com.mycompany.myapp.repository.InventoryTransactionRepository;
import com.mycompany.myapp.security.AuthoritiesConstants;
import com.mycompany.myapp.service.InventoryTransactionService;
import com.mycompany.myapp.service.dto.InventoryTransactionDTO;
import com.mycompany.myapp.web.rest.errors.BadRequestAlertException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.PaginationUtil;
import tech.jhipster.web.util.ResponseUtil;

/**
 * REST controller for managing {@link com.mycompany.myapp.domain.InventoryTransaction}.
 */
@RestController
@RequestMapping("/api")
public class InventoryTransactionResource {

    private final Logger log = LoggerFactory.getLogger(InventoryTransactionResource.class);

    private static final String ENTITY_NAME = "inventoryTransaction";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final InventoryTransactionService inventoryTransactionService;

    private final InventoryTransactionRepository inventoryTransactionRepository;

    public InventoryTransactionResource(
        InventoryTransactionService inventoryTransactionService,
        InventoryTransactionRepository inventoryTransactionRepository
    ) {
        this.inventoryTransactionService = inventoryTransactionService;
        this.inventoryTransactionRepository = inventoryTransactionRepository;
    }

    /**
     * {@code POST  /inventory-transactions} : Create a new inventoryTransaction.
     *
     * @param inventoryTransactionDTO the inventoryTransactionDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new inventoryTransactionDTO, or with status {@code 400 (Bad Request)} if the inventoryTransaction has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    //    @PostMapping("/inventory-transactions")
    //    public ResponseEntity<InventoryTransactionDTO> createInventoryTransaction(
    //        @Valid @RequestBody InventoryTransactionDTO inventoryTransactionDTO
    //    ) throws URISyntaxException {
    //        log.debug("REST request to save InventoryTransaction : {}", inventoryTransactionDTO);
    //        if (inventoryTransactionDTO.getId() != null) {
    //            throw new BadRequestAlertException("A new inventoryTransaction cannot already have an ID", ENTITY_NAME, "idexists");
    //        }
    //        InventoryTransactionDTO result = inventoryTransactionService.save(inventoryTransactionDTO);
    //        return ResponseEntity
    //            .created(new URI("/api/inventory-transactions/" + result.getId()))
    //            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.getId().toString()))
    //            .body(result);
    //    }

    /**
     * {@code PUT  /inventory-transactions/:id} : Updates an existing inventoryTransaction.
     *
     * @param id the id of the inventoryTransactionDTO to save.
     * @param inventoryTransactionDTO the inventoryTransactionDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated inventoryTransactionDTO,
     * or with status {@code 400 (Bad Request)} if the inventoryTransactionDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the inventoryTransactionDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    //    @PutMapping("/inventory-transactions/{id}")
    //    public ResponseEntity<InventoryTransactionDTO> updateInventoryTransaction(
    //        @PathVariable(value = "id", required = false) final Long id,
    //        @Valid @RequestBody InventoryTransactionDTO inventoryTransactionDTO
    //    ) throws URISyntaxException {
    //        log.debug("REST request to update InventoryTransaction : {}, {}", id, inventoryTransactionDTO);
    //        if (inventoryTransactionDTO.getId() == null) {
    //            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
    //        }
    //        if (!Objects.equals(id, inventoryTransactionDTO.getId())) {
    //            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
    //        }
    //
    //        if (!inventoryTransactionRepository.existsById(id)) {
    //            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
    //        }
    //
    //        InventoryTransactionDTO result = inventoryTransactionService.update(inventoryTransactionDTO);
    //        return ResponseEntity
    //            .ok()
    //            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, inventoryTransactionDTO.getId().toString()))
    //            .body(result);
    //    }

    /**
     * {@code PATCH  /inventory-transactions/:id} : Partial updates given fields of an existing inventoryTransaction, field will ignore if it is null
     *
     * @param id the id of the inventoryTransactionDTO to save.
     * @param inventoryTransactionDTO the inventoryTransactionDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated inventoryTransactionDTO,
     * or with status {@code 400 (Bad Request)} if the inventoryTransactionDTO is not valid,
     * or with status {@code 404 (Not Found)} if the inventoryTransactionDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the inventoryTransactionDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    //    @PatchMapping(value = "/inventory-transactions/{id}", consumes = { "application/json", "application/merge-patch+json" })
    //    public ResponseEntity<InventoryTransactionDTO> partialUpdateInventoryTransaction(
    //        @PathVariable(value = "id", required = false) final Long id,
    //        @NotNull @RequestBody InventoryTransactionDTO inventoryTransactionDTO
    //    ) throws URISyntaxException {
    //        log.debug("REST request to partial update InventoryTransaction partially : {}, {}", id, inventoryTransactionDTO);
    //        if (inventoryTransactionDTO.getId() == null) {
    //            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
    //        }
    //        if (!Objects.equals(id, inventoryTransactionDTO.getId())) {
    //            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
    //        }
    //
    //        if (!inventoryTransactionRepository.existsById(id)) {
    //            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
    //        }
    //
    //        Optional<InventoryTransactionDTO> result = inventoryTransactionService.partialUpdate(inventoryTransactionDTO);
    //
    //        return ResponseUtil.wrapOrNotFound(
    //            result,
    //            HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, inventoryTransactionDTO.getId().toString())
    //        );
    //    }

    /**
     * {@code GET  /inventory-transactions} : get all the inventoryTransactions.
     *
     * @param pageable the pagination information.
     * @param eagerload flag to eager load entities from relationships (This is applicable for many-to-many).
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of inventoryTransactions in body.
     */
    @PreAuthorize(
        "hasAnyAuthority(\"" +
        AuthoritiesConstants.ADMIN +
        "\", \"" +
        AuthoritiesConstants.MANAGER +
        "\", \"" +
        AuthoritiesConstants.WAREHOUSE +
        "\", \"" +
        AuthoritiesConstants.ACCOUNTANT +
        "\")"
    )
    @GetMapping("/inventory-transactions")
    public ResponseEntity<List<InventoryTransactionDTO>> getAllInventoryTransactions(
        @org.springdoc.api.annotations.ParameterObject Pageable pageable,
        @RequestParam(required = false, defaultValue = "false") boolean eagerload
    ) {
        log.debug("REST request to get a page of InventoryTransactions");
        Page<InventoryTransactionDTO> page;
        if (eagerload) {
            page = inventoryTransactionService.findAllWithEagerRelationships(pageable);
        } else {
            page = inventoryTransactionService.findAll(pageable);
        }
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /inventory-transactions/:id} : get the "id" inventoryTransaction.
     *
     * @param id the id of the inventoryTransactionDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the inventoryTransactionDTO, or with status {@code 404 (Not Found)}.
     */
    @PreAuthorize(
        "hasAnyAuthority(\"" +
        AuthoritiesConstants.ADMIN +
        "\", \"" +
        AuthoritiesConstants.MANAGER +
        "\", \"" +
        AuthoritiesConstants.WAREHOUSE +
        "\", \"" +
        AuthoritiesConstants.ACCOUNTANT +
        "\")"
    )
    @GetMapping("/inventory-transactions/{id}")
    public ResponseEntity<InventoryTransactionDTO> getInventoryTransaction(@PathVariable Long id) {
        log.debug("REST request to get InventoryTransaction : {}", id);
        Optional<InventoryTransactionDTO> inventoryTransactionDTO = inventoryTransactionService.findOne(id);
        return ResponseUtil.wrapOrNotFound(inventoryTransactionDTO);
    }
    /**
     * {@code DELETE  /inventory-transactions/:id} : delete the "id" inventoryTransaction.
     *
     * @param id the id of the inventoryTransactionDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    //    @DeleteMapping("/inventory-transactions/{id}")
    //    public ResponseEntity<Void> deleteInventoryTransaction(@PathVariable Long id) {
    //        log.debug("REST request to delete InventoryTransaction : {}", id);
    //        inventoryTransactionService.delete(id);
    //        return ResponseEntity
    //            .noContent()
    //            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString()))
    //            .build();
    //    }
}
