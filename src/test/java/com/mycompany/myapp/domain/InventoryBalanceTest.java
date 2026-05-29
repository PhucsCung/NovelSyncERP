package com.mycompany.myapp.domain;

import static org.assertj.core.api.Assertions.assertThat;

import com.mycompany.myapp.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class InventoryBalanceTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(InventoryBalance.class);
        InventoryBalance inventoryBalance1 = new InventoryBalance();
        inventoryBalance1.setId(1L);
        InventoryBalance inventoryBalance2 = new InventoryBalance();
        inventoryBalance2.setId(inventoryBalance1.getId());
        assertThat(inventoryBalance1).isEqualTo(inventoryBalance2);
        inventoryBalance2.setId(2L);
        assertThat(inventoryBalance1).isNotEqualTo(inventoryBalance2);
        inventoryBalance1.setId(null);
        assertThat(inventoryBalance1).isNotEqualTo(inventoryBalance2);
    }
}
