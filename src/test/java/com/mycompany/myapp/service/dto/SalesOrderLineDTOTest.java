package com.mycompany.myapp.service.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.mycompany.myapp.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class SalesOrderLineDTOTest {

    @Test
    void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(SalesOrderLineDTO.class);
        SalesOrderLineDTO salesOrderLineDTO1 = new SalesOrderLineDTO();
        salesOrderLineDTO1.setId(1L);
        SalesOrderLineDTO salesOrderLineDTO2 = new SalesOrderLineDTO();
        assertThat(salesOrderLineDTO1).isNotEqualTo(salesOrderLineDTO2);
        salesOrderLineDTO2.setId(salesOrderLineDTO1.getId());
        assertThat(salesOrderLineDTO1).isEqualTo(salesOrderLineDTO2);
        salesOrderLineDTO2.setId(2L);
        assertThat(salesOrderLineDTO1).isNotEqualTo(salesOrderLineDTO2);
        salesOrderLineDTO1.setId(null);
        assertThat(salesOrderLineDTO1).isNotEqualTo(salesOrderLineDTO2);
    }
}
