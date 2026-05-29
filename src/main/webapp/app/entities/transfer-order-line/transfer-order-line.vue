<template>
  <div>
    <h2 id="page-heading" data-cy="TransferOrderLineHeading">
      <span v-text="$t('novelSyncErpApp.transferOrderLine.home.title')" id="transfer-order-line-heading">Transfer Order Lines</span>
      <div class="d-flex justify-content-end">
        <button class="btn btn-info mr-2" v-on:click="handleSyncList" :disabled="isFetching">
          <font-awesome-icon icon="sync" :spin="isFetching"></font-awesome-icon>
          <span v-text="$t('novelSyncErpApp.transferOrderLine.home.refreshListLabel')">Refresh List</span>
        </button>
        <router-link :to="{ name: 'TransferOrderLineCreate' }" custom v-slot="{ navigate }">
          <button
            @click="navigate"
            id="jh-create-entity"
            data-cy="entityCreateButton"
            class="btn btn-primary jh-create-entity create-transfer-order-line"
          >
            <font-awesome-icon icon="plus"></font-awesome-icon>
            <span v-text="$t('novelSyncErpApp.transferOrderLine.home.createLabel')"> Create a new Transfer Order Line </span>
          </button>
        </router-link>
      </div>
    </h2>
    <br />
    <div class="alert alert-warning" v-if="!isFetching && transferOrderLines && transferOrderLines.length === 0">
      <span v-text="$t('novelSyncErpApp.transferOrderLine.home.notFound')">No transferOrderLines found</span>
    </div>
    <div class="table-responsive" v-if="transferOrderLines && transferOrderLines.length > 0">
      <table class="table table-striped" aria-describedby="transferOrderLines">
        <thead>
          <tr>
            <th scope="row" v-on:click="changeOrder('id')">
              <span v-text="$t('global.field.id')">ID</span>
              <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" :field-name="'id'"></jhi-sort-indicator>
            </th>
            <th scope="row" v-on:click="changeOrder('quantity')">
              <span v-text="$t('novelSyncErpApp.transferOrderLine.quantity')">Quantity</span>
              <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" :field-name="'quantity'"></jhi-sort-indicator>
            </th>
            <th scope="row" v-on:click="changeOrder('product.name')">
              <span v-text="$t('novelSyncErpApp.transferOrderLine.product')">Product</span>
              <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" :field-name="'product.name'"></jhi-sort-indicator>
            </th>
            <th scope="row" v-on:click="changeOrder('transferOrder.id')">
              <span v-text="$t('novelSyncErpApp.transferOrderLine.transferOrder')">Transfer Order</span>
              <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" :field-name="'transferOrder.id'"></jhi-sort-indicator>
            </th>
            <th scope="row"></th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="transferOrderLine in transferOrderLines" :key="transferOrderLine.id" data-cy="entityTable">
            <td>
              <router-link :to="{ name: 'TransferOrderLineView', params: { transferOrderLineId: transferOrderLine.id } }">{{
                transferOrderLine.id
              }}</router-link>
            </td>
            <td>{{ transferOrderLine.quantity }}</td>
            <td>
              <div v-if="transferOrderLine.product">
                <router-link :to="{ name: 'ProductView', params: { productId: transferOrderLine.product.id } }">{{
                  transferOrderLine.product.name
                }}</router-link>
              </div>
            </td>
            <td>
              <div v-if="transferOrderLine.transferOrder">
                <router-link :to="{ name: 'TransferOrderView', params: { transferOrderId: transferOrderLine.transferOrder.id } }">{{
                  transferOrderLine.transferOrder.id
                }}</router-link>
              </div>
            </td>
            <td class="text-right">
              <div class="btn-group">
                <router-link
                  :to="{ name: 'TransferOrderLineView', params: { transferOrderLineId: transferOrderLine.id } }"
                  custom
                  v-slot="{ navigate }"
                >
                  <button @click="navigate" class="btn btn-info btn-sm details" data-cy="entityDetailsButton">
                    <font-awesome-icon icon="eye"></font-awesome-icon>
                    <span class="d-none d-md-inline" v-text="$t('entity.action.view')">View</span>
                  </button>
                </router-link>
                <router-link
                  :to="{ name: 'TransferOrderLineEdit', params: { transferOrderLineId: transferOrderLine.id } }"
                  custom
                  v-slot="{ navigate }"
                >
                  <button @click="navigate" class="btn btn-primary btn-sm edit" data-cy="entityEditButton">
                    <font-awesome-icon icon="pencil-alt"></font-awesome-icon>
                    <span class="d-none d-md-inline" v-text="$t('entity.action.edit')">Edit</span>
                  </button>
                </router-link>
                <b-button
                  v-on:click="prepareRemove(transferOrderLine)"
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
          id="novelSyncErpApp.transferOrderLine.delete.question"
          data-cy="transferOrderLineDeleteDialogHeading"
          v-text="$t('entity.delete.title')"
          >Confirm delete operation</span
        ></span
      >
      <div class="modal-body">
        <p id="jhi-delete-transferOrderLine-heading" v-text="$t('novelSyncErpApp.transferOrderLine.delete.question', { id: removeId })">
          Are you sure you want to delete this Transfer Order Line?
        </p>
      </div>
      <div slot="modal-footer">
        <button type="button" class="btn btn-secondary" v-text="$t('entity.action.cancel')" v-on:click="closeDialog()">Cancel</button>
        <button
          type="button"
          class="btn btn-primary"
          id="jhi-confirm-delete-transferOrderLine"
          data-cy="entityConfirmDeleteButton"
          v-text="$t('entity.action.delete')"
          v-on:click="removeTransferOrderLine()"
        >
          Delete
        </button>
      </div>
    </b-modal>
    <div v-show="transferOrderLines && transferOrderLines.length > 0">
      <div class="row justify-content-center">
        <jhi-item-count :page="page" :total="queryCount" :itemsPerPage="itemsPerPage"></jhi-item-count>
      </div>
      <div class="row justify-content-center">
        <b-pagination size="md" :total-rows="totalItems" v-model="page" :per-page="itemsPerPage" :change="loadPage(page)"></b-pagination>
      </div>
    </div>
  </div>
</template>

<script lang="ts" src="./transfer-order-line.component.ts"></script>
