<template>
  <div class="row justify-content-center">
    <div class="col-8">
      <div v-if="purchaseOrderLine">
        <h2 class="jh-entity-heading" data-cy="purchaseOrderLineDetailsHeading">
          <span v-text="$t('novelSyncErpApp.purchaseOrderLine.detail.title')">PurchaseOrderLine</span> {{ purchaseOrderLine.id }}
        </h2>
        <dl class="row jh-entity-details">
          <dt>
            <span v-text="$t('novelSyncErpApp.purchaseOrderLine.quantity')">Quantity</span>
          </dt>
          <dd>
            <span>{{ purchaseOrderLine.quantity }}</span>
          </dd>
          <dt>
            <span v-text="$t('novelSyncErpApp.purchaseOrderLine.unitPrice')">Unit Price</span>
          </dt>
          <dd>
            <span>{{ purchaseOrderLine.unitPrice }}</span>
          </dd>
          <dt>
            <span v-text="$t('novelSyncErpApp.purchaseOrderLine.product')">Product</span>
          </dt>
          <dd>
            <div v-if="purchaseOrderLine.product">
              <router-link :to="{ name: 'ProductView', params: { productId: purchaseOrderLine.product.id } }">{{
                purchaseOrderLine.product.name
              }}</router-link>
            </div>
          </dd>
          <dt>
            <span v-text="$t('novelSyncErpApp.purchaseOrderLine.purchaseOrder')">Purchase Order</span>
          </dt>
          <dd>
            <div v-if="purchaseOrderLine.purchaseOrder">
              <router-link :to="{ name: 'PurchaseOrderView', params: { purchaseOrderId: purchaseOrderLine.purchaseOrder.id } }">{{
                purchaseOrderLine.purchaseOrder.id
              }}</router-link>
            </div>
          </dd>
        </dl>
        <button type="submit" v-on:click.prevent="previousState()" class="btn btn-info" data-cy="entityDetailsBackButton">
          <font-awesome-icon icon="arrow-left"></font-awesome-icon>&nbsp;<span v-text="$t('entity.action.back')"> Back</span>
        </button>
        <router-link
          v-if="purchaseOrderLine.id"
          :to="{ name: 'PurchaseOrderLineEdit', params: { purchaseOrderLineId: purchaseOrderLine.id } }"
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

<script lang="ts" src="./purchase-order-line-details.component.ts"></script>
