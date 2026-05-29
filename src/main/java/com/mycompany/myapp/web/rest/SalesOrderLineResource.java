//package com.mycompany.myapp.web.rest;
//
//import com.mycompany.myapp.repository.SalesOrderLineRepository;
//import com.mycompany.myapp.service.SalesOrderLineService;
//import com.mycompany.myapp.service.dto.SalesOrderLineDTO;
//import com.mycompany.myapp.web.rest.errors.BadRequestAlertException;
//import java.net.URI;
//import java.net.URISyntaxException;
//import java.util.List;
//import java.util.Objects;
//import java.util.Optional;
//import javax.validation.Valid;
//import javax.validation.constraints.NotNull;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.Pageable;
//import org.springframework.http.HttpHeaders;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
//import tech.jhipster.web.util.HeaderUtil;
//import tech.jhipster.web.util.PaginationUtil;
//import tech.jhipster.web.util.ResponseUtil;
//
///**
// * REST controller for managing {@link com.mycompany.myapp.domain.SalesOrderLine}.
// */
//@RestController
//@RequestMapping("/api")
//public class SalesOrderLineResource {
//
//    private final Logger log = LoggerFactory.getLogger(SalesOrderLineResource.class);
//
//    private static final String ENTITY_NAME = "salesOrderLine";
//
//    @Value("${jhipster.clientApp.name}")
//    private String applicationName;
//
//    private final SalesOrderLineService salesOrderLineService;
//
//    private final SalesOrderLineRepository salesOrderLineRepository;
//
//    public SalesOrderLineResource(SalesOrderLineService salesOrderLineService, SalesOrderLineRepository salesOrderLineRepository) {
//        this.salesOrderLineService = salesOrderLineService;
//        this.salesOrderLineRepository = salesOrderLineRepository;
//    }
//
//    /**
//     * {@code POST  /sales-order-lines} : Create a new salesOrderLine.
//     *
//     * @param salesOrderLineDTO the salesOrderLineDTO to create.
//     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new salesOrderLineDTO, or with status {@code 400 (Bad Request)} if the salesOrderLine has already an ID.
//     * @throws URISyntaxException if the Location URI syntax is incorrect.
//     */
//    @PostMapping("/sales-order-lines")
//    public ResponseEntity<SalesOrderLineDTO> createSalesOrderLine(@Valid @RequestBody SalesOrderLineDTO salesOrderLineDTO)
//        throws URISyntaxException {
//        log.debug("REST request to save SalesOrderLine : {}", salesOrderLineDTO);
//        if (salesOrderLineDTO.getId() != null) {
//            throw new BadRequestAlertException("A new salesOrderLine cannot already have an ID", ENTITY_NAME, "idexists");
//        }
//        SalesOrderLineDTO result = salesOrderLineService.save(salesOrderLineDTO);
//        return ResponseEntity
//            .created(new URI("/api/sales-order-lines/" + result.getId()))
//            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.getId().toString()))
//            .body(result);
//    }
//
//    /**
//     * {@code PUT  /sales-order-lines/:id} : Updates an existing salesOrderLine.
//     *
//     * @param id the id of the salesOrderLineDTO to save.
//     * @param salesOrderLineDTO the salesOrderLineDTO to update.
//     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated salesOrderLineDTO,
//     * or with status {@code 400 (Bad Request)} if the salesOrderLineDTO is not valid,
//     * or with status {@code 500 (Internal Server Error)} if the salesOrderLineDTO couldn't be updated.
//     * @throws URISyntaxException if the Location URI syntax is incorrect.
//     */
//    @PutMapping("/sales-order-lines/{id}")
//    public ResponseEntity<SalesOrderLineDTO> updateSalesOrderLine(
//        @PathVariable(value = "id", required = false) final Long id,
//        @Valid @RequestBody SalesOrderLineDTO salesOrderLineDTO
//    ) throws URISyntaxException {
//        log.debug("REST request to update SalesOrderLine : {}, {}", id, salesOrderLineDTO);
//        if (salesOrderLineDTO.getId() == null) {
//            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
//        }
//        if (!Objects.equals(id, salesOrderLineDTO.getId())) {
//            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
//        }
//
//        if (!salesOrderLineRepository.existsById(id)) {
//            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
//        }
//
//        SalesOrderLineDTO result = salesOrderLineService.update(salesOrderLineDTO);
//        return ResponseEntity
//            .ok()
//            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, salesOrderLineDTO.getId().toString()))
//            .body(result);
//    }
//
//    /**
//     * {@code PATCH  /sales-order-lines/:id} : Partial updates given fields of an existing salesOrderLine, field will ignore if it is null
//     *
//     * @param id the id of the salesOrderLineDTO to save.
//     * @param salesOrderLineDTO the salesOrderLineDTO to update.
//     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated salesOrderLineDTO,
//     * or with status {@code 400 (Bad Request)} if the salesOrderLineDTO is not valid,
//     * or with status {@code 404 (Not Found)} if the salesOrderLineDTO is not found,
//     * or with status {@code 500 (Internal Server Error)} if the salesOrderLineDTO couldn't be updated.
//     * @throws URISyntaxException if the Location URI syntax is incorrect.
//     */
//    @PatchMapping(value = "/sales-order-lines/{id}", consumes = { "application/json", "application/merge-patch+json" })
//    public ResponseEntity<SalesOrderLineDTO> partialUpdateSalesOrderLine(
//        @PathVariable(value = "id", required = false) final Long id,
//        @NotNull @RequestBody SalesOrderLineDTO salesOrderLineDTO
//    ) throws URISyntaxException {
//        log.debug("REST request to partial update SalesOrderLine partially : {}, {}", id, salesOrderLineDTO);
//        if (salesOrderLineDTO.getId() == null) {
//            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
//        }
//        if (!Objects.equals(id, salesOrderLineDTO.getId())) {
//            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
//        }
//
//        if (!salesOrderLineRepository.existsById(id)) {
//            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
//        }
//
//        Optional<SalesOrderLineDTO> result = salesOrderLineService.partialUpdate(salesOrderLineDTO);
//
//        return ResponseUtil.wrapOrNotFound(
//            result,
//            HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, salesOrderLineDTO.getId().toString())
//        );
//    }
//
//    /**
//     * {@code GET  /sales-order-lines} : get all the salesOrderLines.
//     *
//     * @param pageable the pagination information.
//     * @param eagerload flag to eager load entities from relationships (This is applicable for many-to-many).
//     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of salesOrderLines in body.
//     */
//    @GetMapping("/sales-order-lines")
//    public ResponseEntity<List<SalesOrderLineDTO>> getAllSalesOrderLines(
//        @org.springdoc.api.annotations.ParameterObject Pageable pageable,
//        @RequestParam(required = false, defaultValue = "false") boolean eagerload
//    ) {
//        log.debug("REST request to get a page of SalesOrderLines");
//        Page<SalesOrderLineDTO> page;
//        if (eagerload) {
//            page = salesOrderLineService.findAllWithEagerRelationships(pageable);
//        } else {
//            page = salesOrderLineService.findAll(pageable);
//        }
//        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
//        return ResponseEntity.ok().headers(headers).body(page.getContent());
//    }
//
//    /**
//     * {@code GET  /sales-order-lines/:id} : get the "id" salesOrderLine.
//     *
//     * @param id the id of the salesOrderLineDTO to retrieve.
//     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the salesOrderLineDTO, or with status {@code 404 (Not Found)}.
//     */
//    @GetMapping("/sales-order-lines/{id}")
//    public ResponseEntity<SalesOrderLineDTO> getSalesOrderLine(@PathVariable Long id) {
//        log.debug("REST request to get SalesOrderLine : {}", id);
//        Optional<SalesOrderLineDTO> salesOrderLineDTO = salesOrderLineService.findOne(id);
//        return ResponseUtil.wrapOrNotFound(salesOrderLineDTO);
//    }
//
//    /**
//     * {@code DELETE  /sales-order-lines/:id} : delete the "id" salesOrderLine.
//     *
//     * @param id the id of the salesOrderLineDTO to delete.
//     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
//     */
//    @DeleteMapping("/sales-order-lines/{id}")
//    public ResponseEntity<Void> deleteSalesOrderLine(@PathVariable Long id) {
//        log.debug("REST request to delete SalesOrderLine : {}", id);
//        salesOrderLineService.delete(id);
//        return ResponseEntity
//            .noContent()
//            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString()))
//            .build();
//    }
//}
