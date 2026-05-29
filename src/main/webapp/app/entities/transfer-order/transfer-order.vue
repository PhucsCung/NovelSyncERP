<template>
  <div>
    <h2 id="page-heading" data-cy="TransferOrderHeading">
      <span v-text="$t('novelSyncErpApp.transferOrder.home.title')" id="transfer-order-heading">Transfer Orders</span>
      <div class="d-flex justify-content-end">
        <button class="btn btn-info mr-2" v-on:click="handleSyncList" :disabled="isFetching">
          <font-awesome-icon icon="sync" :spin="isFetching"></font-awesome-icon>
          <span v-text="$t('novelSyncErpApp.transferOrder.home.refreshListLabel')">Refresh List</span>
        </button>
        <router-link :to="{ name: 'TransferOrderCreate' }" custom v-slot="{ navigate }">
          <button
            @click="navigate"
            id="jh-create-entity"
            data-cy="entityCreateButton"
            class="btn btn-primary jh-create-entity create-transfer-order"
          >
            <font-awesome-icon icon="plus"></font-awesome-icon>
            <span v-text="$t('novelSyncErpApp.transferOrder.home.createLabel')"> Create a new Transfer Order </span>
          </button>
        </router-link>
      </div>
    </h2>
    <br />
    <div class="alert alert-warning" v-if="!isFetching && transferOrders && transferOrders.length === 0">
      <span v-text="$t('novelSyncErpApp.transferOrder.home.notFound')">No transferOrders found</span>
    </div>
    <div class="table-responsive" v-if="transferOrders && transferOrders.length > 0">
      <table class="table table-striped" aria-describedby="transferOrders">
        <thead>
          <tr>
            <th scope="row" v-on:click="changeOrder('id')">
              <span v-text="$t('global.field.id')">ID</span>
              <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" :field-name="'id'"></jhi-sort-indicator>
            </th>
            <th scope="row" v-on:click="changeOrder('transferCode')">
              <span v-text="$t('novelSyncErpApp.transferOrder.transferCode')">Transfer Code</span>
              <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" :field-name="'transferCode'"></jhi-sort-indicator>
            </th>
            <th scope="row" v-on:click="changeOrder('status')">
              <span v-text="$t('novelSyncErpApp.transferOrder.status')">Status</span>
              <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" :field-name="'status'"></jhi-sort-indicator>
            </th>
            <th scope="row" v-on:click="changeOrder('fromWarehouse.name')">
              <span v-text="$t('novelSyncErpApp.transferOrder.fromWarehouse')">From Warehouse</span>
              <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" :field-name="'fromWarehouse.name'"></jhi-sort-indicator>
            </th>
            <th scope="row" v-on:click="changeOrder('toWarehouse.name')">
              <span v-text="$t('novelSyncErpApp.transferOrder.toWarehouse')">To Warehouse</span>
              <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" :field-name="'toWarehouse.name'"></jhi-sort-indicator>
            </th>
            <th scope="row"></th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="transferOrder in transferOrders" :key="transferOrder.id" data-cy="entityTable">
            <td>
              <router-link :to="{ name: 'TransferOrderView', params: { transferOrderId: transferOrder.id } }">{{
                transferOrder.id
              }}</router-link>
            </td>
            <td>{{ transferOrder.transferCode }}</td>
            <td v-text="$t('novelSyncErpApp.OrderStatus.' + transferOrder.status)">{{ transferOrder.status }}</td>
            <td>
              <div v-if="transferOrder.fromWarehouse">
                <router-link :to="{ name: 'WarehouseView', params: { warehouseId: transferOrder.fromWarehouse.id } }">{{
                  transferOrder.fromWarehouse.name
                }}</router-link>
              </div>
            </td>
            <td>
              <div v-if="transferOrder.toWarehouse">
                <router-link :to="{ name: 'WarehouseView', params: { warehouseId: transferOrder.toWarehouse.id } }">{{
                  transferOrder.toWarehouse.name
                }}</router-link>
              </div>
            </td>
            <td class="text-right">
              <div class="btn-group">
                <router-link
                  :to="{ name: 'TransferOrderView', params: { transferOrderId: transferOrder.id } }"
                  custom
                  v-slot="{ navigate }"
                >
                  <button @click="navigate" class="btn btn-info btn-sm details" data-cy="entityDetailsButton">
                    <font-awesome-icon icon="eye"></font-awesome-icon>
                    <span class="d-none d-md-inline" v-text="$t('entity.action.view')">View</span>
                  </button>
                </router-link>
                <router-link
                  :to="{ name: 'TransferOrderEdit', params: { transferOrderId: transferOrder.id } }"
                  custom
                  v-slot="{ navigate }"
                >
                  <button @click="navigate" class="btn btn-primary btn-sm edit" data-cy="entityEditButton">
                    <font-awesome-icon icon="pencil-alt"></font-awesome-icon>
                    <span class="d-none d-md-inline" v-text="$t('entity.action.edit')">Edit</span>
                  </button>
                </router-link>
                <b-button
                  v-on:click="prepareRemove(transferOrder)"
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
          id="novelSyncErpApp.transferOrder.delete.question"
          data-cy="transferOrderDeleteDialogHeading"
          v-text="$t('entity.delete.title')"
          >Confirm delete operation</span
        ></span
      >
      <div class="modal-body">
        <p id="jhi-delete-transferOrder-heading" v-text="$t('novelSyncErpApp.transferOrder.delete.question', { id: removeId })">
          Are you sure you want to delete this Transfer Order?
        </p>
      </div>
      <div slot="modal-footer">
        <button type="button" class="btn btn-secondary" v-text="$t('entity.action.cancel')" v-on:click="closeDialog()">Cancel</button>
        <button
          type="button"
          class="btn btn-primary"
          id="jhi-confirm-delete-transferOrder"
          data-cy="entityConfirmDeleteButton"
          v-text="$t('entity.action.delete')"
          v-on:click="removeTransferOrder()"
        >
          Delete
        </button>
      </div>
    </b-modal>
    <div v-show="transferOrders && transferOrders.length > 0">
      <div class="row justify-content-center">
        <jhi-item-count :page="page" :total="queryCount" :itemsPerPage="itemsPerPage"></jhi-item-count>
      </div>
      <div class="row justify-content-center">
        <b-pagination size="md" :total-rows="totalItems" v-model="page" :per-page="itemsPerPage" :change="loadPage(page)"></b-pagination>
      </div>
    </div>
  </div>
</template>

<script lang="ts" src="./transfer-order.component.ts"></script>
