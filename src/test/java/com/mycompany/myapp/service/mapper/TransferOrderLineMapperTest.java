package com.mycompany.myapp.service.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TransferOrderLineMapperTest {

    private TransferOrderLineMapper transferOrderLineMapper;

    @BeforeEach
    public void setUp() {
        transferOrderLineMapper = new TransferOrderLineMapperImpl();
    }
}
