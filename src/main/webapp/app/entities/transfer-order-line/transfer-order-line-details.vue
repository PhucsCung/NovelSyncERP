<template>
  <div class="row justify-content-center">
    <div class="col-8">
      <div v-if="transferOrderLine">
        <h2 class="jh-entity-heading" data-cy="transferOrderLineDetailsHeading">
          <span v-text="$t('novelSyncErpApp.transferOrderLine.detail.title')">TransferOrderLine</span> {{ transferOrderLine.id }}
        </h2>
        <dl class="row jh-entity-details">
          <dt>
            <span v-text="$t('novelSyncErpApp.transferOrderLine.quantity')">Quantity</span>
          </dt>
          <dd>
            <span>{{ transferOrderLine.quantity }}</span>
          </dd>
          <dt>
            <span v-text="$t('novelSyncErpApp.transferOrderLine.product')">Product</span>
          </dt>
          <dd>
            <div v-if="transferOrderLine.product">
              <router-link :to="{ name: 'ProductView', params: { productId: transferOrderLine.product.id } }">{{
                transferOrderLine.product.name
              }}</router-link>
            </div>
          </dd>
          <dt>
            <span v-text="$t('novelSyncErpApp.transferOrderLine.transferOrder')">Transfer Order</span>
          </dt>
          <dd>
            <div v-if="transferOrderLine.transferOrder">
              <router-link :to="{ name: 'TransferOrderView', params: { transferOrderId: transferOrderLine.transferOrder.id } }">{{
                transferOrderLine.transferOrder.id
              }}</router-link>
            </div>
          </dd>
        </dl>
        <button type="submit" v-on:click.prevent="previousState()" class="btn btn-info" data-cy="entityDetailsBackButton">
          <font-awesome-icon icon="arrow-left"></font-awesome-icon>&nbsp;<span v-text="$t('entity.action.back')"> Back</span>
        </button>
        <router-link
          v-if="transferOrderLine.id"
          :to="{ name: 'TransferOrderLineEdit', params: { transferOrderLineId: transferOrderLine.id } }"
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

<script lang="ts" src="./transfer-order-line-details.component.ts"></script>
