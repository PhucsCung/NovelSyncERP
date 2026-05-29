<template>
  <div class="row justify-content-center">
    <div class="col-8">
      <div v-if="salesOrderLine">
        <h2 class="jh-entity-heading" data-cy="salesOrderLineDetailsHeading">
          <span v-text="$t('novelSyncErpApp.salesOrderLine.detail.title')">SalesOrderLine</span> {{ salesOrderLine.id }}
        </h2>
        <dl class="row jh-entity-details">
          <dt>
            <span v-text="$t('novelSyncErpApp.salesOrderLine.quantity')">Quantity</span>
          </dt>
          <dd>
            <span>{{ salesOrderLine.quantity }}</span>
          </dd>
          <dt>
            <span v-text="$t('novelSyncErpApp.salesOrderLine.unitPrice')">Unit Price</span>
          </dt>
          <dd>
            <span>{{ salesOrderLine.unitPrice }}</span>
          </dd>
          <dt>
            <span v-text="$t('novelSyncErpApp.salesOrderLine.product')">Product</span>
          </dt>
          <dd>
            <div v-if="salesOrderLine.product">
              <router-link :to="{ name: 'ProductView', params: { productId: salesOrderLine.product.id } }">{{
                salesOrderLine.product.name
              }}</router-link>
            </div>
          </dd>
          <dt>
            <span v-text="$t('novelSyncErpApp.salesOrderLine.salesOrder')">Sales Order</span>
          </dt>
          <dd>
            <div v-if="salesOrderLine.salesOrder">
              <router-link :to="{ name: 'SalesOrderView', params: { salesOrderId: salesOrderLine.salesOrder.id } }">{{
                salesOrderLine.salesOrder.id
              }}</router-link>
            </div>
          </dd>
        </dl>
        <button type="submit" v-on:click.prevent="previousState()" class="btn btn-info" data-cy="entityDetailsBackButton">
          <font-awesome-icon icon="arrow-left"></font-awesome-icon>&nbsp;<span v-text="$t('entity.action.back')"> Back</span>
        </button>
        <router-link
          v-if="salesOrderLine.id"
          :to="{ name: 'SalesOrderLineEdit', params: { salesOrderLineId: salesOrderLine.id } }"
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

<script lang="ts" src="./sales-order-line-details.component.ts"></script>
