<template>
  <div class="row justify-content-center">
    <div class="col-8">
      <div v-if="inventoryTransaction">
        <h2 class="jh-entity-heading" data-cy="inventoryTransactionDetailsHeading">
          <span v-text="$t('novelSyncErpApp.inventoryTransaction.detail.title')">InventoryTransaction</span> {{ inventoryTransaction.id }}
        </h2>
        <dl class="row jh-entity-details">
          <dt>
            <span v-text="$t('novelSyncErpApp.inventoryTransaction.type')">Type</span>
          </dt>
          <dd>
            <span v-text="$t('novelSyncErpApp.TransactionType.' + inventoryTransaction.type)">{{ inventoryTransaction.type }}</span>
          </dd>
          <dt>
            <span v-text="$t('novelSyncErpApp.inventoryTransaction.quantity')">Quantity</span>
          </dt>
          <dd>
            <span>{{ inventoryTransaction.quantity }}</span>
          </dd>
          <dt>
            <span v-text="$t('novelSyncErpApp.inventoryTransaction.unitCost')">Unit Cost</span>
          </dt>
          <dd>
            <span>{{ inventoryTransaction.unitCost }}</span>
          </dd>
          <dt>
            <span v-text="$t('novelSyncErpApp.inventoryTransaction.referenceId')">Reference Id</span>
          </dt>
          <dd>
            <span>{{ inventoryTransaction.referenceId }}</span>
          </dd>
          <dt>
            <span v-text="$t('novelSyncErpApp.inventoryTransaction.createdDate')">Created Date</span>
          </dt>
          <dd>
            <span v-if="inventoryTransaction.createdDate">{{ $d(Date.parse(inventoryTransaction.createdDate), 'long') }}</span>
          </dd>
          <dt>
            <span v-text="$t('novelSyncErpApp.inventoryTransaction.product')">Product</span>
          </dt>
          <dd>
            <div v-if="inventoryTransaction.product">
              <router-link :to="{ name: 'ProductView', params: { productId: inventoryTransaction.product.id } }">{{
                inventoryTransaction.product.name
              }}</router-link>
            </div>
          </dd>
          <dt>
            <span v-text="$t('novelSyncErpApp.inventoryTransaction.warehouse')">Warehouse</span>
          </dt>
          <dd>
            <div v-if="inventoryTransaction.warehouse">
              <router-link :to="{ name: 'WarehouseView', params: { warehouseId: inventoryTransaction.warehouse.id } }">{{
                inventoryTransaction.warehouse.name
              }}</router-link>
            </div>
          </dd>
        </dl>
        <button type="submit" v-on:click.prevent="previousState()" class="btn btn-info" data-cy="entityDetailsBackButton">
          <font-awesome-icon icon="arrow-left"></font-awesome-icon>&nbsp;<span v-text="$t('entity.action.back')"> Back</span>
        </button>
        <router-link
          v-if="inventoryTransaction.id"
          :to="{ name: 'InventoryTransactionEdit', params: { inventoryTransactionId: inventoryTransaction.id } }"
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

<script lang="ts" src="./inventory-transaction-details.component.ts"></script>
