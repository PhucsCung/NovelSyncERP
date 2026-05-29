package com.mycompany.myapp.web.rest;

import static com.mycompany.myapp.web.rest.TestUtil.sameNumber;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.mycompany.myapp.IntegrationTest;
import com.mycompany.myapp.domain.SalesOrderLine;
import com.mycompany.myapp.repository.SalesOrderLineRepository;
import com.mycompany.myapp.service.SalesOrderLineService;
import com.mycompany.myapp.service.dto.SalesOrderLineDTO;
import com.mycompany.myapp.service.mapper.SalesOrderLineMapper;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import javax.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration tests for the {@link SalesOrderLineResource} REST controller.
 */
@IntegrationTest
@ExtendWith(MockitoExtension.class)
@AutoConfigureMockMvc
@WithMockUser
class SalesOrderLineResourceIT {

    private static final Integer DEFAULT_QUANTITY = 1;
    private static final Integer UPDATED_QUANTITY = 2;

    private static final BigDecimal DEFAULT_UNIT_PRICE = new BigDecimal(1);
    private static final BigDecimal UPDATED_UNIT_PRICE = new BigDecimal(2);

    private static final String ENTITY_API_URL = "/api/sales-order-lines";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong count = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private SalesOrderLineRepository salesOrderLineRepository;

    @Mock
    private SalesOrderLineRepository salesOrderLineRepositoryMock;

    @Autowired
    private SalesOrderLineMapper salesOrderLineMapper;

    @Mock
    private SalesOrderLineService salesOrderLineServiceMock;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restSalesOrderLineMockMvc;

    private SalesOrderLine salesOrderLine;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static SalesOrderLine createEntity(EntityManager em) {
        SalesOrderLine salesOrderLine = new SalesOrderLine().quantity(DEFAULT_QUANTITY).unitPrice(DEFAULT_UNIT_PRICE);
        return salesOrderLine;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static SalesOrderLine createUpdatedEntity(EntityManager em) {
        SalesOrderLine salesOrderLine = new SalesOrderLine().quantity(UPDATED_QUANTITY).unitPrice(UPDATED_UNIT_PRICE);
        return salesOrderLine;
    }

    @BeforeEach
    public void initTest() {
        salesOrderLine = createEntity(em);
    }

    @Test
    @Transactional
    void createSalesOrderLine() throws Exception {
        int databaseSizeBeforeCreate = salesOrderLineRepository.findAll().size();
        // Create the SalesOrderLine
        SalesOrderLineDTO salesOrderLineDTO = salesOrderLineMapper.toDto(salesOrderLine);
        restSalesOrderLineMockMvc
            .perform(
                post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(salesOrderLineDTO))
            )
            .andExpect(status().isCreated());

        // Validate the SalesOrderLine in the database
        List<SalesOrderLine> salesOrderLineList = salesOrderLineRepository.findAll();
        assertThat(salesOrderLineList).hasSize(databaseSizeBeforeCreate + 1);
        SalesOrderLine testSalesOrderLine = salesOrderLineList.get(salesOrderLineList.size() - 1);
        assertThat(testSalesOrderLine.getQuantity()).isEqualTo(DEFAULT_QUANTITY);
        assertThat(testSalesOrderLine.getUnitPrice()).isEqualByComparingTo(DEFAULT_UNIT_PRICE);
    }

