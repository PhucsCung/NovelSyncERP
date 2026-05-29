<template>
  <div class="row justify-content-center">
    <div class="col-8">
      <div v-if="transferOrder">
        <h2 class="jh-entity-heading" data-cy="transferOrderDetailsHeading">
          <span v-text="$t('novelSyncErpApp.transferOrder.detail.title')">TransferOrder</span> {{ transferOrder.id }}
        </h2>
        <dl class="row jh-entity-details">
          <dt>
            <span v-text="$t('novelSyncErpApp.transferOrder.transferCode')">Transfer Code</span>
          </dt>
          <dd>
            <span>{{ transferOrder.transferCode }}</span>
          </dd>
          <dt>
            <span v-text="$t('novelSyncErpApp.transferOrder.status')">Status</span>
          </dt>
          <dd>
            <span v-text="$t('novelSyncErpApp.OrderStatus.' + transferOrder.status)">{{ transferOrder.status }}</span>
          </dd>
          <dt>
            <span v-text="$t('novelSyncErpApp.transferOrder.fromWarehouse')">From Warehouse</span>
          </dt>
          <dd>
            <div v-if="transferOrder.fromWarehouse">
              <router-link :to="{ name: 'WarehouseView', params: { warehouseId: transferOrder.fromWarehouse.id } }">{{
                transferOrder.fromWarehouse.name
              }}</router-link>
            </div>
          </dd>
          <dt>
            <span v-text="$t('novelSyncErpApp.transferOrder.toWarehouse')">To Warehouse</span>
          </dt>
          <dd>
            <div v-if="transferOrder.toWarehouse">
              <router-link :to="{ name: 'WarehouseView', params: { warehouseId: transferOrder.toWarehouse.id } }">{{
                transferOrder.toWarehouse.name
              }}</router-link>
            </div>
          </dd>
        </dl>
        <button type="submit" v-on:click.prevent="previousState()" class="btn btn-info" data-cy="entityDetailsBackButton">
          <font-awesome-icon icon="arrow-left"></font-awesome-icon>&nbsp;<span v-text="$t('entity.action.back')"> Back</span>
        </button>
        <router-link
          v-if="transferOrder.id"
          :to="{ name: 'TransferOrderEdit', params: { transferOrderId: transferOrder.id } }"
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

<script lang="ts" src="./transfer-order-details.component.ts"></script>
