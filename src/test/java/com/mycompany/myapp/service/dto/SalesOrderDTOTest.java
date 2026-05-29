package com.mycompany.myapp.service.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.mycompany.myapp.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class SalesOrderDTOTest {

    @Test
    void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(SalesOrderDTO.class);
        SalesOrderDTO salesOrderDTO1 = new SalesOrderDTO();
        salesOrderDTO1.setId(1L);
        SalesOrderDTO salesOrderDTO2 = new SalesOrderDTO();
        assertThat(salesOrderDTO1).isNotEqualTo(salesOrderDTO2);
        salesOrderDTO2.setId(salesOrderDTO1.getId());
        assertThat(salesOrderDTO1).isEqualTo(salesOrderDTO2);
        salesOrderDTO2.setId(2L);
        assertThat(salesOrderDTO1).isNotEqualTo(salesOrderDTO2);
        salesOrderDTO1.setId(null);
        assertThat(salesOrderDTO1).isNotEqualTo(salesOrderDTO2);
    }
}