    @Test
    @Transactional
    void createSalesOrderLineWithExistingId() throws Exception {
        // Create the SalesOrderLine with an existing ID
        salesOrderLine.setId(1L);
        SalesOrderLineDTO salesOrderLineDTO = salesOrderLineMapper.toDto(salesOrderLine);

        int databaseSizeBeforeCreate = salesOrderLineRepository.findAll().size();

        // An entity with an existing ID cannot be created, so this API call must fail
        restSalesOrderLineMockMvc
            .perform(
                post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(salesOrderLineDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the SalesOrderLine in the database
        List<SalesOrderLine> salesOrderLineList = salesOrderLineRepository.findAll();
        assertThat(salesOrderLineList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkQuantityIsRequired() throws Exception {
        int databaseSizeBeforeTest = salesOrderLineRepository.findAll().size();
        // set the field null
        salesOrderLine.setQuantity(null);

        // Create the SalesOrderLine, which fails.
        SalesOrderLineDTO salesOrderLineDTO = salesOrderLineMapper.toDto(salesOrderLine);

        restSalesOrderLineMockMvc
            .perform(
                post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(salesOrderLineDTO))
            )
            .andExpect(status().isBadRequest());

        List<SalesOrderLine> salesOrderLineList = salesOrderLineRepository.findAll();
        assertThat(salesOrderLineList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkUnitPriceIsRequired() throws Exception {
        int databaseSizeBeforeTest = salesOrderLineRepository.findAll().size();
        // set the field null
        salesOrderLine.setUnitPrice(null);

        // Create the SalesOrderLine, which fails.
        SalesOrderLineDTO salesOrderLineDTO = salesOrderLineMapper.toDto(salesOrderLine);

        restSalesOrderLineMockMvc
            .perform(
                post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(salesOrderLineDTO))
            )
            .andExpect(status().isBadRequest());

        List<SalesOrderLine> salesOrderLineList = salesOrderLineRepository.findAll();
        assertThat(salesOrderLineList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllSalesOrderLines() throws Exception {
        // Initialize the database
        salesOrderLineRepository.saveAndFlush(salesOrderLine);

        // Get all the salesOrderLineList
        restSalesOrderLineMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(salesOrderLine.getId().intValue())))
            .andExpect(jsonPath("$.[*].quantity").value(hasItem(DEFAULT_QUANTITY)))
            .andExpect(jsonPath("$.[*].unitPrice").value(hasItem(sameNumber(DEFAULT_UNIT_PRICE))));
    }

    @SuppressWarnings({ "unchecked" })
    void getAllSalesOrderLinesWithEagerRelationshipsIsEnabled() throws Exception {
        when(salesOrderLineServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restSalesOrderLineMockMvc.perform(get(ENTITY_API_URL + "?eagerload=true")).andExpect(status().isOk());

        verify(salesOrderLineServiceMock, times(1)).findAllWithEagerRelationships(any());
    }

    @SuppressWarnings({ "unchecked" })
    void getAllSalesOrderLinesWithEagerRelationshipsIsNotEnabled() throws Exception {
        when(salesOrderLineServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restSalesOrderLineMockMvc.perform(get(ENTITY_API_URL + "?eagerload=false")).andExpect(status().isOk());
        verify(salesOrderLineRepositoryMock, times(1)).findAll(any(Pageable.class));
    }

    @Test
    @Transactional
    void getSalesOrderLine() throws Exception {
        // Initialize the database
        salesOrderLineRepository.saveAndFlush(salesOrderLine);

        // Get the salesOrderLine
        restSalesOrderLineMockMvc
            .perform(get(ENTITY_API_URL_ID, salesOrderLine.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(salesOrderLine.getId().intValue()))
            .andExpect(jsonPath("$.quantity").value(DEFAULT_QUANTITY))
            .andExpect(jsonPath("$.unitPrice").value(sameNumber(DEFAULT_UNIT_PRICE)));
    }

    @Test
    @Transactional
    void getNonExistingSalesOrderLine() throws Exception {
        // Get the salesOrderLine
        restSalesOrderLineMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingSalesOrderLine() throws Exception {
        // Initialize the database
        salesOrderLineRepository.saveAndFlush(salesOrderLine);

        int databaseSizeBeforeUpdate = salesOrderLineRepository.findAll().size();

        // Update the salesOrderLine
        SalesOrderLine updatedSalesOrderLine = salesOrderLineRepository.findById(salesOrderLine.getId()).get();
        // Disconnect from session so that the updates on updatedSalesOrderLine are not directly saved in db
        em.detach(updatedSalesOrderLine);
        updatedSalesOrderLine.quantity(UPDATED_QUANTITY).unitPrice(UPDATED_UNIT_PRICE);
        SalesOrderLineDTO salesOrderLineDTO = salesOrderLineMapper.toDto(updatedSalesOrderLine);

        restSalesOrderLineMockMvc
            .perform(
                put(ENTITY_API_URL_ID, salesOrderLineDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(salesOrderLineDTO))
            )
            .andExpect(status().isOk());

        // Validate the SalesOrderLine in the database
        List<SalesOrderLine> salesOrderLineList = salesOrderLineRepository.findAll();
        assertThat(salesOrderLineList).hasSize(databaseSizeBeforeUpdate);
        SalesOrderLine testSalesOrderLine = salesOrderLineList.get(salesOrderLineList.size() - 1);
        assertThat(testSalesOrderLine.getQuantity()).isEqualTo(UPDATED_QUANTITY);
        assertThat(testSalesOrderLine.getUnitPrice()).isEqualByComparingTo(UPDATED_UNIT_PRICE);
    }

    @Test
    @Transactional
    void putNonExistingSalesOrderLine() throws Exception {
        int databaseSizeBeforeUpdate = salesOrderLineRepository.findAll().size();
        salesOrderLine.setId(count.incrementAndGet());

        // Create the SalesOrderLine
        SalesOrderLineDTO salesOrderLineDTO = salesOrderLineMapper.toDto(salesOrderLine);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restSalesOrderLineMockMvc
            .perform(
                put(ENTITY_API_URL_ID, salesOrderLineDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(salesOrderLineDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the SalesOrderLine in the database
        List<SalesOrderLine> salesOrderLineList = salesOrderLineRepository.findAll();
        assertThat(salesOrderLineList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchSalesOrderLine() throws Exception {
        int databaseSizeBeforeUpdate = salesOrderLineRepository.findAll().size();
        salesOrderLine.setId(count.incrementAndGet());

        // Create the SalesOrderLine
        SalesOrderLineDTO salesOrderLineDTO = salesOrderLineMapper.toDto(salesOrderLine);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restSalesOrderLineMockMvc
            .perform(
                put(ENTITY_API_URL_ID, count.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(salesOrderLineDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the SalesOrderLine in the database
        List<SalesOrderLine> salesOrderLineList = salesOrderLineRepository.findAll();
        assertThat(salesOrderLineList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamSalesOrderLine() throws Exception {
        int databaseSizeBeforeUpdate = salesOrderLineRepository.findAll().size();
        salesOrderLine.setId(count.incrementAndGet());

        // Create the SalesOrderLine
        SalesOrderLineDTO salesOrderLineDTO = salesOrderLineMapper.toDto(salesOrderLine);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restSalesOrderLineMockMvc
            .perform(
                put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(salesOrderLineDTO))
            )
            .andExpect(status().isMethodNotAllowed());

        // Validate the SalesOrderLine in the database
        List<SalesOrderLine> salesOrderLineList = salesOrderLineRepository.findAll();
        assertThat(salesOrderLineList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateSalesOrderLineWithPatch() throws Exception {
        // Initialize the database
        salesOrderLineRepository.saveAndFlush(salesOrderLine);

        int databaseSizeBeforeUpdate = salesOrderLineRepository.findAll().size();

        // Update the salesOrderLine using partial update
        SalesOrderLine partialUpdatedSalesOrderLine = new SalesOrderLine();
        partialUpdatedSalesOrderLine.setId(salesOrderLine.getId());

        partialUpdatedSalesOrderLine.unitPrice(UPDATED_UNIT_PRICE);

        restSalesOrderLineMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedSalesOrderLine.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedSalesOrderLine))
            )
            .andExpect(status().isOk());

        // Validate the SalesOrderLine in the database
        List<SalesOrderLine> salesOrderLineList = salesOrderLineRepository.findAll();
        assertThat(salesOrderLineList).hasSize(databaseSizeBeforeUpdate);
        SalesOrderLine testSalesOrderLine = salesOrderLineList.get(salesOrderLineList.size() - 1);
        assertThat(testSalesOrderLine.getQuantity()).isEqualTo(DEFAULT_QUANTITY);
        assertThat(testSalesOrderLine.getUnitPrice()).isEqualByComparingTo(UPDATED_UNIT_PRICE);
    }

    @Test
    @Transactional
    void fullUpdateSalesOrderLineWithPatch() throws Exception {
        // Initialize the database
        salesOrderLineRepository.saveAndFlush(salesOrderLine);

        int databaseSizeBeforeUpdate = salesOrderLineRepository.findAll().size();

        // Update the salesOrderLine using partial update
        SalesOrderLine partialUpdatedSalesOrderLine = new SalesOrderLine();
        partialUpdatedSalesOrderLine.setId(salesOrderLine.getId());

        partialUpdatedSalesOrderLine.quantity(UPDATED_QUANTITY).unitPrice(UPDATED_UNIT_PRICE);

        restSalesOrderLineMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedSalesOrderLine.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedSalesOrderLine))
            )
            .andExpect(status().isOk());

        // Validate the SalesOrderLine in the database
        List<SalesOrderLine> salesOrderLineList = salesOrderLineRepository.findAll();
        assertThat(salesOrderLineList).hasSize(databaseSizeBeforeUpdate);
        SalesOrderLine testSalesOrderLine = salesOrderLineList.get(salesOrderLineList.size() - 1);
        assertThat(testSalesOrderLine.getQuantity()).isEqualTo(UPDATED_QUANTITY);
        assertThat(testSalesOrderLine.getUnitPrice()).isEqualByComparingTo(UPDATED_UNIT_PRICE);
    }

    @Test
    @Transactional
    void patchNonExistingSalesOrderLine() throws Exception {
        int databaseSizeBeforeUpdate = salesOrderLineRepository.findAll().size();
        salesOrderLine.setId(count.incrementAndGet());

        // Create the SalesOrderLine
        SalesOrderLineDTO salesOrderLineDTO = salesOrderLineMapper.toDto(salesOrderLine);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restSalesOrderLineMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, salesOrderLineDTO.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(salesOrderLineDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the SalesOrderLine in the database
        List<SalesOrderLine> salesOrderLineList = salesOrderLineRepository.findAll();
        assertThat(salesOrderLineList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchSalesOrderLine() throws Exception {
        int databaseSizeBeforeUpdate = salesOrderLineRepository.findAll().size();
        salesOrderLine.setId(count.incrementAndGet());

        // Create the SalesOrderLine
        SalesOrderLineDTO salesOrderLineDTO = salesOrderLineMapper.toDto(salesOrderLine);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restSalesOrderLineMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, count.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(salesOrderLineDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the SalesOrderLine in the database
        List<SalesOrderLine> salesOrderLineList = salesOrderLineRepository.findAll();
        assertThat(salesOrderLineList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamSalesOrderLine() throws Exception {
        int databaseSizeBeforeUpdate = salesOrderLineRepository.findAll().size();
        salesOrderLine.setId(count.incrementAndGet());

        // Create the SalesOrderLine
        SalesOrderLineDTO salesOrderLineDTO = salesOrderLineMapper.toDto(salesOrderLine);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restSalesOrderLineMockMvc
            .perform(
                patch(ENTITY_API_URL)
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(salesOrderLineDTO))
            )
            .andExpect(status().isMethodNotAllowed());

        // Validate the SalesOrderLine in the database
        List<SalesOrderLine> salesOrderLineList = salesOrderLineRepository.findAll();
        assertThat(salesOrderLineList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteSalesOrderLine() throws Exception {
        // Initialize the database
        salesOrderLineRepository.saveAndFlush(salesOrderLine);

        int databaseSizeBeforeDelete = salesOrderLineRepository.findAll().size();

        // Delete the salesOrderLine
        restSalesOrderLineMockMvc
            .perform(delete(ENTITY_API_URL_ID, salesOrderLine.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<SalesOrderLine> salesOrderLineList = salesOrderLineRepository.findAll();
        assertThat(salesOrderLineList).hasSize(databaseSizeBeforeDelete - 1);
    }
}
