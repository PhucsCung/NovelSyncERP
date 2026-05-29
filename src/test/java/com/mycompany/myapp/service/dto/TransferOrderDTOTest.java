package com.mycompany.myapp.service.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.mycompany.myapp.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class TransferOrderDTOTest {

    @Test
    void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(TransferOrderDTO.class);
        TransferOrderDTO transferOrderDTO1 = new TransferOrderDTO();
        transferOrderDTO1.setId(1L);
        TransferOrderDTO transferOrderDTO2 = new TransferOrderDTO();
        assertThat(transferOrderDTO1).isNotEqualTo(transferOrderDTO2);
        transferOrderDTO2.setId(transferOrderDTO1.getId());
        assertThat(transferOrderDTO1).isEqualTo(transferOrderDTO2);
        transferOrderDTO2.setId(2L);
        assertThat(transferOrderDTO1).isNotEqualTo(transferOrderDTO2);
        transferOrderDTO1.setId(null);
        assertThat(transferOrderDTO1).isNotEqualTo(transferOrderDTO2);
    }
}
