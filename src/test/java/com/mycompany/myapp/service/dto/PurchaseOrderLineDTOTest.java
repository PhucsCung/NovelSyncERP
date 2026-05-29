package com.mycompany.myapp.service.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.mycompany.myapp.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class PurchaseOrderLineDTOTest {

    @Test
    void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(PurchaseOrderLineDTO.class);
        PurchaseOrderLineDTO purchaseOrderLineDTO1 = new PurchaseOrderLineDTO();
        purchaseOrderLineDTO1.setId(1L);
        PurchaseOrderLineDTO purchaseOrderLineDTO2 = new PurchaseOrderLineDTO();
        assertThat(purchaseOrderLineDTO1).isNotEqualTo(purchaseOrderLineDTO2);
        purchaseOrderLineDTO2.setId(purchaseOrderLineDTO1.getId());
        assertThat(purchaseOrderLineDTO1).isEqualTo(purchaseOrderLineDTO2);
        purchaseOrderLineDTO2.setId(2L);
        assertThat(purchaseOrderLineDTO1).isNotEqualTo(purchaseOrderLineDTO2);
        purchaseOrderLineDTO1.setId(null);
        assertThat(purchaseOrderLineDTO1).isNotEqualTo(purchaseOrderLineDTO2);
    }
}
