<template>
  <div class="row justify-content-center">
    <div class="col-8">
      <div v-if="purchaseOrder">
        <h2 class="jh-entity-heading" data-cy="purchaseOrderDetailsHeading">
          <span v-text="$t('novelSyncErpApp.purchaseOrder.detail.title')">PurchaseOrder</span> {{ purchaseOrder.id }}
        </h2>
        <dl class="row jh-entity-details">
          <dt>
            <span v-text="$t('novelSyncErpApp.purchaseOrder.poCode')">Po Code</span>
          </dt>
          <dd>
            <span>{{ purchaseOrder.poCode }}</span>
          </dd>
          <dt>
            <span v-text="$t('novelSyncErpApp.purchaseOrder.totalAmount')">Total Amount</span>
          </dt>
          <dd>
            <span>{{ purchaseOrder.totalAmount }}</span>
          </dd>
          <dt>
            <span v-text="$t('novelSyncErpApp.purchaseOrder.status')">Status</span>
          </dt>
          <dd>
            <span v-text="$t('novelSyncErpApp.OrderStatus.' + purchaseOrder.status)">{{ purchaseOrder.status }}</span>
          </dd>
          <dt>
            <span v-text="$t('novelSyncErpApp.purchaseOrder.supplier')">Supplier</span>
          </dt>
          <dd>
            <div v-if="purchaseOrder.supplier">
              <router-link :to="{ name: 'SupplierView', params: { supplierId: purchaseOrder.supplier.id } }">{{
                purchaseOrder.supplier.name
              }}</router-link>
            </div>
          </dd>
          <dt>
            <span v-text="$t('novelSyncErpApp.purchaseOrder.employee')">Employee</span>
          </dt>
          <dd>
            <div v-if="purchaseOrder.employee">
              <router-link :to="{ name: 'EmployeeView', params: { employeeId: purchaseOrder.employee.id } }">{{
                purchaseOrder.employee.fullName
              }}</router-link>
            </div>
          </dd>
          <dt>
            <span v-text="$t('novelSyncErpApp.purchaseOrder.warehouse')">Warehouse</span>
          </dt>
          <dd>
            <div v-if="purchaseOrder.warehouse">
              <router-link :to="{ name: 'WarehouseView', params: { warehouseId: purchaseOrder.warehouse.id } }">{{
                purchaseOrder.warehouse.name
              }}</router-link>
            </div>
          </dd>
        </dl>
        <button type="submit" v-on:click.prevent="previousState()" class="btn btn-info" data-cy="entityDetailsBackButton">
          <font-awesome-icon icon="arrow-left"></font-awesome-icon>&nbsp;<span v-text="$t('entity.action.back')"> Back</span>
        </button>
        <router-link
          v-if="purchaseOrder.id"
          :to="{ name: 'PurchaseOrderEdit', params: { purchaseOrderId: purchaseOrder.id } }"
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

<script lang="ts" src="./purchase-order-details.component.ts"></script>
