package com.mycompany.myapp.service.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SalesOrderLineMapperTest {

    private SalesOrderLineMapper salesOrderLineMapper;

    @BeforeEach
    public void setUp() {
        salesOrderLineMapper = new SalesOrderLineMapperImpl();
    }
}
