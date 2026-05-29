<template>
  <div class="row justify-content-center">
    <div class="col-8">
      <div v-if="salesOrder">
        <h2 class="jh-entity-heading" data-cy="salesOrderDetailsHeading">
          <span v-text="$t('novelSyncErpApp.salesOrder.detail.title')">SalesOrder</span> {{ salesOrder.id }}
        </h2>
        <dl class="row jh-entity-details">
          <dt>
            <span v-text="$t('novelSyncErpApp.salesOrder.orderCode')">Order Code</span>
          </dt>
          <dd>
            <span>{{ salesOrder.orderCode }}</span>
          </dd>
          <dt>
            <span v-text="$t('novelSyncErpApp.salesOrder.totalAmount')">Total Amount</span>
          </dt>
          <dd>
            <span>{{ salesOrder.totalAmount }}</span>
          </dd>
          <dt>
            <span v-text="$t('novelSyncErpApp.salesOrder.status')">Status</span>
          </dt>
          <dd>
            <span v-text="$t('novelSyncErpApp.OrderStatus.' + salesOrder.status)">{{ salesOrder.status }}</span>
          </dd>
          <dt>
            <span v-text="$t('novelSyncErpApp.salesOrder.customer')">Customer</span>
          </dt>
          <dd>
            <div v-if="salesOrder.customer">
              <router-link :to="{ name: 'CustomerView', params: { customerId: salesOrder.customer.id } }">{{
                salesOrder.customer.name
              }}</router-link>
            </div>
          </dd>
          <dt>
            <span v-text="$t('novelSyncErpApp.salesOrder.employee')">Employee</span>
          </dt>
          <dd>
            <div v-if="salesOrder.employee">
              <router-link :to="{ name: 'EmployeeView', params: { employeeId: salesOrder.employee.id } }">{{
                salesOrder.employee.fullName
              }}</router-link>
            </div>
          </dd>
          <dt>
            <span v-text="$t('novelSyncErpApp.salesOrder.warehouse')">Warehouse</span>
          </dt>
          <dd>
            <div v-if="salesOrder.warehouse">
              <router-link :to="{ name: 'WarehouseView', params: { warehouseId: salesOrder.warehouse.id } }">{{
                salesOrder.warehouse.name
              }}</router-link>
            </div>
          </dd>
        </dl>
        <button type="submit" v-on:click.prevent="previousState()" class="btn btn-info" data-cy="entityDetailsBackButton">
          <font-awesome-icon icon="arrow-left"></font-awesome-icon>&nbsp;<span v-text="$t('entity.action.back')"> Back</span>
        </button>
        <router-link
          v-if="salesOrder.id"
          :to="{ name: 'SalesOrderEdit', params: { salesOrderId: salesOrder.id } }"
          custom
          v-slot="{ navigate }"
        >
          <button @click="navigate" class="btn btn-primary">
            <font-awesome-icon icon="pencil-alt"></font-awesome-icon>&nbsp;<span v-text="$t('entity.action.edit')"> Edit</span>
          </button>
        </router-link>
      </div>
    </div>
  </div>
</template>

<script lang="ts" src="./sales-order-details.component.ts"></script>
