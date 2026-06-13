package com.mycompany.myapp.web.rest;

import com.mycompany.myapp.repository.InventoryBalanceRepository;
import com.mycompany.myapp.security.AuthoritiesConstants;
import com.mycompany.myapp.service.InventoryBalanceService;
import com.mycompany.myapp.service.dto.InventoryBalanceDTO;
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
 * REST controller for managing {@link com.mycompany.myapp.domain.InventoryBalance}.
 */
@RestController
@RequestMapping("/api")
public class InventoryBalanceResource {

    private final Logger log = LoggerFactory.getLogger(InventoryBalanceResource.class);

    private static final String ENTITY_NAME = "inventoryBalance";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final InventoryBalanceService inventoryBalanceService;

    private final InventoryBalanceRepository inventoryBalanceRepository;

    public InventoryBalanceResource(
        InventoryBalanceService inventoryBalanceService,
        InventoryBalanceRepository inventoryBalanceRepository
    ) {
        this.inventoryBalanceService = inventoryBalanceService;
        this.inventoryBalanceRepository = inventoryBalanceRepository;
    }

    /**
     * {@code POST  /inventory-balances} : Create a new inventoryBalance.
     *
     * @param inventoryBalanceDTO the inventoryBalanceDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new inventoryBalanceDTO, or with status {@code 400 (Bad Request)} if the inventoryBalance has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PreAuthorize("hasAnyAuthority(\"" + AuthoritiesConstants.ADMIN + "\", \"" + AuthoritiesConstants.MANAGER + "\")")
    @PostMapping("/inventory-balances")
    public ResponseEntity<InventoryBalanceDTO> createInventoryBalance(@Valid @RequestBody InventoryBalanceDTO inventoryBalanceDTO)
        throws URISyntaxException {
        log.debug("REST request to save InventoryBalance : {}", inventoryBalanceDTO);
        if (inventoryBalanceDTO.getId() != null) {
            throw new BadRequestAlertException("A new inventoryBalance cannot already have an ID", ENTITY_NAME, "idexists");
        }
        InventoryBalanceDTO result = inventoryBalanceService.save(inventoryBalanceDTO);
        return ResponseEntity
            .created(new URI("/api/inventory-balances/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * {@code PUT  /inventory-balances/:id} : Updates an existing inventoryBalance.
     *
     * @param id the id of the inventoryBalanceDTO to save.
     * @param inventoryBalanceDTO the inventoryBalanceDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated inventoryBalanceDTO,
     * or with status {@code 400 (Bad Request)} if the inventoryBalanceDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the inventoryBalanceDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PreAuthorize("hasAnyAuthority(\"" + AuthoritiesConstants.ADMIN + "\", \"" + AuthoritiesConstants.MANAGER + "\")")
    @PutMapping("/inventory-balances/{id}")
    public ResponseEntity<InventoryBalanceDTO> updateInventoryBalance(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody InventoryBalanceDTO inventoryBalanceDTO
    ) throws URISyntaxException {
        log.debug("REST request to update InventoryBalance : {}, {}", id, inventoryBalanceDTO);
        if (inventoryBalanceDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, inventoryBalanceDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!inventoryBalanceRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        InventoryBalanceDTO result = inventoryBalanceService.update(inventoryBalanceDTO);
        return ResponseEntity
            .ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, inventoryBalanceDTO.getId().toString()))
            .body(result);
    }

    /**
     * {@code PATCH  /inventory-balances/:id} : Partial updates given fields of an existing inventoryBalance, field will ignore if it is null
     *
     * @param id the id of the inventoryBalanceDTO to save.
     * @param inventoryBalanceDTO the inventoryBalanceDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated inventoryBalanceDTO,
     * or with status {@code 400 (Bad Request)} if the inventoryBalanceDTO is not valid,
     * or with status {@code 404 (Not Found)} if the inventoryBalanceDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the inventoryBalanceDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PreAuthorize("hasAnyAuthority(\"" + AuthoritiesConstants.ADMIN + "\", \"" + AuthoritiesConstants.MANAGER + "\")")
    @PatchMapping(value = "/inventory-balances/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<InventoryBalanceDTO> partialUpdateInventoryBalance(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody InventoryBalanceDTO inventoryBalanceDTO
    ) throws URISyntaxException {
        log.debug("REST request to partial update InventoryBalance partially : {}, {}", id, inventoryBalanceDTO);
        if (inventoryBalanceDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, inventoryBalanceDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!inventoryBalanceRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<InventoryBalanceDTO> result = inventoryBalanceService.partialUpdate(inventoryBalanceDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, inventoryBalanceDTO.getId().toString())
        );
    }

    /**
     * {@code GET  /inventory-balances} : get all the inventoryBalances.
     *
     * @param pageable the pagination information.
     * @param eagerload flag to eager load entities from relationships (This is applicable for many-to-many).
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of inventoryBalances in body.
     */
    @PreAuthorize(
        "hasAnyAuthority(\"" +
        AuthoritiesConstants.ADMIN +
        "\", \"" +
        AuthoritiesConstants.SALES +
        "\", \"" +
        AuthoritiesConstants.WAREHOUSE +
        "\", \"" +
        AuthoritiesConstants.MANAGER +
        "\")"
    )
    @GetMapping("/inventory-balances")
    public ResponseEntity<List<InventoryBalanceDTO>> getAllInventoryBalances(
        @org.springdoc.api.annotations.ParameterObject Pageable pageable,
        @RequestParam(required = false, defaultValue = "false") boolean eagerload
    ) {
        log.debug("REST request to get a page of InventoryBalances");
        Page<InventoryBalanceDTO> page;
        if (eagerload) {
            page = inventoryBalanceService.findAllWithEagerRelationships(pageable);
        } else {
            page = inventoryBalanceService.findAll(pageable);
        }
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /inventory-balances/:id} : get the "id" inventoryBalance.
     *
     * @param id the id of the inventoryBalanceDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the inventoryBalanceDTO, or with status {@code 404 (Not Found)}.
     */
    @PreAuthorize(
        "hasAnyAuthority(\"" +
        AuthoritiesConstants.ADMIN +
        "\", \"" +
        AuthoritiesConstants.SALES +
        "\", \"" +
        AuthoritiesConstants.WAREHOUSE +
        "\", \"" +
        AuthoritiesConstants.MANAGER +
        "\")"
    )
    @GetMapping("/inventory-balances/{id}")
    public ResponseEntity<InventoryBalanceDTO> getInventoryBalance(@PathVariable Long id) {
        log.debug("REST request to get InventoryBalance : {}", id);
        Optional<InventoryBalanceDTO> inventoryBalanceDTO = inventoryBalanceService.findOne(id);
        return ResponseUtil.wrapOrNotFound(inventoryBalanceDTO);
    }

    /**
     * {@code DELETE  /inventory-balances/:id} : delete the "id" inventoryBalance.
     *
     * @param id the id of the inventoryBalanceDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @PreAuthorize("hasAnyAuthority(\"" + AuthoritiesConstants.ADMIN + "\", \"" + AuthoritiesConstants.MANAGER + "\")")
    @DeleteMapping("/inventory-balances/{id}")
    public ResponseEntity<Void> deleteInventoryBalance(@PathVariable Long id) {
        log.debug("REST request to delete InventoryBalance : {}", id);
        inventoryBalanceService.delete(id);
        return ResponseEntity
            .noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString()))
            .build();
    }
}
