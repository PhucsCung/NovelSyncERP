package com.mycompany.myapp.web.rest;

import static com.mycompany.myapp.web.rest.TestUtil.sameNumber;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.mycompany.myapp.IntegrationTest;
import com.mycompany.myapp.domain.PurchaseOrderLine;
import com.mycompany.myapp.repository.PurchaseOrderLineRepository;
import com.mycompany.myapp.service.PurchaseOrderLineService;
import com.mycompany.myapp.service.dto.PurchaseOrderLineDTO;
import com.mycompany.myapp.service.mapper.PurchaseOrderLineMapper;
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
 * Integration tests for the {@link PurchaseOrderLineResource} REST controller.
 */
@IntegrationTest
@ExtendWith(MockitoExtension.class)
@AutoConfigureMockMvc
@WithMockUser
class PurchaseOrderLineResourceIT {

    private static final Integer DEFAULT_QUANTITY = 1;
    private static final Integer UPDATED_QUANTITY = 2;

    private static final BigDecimal DEFAULT_UNIT_PRICE = new BigDecimal(1);
    private static final BigDecimal UPDATED_UNIT_PRICE = new BigDecimal(2);

    private static final String ENTITY_API_URL = "/api/purchase-order-lines";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong count = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private PurchaseOrderLineRepository purchaseOrderLineRepository;

    @Mock
    private PurchaseOrderLineRepository purchaseOrderLineRepositoryMock;

    @Autowired
    private PurchaseOrderLineMapper purchaseOrderLineMapper;

    @Mock
    private PurchaseOrderLineService purchaseOrderLineServiceMock;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restPurchaseOrderLineMockMvc;

