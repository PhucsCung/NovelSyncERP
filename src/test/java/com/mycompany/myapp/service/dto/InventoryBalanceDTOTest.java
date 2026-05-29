package com.mycompany.myapp.service.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.mycompany.myapp.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class InventoryBalanceDTOTest {

    @Test
    void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(InventoryBalanceDTO.class);
        InventoryBalanceDTO inventoryBalanceDTO1 = new InventoryBalanceDTO();
        inventoryBalanceDTO1.setId(1L);
        InventoryBalanceDTO inventoryBalanceDTO2 = new InventoryBalanceDTO();
        assertThat(inventoryBalanceDTO1).isNotEqualTo(inventoryBalanceDTO2);
        inventoryBalanceDTO2.setId(inventoryBalanceDTO1.getId());
        assertThat(inventoryBalanceDTO1).isEqualTo(inventoryBalanceDTO2);
        inventoryBalanceDTO2.setId(2L);
        assertThat(inventoryBalanceDTO1).isNotEqualTo(inventoryBalanceDTO2);
        inventoryBalanceDTO1.setId(null);
        assertThat(inventoryBalanceDTO1).isNotEqualTo(inventoryBalanceDTO2);
    }
}
