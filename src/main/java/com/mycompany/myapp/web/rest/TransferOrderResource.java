package com.mycompany.myapp.web.rest;

import com.mycompany.myapp.repository.TransferOrderRepository;
import com.mycompany.myapp.security.AuthoritiesConstants;
import com.mycompany.myapp.service.TransferOrderService;
import com.mycompany.myapp.service.dto.TransferOrderDTO;
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
 * REST controller for managing {@link com.mycompany.myapp.domain.TransferOrder}.
 */
@RestController
@RequestMapping("/api")
public class TransferOrderResource {

    private final Logger log = LoggerFactory.getLogger(TransferOrderResource.class);

    private static final String ENTITY_NAME = "transferOrder";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final TransferOrderService transferOrderService;

    private final TransferOrderRepository transferOrderRepository;

    public TransferOrderResource(TransferOrderService transferOrderService, TransferOrderRepository transferOrderRepository) {
        this.transferOrderService = transferOrderService;
        this.transferOrderRepository = transferOrderRepository;
    }

    /**
     * {@code POST  /transfer-orders} : Create a new transferOrder.
     *
     * @param transferOrderDTO the transferOrderDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new transferOrderDTO, or with status {@code 400 (Bad Request)} if the transferOrder has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PreAuthorize(
        "hasAnyAuthority(\"" +
        AuthoritiesConstants.ADMIN +
        "\", \"" +
        AuthoritiesConstants.MANAGER +
        "\", \"" +
        AuthoritiesConstants.WAREHOUSE +
        "\")"
    )
    @PostMapping("/transfer-orders")
    public ResponseEntity<TransferOrderDTO> createTransferOrder(@Valid @RequestBody TransferOrderDTO transferOrderDTO)
        throws URISyntaxException {
        log.debug("REST request to save TransferOrder : {}", transferOrderDTO);
        if (transferOrderDTO.getId() != null) {
            throw new BadRequestAlertException("A new transferOrder cannot already have an ID", ENTITY_NAME, "idexists");
        }
        TransferOrderDTO result = transferOrderService.save(transferOrderDTO);
        return ResponseEntity
            .created(new URI("/api/transfer-orders/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * {@code PUT  /transfer-orders/:id} : Updates an existing transferOrder.
     *
     * @param id the id of the transferOrderDTO to save.
     * @param transferOrderDTO the transferOrderDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated transferOrderDTO,
     * or with status {@code 400 (Bad Request)} if the transferOrderDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the transferOrderDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PreAuthorize(
        "hasAnyAuthority(\"" +
        AuthoritiesConstants.ADMIN +
        "\", \"" +
        AuthoritiesConstants.MANAGER +
        "\", \"" +
        AuthoritiesConstants.WAREHOUSE +
        "\")"
    )
    @PutMapping("/transfer-orders/{id}")
    public ResponseEntity<TransferOrderDTO> updateTransferOrder(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody TransferOrderDTO transferOrderDTO
    ) throws URISyntaxException {
        log.debug("REST request to update TransferOrder : {}, {}", id, transferOrderDTO);
        if (transferOrderDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, transferOrderDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!transferOrderRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        TransferOrderDTO result = transferOrderService.update(transferOrderDTO);
        return ResponseEntity
            .ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, transferOrderDTO.getId().toString()))
            .body(result);
    }

    /**
     * {@code PATCH  /transfer-orders/:id} : Partial updates given fields of an existing transferOrder, field will ignore if it is null
     *
     * @param id the id of the transferOrderDTO to save.
     * @param transferOrderDTO the transferOrderDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated transferOrderDTO,
     * or with status {@code 400 (Bad Request)} if the transferOrderDTO is not valid,
     * or with status {@code 404 (Not Found)} if the transferOrderDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the transferOrderDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PreAuthorize(
        "hasAnyAuthority(\"" +
        AuthoritiesConstants.ADMIN +
        "\", \"" +
        AuthoritiesConstants.MANAGER +
        "\", \"" +
        AuthoritiesConstants.WAREHOUSE +
        "\")"
    )
    @PatchMapping(value = "/transfer-orders/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<TransferOrderDTO> partialUpdateTransferOrder(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody TransferOrderDTO transferOrderDTO
    ) throws URISyntaxException {
        log.debug("REST request to partial update TransferOrder partially : {}, {}", id, transferOrderDTO);
        if (transferOrderDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, transferOrderDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!transferOrderRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<TransferOrderDTO> result = transferOrderService.partialUpdate(transferOrderDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, transferOrderDTO.getId().toString())
        );
    }

    /**
     * {@code GET  /transfer-orders} : get all the transferOrders.
     *
     * @param pageable the pagination information.
     * @param eagerload flag to eager load entities from relationships (This is applicable for many-to-many).
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of transferOrders in body.
     */
    @PreAuthorize(
        "hasAnyAuthority(\"" +
        AuthoritiesConstants.ADMIN +
        "\", \"" +
        AuthoritiesConstants.MANAGER +
        "\", \"" +
        AuthoritiesConstants.SHIPPER +
        "\", \"" +
        AuthoritiesConstants.WAREHOUSE +
        "\")"
    )
    @GetMapping("/transfer-orders")
    public ResponseEntity<List<TransferOrderDTO>> getAllTransferOrders(
        @org.springdoc.api.annotations.ParameterObject Pageable pageable,
        @RequestParam(required = false, defaultValue = "false") boolean eagerload
    ) {
        log.debug("REST request to get a page of TransferOrders");
        Page<TransferOrderDTO> page;
        if (eagerload) {
            page = transferOrderService.findAllWithEagerRelationships(pageable);
        } else {
            page = transferOrderService.findAll(pageable);
        }
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /transfer-orders/:id} : get the "id" transferOrder.
     *
     * @param id the id of the transferOrderDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the transferOrderDTO, or with status {@code 404 (Not Found)}.
     */
    @PreAuthorize(
        "hasAnyAuthority(\"" +
        AuthoritiesConstants.ADMIN +
        "\", \"" +
        AuthoritiesConstants.MANAGER +
        "\", \"" +
        AuthoritiesConstants.SHIPPER +
        "\", \"" +
        AuthoritiesConstants.WAREHOUSE +
        "\")"
    )
    @GetMapping("/transfer-orders/{id}")
    public ResponseEntity<TransferOrderDTO> getTransferOrder(@PathVariable Long id) {
        log.debug("REST request to get TransferOrder : {}", id);
        Optional<TransferOrderDTO> transferOrderDTO = transferOrderService.findOne(id);
        return ResponseUtil.wrapOrNotFound(transferOrderDTO);
    }