    private PurchaseOrderLine purchaseOrderLine;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static PurchaseOrderLine createEntity(EntityManager em) {
        PurchaseOrderLine purchaseOrderLine = new PurchaseOrderLine().quantity(DEFAULT_QUANTITY).unitPrice(DEFAULT_UNIT_PRICE);
        return purchaseOrderLine;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static PurchaseOrderLine createUpdatedEntity(EntityManager em) {
        PurchaseOrderLine purchaseOrderLine = new PurchaseOrderLine().quantity(UPDATED_QUANTITY).unitPrice(UPDATED_UNIT_PRICE);
        return purchaseOrderLine;
    }

    @BeforeEach
    public void initTest() {
        purchaseOrderLine = createEntity(em);
    }

    @Test
    @Transactional
    void createPurchaseOrderLine() throws Exception {
        int databaseSizeBeforeCreate = purchaseOrderLineRepository.findAll().size();
        // Create the PurchaseOrderLine
        PurchaseOrderLineDTO purchaseOrderLineDTO = purchaseOrderLineMapper.toDto(purchaseOrderLine);
        restPurchaseOrderLineMockMvc
            .perform(
                post(ENTITY_API_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(purchaseOrderLineDTO))
            )
            .andExpect(status().isCreated());

        // Validate the PurchaseOrderLine in the database
        List<PurchaseOrderLine> purchaseOrderLineList = purchaseOrderLineRepository.findAll();
        assertThat(purchaseOrderLineList).hasSize(databaseSizeBeforeCreate + 1);
        PurchaseOrderLine testPurchaseOrderLine = purchaseOrderLineList.get(purchaseOrderLineList.size() - 1);
        assertThat(testPurchaseOrderLine.getQuantity()).isEqualTo(DEFAULT_QUANTITY);
        assertThat(testPurchaseOrderLine.getUnitPrice()).isEqualByComparingTo(DEFAULT_UNIT_PRICE);
    }

    @Test
    @Transactional
    void createPurchaseOrderLineWithExistingId() throws Exception {
        // Create the PurchaseOrderLine with an existing ID
        purchaseOrderLine.setId(1L);
        PurchaseOrderLineDTO purchaseOrderLineDTO = purchaseOrderLineMapper.toDto(purchaseOrderLine);

        int databaseSizeBeforeCreate = purchaseOrderLineRepository.findAll().size();

        // An entity with an existing ID cannot be created, so this API call must fail
        restPurchaseOrderLineMockMvc
            .perform(
                post(ENTITY_API_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(purchaseOrderLineDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the PurchaseOrderLine in the database
        List<PurchaseOrderLine> purchaseOrderLineList = purchaseOrderLineRepository.findAll();
        assertThat(purchaseOrderLineList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkQuantityIsRequired() throws Exception {
        int databaseSizeBeforeTest = purchaseOrderLineRepository.findAll().size();
        // set the field null
        purchaseOrderLine.setQuantity(null);

        // Create the PurchaseOrderLine, which fails.
        PurchaseOrderLineDTO purchaseOrderLineDTO = purchaseOrderLineMapper.toDto(purchaseOrderLine);

        restPurchaseOrderLineMockMvc
            .perform(
                post(ENTITY_API_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(purchaseOrderLineDTO))
            )
            .andExpect(status().isBadRequest());

        List<PurchaseOrderLine> purchaseOrderLineList = purchaseOrderLineRepository.findAll();
        assertThat(purchaseOrderLineList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkUnitPriceIsRequired() throws Exception {
        int databaseSizeBeforeTest = purchaseOrderLineRepository.findAll().size();
        // set the field null
        purchaseOrderLine.setUnitPrice(null);

        // Create the PurchaseOrderLine, which fails.
        PurchaseOrderLineDTO purchaseOrderLineDTO = purchaseOrderLineMapper.toDto(purchaseOrderLine);

        restPurchaseOrderLineMockMvc
            .perform(
                post(ENTITY_API_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(purchaseOrderLineDTO))
            )
            .andExpect(status().isBadRequest());

        List<PurchaseOrderLine> purchaseOrderLineList = purchaseOrderLineRepository.findAll();
        assertThat(purchaseOrderLineList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllPurchaseOrderLines() throws Exception {
        // Initialize the database
        purchaseOrderLineRepository.saveAndFlush(purchaseOrderLine);

        // Get all the purchaseOrderLineList
        restPurchaseOrderLineMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(purchaseOrderLine.getId().intValue())))
            .andExpect(jsonPath("$.[*].quantity").value(hasItem(DEFAULT_QUANTITY)))
            .andExpect(jsonPath("$.[*].unitPrice").value(hasItem(sameNumber(DEFAULT_UNIT_PRICE))));
    }

    @SuppressWarnings({ "unchecked" })
    void getAllPurchaseOrderLinesWithEagerRelationshipsIsEnabled() throws Exception {
        when(purchaseOrderLineServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restPurchaseOrderLineMockMvc.perform(get(ENTITY_API_URL + "?eagerload=true")).andExpect(status().isOk());

        verify(purchaseOrderLineServiceMock, times(1)).findAllWithEagerRelationships(any());
    }

    @SuppressWarnings({ "unchecked" })
    void getAllPurchaseOrderLinesWithEagerRelationshipsIsNotEnabled() throws Exception {
        when(purchaseOrderLineServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restPurchaseOrderLineMockMvc.perform(get(ENTITY_API_URL + "?eagerload=false")).andExpect(status().isOk());
        verify(purchaseOrderLineRepositoryMock, times(1)).findAll(any(Pageable.class));
    }

    @Test
    @Transactional
    void getPurchaseOrderLine() throws Exception {
        // Initialize the database
        purchaseOrderLineRepository.saveAndFlush(purchaseOrderLine);

        // Get the purchaseOrderLine
        restPurchaseOrderLineMockMvc
            .perform(get(ENTITY_API_URL_ID, purchaseOrderLine.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(purchaseOrderLine.getId().intValue()))
            .andExpect(jsonPath("$.quantity").value(DEFAULT_QUANTITY))
            .andExpect(jsonPath("$.unitPrice").value(sameNumber(DEFAULT_UNIT_PRICE)));
    }

    @Test
    @Transactional
    void getNonExistingPurchaseOrderLine() throws Exception {
        // Get the purchaseOrderLine
        restPurchaseOrderLineMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingPurchaseOrderLine() throws Exception {
        // Initialize the database
        purchaseOrderLineRepository.saveAndFlush(purchaseOrderLine);

        int databaseSizeBeforeUpdate = purchaseOrderLineRepository.findAll().size();

        // Update the purchaseOrderLine
        PurchaseOrderLine updatedPurchaseOrderLine = purchaseOrderLineRepository.findById(purchaseOrderLine.getId()).get();
        // Disconnect from session so that the updates on updatedPurchaseOrderLine are not directly saved in db
        em.detach(updatedPurchaseOrderLine);
        updatedPurchaseOrderLine.quantity(UPDATED_QUANTITY).unitPrice(UPDATED_UNIT_PRICE);
        PurchaseOrderLineDTO purchaseOrderLineDTO = purchaseOrderLineMapper.toDto(updatedPurchaseOrderLine);

        restPurchaseOrderLineMockMvc
            .perform(
                put(ENTITY_API_URL_ID, purchaseOrderLineDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(purchaseOrderLineDTO))
            )
            .andExpect(status().isOk());

        // Validate the PurchaseOrderLine in the database
        List<PurchaseOrderLine> purchaseOrderLineList = purchaseOrderLineRepository.findAll();
        assertThat(purchaseOrderLineList).hasSize(databaseSizeBeforeUpdate);
        PurchaseOrderLine testPurchaseOrderLine = purchaseOrderLineList.get(purchaseOrderLineList.size() - 1);
        assertThat(testPurchaseOrderLine.getQuantity()).isEqualTo(UPDATED_QUANTITY);
        assertThat(testPurchaseOrderLine.getUnitPrice()).isEqualByComparingTo(UPDATED_UNIT_PRICE);
    }

    @Test
    @Transactional
    void putNonExistingPurchaseOrderLine() throws Exception {
        int databaseSizeBeforeUpdate = purchaseOrderLineRepository.findAll().size();
        purchaseOrderLine.setId(count.incrementAndGet());

        // Create the PurchaseOrderLine
        PurchaseOrderLineDTO purchaseOrderLineDTO = purchaseOrderLineMapper.toDto(purchaseOrderLine);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restPurchaseOrderLineMockMvc
            .perform(
                put(ENTITY_API_URL_ID, purchaseOrderLineDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(purchaseOrderLineDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the PurchaseOrderLine in the database
        List<PurchaseOrderLine> purchaseOrderLineList = purchaseOrderLineRepository.findAll();
        assertThat(purchaseOrderLineList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchPurchaseOrderLine() throws Exception {
        int databaseSizeBeforeUpdate = purchaseOrderLineRepository.findAll().size();
        purchaseOrderLine.setId(count.incrementAndGet());

        // Create the PurchaseOrderLine
        PurchaseOrderLineDTO purchaseOrderLineDTO = purchaseOrderLineMapper.toDto(purchaseOrderLine);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restPurchaseOrderLineMockMvc
            .perform(
                put(ENTITY_API_URL_ID, count.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(purchaseOrderLineDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the PurchaseOrderLine in the database
        List<PurchaseOrderLine> purchaseOrderLineList = purchaseOrderLineRepository.findAll();
        assertThat(purchaseOrderLineList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamPurchaseOrderLine() throws Exception {
        int databaseSizeBeforeUpdate = purchaseOrderLineRepository.findAll().size();
        purchaseOrderLine.setId(count.incrementAndGet());

        // Create the PurchaseOrderLine
        PurchaseOrderLineDTO purchaseOrderLineDTO = purchaseOrderLineMapper.toDto(purchaseOrderLine);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restPurchaseOrderLineMockMvc
            .perform(
                put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(purchaseOrderLineDTO))
            )
            .andExpect(status().isMethodNotAllowed());

        // Validate the PurchaseOrderLine in the database
        List<PurchaseOrderLine> purchaseOrderLineList = purchaseOrderLineRepository.findAll();
        assertThat(purchaseOrderLineList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdatePurchaseOrderLineWithPatch() throws Exception {
        // Initialize the database
        purchaseOrderLineRepository.saveAndFlush(purchaseOrderLine);

        int databaseSizeBeforeUpdate = purchaseOrderLineRepository.findAll().size();

        // Update the purchaseOrderLine using partial update
        PurchaseOrderLine partialUpdatedPurchaseOrderLine = new PurchaseOrderLine();
        partialUpdatedPurchaseOrderLine.setId(purchaseOrderLine.getId());

        restPurchaseOrderLineMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedPurchaseOrderLine.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedPurchaseOrderLine))
            )
            .andExpect(status().isOk());

        // Validate the PurchaseOrderLine in the database
        List<PurchaseOrderLine> purchaseOrderLineList = purchaseOrderLineRepository.findAll();
        assertThat(purchaseOrderLineList).hasSize(databaseSizeBeforeUpdate);
        PurchaseOrderLine testPurchaseOrderLine = purchaseOrderLineList.get(purchaseOrderLineList.size() - 1);
        assertThat(testPurchaseOrderLine.getQuantity()).isEqualTo(DEFAULT_QUANTITY);
        assertThat(testPurchaseOrderLine.getUnitPrice()).isEqualByComparingTo(DEFAULT_UNIT_PRICE);
    }

    @Test
    @Transactional
    void fullUpdatePurchaseOrderLineWithPatch() throws Exception {
        // Initialize the database
        purchaseOrderLineRepository.saveAndFlush(purchaseOrderLine);

        int databaseSizeBeforeUpdate = purchaseOrderLineRepository.findAll().size();

        // Update the purchaseOrderLine using partial update
        PurchaseOrderLine partialUpdatedPurchaseOrderLine = new PurchaseOrderLine();
        partialUpdatedPurchaseOrderLine.setId(purchaseOrderLine.getId());

        partialUpdatedPurchaseOrderLine.quantity(UPDATED_QUANTITY).unitPrice(UPDATED_UNIT_PRICE);

        restPurchaseOrderLineMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedPurchaseOrderLine.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedPurchaseOrderLine))
            )
            .andExpect(status().isOk());

        // Validate the PurchaseOrderLine in the database
        List<PurchaseOrderLine> purchaseOrderLineList = purchaseOrderLineRepository.findAll();
        assertThat(purchaseOrderLineList).hasSize(databaseSizeBeforeUpdate);
        PurchaseOrderLine testPurchaseOrderLine = purchaseOrderLineList.get(purchaseOrderLineList.size() - 1);
        assertThat(testPurchaseOrderLine.getQuantity()).isEqualTo(UPDATED_QUANTITY);
        assertThat(testPurchaseOrderLine.getUnitPrice()).isEqualByComparingTo(UPDATED_UNIT_PRICE);
    }

    @Test
    @Transactional
    void patchNonExistingPurchaseOrderLine() throws Exception {
        int databaseSizeBeforeUpdate = purchaseOrderLineRepository.findAll().size();
        purchaseOrderLine.setId(count.incrementAndGet());

        // Create the PurchaseOrderLine
        PurchaseOrderLineDTO purchaseOrderLineDTO = purchaseOrderLineMapper.toDto(purchaseOrderLine);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restPurchaseOrderLineMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, purchaseOrderLineDTO.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(purchaseOrderLineDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the PurchaseOrderLine in the database
        List<PurchaseOrderLine> purchaseOrderLineList = purchaseOrderLineRepository.findAll();
        assertThat(purchaseOrderLineList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchPurchaseOrderLine() throws Exception {
        int databaseSizeBeforeUpdate = purchaseOrderLineRepository.findAll().size();
        purchaseOrderLine.setId(count.incrementAndGet());

        // Create the PurchaseOrderLine
        PurchaseOrderLineDTO purchaseOrderLineDTO = purchaseOrderLineMapper.toDto(purchaseOrderLine);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restPurchaseOrderLineMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, count.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(purchaseOrderLineDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the PurchaseOrderLine in the database
        List<PurchaseOrderLine> purchaseOrderLineList = purchaseOrderLineRepository.findAll();
        assertThat(purchaseOrderLineList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamPurchaseOrderLine() throws Exception {
        int databaseSizeBeforeUpdate = purchaseOrderLineRepository.findAll().size();
        purchaseOrderLine.setId(count.incrementAndGet());

        // Create the PurchaseOrderLine
        PurchaseOrderLineDTO purchaseOrderLineDTO = purchaseOrderLineMapper.toDto(purchaseOrderLine);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restPurchaseOrderLineMockMvc
            .perform(
                patch(ENTITY_API_URL)
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(purchaseOrderLineDTO))
            )
            .andExpect(status().isMethodNotAllowed());

        // Validate the PurchaseOrderLine in the database
        List<PurchaseOrderLine> purchaseOrderLineList = purchaseOrderLineRepository.findAll();
        assertThat(purchaseOrderLineList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deletePurchaseOrderLine() throws Exception {
        // Initialize the database
        purchaseOrderLineRepository.saveAndFlush(purchaseOrderLine);

        int databaseSizeBeforeDelete = purchaseOrderLineRepository.findAll().size();

        // Delete the purchaseOrderLine
        restPurchaseOrderLineMockMvc
            .perform(delete(ENTITY_API_URL_ID, purchaseOrderLine.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<PurchaseOrderLine> purchaseOrderLineList = purchaseOrderLineRepository.findAll();
        assertThat(purchaseOrderLineList).hasSize(databaseSizeBeforeDelete - 1);
    }
}
