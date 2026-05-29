package com.mycompany.myapp.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.mycompany.myapp.IntegrationTest;
import com.mycompany.myapp.domain.TransferOrderLine;
import com.mycompany.myapp.repository.TransferOrderLineRepository;
import com.mycompany.myapp.service.TransferOrderLineService;
import com.mycompany.myapp.service.dto.TransferOrderLineDTO;
import com.mycompany.myapp.service.mapper.TransferOrderLineMapper;
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
 * Integration tests for the {@link TransferOrderLineResource} REST controller.
 */
@IntegrationTest
@ExtendWith(MockitoExtension.class)
@AutoConfigureMockMvc
@WithMockUser
class TransferOrderLineResourceIT {

    private static final Integer DEFAULT_QUANTITY = 1;
    private static final Integer UPDATED_QUANTITY = 2;

    private static final String ENTITY_API_URL = "/api/transfer-order-lines";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong count = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private TransferOrderLineRepository transferOrderLineRepository;

    @Mock
    private TransferOrderLineRepository transferOrderLineRepositoryMock;

    @Autowired
    private TransferOrderLineMapper transferOrderLineMapper;

    @Mock
    private TransferOrderLineService transferOrderLineServiceMock;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restTransferOrderLineMockMvc;

    private TransferOrderLine transferOrderLine;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static TransferOrderLine createEntity(EntityManager em) {
        TransferOrderLine transferOrderLine = new TransferOrderLine().quantity(DEFAULT_QUANTITY);
        return transferOrderLine;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static TransferOrderLine createUpdatedEntity(EntityManager em) {
        TransferOrderLine transferOrderLine = new TransferOrderLine().quantity(UPDATED_QUANTITY);
        return transferOrderLine;
    }

    @BeforeEach
    public void initTest() {
        transferOrderLine = createEntity(em);
    }

    @Test
    @Transactional
    void createTransferOrderLine() throws Exception {
        int databaseSizeBeforeCreate = transferOrderLineRepository.findAll().size();
        // Create the TransferOrderLine
        TransferOrderLineDTO transferOrderLineDTO = transferOrderLineMapper.toDto(transferOrderLine);
        restTransferOrderLineMockMvc
            .perform(
                post(ENTITY_API_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(transferOrderLineDTO))
            )
            .andExpect(status().isCreated());

        // Validate the TransferOrderLine in the database
        List<TransferOrderLine> transferOrderLineList = transferOrderLineRepository.findAll();
        assertThat(transferOrderLineList).hasSize(databaseSizeBeforeCreate + 1);
        TransferOrderLine testTransferOrderLine = transferOrderLineList.get(transferOrderLineList.size() - 1);
        assertThat(testTransferOrderLine.getQuantity()).isEqualTo(DEFAULT_QUANTITY);
    }

    @Test
    @Transactional
    void createTransferOrderLineWithExistingId() throws Exception {
        // Create the TransferOrderLine with an existing ID
        transferOrderLine.setId(1L);
        TransferOrderLineDTO transferOrderLineDTO = transferOrderLineMapper.toDto(transferOrderLine);

        int databaseSizeBeforeCreate = transferOrderLineRepository.findAll().size();

        // An entity with an existing ID cannot be created, so this API call must fail
        restTransferOrderLineMockMvc
            .perform(
                post(ENTITY_API_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(transferOrderLineDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the TransferOrderLine in the database
        List<TransferOrderLine> transferOrderLineList = transferOrderLineRepository.findAll();
        assertThat(transferOrderLineList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkQuantityIsRequired() throws Exception {
        int databaseSizeBeforeTest = transferOrderLineRepository.findAll().size();
        // set the field null
        transferOrderLine.setQuantity(null);

        // Create the TransferOrderLine, which fails.
        TransferOrderLineDTO transferOrderLineDTO = transferOrderLineMapper.toDto(transferOrderLine);

        restTransferOrderLineMockMvc
            .perform(
                post(ENTITY_API_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(transferOrderLineDTO))
            )
            .andExpect(status().isBadRequest());

        List<TransferOrderLine> transferOrderLineList = transferOrderLineRepository.findAll();
        assertThat(transferOrderLineList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllTransferOrderLines() throws Exception {
        // Initialize the database
        transferOrderLineRepository.saveAndFlush(transferOrderLine);

        // Get all the transferOrderLineList
        restTransferOrderLineMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(transferOrderLine.getId().intValue())))
            .andExpect(jsonPath("$.[*].quantity").value(hasItem(DEFAULT_QUANTITY)));
    }

    @SuppressWarnings({ "unchecked" })
    void getAllTransferOrderLinesWithEagerRelationshipsIsEnabled() throws Exception {
        when(transferOrderLineServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restTransferOrderLineMockMvc.perform(get(ENTITY_API_URL + "?eagerload=true")).andExpect(status().isOk());

        verify(transferOrderLineServiceMock, times(1)).findAllWithEagerRelationships(any());
    }

    @SuppressWarnings({ "unchecked" })
    void getAllTransferOrderLinesWithEagerRelationshipsIsNotEnabled() throws Exception {
        when(transferOrderLineServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restTransferOrderLineMockMvc.perform(get(ENTITY_API_URL + "?eagerload=false")).andExpect(status().isOk());
        verify(transferOrderLineRepositoryMock, times(1)).findAll(any(Pageable.class));
    }

    @Test
    @Transactional
    void getTransferOrderLine() throws Exception {
        // Initialize the database
        transferOrderLineRepository.saveAndFlush(transferOrderLine);

        // Get the transferOrderLine
        restTransferOrderLineMockMvc
            .perform(get(ENTITY_API_URL_ID, transferOrderLine.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(transferOrderLine.getId().intValue()))
            .andExpect(jsonPath("$.quantity").value(DEFAULT_QUANTITY));
    }

    @Test
    @Transactional
    void getNonExistingTransferOrderLine() throws Exception {
        // Get the transferOrderLine
        restTransferOrderLineMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingTransferOrderLine() throws Exception {
        // Initialize the database
        transferOrderLineRepository.saveAndFlush(transferOrderLine);

        int databaseSizeBeforeUpdate = transferOrderLineRepository.findAll().size();

        // Update the transferOrderLine
        TransferOrderLine updatedTransferOrderLine = transferOrderLineRepository.findById(transferOrderLine.getId()).get();
        // Disconnect from session so that the updates on updatedTransferOrderLine are not directly saved in db
        em.detach(updatedTransferOrderLine);
        updatedTransferOrderLine.quantity(UPDATED_QUANTITY);
        TransferOrderLineDTO transferOrderLineDTO = transferOrderLineMapper.toDto(updatedTransferOrderLine);

        restTransferOrderLineMockMvc
            .perform(
                put(ENTITY_API_URL_ID, transferOrderLineDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(transferOrderLineDTO))
            )
            .andExpect(status().isOk());

        // Validate the TransferOrderLine in the database
        List<TransferOrderLine> transferOrderLineList = transferOrderLineRepository.findAll();
        assertThat(transferOrderLineList).hasSize(databaseSizeBeforeUpdate);
        TransferOrderLine testTransferOrderLine = transferOrderLineList.get(transferOrderLineList.size() - 1);
        assertThat(testTransferOrderLine.getQuantity()).isEqualTo(UPDATED_QUANTITY);
    }

    @Test
    @Transactional
    void putNonExistingTransferOrderLine() throws Exception {
        int databaseSizeBeforeUpdate = transferOrderLineRepository.findAll().size();
        transferOrderLine.setId(count.incrementAndGet());

        // Create the TransferOrderLine
        TransferOrderLineDTO transferOrderLineDTO = transferOrderLineMapper.toDto(transferOrderLine);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restTransferOrderLineMockMvc
            .perform(
                put(ENTITY_API_URL_ID, transferOrderLineDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(transferOrderLineDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the TransferOrderLine in the database
        List<TransferOrderLine> transferOrderLineList = transferOrderLineRepository.findAll();
        assertThat(transferOrderLineList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchTransferOrderLine() throws Exception {
        int databaseSizeBeforeUpdate = transferOrderLineRepository.findAll().size();
        transferOrderLine.setId(count.incrementAndGet());

        // Create the TransferOrderLine
        TransferOrderLineDTO transferOrderLineDTO = transferOrderLineMapper.toDto(transferOrderLine);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restTransferOrderLineMockMvc
            .perform(
                put(ENTITY_API_URL_ID, count.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(transferOrderLineDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the TransferOrderLine in the database
        List<TransferOrderLine> transferOrderLineList = transferOrderLineRepository.findAll();
        assertThat(transferOrderLineList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamTransferOrderLine() throws Exception {
        int databaseSizeBeforeUpdate = transferOrderLineRepository.findAll().size();
        transferOrderLine.setId(count.incrementAndGet());

        // Create the TransferOrderLine
        TransferOrderLineDTO transferOrderLineDTO = transferOrderLineMapper.toDto(transferOrderLine);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restTransferOrderLineMockMvc
            .perform(
                put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(transferOrderLineDTO))
            )
            .andExpect(status().isMethodNotAllowed());

        // Validate the TransferOrderLine in the database
        List<TransferOrderLine> transferOrderLineList = transferOrderLineRepository.findAll();
        assertThat(transferOrderLineList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateTransferOrderLineWithPatch() throws Exception {
        // Initialize the database
        transferOrderLineRepository.saveAndFlush(transferOrderLine);

        int databaseSizeBeforeUpdate = transferOrderLineRepository.findAll().size();

        // Update the transferOrderLine using partial update
        TransferOrderLine partialUpdatedTransferOrderLine = new TransferOrderLine();
        partialUpdatedTransferOrderLine.setId(transferOrderLine.getId());

        restTransferOrderLineMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedTransferOrderLine.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedTransferOrderLine))
            )
            .andExpect(status().isOk());

        // Validate the TransferOrderLine in the database
        List<TransferOrderLine> transferOrderLineList = transferOrderLineRepository.findAll();
        assertThat(transferOrderLineList).hasSize(databaseSizeBeforeUpdate);
        TransferOrderLine testTransferOrderLine = transferOrderLineList.get(transferOrderLineList.size() - 1);
        assertThat(testTransferOrderLine.getQuantity()).isEqualTo(DEFAULT_QUANTITY);
    }

    @Test
    @Transactional
    void fullUpdateTransferOrderLineWithPatch() throws Exception {
        // Initialize the database
        transferOrderLineRepository.saveAndFlush(transferOrderLine);

        int databaseSizeBeforeUpdate = transferOrderLineRepository.findAll().size();

        // Update the transferOrderLine using partial update
        TransferOrderLine partialUpdatedTransferOrderLine = new TransferOrderLine();
        partialUpdatedTransferOrderLine.setId(transferOrderLine.getId());

        partialUpdatedTransferOrderLine.quantity(UPDATED_QUANTITY);

        restTransferOrderLineMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedTransferOrderLine.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedTransferOrderLine))
            )
            .andExpect(status().isOk());

        // Validate the TransferOrderLine in the database
        List<TransferOrderLine> transferOrderLineList = transferOrderLineRepository.findAll();
        assertThat(transferOrderLineList).hasSize(databaseSizeBeforeUpdate);
        TransferOrderLine testTransferOrderLine = transferOrderLineList.get(transferOrderLineList.size() - 1);
        assertThat(testTransferOrderLine.getQuantity()).isEqualTo(UPDATED_QUANTITY);
    }

    @Test
    @Transactional
    void patchNonExistingTransferOrderLine() throws Exception {
        int databaseSizeBeforeUpdate = transferOrderLineRepository.findAll().size();
        transferOrderLine.setId(count.incrementAndGet());

        // Create the TransferOrderLine
        TransferOrderLineDTO transferOrderLineDTO = transferOrderLineMapper.toDto(transferOrderLine);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restTransferOrderLineMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, transferOrderLineDTO.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(transferOrderLineDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the TransferOrderLine in the database
        List<TransferOrderLine> transferOrderLineList = transferOrderLineRepository.findAll();
        assertThat(transferOrderLineList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchTransferOrderLine() throws Exception {
        int databaseSizeBeforeUpdate = transferOrderLineRepository.findAll().size();
        transferOrderLine.setId(count.incrementAndGet());

        // Create the TransferOrderLine
        TransferOrderLineDTO transferOrderLineDTO = transferOrderLineMapper.toDto(transferOrderLine);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restTransferOrderLineMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, count.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(transferOrderLineDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the TransferOrderLine in the database
        List<TransferOrderLine> transferOrderLineList = transferOrderLineRepository.findAll();
        assertThat(transferOrderLineList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamTransferOrderLine() throws Exception {
        int databaseSizeBeforeUpdate = transferOrderLineRepository.findAll().size();
        transferOrderLine.setId(count.incrementAndGet());

        // Create the TransferOrderLine
        TransferOrderLineDTO transferOrderLineDTO = transferOrderLineMapper.toDto(transferOrderLine);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restTransferOrderLineMockMvc
            .perform(
                patch(ENTITY_API_URL)
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(transferOrderLineDTO))
            )
            .andExpect(status().isMethodNotAllowed());

        // Validate the TransferOrderLine in the database
        List<TransferOrderLine> transferOrderLineList = transferOrderLineRepository.findAll();
        assertThat(transferOrderLineList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteTransferOrderLine() throws Exception {
        // Initialize the database
        transferOrderLineRepository.saveAndFlush(transferOrderLine);

        int databaseSizeBeforeDelete = transferOrderLineRepository.findAll().size();

        // Delete the transferOrderLine
        restTransferOrderLineMockMvc
            .perform(delete(ENTITY_API_URL_ID, transferOrderLine.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<TransferOrderLine> transferOrderLineList = transferOrderLineRepository.findAll();
        assertThat(transferOrderLineList).hasSize(databaseSizeBeforeDelete - 1);
    }
}
