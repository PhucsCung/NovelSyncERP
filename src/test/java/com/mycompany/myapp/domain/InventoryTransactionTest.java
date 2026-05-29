package com.mycompany.myapp.domain;

import static org.assertj.core.api.Assertions.assertThat;

import com.mycompany.myapp.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class InventoryTransactionTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(InventoryTransaction.class);
        InventoryTransaction inventoryTransaction1 = new InventoryTransaction();
        inventoryTransaction1.setId(1L);
        InventoryTransaction inventoryTransaction2 = new InventoryTransaction();
        inventoryTransaction2.setId(inventoryTransaction1.getId());
        assertThat(inventoryTransaction1).isEqualTo(inventoryTransaction2);
        inventoryTransaction2.setId(2L);
        assertThat(inventoryTransaction1).isNotEqualTo(inventoryTransaction2);
        inventoryTransaction1.setId(null);
        assertThat(inventoryTransaction1).isNotEqualTo(inventoryTransaction2);
    }
}
