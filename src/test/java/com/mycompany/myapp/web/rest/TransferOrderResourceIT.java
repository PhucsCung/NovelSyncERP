package com.mycompany.myapp.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.mycompany.myapp.IntegrationTest;
import com.mycompany.myapp.domain.TransferOrder;
import com.mycompany.myapp.domain.enumeration.OrderStatus;
import com.mycompany.myapp.repository.TransferOrderRepository;
import com.mycompany.myapp.service.TransferOrderService;
import com.mycompany.myapp.service.dto.TransferOrderDTO;
import com.mycompany.myapp.service.mapper.TransferOrderMapper;
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
 * Integration tests for the {@link TransferOrderResource} REST controller.
 */
@IntegrationTest
@ExtendWith(MockitoExtension.class)
@AutoConfigureMockMvc
@WithMockUser
class TransferOrderResourceIT {

    private static final String DEFAULT_TRANSFER_CODE = "AAAAAAAAAA";
    private static final String UPDATED_TRANSFER_CODE = "BBBBBBBBBB";

    private static final OrderStatus DEFAULT_STATUS = OrderStatus.DRAFT;
    private static final OrderStatus UPDATED_STATUS = OrderStatus.APPROVED;

    private static final String ENTITY_API_URL = "/api/transfer-orders";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong count = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private TransferOrderRepository transferOrderRepository;

    @Mock
    private TransferOrderRepository transferOrderRepositoryMock;

    @Autowired
    private TransferOrderMapper transferOrderMapper;

    @Mock
    private TransferOrderService transferOrderServiceMock;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restTransferOrderMockMvc;

    private TransferOrder transferOrder;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static TransferOrder createEntity(EntityManager em) {
        TransferOrder transferOrder = new TransferOrder().transferCode(DEFAULT_TRANSFER_CODE).status(DEFAULT_STATUS);
        return transferOrder;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static TransferOrder createUpdatedEntity(EntityManager em) {
        TransferOrder transferOrder = new TransferOrder().transferCode(UPDATED_TRANSFER_CODE).status(UPDATED_STATUS);
        return transferOrder;
    }

    @BeforeEach
    public void initTest() {
        transferOrder = createEntity(em);
    }

    @Test
    @Transactional
    void createTransferOrder() throws Exception {
        int databaseSizeBeforeCreate = transferOrderRepository.findAll().size();
        // Create the TransferOrder
        TransferOrderDTO transferOrderDTO = transferOrderMapper.toDto(transferOrder);
        restTransferOrderMockMvc
            .perform(
                post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(transferOrderDTO))
            )
            .andExpect(status().isCreated());

        // Validate the TransferOrder in the database
        List<TransferOrder> transferOrderList = transferOrderRepository.findAll();
        assertThat(transferOrderList).hasSize(databaseSizeBeforeCreate + 1);
        TransferOrder testTransferOrder = transferOrderList.get(transferOrderList.size() - 1);
        assertThat(testTransferOrder.getTransferCode()).isEqualTo(DEFAULT_TRANSFER_CODE);
        assertThat(testTransferOrder.getStatus()).isEqualTo(DEFAULT_STATUS);
    }

