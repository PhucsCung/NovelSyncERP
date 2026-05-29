<template>
  <div>
    <h2 id="page-heading" data-cy="SalesOrderLineHeading">
      <span v-text="$t('novelSyncErpApp.salesOrderLine.home.title')" id="sales-order-line-heading">Sales Order Lines</span>
      <div class="d-flex justify-content-end">
        <button class="btn btn-info mr-2" v-on:click="handleSyncList" :disabled="isFetching">
          <font-awesome-icon icon="sync" :spin="isFetching"></font-awesome-icon>
          <span v-text="$t('novelSyncErpApp.salesOrderLine.home.refreshListLabel')">Refresh List</span>
        </button>
        <router-link :to="{ name: 'SalesOrderLineCreate' }" custom v-slot="{ navigate }">
          <button
            @click="navigate"
            id="jh-create-entity"
            data-cy="entityCreateButton"
            class="btn btn-primary jh-create-entity create-sales-order-line"
          >
            <font-awesome-icon icon="plus"></font-awesome-icon>
            <span v-text="$t('novelSyncErpApp.salesOrderLine.home.createLabel')"> Create a new Sales Order Line </span>
          </button>
        </router-link>
      </div>
    </h2>
    <br />
    <div class="alert alert-warning" v-if="!isFetching && salesOrderLines && salesOrderLines.length === 0">
      <span v-text="$t('novelSyncErpApp.salesOrderLine.home.notFound')">No salesOrderLines found</span>
    </div>
    <div class="table-responsive" v-if="salesOrderLines && salesOrderLines.length > 0">
      <table class="table table-striped" aria-describedby="salesOrderLines">
        <thead>
          <tr>
            <th scope="row" v-on:click="changeOrder('id')">
              <span v-text="$t('global.field.id')">ID</span>
              <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" :field-name="'id'"></jhi-sort-indicator>
            </th>
            <th scope="row" v-on:click="changeOrder('quantity')">
              <span v-text="$t('novelSyncErpApp.salesOrderLine.quantity')">Quantity</span>
              <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" :field-name="'quantity'"></jhi-sort-indicator>
            </th>
            <th scope="row" v-on:click="changeOrder('unitPrice')">
              <span v-text="$t('novelSyncErpApp.salesOrderLine.unitPrice')">Unit Price</span>
              <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" :field-name="'unitPrice'"></jhi-sort-indicator>
            </th>
            <th scope="row" v-on:click="changeOrder('product.name')">
              <span v-text="$t('novelSyncErpApp.salesOrderLine.product')">Product</span>
              <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" :field-name="'product.name'"></jhi-sort-indicator>
            </th>
            <th scope="row" v-on:click="changeOrder('salesOrder.id')">
              <span v-text="$t('novelSyncErpApp.salesOrderLine.salesOrder')">Sales Order</span>
              <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" :field-name="'salesOrder.id'"></jhi-sort-indicator>
            </th>
            <th scope="row"></th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="salesOrderLine in salesOrderLines" :key="salesOrderLine.id" data-cy="entityTable">
            <td>
              <router-link :to="{ name: 'SalesOrderLineView', params: { salesOrderLineId: salesOrderLine.id } }">{{
                salesOrderLine.id
              }}</router-link>
            </td>
            <td>{{ salesOrderLine.quantity }}</td>
            <td>{{ salesOrderLine.unitPrice }}</td>
            <td>
              <div v-if="salesOrderLine.product">
                <router-link :to="{ name: 'ProductView', params: { productId: salesOrderLine.product.id } }">{{
                  salesOrderLine.product.name
                }}</router-link>
              </div>
            </td>
            <td>
              <div v-if="salesOrderLine.salesOrder">
                <router-link :to="{ name: 'SalesOrderView', params: { salesOrderId: salesOrderLine.salesOrder.id } }">{{
                  salesOrderLine.salesOrder.id
                }}</router-link>
              </div>
            </td>
            <td class="text-right">
              <div class="btn-group">
                <router-link
                  :to="{ name: 'SalesOrderLineView', params: { salesOrderLineId: salesOrderLine.id } }"
                  custom
                  v-slot="{ navigate }"
                >
                  <button @click="navigate" class="btn btn-info btn-sm details" data-cy="entityDetailsButton">
                    <font-awesome-icon icon="eye"></font-awesome-icon>
                    <span class="d-none d-md-inline" v-text="$t('entity.action.view')">View</span>
                  </button>
                </router-link>
                <router-link
                  :to="{ name: 'SalesOrderLineEdit', params: { salesOrderLineId: salesOrderLine.id } }"
                  custom
                  v-slot="{ navigate }"
                >
                  <button @click="navigate" class="btn btn-primary btn-sm edit" data-cy="entityEditButton">
                    <font-awesome-icon icon="pencil-alt"></font-awesome-icon>
                    <span class="d-none d-md-inline" v-text="$t('entity.action.edit')">Edit</span>
                  </button>
                </router-link>
                <b-button
                  v-on:click="prepareRemove(salesOrderLine)"
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
        ><span
          id="novelSyncErpApp.salesOrderLine.delete.question"
          data-cy="salesOrderLineDeleteDialogHeading"
          v-text="$t('entity.delete.title')"
          >Confirm delete operation</span
        ></span
      >
      <div class="modal-body">
        <p id="jhi-delete-salesOrderLine-heading" v-text="$t('novelSyncErpApp.salesOrderLine.delete.question', { id: removeId })">
          Are you sure you want to delete this Sales Order Line?
        </p>
      </div>
      <div slot="modal-footer">
        <button type="button" class="btn btn-secondary" v-text="$t('entity.action.cancel')" v-on:click="closeDialog()">Cancel</button>
        <button
          type="button"
          class="btn btn-primary"
          id="jhi-confirm-delete-salesOrderLine"
          data-cy="entityConfirmDeleteButton"
          v-text="$t('entity.action.delete')"
          v-on:click="removeSalesOrderLine()"
        >
          Delete
        </button>
      </div>
    </b-modal>
    <div v-show="salesOrderLines && salesOrderLines.length > 0">
      <div class="row justify-content-center">
        <jhi-item-count :page="page" :total="queryCount" :itemsPerPage="itemsPerPage"></jhi-item-count>
      </div>
      <div class="row justify-content-center">
        <b-pagination size="md" :total-rows="totalItems" v-model="page" :per-page="itemsPerPage" :change="loadPage(page)"></b-pagination>
      </div>
    </div>
  </div>
</template>

<script lang="ts" src="./sales-order-line.component.ts"></script>
