package com.mycompany.myapp.domain;

import static org.assertj.core.api.Assertions.assertThat;

import com.mycompany.myapp.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class TransferOrderLineTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(TransferOrderLine.class);
        TransferOrderLine transferOrderLine1 = new TransferOrderLine();
        transferOrderLine1.setId(1L);
        TransferOrderLine transferOrderLine2 = new TransferOrderLine();
        transferOrderLine2.setId(transferOrderLine1.getId());
        assertThat(transferOrderLine1).isEqualTo(transferOrderLine2);
        transferOrderLine2.setId(2L);
        assertThat(transferOrderLine1).isNotEqualTo(transferOrderLine2);
        transferOrderLine1.setId(null);
        assertThat(transferOrderLine1).isNotEqualTo(transferOrderLine2);
    }
}