    @Test
    @Transactional
    void createTransferOrderWithExistingId() throws Exception {
        // Create the TransferOrder with an existing ID
        transferOrder.setId(1L);
        TransferOrderDTO transferOrderDTO = transferOrderMapper.toDto(transferOrder);

        int databaseSizeBeforeCreate = transferOrderRepository.findAll().size();

        // An entity with an existing ID cannot be created, so this API call must fail
        restTransferOrderMockMvc
            .perform(
                post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(transferOrderDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the TransferOrder in the database
        List<TransferOrder> transferOrderList = transferOrderRepository.findAll();
        assertThat(transferOrderList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkTransferCodeIsRequired() throws Exception {
        int databaseSizeBeforeTest = transferOrderRepository.findAll().size();
        // set the field null
        transferOrder.setTransferCode(null);

        // Create the TransferOrder, which fails.
        TransferOrderDTO transferOrderDTO = transferOrderMapper.toDto(transferOrder);

        restTransferOrderMockMvc
            .perform(
                post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(transferOrderDTO))
            )
            .andExpect(status().isBadRequest());

        List<TransferOrder> transferOrderList = transferOrderRepository.findAll();
        assertThat(transferOrderList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkStatusIsRequired() throws Exception {
        int databaseSizeBeforeTest = transferOrderRepository.findAll().size();
        // set the field null
        transferOrder.setStatus(null);

        // Create the TransferOrder, which fails.
        TransferOrderDTO transferOrderDTO = transferOrderMapper.toDto(transferOrder);

        restTransferOrderMockMvc
            .perform(
                post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(transferOrderDTO))
            )
            .andExpect(status().isBadRequest());

        List<TransferOrder> transferOrderList = transferOrderRepository.findAll();
        assertThat(transferOrderList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllTransferOrders() throws Exception {
        // Initialize the database
        transferOrderRepository.saveAndFlush(transferOrder);

        // Get all the transferOrderList
        restTransferOrderMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(transferOrder.getId().intValue())))
            .andExpect(jsonPath("$.[*].transferCode").value(hasItem(DEFAULT_TRANSFER_CODE)))
            .andExpect(jsonPath("$.[*].status").value(hasItem(DEFAULT_STATUS.toString())));
    }

    @SuppressWarnings({ "unchecked" })
    void getAllTransferOrdersWithEagerRelationshipsIsEnabled() throws Exception {
        when(transferOrderServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restTransferOrderMockMvc.perform(get(ENTITY_API_URL + "?eagerload=true")).andExpect(status().isOk());

        verify(transferOrderServiceMock, times(1)).findAllWithEagerRelationships(any());
    }

    @SuppressWarnings({ "unchecked" })
    void getAllTransferOrdersWithEagerRelationshipsIsNotEnabled() throws Exception {
        when(transferOrderServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restTransferOrderMockMvc.perform(get(ENTITY_API_URL + "?eagerload=false")).andExpect(status().isOk());
        verify(transferOrderRepositoryMock, times(1)).findAll(any(Pageable.class));
    }

    @Test
    @Transactional
    void getTransferOrder() throws Exception {
        // Initialize the database
        transferOrderRepository.saveAndFlush(transferOrder);

        // Get the transferOrder
        restTransferOrderMockMvc
            .perform(get(ENTITY_API_URL_ID, transferOrder.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(transferOrder.getId().intValue()))
            .andExpect(jsonPath("$.transferCode").value(DEFAULT_TRANSFER_CODE))
            .andExpect(jsonPath("$.status").value(DEFAULT_STATUS.toString()));
    }

    @Test
    @Transactional
    void getNonExistingTransferOrder() throws Exception {
        // Get the transferOrder
        restTransferOrderMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingTransferOrder() throws Exception {
        // Initialize the database
        transferOrderRepository.saveAndFlush(transferOrder);

        int databaseSizeBeforeUpdate = transferOrderRepository.findAll().size();

        // Update the transferOrder
        TransferOrder updatedTransferOrder = transferOrderRepository.findById(transferOrder.getId()).get();
        // Disconnect from session so that the updates on updatedTransferOrder are not directly saved in db
        em.detach(updatedTransferOrder);
        updatedTransferOrder.transferCode(UPDATED_TRANSFER_CODE).status(UPDATED_STATUS);
        TransferOrderDTO transferOrderDTO = transferOrderMapper.toDto(updatedTransferOrder);

        restTransferOrderMockMvc
            .perform(
                put(ENTITY_API_URL_ID, transferOrderDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(transferOrderDTO))
            )
            .andExpect(status().isOk());

        // Validate the TransferOrder in the database
        List<TransferOrder> transferOrderList = transferOrderRepository.findAll();
        assertThat(transferOrderList).hasSize(databaseSizeBeforeUpdate);
        TransferOrder testTransferOrder = transferOrderList.get(transferOrderList.size() - 1);
        assertThat(testTransferOrder.getTransferCode()).isEqualTo(UPDATED_TRANSFER_CODE);
        assertThat(testTransferOrder.getStatus()).isEqualTo(UPDATED_STATUS);
    }

    @Test
    @Transactional
    void putNonExistingTransferOrder() throws Exception {
        int databaseSizeBeforeUpdate = transferOrderRepository.findAll().size();
        transferOrder.setId(count.incrementAndGet());

        // Create the TransferOrder
        TransferOrderDTO transferOrderDTO = transferOrderMapper.toDto(transferOrder);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restTransferOrderMockMvc
            .perform(
                put(ENTITY_API_URL_ID, transferOrderDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(transferOrderDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the TransferOrder in the database
        List<TransferOrder> transferOrderList = transferOrderRepository.findAll();
        assertThat(transferOrderList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchTransferOrder() throws Exception {
        int databaseSizeBeforeUpdate = transferOrderRepository.findAll().size();
        transferOrder.setId(count.incrementAndGet());

        // Create the TransferOrder
        TransferOrderDTO transferOrderDTO = transferOrderMapper.toDto(transferOrder);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restTransferOrderMockMvc
            .perform(
                put(ENTITY_API_URL_ID, count.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(transferOrderDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the TransferOrder in the database
        List<TransferOrder> transferOrderList = transferOrderRepository.findAll();
        assertThat(transferOrderList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamTransferOrder() throws Exception {
        int databaseSizeBeforeUpdate = transferOrderRepository.findAll().size();
        transferOrder.setId(count.incrementAndGet());

        // Create the TransferOrder
        TransferOrderDTO transferOrderDTO = transferOrderMapper.toDto(transferOrder);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restTransferOrderMockMvc
            .perform(
                put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(transferOrderDTO))
            )
            .andExpect(status().isMethodNotAllowed());

        // Validate the TransferOrder in the database
        List<TransferOrder> transferOrderList = transferOrderRepository.findAll();
        assertThat(transferOrderList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateTransferOrderWithPatch() throws Exception {
        // Initialize the database
        transferOrderRepository.saveAndFlush(transferOrder);

        int databaseSizeBeforeUpdate = transferOrderRepository.findAll().size();

        // Update the transferOrder using partial update
        TransferOrder partialUpdatedTransferOrder = new TransferOrder();
        partialUpdatedTransferOrder.setId(transferOrder.getId());

        partialUpdatedTransferOrder.transferCode(UPDATED_TRANSFER_CODE).status(UPDATED_STATUS);

        restTransferOrderMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedTransferOrder.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedTransferOrder))
            )
            .andExpect(status().isOk());

        // Validate the TransferOrder in the database
        List<TransferOrder> transferOrderList = transferOrderRepository.findAll();
        assertThat(transferOrderList).hasSize(databaseSizeBeforeUpdate);
        TransferOrder testTransferOrder = transferOrderList.get(transferOrderList.size() - 1);
        assertThat(testTransferOrder.getTransferCode()).isEqualTo(UPDATED_TRANSFER_CODE);
        assertThat(testTransferOrder.getStatus()).isEqualTo(UPDATED_STATUS);
    }

    @Test
    @Transactional
    void fullUpdateTransferOrderWithPatch() throws Exception {
        // Initialize the database
        transferOrderRepository.saveAndFlush(transferOrder);

        int databaseSizeBeforeUpdate = transferOrderRepository.findAll().size();

        // Update the transferOrder using partial update
        TransferOrder partialUpdatedTransferOrder = new TransferOrder();
        partialUpdatedTransferOrder.setId(transferOrder.getId());

        partialUpdatedTransferOrder.transferCode(UPDATED_TRANSFER_CODE).status(UPDATED_STATUS);

        restTransferOrderMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedTransferOrder.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedTransferOrder))
            )
            .andExpect(status().isOk());

        // Validate the TransferOrder in the database
        List<TransferOrder> transferOrderList = transferOrderRepository.findAll();
        assertThat(transferOrderList).hasSize(databaseSizeBeforeUpdate);
        TransferOrder testTransferOrder = transferOrderList.get(transferOrderList.size() - 1);
        assertThat(testTransferOrder.getTransferCode()).isEqualTo(UPDATED_TRANSFER_CODE);
        assertThat(testTransferOrder.getStatus()).isEqualTo(UPDATED_STATUS);
    }

    @Test
    @Transactional
    void patchNonExistingTransferOrder() throws Exception {
        int databaseSizeBeforeUpdate = transferOrderRepository.findAll().size();
        transferOrder.setId(count.incrementAndGet());

        // Create the TransferOrder
        TransferOrderDTO transferOrderDTO = transferOrderMapper.toDto(transferOrder);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restTransferOrderMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, transferOrderDTO.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(transferOrderDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the TransferOrder in the database
        List<TransferOrder> transferOrderList = transferOrderRepository.findAll();
        assertThat(transferOrderList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchTransferOrder() throws Exception {
        int databaseSizeBeforeUpdate = transferOrderRepository.findAll().size();
        transferOrder.setId(count.incrementAndGet());

        // Create the TransferOrder
        TransferOrderDTO transferOrderDTO = transferOrderMapper.toDto(transferOrder);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restTransferOrderMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, count.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(transferOrderDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the TransferOrder in the database
        List<TransferOrder> transferOrderList = transferOrderRepository.findAll();
        assertThat(transferOrderList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamTransferOrder() throws Exception {
        int databaseSizeBeforeUpdate = transferOrderRepository.findAll().size();
        transferOrder.setId(count.incrementAndGet());

        // Create the TransferOrder
        TransferOrderDTO transferOrderDTO = transferOrderMapper.toDto(transferOrder);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restTransferOrderMockMvc
            .perform(
                patch(ENTITY_API_URL)
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(transferOrderDTO))
            )
            .andExpect(status().isMethodNotAllowed());

        // Validate the TransferOrder in the database
        List<TransferOrder> transferOrderList = transferOrderRepository.findAll();
        assertThat(transferOrderList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteTransferOrder() throws Exception {
        // Initialize the database
        transferOrderRepository.saveAndFlush(transferOrder);

        int databaseSizeBeforeDelete = transferOrderRepository.findAll().size();

        // Delete the transferOrder
        restTransferOrderMockMvc
            .perform(delete(ENTITY_API_URL_ID, transferOrder.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<TransferOrder> transferOrderList = transferOrderRepository.findAll();
        assertThat(transferOrderList).hasSize(databaseSizeBeforeDelete - 1);
    }
}
