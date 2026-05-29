<template>
  <div class="row justify-content-center">
    <div class="col-8">
      <div v-if="inventoryBalance">
        <h2 class="jh-entity-heading" data-cy="inventoryBalanceDetailsHeading">
          <span v-text="$t('novelSyncErpApp.inventoryBalance.detail.title')">InventoryBalance</span> {{ inventoryBalance.id }}
        </h2>
        <dl class="row jh-entity-details">
          <dt>
            <span v-text="$t('novelSyncErpApp.inventoryBalance.quantity')">Quantity</span>
          </dt>
          <dd>
            <span>{{ inventoryBalance.quantity }}</span>
          </dd>
          <dt>
            <span v-text="$t('novelSyncErpApp.inventoryBalance.product')">Product</span>
          </dt>
          <dd>
            <div v-if="inventoryBalance.product">
              <router-link :to="{ name: 'ProductView', params: { productId: inventoryBalance.product.id } }">{{
                inventoryBalance.product.name
              }}</router-link>
            </div>
          </dd>
          <dt>
            <span v-text="$t('novelSyncErpApp.inventoryBalance.warehouse')">Warehouse</span>
          </dt>
          <dd>
            <div v-if="inventoryBalance.warehouse">
              <router-link :to="{ name: 'WarehouseView', params: { warehouseId: inventoryBalance.warehouse.id } }">{{
                inventoryBalance.warehouse.name
              }}</router-link>
            </div>
          </dd>
        </dl>
        <button type="submit" v-on:click.prevent="previousState()" class="btn btn-info" data-cy="entityDetailsBackButton">
          <font-awesome-icon icon="arrow-left"></font-awesome-icon>&nbsp;<span v-text="$t('entity.action.back')"> Back</span>
        </button>
        <router-link
          v-if="inventoryBalance.id"
          :to="{ name: 'InventoryBalanceEdit', params: { inventoryBalanceId: inventoryBalance.id } }"
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

<script lang="ts" src="./inventory-balance-details.component.ts"></script>
