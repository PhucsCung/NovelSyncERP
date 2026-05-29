<template>
  <div>
    <h2 id="page-heading" data-cy="PaymentHeading">
      <span v-text="$t('novelSyncErpApp.payment.home.title')" id="payment-heading">Payments</span>
      <div class="d-flex justify-content-end">
        <button class="btn btn-info mr-2" v-on:click="handleSyncList" :disabled="isFetching">
          <font-awesome-icon icon="sync" :spin="isFetching"></font-awesome-icon>
          <span v-text="$t('novelSyncErpApp.payment.home.refreshListLabel')">Refresh List</span>
        </button>
        <router-link :to="{ name: 'PaymentCreate' }" custom v-slot="{ navigate }">
          <button
            @click="navigate"
            id="jh-create-entity"
            data-cy="entityCreateButton"
            class="btn btn-primary jh-create-entity create-payment"
          >
            <font-awesome-icon icon="plus"></font-awesome-icon>
            <span v-text="$t('novelSyncErpApp.payment.home.createLabel')"> Create a new Payment </span>
          </button>
        </router-link>
      </div>
    </h2>
    <br />
    <div class="alert alert-warning" v-if="!isFetching && payments && payments.length === 0">
      <span v-text="$t('novelSyncErpApp.payment.home.notFound')">No payments found</span>
    </div>
    <div class="table-responsive" v-if="payments && payments.length > 0">
      <table class="table table-striped" aria-describedby="payments">
        <thead>
          <tr>
            <th scope="row" v-on:click="changeOrder('id')">
              <span v-text="$t('global.field.id')">ID</span>
              <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" :field-name="'id'"></jhi-sort-indicator>
            </th>
            <th scope="row" v-on:click="changeOrder('paymentCode')">
              <span v-text="$t('novelSyncErpApp.payment.paymentCode')">Payment Code</span>
              <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" :field-name="'paymentCode'"></jhi-sort-indicator>
            </th>
            <th scope="row" v-on:click="changeOrder('type')">
              <span v-text="$t('novelSyncErpApp.payment.type')">Type</span>
              <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" :field-name="'type'"></jhi-sort-indicator>
            </th>
            <th scope="row" v-on:click="changeOrder('amount')">
              <span v-text="$t('novelSyncErpApp.payment.amount')">Amount</span>
              <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" :field-name="'amount'"></jhi-sort-indicator>
            </th>
            <th scope="row" v-on:click="changeOrder('referenceOrderId')">
              <span v-text="$t('novelSyncErpApp.payment.referenceOrderId')">Reference Order Id</span>
              <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" :field-name="'referenceOrderId'"></jhi-sort-indicator>
            </th>
            <th scope="row" v-on:click="changeOrder('createdAt')">
              <span v-text="$t('novelSyncErpApp.payment.createdAt')">Created At</span>
              <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" :field-name="'createdAt'"></jhi-sort-indicator>
            </th>
            <th scope="row" v-on:click="changeOrder('customer.name')">
              <span v-text="$t('novelSyncErpApp.payment.customer')">Customer</span>
              <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" :field-name="'customer.name'"></jhi-sort-indicator>
            </th>
            <th scope="row" v-on:click="changeOrder('supplier.name')">
              <span v-text="$t('novelSyncErpApp.payment.supplier')">Supplier</span>
              <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" :field-name="'supplier.name'"></jhi-sort-indicator>
            </th>
            <th scope="row"></th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="payment in payments" :key="payment.id" data-cy="entityTable">
            <td>
              <router-link :to="{ name: 'PaymentView', params: { paymentId: payment.id } }">{{ payment.id }}</router-link>
            </td>
            <td>{{ payment.paymentCode }}</td>
            <td v-text="$t('novelSyncErpApp.PaymentType.' + payment.type)">{{ payment.type }}</td>
            <td>{{ payment.amount }}</td>
            <td>{{ payment.referenceOrderId }}</td>
            <td>{{ payment.createdAt ? $d(Date.parse(payment.createdAt), 'short') : '' }}</td>
            <td>
              <div v-if="payment.customer">
                <router-link :to="{ name: 'CustomerView', params: { customerId: payment.customer.id } }">{{
                  payment.customer.name
                }}</router-link>
              </div>
            </td>
            <td>
              <div v-if="payment.supplier">
                <router-link :to="{ name: 'SupplierView', params: { supplierId: payment.supplier.id } }">{{
                  payment.supplier.name
                }}</router-link>
              </div>
            </td>
            <td class="text-right">
              <div class="btn-group">
                <router-link :to="{ name: 'PaymentView', params: { paymentId: payment.id } }" custom v-slot="{ navigate }">
                  <button @click="navigate" class="btn btn-info btn-sm details" data-cy="entityDetailsButton">
                    <font-awesome-icon icon="eye"></font-awesome-icon>
                    <span class="d-none d-md-inline" v-text="$t('entity.action.view')">View</span>
                  </button>
                </router-link>
                <router-link :to="{ name: 'PaymentEdit', params: { paymentId: payment.id } }" custom v-slot="{ navigate }">
                  <button @click="navigate" class="btn btn-primary btn-sm edit" data-cy="entityEditButton">
                    <font-awesome-icon icon="pencil-alt"></font-awesome-icon>
                    <span class="d-none d-md-inline" v-text="$t('entity.action.edit')">Edit</span>
                  </button>
                </router-link>
                <b-button
                  v-on:click="prepareRemove(payment)"
                  variant="danger"
                  class="btn btn-sm"
                  data-cy="entityDeleteButton"
                  v-b-modal.removeEntity
                >
                  <font-awesome-icon icon="times"></font-awesome-icon>
                  <span class="d-none d-md-inline" v-text="$t('entity.action.delete')">Delete</span>
                </b-button>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
    <b-modal ref="removeEntity" id="removeEntity">
      <span slot="modal-title"
        ><span id="novelSyncErpApp.payment.delete.question" data-cy="paymentDeleteDialogHeading" v-text="$t('entity.delete.title')"
          >Confirm delete operation</span
        ></span
      >
      <div class="modal-body">
        <p id="jhi-delete-payment-heading" v-text="$t('novelSyncErpApp.payment.delete.question', { id: removeId })">
          Are you sure you want to delete this Payment?
        </p>
      </div>
      <div slot="modal-footer">
        <button type="button" class="btn btn-secondary" v-text="$t('entity.action.cancel')" v-on:click="closeDialog()">Cancel</button>
        <button
          type="button"
          class="btn btn-primary"
          id="jhi-confirm-delete-payment"
          data-cy="entityConfirmDeleteButton"
          v-text="$t('entity.action.delete')"
          v-on:click="removePayment()"
        >
          Delete
        </button>
      </div>
    </b-modal>
    <div v-show="payments && payments.length > 0">
      <div class="row justify-content-center">
        <jhi-item-count :page="page" :total="queryCount" :itemsPerPage="itemsPerPage"></jhi-item-count>
      </div>
      <div class="row justify-content-center">
        <b-pagination size="md" :total-rows="totalItems" v-model="page" :per-page="itemsPerPage" :change="loadPage(page)"></b-pagination>
      </div>
    </div>
  </div>
</template>

<script lang="ts" src="./payment.component.ts"></script>
