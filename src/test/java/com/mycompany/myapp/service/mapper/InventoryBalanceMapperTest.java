package com.mycompany.myapp.service.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class InventoryBalanceMapperTest {

    private InventoryBalanceMapper inventoryBalanceMapper;

    @BeforeEach
    public void setUp() {
        inventoryBalanceMapper = new InventoryBalanceMapperImpl();
    }
}
