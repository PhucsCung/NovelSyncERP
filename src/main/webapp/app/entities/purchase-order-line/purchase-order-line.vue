<template>
  <div>
    <h2 id="page-heading" data-cy="PurchaseOrderLineHeading">
      <span v-text="$t('novelSyncErpApp.purchaseOrderLine.home.title')" id="purchase-order-line-heading">Purchase Order Lines</span>
      <div class="d-flex justify-content-end">
        <button class="btn btn-info mr-2" v-on:click="handleSyncList" :disabled="isFetching">
          <font-awesome-icon icon="sync" :spin="isFetching"></font-awesome-icon>
          <span v-text="$t('novelSyncErpApp.purchaseOrderLine.home.refreshListLabel')">Refresh List</span>
        </button>
        <router-link :to="{ name: 'PurchaseOrderLineCreate' }" custom v-slot="{ navigate }">
          <button
            @click="navigate"
            id="jh-create-entity"
            data-cy="entityCreateButton"
            class="btn btn-primary jh-create-entity create-purchase-order-line"
          >
            <font-awesome-icon icon="plus"></font-awesome-icon>
            <span v-text="$t('novelSyncErpApp.purchaseOrderLine.home.createLabel')"> Create a new Purchase Order Line </span>
          </button>
        </router-link>
      </div>
    </h2>
    <br />
    <div class="alert alert-warning" v-if="!isFetching && purchaseOrderLines && purchaseOrderLines.length === 0">
      <span v-text="$t('novelSyncErpApp.purchaseOrderLine.home.notFound')">No purchaseOrderLines found</span>
    </div>
    <div class="table-responsive" v-if="purchaseOrderLines && purchaseOrderLines.length > 0">
      <table class="table table-striped" aria-describedby="purchaseOrderLines">
        <thead>
          <tr>
            <th scope="row" v-on:click="changeOrder('id')">
              <span v-text="$t('global.field.id')">ID</span>
              <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" :field-name="'id'"></jhi-sort-indicator>
            </th>
            <th scope="row" v-on:click="changeOrder('quantity')">
              <span v-text="$t('novelSyncErpApp.purchaseOrderLine.quantity')">Quantity</span>
              <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" :field-name="'quantity'"></jhi-sort-indicator>
            </th>
            <th scope="row" v-on:click="changeOrder('unitPrice')">
              <span v-text="$t('novelSyncErpApp.purchaseOrderLine.unitPrice')">Unit Price</span>
              <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" :field-name="'unitPrice'"></jhi-sort-indicator>
            </th>
            <th scope="row" v-on:click="changeOrder('product.name')">
              <span v-text="$t('novelSyncErpApp.purchaseOrderLine.product')">Product</span>
              <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" :field-name="'product.name'"></jhi-sort-indicator>
            </th>
            <th scope="row" v-on:click="changeOrder('purchaseOrder.id')">
              <span v-text="$t('novelSyncErpApp.purchaseOrderLine.purchaseOrder')">Purchase Order</span>
              <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" :field-name="'purchaseOrder.id'"></jhi-sort-indicator>
            </th>
            <th scope="row"></th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="purchaseOrderLine in purchaseOrderLines" :key="purchaseOrderLine.id" data-cy="entityTable">
            <td>
              <router-link :to="{ name: 'PurchaseOrderLineView', params: { purchaseOrderLineId: purchaseOrderLine.id } }">{{
                purchaseOrderLine.id
              }}</router-link>
            </td>
            <td>{{ purchaseOrderLine.quantity }}</td>
            <td>{{ purchaseOrderLine.unitPrice }}</td>
            <td>
              <div v-if="purchaseOrderLine.product">
                <router-link :to="{ name: 'ProductView', params: { productId: purchaseOrderLine.product.id } }">{{
                  purchaseOrderLine.product.name
                }}</router-link>
              </div>
            </td>
            <td>
              <div v-if="purchaseOrderLine.purchaseOrder">
                <router-link :to="{ name: 'PurchaseOrderView', params: { purchaseOrderId: purchaseOrderLine.purchaseOrder.id } }">{{
                  purchaseOrderLine.purchaseOrder.id
                }}</router-link>
              </div>
            </td>
            <td class="text-right">
              <div class="btn-group">
                <router-link
                  :to="{ name: 'PurchaseOrderLineView', params: { purchaseOrderLineId: purchaseOrderLine.id } }"
                  custom
                  v-slot="{ navigate }"
                >
                  <button @click="navigate" class="btn btn-info btn-sm details" data-cy="entityDetailsButton">
                    <font-awesome-icon icon="eye"></font-awesome-icon>
                    <span class="d-none d-md-inline" v-text="$t('entity.action.view')">View</span>
                  </button>
                </router-link>
                <router-link
                  :to="{ name: 'PurchaseOrderLineEdit', params: { purchaseOrderLineId: purchaseOrderLine.id } }"
                  custom
                  v-slot="{ navigate }"
                >
                  <button @click="navigate" class="btn btn-primary btn-sm edit" data-cy="entityEditButton">
                    <font-awesome-icon icon="pencil-alt"></font-awesome-icon>
                    <span class="d-none d-md-inline" v-text="$t('entity.action.edit')">Edit</span>
                  </button>
                </router-link>
                <b-button
                  v-on:click="prepareRemove(purchaseOrderLine)"
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
          id="novelSyncErpApp.purchaseOrderLine.delete.question"
          data-cy="purchaseOrderLineDeleteDialogHeading"
          v-text="$t('entity.delete.title')"
          >Confirm delete operation</span
        ></span
      >
      <div class="modal-body">
        <p id="jhi-delete-purchaseOrderLine-heading" v-text="$t('novelSyncErpApp.purchaseOrderLine.delete.question', { id: removeId })">
          Are you sure you want to delete this Purchase Order Line?
        </p>
      </div>
      <div slot="modal-footer">
        <button type="button" class="btn btn-secondary" v-text="$t('entity.action.cancel')" v-on:click="closeDialog()">Cancel</button>
        <button
          type="button"
          class="btn btn-primary"
          id="jhi-confirm-delete-purchaseOrderLine"
          data-cy="entityConfirmDeleteButton"
          v-text="$t('entity.action.delete')"
          v-on:click="removePurchaseOrderLine()"
        >
          Delete
        </button>
      </div>
    </b-modal>
    <div v-show="purchaseOrderLines && purchaseOrderLines.length > 0">
      <div class="row justify-content-center">
        <jhi-item-count :page="page" :total="queryCount" :itemsPerPage="itemsPerPage"></jhi-item-count>
      </div>
      <div class="row justify-content-center">
        <b-pagination size="md" :total-rows="totalItems" v-model="page" :per-page="itemsPerPage" :change="loadPage(page)"></b-pagination>
      </div>
    </div>
  </div>
</template>

<script lang="ts" src="./purchase-order-line.component.ts"></script>
