package com.mycompany.myapp.service.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.mycompany.myapp.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class TransferOrderLineDTOTest {

    @Test
    void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(TransferOrderLineDTO.class);
        TransferOrderLineDTO transferOrderLineDTO1 = new TransferOrderLineDTO();
        transferOrderLineDTO1.setId(1L);
        TransferOrderLineDTO transferOrderLineDTO2 = new TransferOrderLineDTO();
        assertThat(transferOrderLineDTO1).isNotEqualTo(transferOrderLineDTO2);
        transferOrderLineDTO2.setId(transferOrderLineDTO1.getId());
        assertThat(transferOrderLineDTO1).isEqualTo(transferOrderLineDTO2);
        transferOrderLineDTO2.setId(2L);
        assertThat(transferOrderLineDTO1).isNotEqualTo(transferOrderLineDTO2);
        transferOrderLineDTO1.setId(null);
        assertThat(transferOrderLineDTO1).isNotEqualTo(transferOrderLineDTO2);
    }
}
