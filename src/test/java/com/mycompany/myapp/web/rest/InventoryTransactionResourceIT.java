package com.mycompany.myapp.web.rest;

import static com.mycompany.myapp.web.rest.TestUtil.sameNumber;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.mycompany.myapp.IntegrationTest;
import com.mycompany.myapp.domain.InventoryTransaction;
import com.mycompany.myapp.domain.enumeration.TransactionType;
import com.mycompany.myapp.repository.InventoryTransactionRepository;
import com.mycompany.myapp.service.InventoryTransactionService;
import com.mycompany.myapp.service.dto.InventoryTransactionDTO;
import com.mycompany.myapp.service.mapper.InventoryTransactionMapper;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
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
 * Integration tests for the {@link InventoryTransactionResource} REST controller.
 */
@IntegrationTest
@ExtendWith(MockitoExtension.class)
@AutoConfigureMockMvc
@WithMockUser
class InventoryTransactionResourceIT {

    private static final TransactionType DEFAULT_TYPE = TransactionType.RECEIPT;
    private static final TransactionType UPDATED_TYPE = TransactionType.ISSUE;

    private static final Integer DEFAULT_QUANTITY = 1;
    private static final Integer UPDATED_QUANTITY = 2;

    private static final BigDecimal DEFAULT_UNIT_COST = new BigDecimal(1);
    private static final BigDecimal UPDATED_UNIT_COST = new BigDecimal(2);

    private static final Long DEFAULT_REFERENCE_ID = 1L;
    private static final Long UPDATED_REFERENCE_ID = 2L;

    private static final Instant DEFAULT_CREATED_DATE = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_CREATED_DATE = Instant.now().truncatedTo(ChronoUnit.MILLIS);

    private static final String ENTITY_API_URL = "/api/inventory-transactions";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong count = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private InventoryTransactionRepository inventoryTransactionRepository;

    @Mock
    private InventoryTransactionRepository inventoryTransactionRepositoryMock;

    @Autowired
    private InventoryTransactionMapper inventoryTransactionMapper;

    @Mock
    private InventoryTransactionService inventoryTransactionServiceMock;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restInventoryTransactionMockMvc;

