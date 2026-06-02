package com.mycompany.myapp.web.rest;

import com.mycompany.myapp.repository.SalesOrderRepository;
import com.mycompany.myapp.security.AuthoritiesConstants;
import com.mycompany.myapp.service.SalesOrderService;
import com.mycompany.myapp.service.dto.SalesOrderDTO;
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
 * REST controller for managing {@link com.mycompany.myapp.domain.SalesOrder}.
 */
@RestController
@RequestMapping("/api")
public class SalesOrderResource {

    private final Logger log = LoggerFactory.getLogger(SalesOrderResource.class);

    private static final String ENTITY_NAME = "salesOrder";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final SalesOrderService salesOrderService;

    private final SalesOrderRepository salesOrderRepository;

    public SalesOrderResource(SalesOrderService salesOrderService, SalesOrderRepository salesOrderRepository) {
        this.salesOrderService = salesOrderService;
        this.salesOrderRepository = salesOrderRepository;
    }

    /**
     * {@code POST  /sales-orders} : Create a new salesOrder.
     *
     * @param salesOrderDTO the salesOrderDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new salesOrderDTO, or with status {@code 400 (Bad Request)} if the salesOrder has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PreAuthorize(
        "hasAnyAuthority(\"" +
        AuthoritiesConstants.ADMIN +
        "\", \"" +
        AuthoritiesConstants.MANAGER +
        "\", \"" +
        AuthoritiesConstants.SALES +
        "\")"
    )
    @PostMapping("/sales-orders")
    public ResponseEntity<SalesOrderDTO> createSalesOrder(@Valid @RequestBody SalesOrderDTO salesOrderDTO) throws URISyntaxException {
        log.debug("REST request to save SalesOrder : {}", salesOrderDTO);
        if (salesOrderDTO.getId() != null) {
            throw new BadRequestAlertException("A new salesOrder cannot already have an ID", ENTITY_NAME, "idexists");
        }
        SalesOrderDTO result = salesOrderService.save(salesOrderDTO);
        return ResponseEntity
            .created(new URI("/api/sales-orders/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * {@code PUT  /sales-orders/:id} : Updates an existing salesOrder.
     *
     * @param id the id of the salesOrderDTO to save.
     * @param salesOrderDTO the salesOrderDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated salesOrderDTO,
     * or with status {@code 400 (Bad Request)} if the salesOrderDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the salesOrderDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PreAuthorize(
        "hasAnyAuthority(\"" +
        AuthoritiesConstants.ADMIN +
        "\", \"" +
        AuthoritiesConstants.MANAGER +
        "\", \"" +
        AuthoritiesConstants.SALES +
        "\")"
    )
    @PutMapping("/sales-orders/{id}")
    public ResponseEntity<SalesOrderDTO> updateSalesOrder(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody SalesOrderDTO salesOrderDTO
    ) throws URISyntaxException {
        log.debug("REST request to update SalesOrder : {}, {}", id, salesOrderDTO);
        if (salesOrderDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, salesOrderDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!salesOrderRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        SalesOrderDTO result = salesOrderService.update(salesOrderDTO);
        return ResponseEntity
            .ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, salesOrderDTO.getId().toString()))
            .body(result);
    }

    /**
     * {@code PATCH  /sales-orders/:id} : Partial updates given fields of an existing salesOrder, field will ignore if it is null
     *
     * @param id the id of the salesOrderDTO to save.
     * @param salesOrderDTO the salesOrderDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated salesOrderDTO,
     * or with status {@code 400 (Bad Request)} if the salesOrderDTO is not valid,
     * or with status {@code 404 (Not Found)} if the salesOrderDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the salesOrderDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PreAuthorize(
        "hasAnyAuthority(\"" +
        AuthoritiesConstants.ADMIN +
        "\", \"" +
        AuthoritiesConstants.MANAGER +
        "\", \"" +
        AuthoritiesConstants.SALES +
        "\")"
    )
    @PatchMapping(value = "/sales-orders/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<SalesOrderDTO> partialUpdateSalesOrder(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody SalesOrderDTO salesOrderDTO
    ) throws URISyntaxException {
        log.debug("REST request to partial update SalesOrder partially : {}, {}", id, salesOrderDTO);
        if (salesOrderDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, salesOrderDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!salesOrderRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<SalesOrderDTO> result = salesOrderService.partialUpdate(salesOrderDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, salesOrderDTO.getId().toString())
        );
    }

    /**
     * {@code GET  /sales-orders} : get all the salesOrders.
     *
     * @param pageable the pagination information.
     * @param eagerload flag to eager load entities from relationships (This is applicable for many-to-many).
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of salesOrders in body.
     */
    @PreAuthorize(
        "hasAnyAuthority(\"" +
        AuthoritiesConstants.ADMIN +
        "\", \"" +
        AuthoritiesConstants.MANAGER +
        "\", \"" +
        AuthoritiesConstants.SHIPPER +
        "\", \"" +
        AuthoritiesConstants.SALES +
        "\", \"" +
        AuthoritiesConstants.ACCOUNTANT +
        "\")"
    )
    @GetMapping("/sales-orders")
    public ResponseEntity<List<SalesOrderDTO>> getAllSalesOrders(
        @org.springdoc.api.annotations.ParameterObject Pageable pageable,
        @RequestParam(required = false, defaultValue = "false") boolean eagerload
    ) {
        log.debug("REST request to get a page of SalesOrders");
        Page<SalesOrderDTO> page;
        if (eagerload) {
            page = salesOrderService.findAllWithEagerRelationships(pageable);
        } else {
            page = salesOrderService.findAll(pageable);
        }
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /sales-orders/:id} : get the "id" salesOrder.
     *
     * @param id the id of the salesOrderDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the salesOrderDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/sales-orders/{id}")
    @PreAuthorize(
        "hasAnyAuthority(\"" +
        AuthoritiesConstants.ADMIN +
        "\", \"" +
        AuthoritiesConstants.MANAGER +
        "\", \"" +
        AuthoritiesConstants.SHIPPER +
        "\", \"" +
        AuthoritiesConstants.SALES +
        "\", \"" +
        AuthoritiesConstants.ACCOUNTANT +
        "\")"
    )
    public ResponseEntity<SalesOrderDTO> getSalesOrder(@PathVariable Long id) {
        log.debug("REST request to get SalesOrder : {}", id);
        Optional<SalesOrderDTO> salesOrderDTO = salesOrderService.findOne(id);
        return ResponseUtil.wrapOrNotFound(salesOrderDTO);
    }

