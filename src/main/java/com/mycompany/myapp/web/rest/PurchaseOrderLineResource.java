//package com.mycompany.myapp.web.rest;
//
//import com.mycompany.myapp.repository.PurchaseOrderLineRepository;
//import com.mycompany.myapp.service.PurchaseOrderLineService;
//import com.mycompany.myapp.service.dto.PurchaseOrderLineDTO;
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
// * REST controller for managing {@link com.mycompany.myapp.domain.PurchaseOrderLine}.
// */
//@RestController
//@RequestMapping("/api")
//public class PurchaseOrderLineResource {
//
//    private final Logger log = LoggerFactory.getLogger(PurchaseOrderLineResource.class);
//
//    private static final String ENTITY_NAME = "purchaseOrderLine";
//
//    @Value("${jhipster.clientApp.name}")
//    private String applicationName;
//
//    private final PurchaseOrderLineService purchaseOrderLineService;
//
//    private final PurchaseOrderLineRepository purchaseOrderLineRepository;
//
//    public PurchaseOrderLineResource(
//        PurchaseOrderLineService purchaseOrderLineService,
//        PurchaseOrderLineRepository purchaseOrderLineRepository
//    ) {
//        this.purchaseOrderLineService = purchaseOrderLineService;
//        this.purchaseOrderLineRepository = purchaseOrderLineRepository;
//    }
//
//    /**
//     * {@code POST  /purchase-order-lines} : Create a new purchaseOrderLine.
//     *
//     * @param purchaseOrderLineDTO the purchaseOrderLineDTO to create.
//     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new purchaseOrderLineDTO, or with status {@code 400 (Bad Request)} if the purchaseOrderLine has already an ID.
//     * @throws URISyntaxException if the Location URI syntax is incorrect.
//     */
//    @PostMapping("/purchase-order-lines")
//    public ResponseEntity<PurchaseOrderLineDTO> createPurchaseOrderLine(@Valid @RequestBody PurchaseOrderLineDTO purchaseOrderLineDTO)
//        throws URISyntaxException {
//        log.debug("REST request to save PurchaseOrderLine : {}", purchaseOrderLineDTO);
//        if (purchaseOrderLineDTO.getId() != null) {
//            throw new BadRequestAlertException("A new purchaseOrderLine cannot already have an ID", ENTITY_NAME, "idexists");
//        }
//        PurchaseOrderLineDTO result = purchaseOrderLineService.save(purchaseOrderLineDTO);
//        return ResponseEntity
//            .created(new URI("/api/purchase-order-lines/" + result.getId()))
//            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.getId().toString()))
//            .body(result);
//    }
//
//    /**
//     * {@code PUT  /purchase-order-lines/:id} : Updates an existing purchaseOrderLine.
//     *
//     * @param id the id of the purchaseOrderLineDTO to save.
//     * @param purchaseOrderLineDTO the purchaseOrderLineDTO to update.
//     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated purchaseOrderLineDTO,
//     * or with status {@code 400 (Bad Request)} if the purchaseOrderLineDTO is not valid,
//     * or with status {@code 500 (Internal Server Error)} if the purchaseOrderLineDTO couldn't be updated.
//     * @throws URISyntaxException if the Location URI syntax is incorrect.
//     */
//    @PutMapping("/purchase-order-lines/{id}")
//    public ResponseEntity<PurchaseOrderLineDTO> updatePurchaseOrderLine(
//        @PathVariable(value = "id", required = false) final Long id,
//        @Valid @RequestBody PurchaseOrderLineDTO purchaseOrderLineDTO
//    ) throws URISyntaxException {
//        log.debug("REST request to update PurchaseOrderLine : {}, {}", id, purchaseOrderLineDTO);
//        if (purchaseOrderLineDTO.getId() == null) {
//            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
//        }
//        if (!Objects.equals(id, purchaseOrderLineDTO.getId())) {
//            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
//        }
//
//        if (!purchaseOrderLineRepository.existsById(id)) {
//            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
//        }
//
//        PurchaseOrderLineDTO result = purchaseOrderLineService.update(purchaseOrderLineDTO);
//        return ResponseEntity
//            .ok()
//            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, purchaseOrderLineDTO.getId().toString()))
//            .body(result);
//    }
//
//    /**
//     * {@code PATCH  /purchase-order-lines/:id} : Partial updates given fields of an existing purchaseOrderLine, field will ignore if it is null
//     *
//     * @param id the id of the purchaseOrderLineDTO to save.
//     * @param purchaseOrderLineDTO the purchaseOrderLineDTO to update.
//     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated purchaseOrderLineDTO,
//     * or with status {@code 400 (Bad Request)} if the purchaseOrderLineDTO is not valid,
//     * or with status {@code 404 (Not Found)} if the purchaseOrderLineDTO is not found,
//     * or with status {@code 500 (Internal Server Error)} if the purchaseOrderLineDTO couldn't be updated.
//     * @throws URISyntaxException if the Location URI syntax is incorrect.
//     */
//    @PatchMapping(value = "/purchase-order-lines/{id}", consumes = { "application/json", "application/merge-patch+json" })
//    public ResponseEntity<PurchaseOrderLineDTO> partialUpdatePurchaseOrderLine(
//        @PathVariable(value = "id", required = false) final Long id,
//        @NotNull @RequestBody PurchaseOrderLineDTO purchaseOrderLineDTO
//    ) throws URISyntaxException {
//        log.debug("REST request to partial update PurchaseOrderLine partially : {}, {}", id, purchaseOrderLineDTO);
//        if (purchaseOrderLineDTO.getId() == null) {
//            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
//        }
//        if (!Objects.equals(id, purchaseOrderLineDTO.getId())) {
//            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
//        }
//
//        if (!purchaseOrderLineRepository.existsById(id)) {
//            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
//        }
//
//        Optional<PurchaseOrderLineDTO> result = purchaseOrderLineService.partialUpdate(purchaseOrderLineDTO);
//
//        return ResponseUtil.wrapOrNotFound(
//            result,
//            HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, purchaseOrderLineDTO.getId().toString())
//        );
//    }
//
//    /**
//     * {@code GET  /purchase-order-lines} : get all the purchaseOrderLines.
//     *
//     * @param pageable the pagination information.
//     * @param eagerload flag to eager load entities from relationships (This is applicable for many-to-many).
//     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of purchaseOrderLines in body.
//     */
//    @GetMapping("/purchase-order-lines")
//    public ResponseEntity<List<PurchaseOrderLineDTO>> getAllPurchaseOrderLines(
//        @org.springdoc.api.annotations.ParameterObject Pageable pageable,
//        @RequestParam(required = false, defaultValue = "false") boolean eagerload
//    ) {
//        log.debug("REST request to get a page of PurchaseOrderLines");
//        Page<PurchaseOrderLineDTO> page;
//        if (eagerload) {
//            page = purchaseOrderLineService.findAllWithEagerRelationships(pageable);
//        } else {
//            page = purchaseOrderLineService.findAll(pageable);
//        }
//        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
//        return ResponseEntity.ok().headers(headers).body(page.getContent());
//    }
//
//    /**
//     * {@code GET  /purchase-order-lines/:id} : get the "id" purchaseOrderLine.
//     *
//     * @param id the id of the purchaseOrderLineDTO to retrieve.
//     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the purchaseOrderLineDTO, or with status {@code 404 (Not Found)}.
//     */
//    @GetMapping("/purchase-order-lines/{id}")
//    public ResponseEntity<PurchaseOrderLineDTO> getPurchaseOrderLine(@PathVariable Long id) {
//        log.debug("REST request to get PurchaseOrderLine : {}", id);
//        Optional<PurchaseOrderLineDTO> purchaseOrderLineDTO = purchaseOrderLineService.findOne(id);
//        return ResponseUtil.wrapOrNotFound(purchaseOrderLineDTO);
//    }
//
//    /**
//     * {@code DELETE  /purchase-order-lines/:id} : delete the "id" purchaseOrderLine.
//     *
//     * @param id the id of the purchaseOrderLineDTO to delete.
//     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
//     */
//    @DeleteMapping("/purchase-order-lines/{id}")
//    public ResponseEntity<Void> deletePurchaseOrderLine(@PathVariable Long id) {
//        log.debug("REST request to delete PurchaseOrderLine : {}", id);
//        purchaseOrderLineService.delete(id);
//        return ResponseEntity
//            .noContent()
//            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString()))
//            .build();
//    }
//}
