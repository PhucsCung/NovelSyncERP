//package com.mycompany.myapp.web.rest;
//
//import com.mycompany.myapp.repository.TransferOrderLineRepository;
//import com.mycompany.myapp.service.TransferOrderLineService;
//import com.mycompany.myapp.service.dto.TransferOrderLineDTO;
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
// * REST controller for managing {@link com.mycompany.myapp.domain.TransferOrderLine}.
// */
//@RestController
//@RequestMapping("/api")
//public class TransferOrderLineResource {
//
//    private final Logger log = LoggerFactory.getLogger(TransferOrderLineResource.class);
//
//    private static final String ENTITY_NAME = "transferOrderLine";
//
//    @Value("${jhipster.clientApp.name}")
//    private String applicationName;
//
//    private final TransferOrderLineService transferOrderLineService;
//
//    private final TransferOrderLineRepository transferOrderLineRepository;
//
//    public TransferOrderLineResource(
//        TransferOrderLineService transferOrderLineService,
//        TransferOrderLineRepository transferOrderLineRepository
//    ) {
//        this.transferOrderLineService = transferOrderLineService;
//        this.transferOrderLineRepository = transferOrderLineRepository;
//    }
//
//    /**
//     * {@code POST  /transfer-order-lines} : Create a new transferOrderLine.
//     *
//     * @param transferOrderLineDTO the transferOrderLineDTO to create.
//     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new transferOrderLineDTO, or with status {@code 400 (Bad Request)} if the transferOrderLine has already an ID.
//     * @throws URISyntaxException if the Location URI syntax is incorrect.
//     */
//    @PostMapping("/transfer-order-lines")
//    public ResponseEntity<TransferOrderLineDTO> createTransferOrderLine(@Valid @RequestBody TransferOrderLineDTO transferOrderLineDTO)
//        throws URISyntaxException {
//        log.debug("REST request to save TransferOrderLine : {}", transferOrderLineDTO);
//        if (transferOrderLineDTO.getId() != null) {
//            throw new BadRequestAlertException("A new transferOrderLine cannot already have an ID", ENTITY_NAME, "idexists");
//        }
//        TransferOrderLineDTO result = transferOrderLineService.save(transferOrderLineDTO);
//        return ResponseEntity
//            .created(new URI("/api/transfer-order-lines/" + result.getId()))
//            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.getId().toString()))
//            .body(result);
//    }
//
//    /**
//     * {@code PUT  /transfer-order-lines/:id} : Updates an existing transferOrderLine.
//     *
//     * @param id the id of the transferOrderLineDTO to save.
//     * @param transferOrderLineDTO the transferOrderLineDTO to update.
//     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated transferOrderLineDTO,
//     * or with status {@code 400 (Bad Request)} if the transferOrderLineDTO is not valid,
//     * or with status {@code 500 (Internal Server Error)} if the transferOrderLineDTO couldn't be updated.
//     * @throws URISyntaxException if the Location URI syntax is incorrect.
//     */
//    @PutMapping("/transfer-order-lines/{id}")
//    public ResponseEntity<TransferOrderLineDTO> updateTransferOrderLine(
//        @PathVariable(value = "id", required = false) final Long id,
//        @Valid @RequestBody TransferOrderLineDTO transferOrderLineDTO
//    ) throws URISyntaxException {
//        log.debug("REST request to update TransferOrderLine : {}, {}", id, transferOrderLineDTO);
//        if (transferOrderLineDTO.getId() == null) {
//            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
//        }
//        if (!Objects.equals(id, transferOrderLineDTO.getId())) {
//            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
//        }
//
//        if (!transferOrderLineRepository.existsById(id)) {
//            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
//        }
//
//        TransferOrderLineDTO result = transferOrderLineService.update(transferOrderLineDTO);
//        return ResponseEntity
//            .ok()
//            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, transferOrderLineDTO.getId().toString()))
//            .body(result);
//    }
//
//    /**
//     * {@code PATCH  /transfer-order-lines/:id} : Partial updates given fields of an existing transferOrderLine, field will ignore if it is null
//     *
//     * @param id the id of the transferOrderLineDTO to save.
//     * @param transferOrderLineDTO the transferOrderLineDTO to update.
//     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated transferOrderLineDTO,
//     * or with status {@code 400 (Bad Request)} if the transferOrderLineDTO is not valid,
//     * or with status {@code 404 (Not Found)} if the transferOrderLineDTO is not found,
//     * or with status {@code 500 (Internal Server Error)} if the transferOrderLineDTO couldn't be updated.
//     * @throws URISyntaxException if the Location URI syntax is incorrect.
//     */
//    @PatchMapping(value = "/transfer-order-lines/{id}", consumes = { "application/json", "application/merge-patch+json" })
//    public ResponseEntity<TransferOrderLineDTO> partialUpdateTransferOrderLine(
//        @PathVariable(value = "id", required = false) final Long id,
//        @NotNull @RequestBody TransferOrderLineDTO transferOrderLineDTO
//    ) throws URISyntaxException {
//        log.debug("REST request to partial update TransferOrderLine partially : {}, {}", id, transferOrderLineDTO);
//        if (transferOrderLineDTO.getId() == null) {
//            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
//        }
//        if (!Objects.equals(id, transferOrderLineDTO.getId())) {
//            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
//        }
//
//        if (!transferOrderLineRepository.existsById(id)) {
//            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
//        }
//
//        Optional<TransferOrderLineDTO> result = transferOrderLineService.partialUpdate(transferOrderLineDTO);
//
//        return ResponseUtil.wrapOrNotFound(
//            result,
//            HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, transferOrderLineDTO.getId().toString())
//        );
//    }
//
//    /**
//     * {@code GET  /transfer-order-lines} : get all the transferOrderLines.
//     *
//     * @param pageable the pagination information.
//     * @param eagerload flag to eager load entities from relationships (This is applicable for many-to-many).
//     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of transferOrderLines in body.
//     */
//    @GetMapping("/transfer-order-lines")
//    public ResponseEntity<List<TransferOrderLineDTO>> getAllTransferOrderLines(
//        @org.springdoc.api.annotations.ParameterObject Pageable pageable,
//        @RequestParam(required = false, defaultValue = "false") boolean eagerload
//    ) {
//        log.debug("REST request to get a page of TransferOrderLines");
//        Page<TransferOrderLineDTO> page;
//        if (eagerload) {
//            page = transferOrderLineService.findAllWithEagerRelationships(pageable);
//        } else {
//            page = transferOrderLineService.findAll(pageable);
//        }
//        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
//        return ResponseEntity.ok().headers(headers).body(page.getContent());
//    }
//
//    /**
//     * {@code GET  /transfer-order-lines/:id} : get the "id" transferOrderLine.
//     *
//     * @param id the id of the transferOrderLineDTO to retrieve.
//     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the transferOrderLineDTO, or with status {@code 404 (Not Found)}.
//     */
//    @GetMapping("/transfer-order-lines/{id}")
//    public ResponseEntity<TransferOrderLineDTO> getTransferOrderLine(@PathVariable Long id) {
//        log.debug("REST request to get TransferOrderLine : {}", id);
//        Optional<TransferOrderLineDTO> transferOrderLineDTO = transferOrderLineService.findOne(id);
//        return ResponseUtil.wrapOrNotFound(transferOrderLineDTO);
//    }
//
//    /**
//     * {@code DELETE  /transfer-order-lines/:id} : delete the "id" transferOrderLine.
//     *
//     * @param id the id of the transferOrderLineDTO to delete.
//     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
//     */
//    @DeleteMapping("/transfer-order-lines/{id}")
//    public ResponseEntity<Void> deleteTransferOrderLine(@PathVariable Long id) {
//        log.debug("REST request to delete TransferOrderLine : {}", id);
//        transferOrderLineService.delete(id);
//        return ResponseEntity
//            .noContent()
//            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString()))
//            .build();
//    }
//}
