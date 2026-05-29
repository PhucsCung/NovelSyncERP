package com.mycompany.myapp.service.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SalesOrderMapperTest {

    private SalesOrderMapper salesOrderMapper;

    @BeforeEach
    public void setUp() {
        salesOrderMapper = new SalesOrderMapperImpl();
    }
}
