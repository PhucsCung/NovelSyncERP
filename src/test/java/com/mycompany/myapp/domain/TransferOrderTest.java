package com.mycompany.myapp.domain;

import static org.assertj.core.api.Assertions.assertThat;

import com.mycompany.myapp.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class TransferOrderTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(TransferOrder.class);
        TransferOrder transferOrder1 = new TransferOrder();
        transferOrder1.setId(1L);
        TransferOrder transferOrder2 = new TransferOrder();
        transferOrder2.setId(transferOrder1.getId());
        assertThat(transferOrder1).isEqualTo(transferOrder2);
        transferOrder2.setId(2L);
        assertThat(transferOrder1).isNotEqualTo(transferOrder2);
        transferOrder1.setId(null);
        assertThat(transferOrder1).isNotEqualTo(transferOrder2);
    }
}
