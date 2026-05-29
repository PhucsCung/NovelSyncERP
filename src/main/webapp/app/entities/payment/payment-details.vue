<template>
  <div class="row justify-content-center">
    <div class="col-8">
      <div v-if="payment">
        <h2 class="jh-entity-heading" data-cy="paymentDetailsHeading">
          <span v-text="$t('novelSyncErpApp.payment.detail.title')">Payment</span> {{ payment.id }}
        </h2>
        <dl class="row jh-entity-details">
          <dt>
            <span v-text="$t('novelSyncErpApp.payment.paymentCode')">Payment Code</span>
          </dt>
          <dd>
            <span>{{ payment.paymentCode }}</span>
          </dd>
          <dt>
            <span v-text="$t('novelSyncErpApp.payment.type')">Type</span>
          </dt>
          <dd>
            <span v-text="$t('novelSyncErpApp.PaymentType.' + payment.type)">{{ payment.type }}</span>
          </dd>
          <dt>
            <span v-text="$t('novelSyncErpApp.payment.amount')">Amount</span>
          </dt>
          <dd>
            <span>{{ payment.amount }}</span>
          </dd>
          <dt>
            <span v-text="$t('novelSyncErpApp.payment.referenceOrderId')">Reference Order Id</span>
          </dt>
          <dd>
            <span>{{ payment.referenceOrderId }}</span>
          </dd>
          <dt>
            <span v-text="$t('novelSyncErpApp.payment.createdAt')">Created At</span>
          </dt>
          <dd>
            <span v-if="payment.createdAt">{{ $d(Date.parse(payment.createdAt), 'long') }}</span>
          </dd>
          <dt>
            <span v-text="$t('novelSyncErpApp.payment.customer')">Customer</span>
          </dt>
          <dd>
            <div v-if="payment.customer">
              <router-link :to="{ name: 'CustomerView', params: { customerId: payment.customer.id } }">{{
                payment.customer.name
              }}</router-link>
            </div>
          </dd>
          <dt>
            <span v-text="$t('novelSyncErpApp.payment.supplier')">Supplier</span>
          </dt>
          <dd>
            <div v-if="payment.supplier">
              <router-link :to="{ name: 'SupplierView', params: { supplierId: payment.supplier.id } }">{{
                payment.supplier.name
              }}</router-link>
            </div>
          </dd>
        </dl>
        <button type="submit" v-on:click.prevent="previousState()" class="btn btn-info" data-cy="entityDetailsBackButton">
          <font-awesome-icon icon="arrow-left"></font-awesome-icon>&nbsp;<span v-text="$t('entity.action.back')"> Back</span>
        </button>
        <router-link v-if="payment.id" :to="{ name: 'PaymentEdit', params: { paymentId: payment.id } }" custom v-slot="{ navigate }">
          <button @click="navigate" class="btn btn-primary">
            <font-awesome-icon icon="pencil-alt"></font-awesome-icon>&nbsp;<span v-text="$t('entity.action.edit')"> Edit</span>
          </button>
        </router-link>
      </div>
    </div>
  </div>
</template>

<script lang="ts" src="./payment-details.component.ts"></script>