    /**
     * {@code DELETE  /sales-orders/:id} : delete the "id" salesOrder.
     *
     * @param id the id of the salesOrderDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @PreAuthorize(
        "hasAnyAuthority(\"" +
        AuthoritiesConstants.ADMIN +
        "\", \"" +
        AuthoritiesConstants.MANAGER +
        "\", \"" +
        AuthoritiesConstants.SALES +
        "\")"
    )
    @DeleteMapping("/sales-orders/{id}")
    public ResponseEntity<Void> deleteSalesOrder(@PathVariable Long id) {
        log.debug("REST request to delete SalesOrder : {}", id);
        salesOrderService.delete(id);
        return ResponseEntity
            .noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString()))
            .build();
    }

    /**
     * {@code PUT  /sales-orders/{id}/approve} : Duyệt xuất kho cho Đơn bán hàng.
     */
    @PreAuthorize("hasAnyAuthority(\"" + AuthoritiesConstants.ADMIN + "\", \"" + AuthoritiesConstants.MANAGER + "\")")
    @PutMapping("/sales-orders/{id}/approve")
    public ResponseEntity<SalesOrderDTO> approveSalesOrder(@PathVariable Long id) {
        log.debug("REST request to approve SalesOrder : {}", id);

        SalesOrderDTO result = salesOrderService.approveOrder(id);

        return ResponseEntity
            .ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, id.toString()))
            .body(result);
    }

    @PutMapping("/sales-orders/{id}/complete")
    @PreAuthorize("hasAnyAuthority(\"" + AuthoritiesConstants.ADMIN + "\", \"" + AuthoritiesConstants.ACCOUNTANT + "\")")
    public ResponseEntity<SalesOrderDTO> completeSalesOrder(@PathVariable Long id) {
        log.debug("REST request to complete SalesOrder : {}", id);
        SalesOrderDTO result = salesOrderService.completeOrder(id);
        return ResponseEntity
            .ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, "salesOrder", id.toString()))
            .body(result);
    }

    /**
     * {@code PUT  /sales-orders/{id}/cancel} : Hủy đơn bán hàng và hoàn trả dữ liệu.
     */
    @PutMapping("/sales-orders/{id}/cancel")
    @PreAuthorize(
        "hasAnyAuthority(\"" +
        AuthoritiesConstants.ADMIN +
        "\", \"" +
        AuthoritiesConstants.MANAGER +
        "\", \"" +
        AuthoritiesConstants.ACCOUNTANT +
        "\")"
    )
    public ResponseEntity<SalesOrderDTO> cancelSalesOrder(@PathVariable Long id) {
        log.debug("REST request to cancel SalesOrder : {}", id);

        SalesOrderDTO result = salesOrderService.cancelOrder(id);

        return ResponseEntity
            .ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, id.toString()))
            .body(result);
    }

    /**
     * {@code PUT  /sales-orders/{id}/start-delivery} : Shipper nhận hàng đi giao.
     */
    @PreAuthorize("hasAnyAuthority(\"" + AuthoritiesConstants.ADMIN + "\", \"" + AuthoritiesConstants.SHIPPER + "\")")
    @PutMapping("/sales-orders/{id}/start-delivery")
    public ResponseEntity<SalesOrderDTO> startDelivery(@PathVariable Long id) {
        log.debug("REST request to start delivery for SalesOrder : {}", id);
        SalesOrderDTO result = salesOrderService.startDelivery(id);
        return ResponseEntity.ok().body(result);
    }

    /**
     * {@code PUT  /sales-orders/{id}/confirm-delivery} : Shipper báo cáo giao thành công chờ tiền.
     */
    @PreAuthorize("hasAnyAuthority(\"" + AuthoritiesConstants.ADMIN + "\", \"" + AuthoritiesConstants.SHIPPER + "\")")
    @PutMapping("/sales-orders/{id}/confirm-delivery")
    public ResponseEntity<SalesOrderDTO> confirmDelivery(@PathVariable Long id) {
        log.debug("REST request to confirm delivery for SalesOrder : {}", id);
        SalesOrderDTO result = salesOrderService.confirmDelivery(id);
        return ResponseEntity.ok().body(result);
    }
}
