package com.mycompany.myapp.domain;

import static org.assertj.core.api.Assertions.assertThat;

import com.mycompany.myapp.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class SalesOrderLineTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(SalesOrderLine.class);
        SalesOrderLine salesOrderLine1 = new SalesOrderLine();
        salesOrderLine1.setId(1L);
        SalesOrderLine salesOrderLine2 = new SalesOrderLine();
        salesOrderLine2.setId(salesOrderLine1.getId());
        assertThat(salesOrderLine1).isEqualTo(salesOrderLine2);
        salesOrderLine2.setId(2L);
        assertThat(salesOrderLine1).isNotEqualTo(salesOrderLine2);
        salesOrderLine1.setId(null);
        assertThat(salesOrderLine1).isNotEqualTo(salesOrderLine2);
    }
}