    private InventoryTransaction inventoryTransaction;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static InventoryTransaction createEntity(EntityManager em) {
        InventoryTransaction inventoryTransaction = new InventoryTransaction()
            .type(DEFAULT_TYPE)
            .quantity(DEFAULT_QUANTITY)
            .unitCost(DEFAULT_UNIT_COST)
            .referenceId(DEFAULT_REFERENCE_ID)
            .createdDate(DEFAULT_CREATED_DATE);
        return inventoryTransaction;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static InventoryTransaction createUpdatedEntity(EntityManager em) {
        InventoryTransaction inventoryTransaction = new InventoryTransaction()
            .type(UPDATED_TYPE)
            .quantity(UPDATED_QUANTITY)
            .unitCost(UPDATED_UNIT_COST)
            .referenceId(UPDATED_REFERENCE_ID)
            .createdDate(UPDATED_CREATED_DATE);
        return inventoryTransaction;
    }

    @BeforeEach
    public void initTest() {
        inventoryTransaction = createEntity(em);
    }

    @Test
    @Transactional
    void createInventoryTransaction() throws Exception {
        int databaseSizeBeforeCreate = inventoryTransactionRepository.findAll().size();
        // Create the InventoryTransaction
        InventoryTransactionDTO inventoryTransactionDTO = inventoryTransactionMapper.toDto(inventoryTransaction);
        restInventoryTransactionMockMvc
            .perform(
                post(ENTITY_API_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(inventoryTransactionDTO))
            )
            .andExpect(status().isCreated());

        // Validate the InventoryTransaction in the database
        List<InventoryTransaction> inventoryTransactionList = inventoryTransactionRepository.findAll();
        assertThat(inventoryTransactionList).hasSize(databaseSizeBeforeCreate + 1);
        InventoryTransaction testInventoryTransaction = inventoryTransactionList.get(inventoryTransactionList.size() - 1);
        assertThat(testInventoryTransaction.getType()).isEqualTo(DEFAULT_TYPE);
        assertThat(testInventoryTransaction.getQuantity()).isEqualTo(DEFAULT_QUANTITY);
        assertThat(testInventoryTransaction.getUnitCost()).isEqualByComparingTo(DEFAULT_UNIT_COST);
        assertThat(testInventoryTransaction.getReferenceId()).isEqualTo(DEFAULT_REFERENCE_ID);
        assertThat(testInventoryTransaction.getCreatedDate()).isEqualTo(DEFAULT_CREATED_DATE);
    }

    @Test
    @Transactional
    void createInventoryTransactionWithExistingId() throws Exception {
        // Create the InventoryTransaction with an existing ID
        inventoryTransaction.setId(1L);
        InventoryTransactionDTO inventoryTransactionDTO = inventoryTransactionMapper.toDto(inventoryTransaction);

        int databaseSizeBeforeCreate = inventoryTransactionRepository.findAll().size();

        // An entity with an existing ID cannot be created, so this API call must fail
        restInventoryTransactionMockMvc
            .perform(
                post(ENTITY_API_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(inventoryTransactionDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the InventoryTransaction in the database
        List<InventoryTransaction> inventoryTransactionList = inventoryTransactionRepository.findAll();
        assertThat(inventoryTransactionList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkTypeIsRequired() throws Exception {
        int databaseSizeBeforeTest = inventoryTransactionRepository.findAll().size();
        // set the field null
        inventoryTransaction.setType(null);

        // Create the InventoryTransaction, which fails.
        InventoryTransactionDTO inventoryTransactionDTO = inventoryTransactionMapper.toDto(inventoryTransaction);

        restInventoryTransactionMockMvc
            .perform(
                post(ENTITY_API_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(inventoryTransactionDTO))
            )
            .andExpect(status().isBadRequest());

        List<InventoryTransaction> inventoryTransactionList = inventoryTransactionRepository.findAll();
        assertThat(inventoryTransactionList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkQuantityIsRequired() throws Exception {
        int databaseSizeBeforeTest = inventoryTransactionRepository.findAll().size();
        // set the field null
        inventoryTransaction.setQuantity(null);

        // Create the InventoryTransaction, which fails.
        InventoryTransactionDTO inventoryTransactionDTO = inventoryTransactionMapper.toDto(inventoryTransaction);

        restInventoryTransactionMockMvc
            .perform(
                post(ENTITY_API_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(inventoryTransactionDTO))
            )
            .andExpect(status().isBadRequest());

        List<InventoryTransaction> inventoryTransactionList = inventoryTransactionRepository.findAll();
        assertThat(inventoryTransactionList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkCreatedDateIsRequired() throws Exception {
        int databaseSizeBeforeTest = inventoryTransactionRepository.findAll().size();
        // set the field null
        inventoryTransaction.setCreatedDate(null);

        // Create the InventoryTransaction, which fails.
        InventoryTransactionDTO inventoryTransactionDTO = inventoryTransactionMapper.toDto(inventoryTransaction);

        restInventoryTransactionMockMvc
            .perform(
                post(ENTITY_API_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(inventoryTransactionDTO))
            )
            .andExpect(status().isBadRequest());

        List<InventoryTransaction> inventoryTransactionList = inventoryTransactionRepository.findAll();
        assertThat(inventoryTransactionList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllInventoryTransactions() throws Exception {
        // Initialize the database
        inventoryTransactionRepository.saveAndFlush(inventoryTransaction);

        // Get all the inventoryTransactionList
        restInventoryTransactionMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(inventoryTransaction.getId().intValue())))
            .andExpect(jsonPath("$.[*].type").value(hasItem(DEFAULT_TYPE.toString())))
            .andExpect(jsonPath("$.[*].quantity").value(hasItem(DEFAULT_QUANTITY)))
            .andExpect(jsonPath("$.[*].unitCost").value(hasItem(sameNumber(DEFAULT_UNIT_COST))))
            .andExpect(jsonPath("$.[*].referenceId").value(hasItem(DEFAULT_REFERENCE_ID.intValue())))
            .andExpect(jsonPath("$.[*].createdDate").value(hasItem(DEFAULT_CREATED_DATE.toString())));
    }

    @SuppressWarnings({ "unchecked" })
    void getAllInventoryTransactionsWithEagerRelationshipsIsEnabled() throws Exception {
        when(inventoryTransactionServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restInventoryTransactionMockMvc.perform(get(ENTITY_API_URL + "?eagerload=true")).andExpect(status().isOk());

        verify(inventoryTransactionServiceMock, times(1)).findAllWithEagerRelationships(any());
    }

    @SuppressWarnings({ "unchecked" })
    void getAllInventoryTransactionsWithEagerRelationshipsIsNotEnabled() throws Exception {
        when(inventoryTransactionServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restInventoryTransactionMockMvc.perform(get(ENTITY_API_URL + "?eagerload=false")).andExpect(status().isOk());
        verify(inventoryTransactionRepositoryMock, times(1)).findAll(any(Pageable.class));
    }

    @Test
    @Transactional
    void getInventoryTransaction() throws Exception {
        // Initialize the database
        inventoryTransactionRepository.saveAndFlush(inventoryTransaction);

        // Get the inventoryTransaction
        restInventoryTransactionMockMvc
            .perform(get(ENTITY_API_URL_ID, inventoryTransaction.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(inventoryTransaction.getId().intValue()))
            .andExpect(jsonPath("$.type").value(DEFAULT_TYPE.toString()))
            .andExpect(jsonPath("$.quantity").value(DEFAULT_QUANTITY))
            .andExpect(jsonPath("$.unitCost").value(sameNumber(DEFAULT_UNIT_COST)))
            .andExpect(jsonPath("$.referenceId").value(DEFAULT_REFERENCE_ID.intValue()))
            .andExpect(jsonPath("$.createdDate").value(DEFAULT_CREATED_DATE.toString()));
    }

    @Test
    @Transactional
    void getNonExistingInventoryTransaction() throws Exception {
        // Get the inventoryTransaction
        restInventoryTransactionMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingInventoryTransaction() throws Exception {
        // Initialize the database
        inventoryTransactionRepository.saveAndFlush(inventoryTransaction);

        int databaseSizeBeforeUpdate = inventoryTransactionRepository.findAll().size();

        // Update the inventoryTransaction
        InventoryTransaction updatedInventoryTransaction = inventoryTransactionRepository.findById(inventoryTransaction.getId()).get();
        // Disconnect from session so that the updates on updatedInventoryTransaction are not directly saved in db
        em.detach(updatedInventoryTransaction);
        updatedInventoryTransaction
            .type(UPDATED_TYPE)
            .quantity(UPDATED_QUANTITY)
            .unitCost(UPDATED_UNIT_COST)
            .referenceId(UPDATED_REFERENCE_ID)
            .createdDate(UPDATED_CREATED_DATE);
        InventoryTransactionDTO inventoryTransactionDTO = inventoryTransactionMapper.toDto(updatedInventoryTransaction);

        restInventoryTransactionMockMvc
            .perform(
                put(ENTITY_API_URL_ID, inventoryTransactionDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(inventoryTransactionDTO))
            )
            .andExpect(status().isOk());

        // Validate the InventoryTransaction in the database
        List<InventoryTransaction> inventoryTransactionList = inventoryTransactionRepository.findAll();
        assertThat(inventoryTransactionList).hasSize(databaseSizeBeforeUpdate);
        InventoryTransaction testInventoryTransaction = inventoryTransactionList.get(inventoryTransactionList.size() - 1);
        assertThat(testInventoryTransaction.getType()).isEqualTo(UPDATED_TYPE);
        assertThat(testInventoryTransaction.getQuantity()).isEqualTo(UPDATED_QUANTITY);
        assertThat(testInventoryTransaction.getUnitCost()).isEqualByComparingTo(UPDATED_UNIT_COST);
        assertThat(testInventoryTransaction.getReferenceId()).isEqualTo(UPDATED_REFERENCE_ID);
        assertThat(testInventoryTransaction.getCreatedDate()).isEqualTo(UPDATED_CREATED_DATE);
    }

    @Test
    @Transactional
    void putNonExistingInventoryTransaction() throws Exception {
        int databaseSizeBeforeUpdate = inventoryTransactionRepository.findAll().size();
        inventoryTransaction.setId(count.incrementAndGet());

        // Create the InventoryTransaction
        InventoryTransactionDTO inventoryTransactionDTO = inventoryTransactionMapper.toDto(inventoryTransaction);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restInventoryTransactionMockMvc
            .perform(
                put(ENTITY_API_URL_ID, inventoryTransactionDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(inventoryTransactionDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the InventoryTransaction in the database
        List<InventoryTransaction> inventoryTransactionList = inventoryTransactionRepository.findAll();
        assertThat(inventoryTransactionList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchInventoryTransaction() throws Exception {
        int databaseSizeBeforeUpdate = inventoryTransactionRepository.findAll().size();
        inventoryTransaction.setId(count.incrementAndGet());

        // Create the InventoryTransaction
        InventoryTransactionDTO inventoryTransactionDTO = inventoryTransactionMapper.toDto(inventoryTransaction);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restInventoryTransactionMockMvc
            .perform(
                put(ENTITY_API_URL_ID, count.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(inventoryTransactionDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the InventoryTransaction in the database
        List<InventoryTransaction> inventoryTransactionList = inventoryTransactionRepository.findAll();
        assertThat(inventoryTransactionList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamInventoryTransaction() throws Exception {
        int databaseSizeBeforeUpdate = inventoryTransactionRepository.findAll().size();
        inventoryTransaction.setId(count.incrementAndGet());

        // Create the InventoryTransaction
        InventoryTransactionDTO inventoryTransactionDTO = inventoryTransactionMapper.toDto(inventoryTransaction);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restInventoryTransactionMockMvc
            .perform(
                put(ENTITY_API_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(inventoryTransactionDTO))
            )
            .andExpect(status().isMethodNotAllowed());

        // Validate the InventoryTransaction in the database
        List<InventoryTransaction> inventoryTransactionList = inventoryTransactionRepository.findAll();
        assertThat(inventoryTransactionList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateInventoryTransactionWithPatch() throws Exception {
        // Initialize the database
        inventoryTransactionRepository.saveAndFlush(inventoryTransaction);

        int databaseSizeBeforeUpdate = inventoryTransactionRepository.findAll().size();

        // Update the inventoryTransaction using partial update
        InventoryTransaction partialUpdatedInventoryTransaction = new InventoryTransaction();
        partialUpdatedInventoryTransaction.setId(inventoryTransaction.getId());

        partialUpdatedInventoryTransaction.quantity(UPDATED_QUANTITY).unitCost(UPDATED_UNIT_COST).referenceId(UPDATED_REFERENCE_ID);

        restInventoryTransactionMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedInventoryTransaction.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedInventoryTransaction))
            )
            .andExpect(status().isOk());

        // Validate the InventoryTransaction in the database
        List<InventoryTransaction> inventoryTransactionList = inventoryTransactionRepository.findAll();
        assertThat(inventoryTransactionList).hasSize(databaseSizeBeforeUpdate);
        InventoryTransaction testInventoryTransaction = inventoryTransactionList.get(inventoryTransactionList.size() - 1);
        assertThat(testInventoryTransaction.getType()).isEqualTo(DEFAULT_TYPE);
        assertThat(testInventoryTransaction.getQuantity()).isEqualTo(UPDATED_QUANTITY);
        assertThat(testInventoryTransaction.getUnitCost()).isEqualByComparingTo(UPDATED_UNIT_COST);
        assertThat(testInventoryTransaction.getReferenceId()).isEqualTo(UPDATED_REFERENCE_ID);
        assertThat(testInventoryTransaction.getCreatedDate()).isEqualTo(DEFAULT_CREATED_DATE);
    }

    @Test
    @Transactional
    void fullUpdateInventoryTransactionWithPatch() throws Exception {
        // Initialize the database
        inventoryTransactionRepository.saveAndFlush(inventoryTransaction);

        int databaseSizeBeforeUpdate = inventoryTransactionRepository.findAll().size();

        // Update the inventoryTransaction using partial update
        InventoryTransaction partialUpdatedInventoryTransaction = new InventoryTransaction();
        partialUpdatedInventoryTransaction.setId(inventoryTransaction.getId());

        partialUpdatedInventoryTransaction
            .type(UPDATED_TYPE)
            .quantity(UPDATED_QUANTITY)
            .unitCost(UPDATED_UNIT_COST)
            .referenceId(UPDATED_REFERENCE_ID)
            .createdDate(UPDATED_CREATED_DATE);

        restInventoryTransactionMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedInventoryTransaction.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedInventoryTransaction))
            )
            .andExpect(status().isOk());

        // Validate the InventoryTransaction in the database
        List<InventoryTransaction> inventoryTransactionList = inventoryTransactionRepository.findAll();
        assertThat(inventoryTransactionList).hasSize(databaseSizeBeforeUpdate);
        InventoryTransaction testInventoryTransaction = inventoryTransactionList.get(inventoryTransactionList.size() - 1);
        assertThat(testInventoryTransaction.getType()).isEqualTo(UPDATED_TYPE);
        assertThat(testInventoryTransaction.getQuantity()).isEqualTo(UPDATED_QUANTITY);
        assertThat(testInventoryTransaction.getUnitCost()).isEqualByComparingTo(UPDATED_UNIT_COST);
        assertThat(testInventoryTransaction.getReferenceId()).isEqualTo(UPDATED_REFERENCE_ID);
        assertThat(testInventoryTransaction.getCreatedDate()).isEqualTo(UPDATED_CREATED_DATE);
    }

    @Test
    @Transactional
    void patchNonExistingInventoryTransaction() throws Exception {
        int databaseSizeBeforeUpdate = inventoryTransactionRepository.findAll().size();
        inventoryTransaction.setId(count.incrementAndGet());

        // Create the InventoryTransaction
        InventoryTransactionDTO inventoryTransactionDTO = inventoryTransactionMapper.toDto(inventoryTransaction);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restInventoryTransactionMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, inventoryTransactionDTO.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(inventoryTransactionDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the InventoryTransaction in the database
        List<InventoryTransaction> inventoryTransactionList = inventoryTransactionRepository.findAll();
        assertThat(inventoryTransactionList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchInventoryTransaction() throws Exception {
        int databaseSizeBeforeUpdate = inventoryTransactionRepository.findAll().size();
        inventoryTransaction.setId(count.incrementAndGet());

        // Create the InventoryTransaction
        InventoryTransactionDTO inventoryTransactionDTO = inventoryTransactionMapper.toDto(inventoryTransaction);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restInventoryTransactionMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, count.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(inventoryTransactionDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the InventoryTransaction in the database
        List<InventoryTransaction> inventoryTransactionList = inventoryTransactionRepository.findAll();
        assertThat(inventoryTransactionList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamInventoryTransaction() throws Exception {
        int databaseSizeBeforeUpdate = inventoryTransactionRepository.findAll().size();
        inventoryTransaction.setId(count.incrementAndGet());

        // Create the InventoryTransaction
        InventoryTransactionDTO inventoryTransactionDTO = inventoryTransactionMapper.toDto(inventoryTransaction);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restInventoryTransactionMockMvc
            .perform(
                patch(ENTITY_API_URL)
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(inventoryTransactionDTO))
            )
            .andExpect(status().isMethodNotAllowed());

        // Validate the InventoryTransaction in the database
        List<InventoryTransaction> inventoryTransactionList = inventoryTransactionRepository.findAll();
        assertThat(inventoryTransactionList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteInventoryTransaction() throws Exception {
        // Initialize the database
        inventoryTransactionRepository.saveAndFlush(inventoryTransaction);

        int databaseSizeBeforeDelete = inventoryTransactionRepository.findAll().size();

        // Delete the inventoryTransaction
        restInventoryTransactionMockMvc
            .perform(delete(ENTITY_API_URL_ID, inventoryTransaction.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<InventoryTransaction> inventoryTransactionList = inventoryTransactionRepository.findAll();
        assertThat(inventoryTransactionList).hasSize(databaseSizeBeforeDelete - 1);
    }
}