    /**
     * {@code DELETE  /transfer-orders/:id} : delete the "id" transferOrder.
     *
     * @param id the id of the transferOrderDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @PreAuthorize(
        "hasAnyAuthority(\"" +
        AuthoritiesConstants.ADMIN +
        "\", \"" +
        AuthoritiesConstants.MANAGER +
        "\", \"" +
        AuthoritiesConstants.WAREHOUSE +
        "\")"
    )
    @DeleteMapping("/transfer-orders/{id}")
    public ResponseEntity<Void> deleteTransferOrder(@PathVariable Long id) {
        log.debug("REST request to delete TransferOrder : {}", id);
        transferOrderService.delete(id);
        return ResponseEntity
            .noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString()))
            .build();
    }

    /**
     * {@code PUT  /transfer-orders/{id}/approve} : Duyệt lệnh điều chuyển.
     */
    @PreAuthorize("hasAnyAuthority(\"" + AuthoritiesConstants.ADMIN + "\", \"" + AuthoritiesConstants.MANAGER + "\")")
    @PutMapping("/transfer-orders/{id}/approve")
    public ResponseEntity<TransferOrderDTO> approveTransferOrder(@PathVariable Long id) {
        log.debug("REST request to approve TransferOrder : {}", id);

        TransferOrderDTO result = transferOrderService.approveOrder(id);

        return ResponseEntity
            .ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, id.toString()))
            .body(result);
    }

    /**
     * {@code PUT  /transfer-orders/{id}/cancel} : Hủy phiếu điều chuyển.
     */
    @PreAuthorize("hasAnyAuthority(\"" + AuthoritiesConstants.ADMIN + "\", \"" + AuthoritiesConstants.MANAGER + "\")")
    @PutMapping("/transfer-orders/{id}/cancel")
    public ResponseEntity<TransferOrderDTO> cancelTransferOrder(@PathVariable Long id) {
        log.debug("REST request to cancel TransferOrder : {}", id);

        TransferOrderDTO result = transferOrderService.cancelOrder(id);

        return ResponseEntity
            .ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, id.toString()))
            .body(result);
    }

    @PreAuthorize("hasAnyAuthority(\"" + AuthoritiesConstants.ADMIN + "\", \"" + AuthoritiesConstants.SHIPPER + "\")")
    @PutMapping("/transfer-orders/{id}/start-delivery")
    public ResponseEntity<TransferOrderDTO> startDelivery(@PathVariable Long id) {
        return ResponseEntity.ok().body(transferOrderService.startDelivery(id));
    }

    @PreAuthorize("hasAnyAuthority(\"" + AuthoritiesConstants.ADMIN + "\", \"" + AuthoritiesConstants.SHIPPER + "\")")
    @PutMapping("/transfer-orders/{id}/confirm-delivery")
    public ResponseEntity<TransferOrderDTO> confirmDelivery(@PathVariable Long id) {
        return ResponseEntity.ok().body(transferOrderService.confirmDelivery(id));
    }

    @PreAuthorize("hasAnyAuthority(\"" + AuthoritiesConstants.ADMIN + "\", \"" + AuthoritiesConstants.MANAGER + "\")")
    @PutMapping("/transfer-orders/{id}/complete")
    public ResponseEntity<TransferOrderDTO> completeTransferOrder(@PathVariable Long id) {
        return ResponseEntity.ok().body(transferOrderService.completeOrder(id));
    }
}
