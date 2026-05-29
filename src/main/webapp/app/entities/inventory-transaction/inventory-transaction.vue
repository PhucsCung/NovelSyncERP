<template>
  <div>
    <h2 id="page-heading" data-cy="InventoryTransactionHeading">
      <span v-text="$t('novelSyncErpApp.inventoryTransaction.home.title')" id="inventory-transaction-heading">Inventory Transactions</span>
      <div class="d-flex justify-content-end">
        <button class="btn btn-info mr-2" v-on:click="handleSyncList" :disabled="isFetching">
          <font-awesome-icon icon="sync" :spin="isFetching"></font-awesome-icon>
          <span v-text="$t('novelSyncErpApp.inventoryTransaction.home.refreshListLabel')">Refresh List</span>
        </button>
        <router-link :to="{ name: 'InventoryTransactionCreate' }" custom v-slot="{ navigate }">
          <button
            @click="navigate"
            id="jh-create-entity"
            data-cy="entityCreateButton"
            class="btn btn-primary jh-create-entity create-inventory-transaction"
          >
            <font-awesome-icon icon="plus"></font-awesome-icon>
            <span v-text="$t('novelSyncErpApp.inventoryTransaction.home.createLabel')"> Create a new Inventory Transaction </span>
          </button>
        </router-link>
      </div>
    </h2>
    <br />
    <div class="alert alert-warning" v-if="!isFetching && inventoryTransactions && inventoryTransactions.length === 0">
      <span v-text="$t('novelSyncErpApp.inventoryTransaction.home.notFound')">No inventoryTransactions found</span>
    </div>
    <div class="table-responsive" v-if="inventoryTransactions && inventoryTransactions.length > 0">
      <table class="table table-striped" aria-describedby="inventoryTransactions">
        <thead>
          <tr>
            <th scope="row" v-on:click="changeOrder('id')">
              <span v-text="$t('global.field.id')">ID</span>
              <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" :field-name="'id'"></jhi-sort-indicator>
            </th>
            <th scope="row" v-on:click="changeOrder('type')">
              <span v-text="$t('novelSyncErpApp.inventoryTransaction.type')">Type</span>
              <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" :field-name="'type'"></jhi-sort-indicator>
            </th>
            <th scope="row" v-on:click="changeOrder('quantity')">
              <span v-text="$t('novelSyncErpApp.inventoryTransaction.quantity')">Quantity</span>
              <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" :field-name="'quantity'"></jhi-sort-indicator>
            </th>
            <th scope="row" v-on:click="changeOrder('unitCost')">
              <span v-text="$t('novelSyncErpApp.inventoryTransaction.unitCost')">Unit Cost</span>
              <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" :field-name="'unitCost'"></jhi-sort-indicator>
            </th>
            <th scope="row" v-on:click="changeOrder('referenceId')">
              <span v-text="$t('novelSyncErpApp.inventoryTransaction.referenceId')">Reference Id</span>
              <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" :field-name="'referenceId'"></jhi-sort-indicator>
            </th>
            <th scope="row" v-on:click="changeOrder('createdDate')">
              <span v-text="$t('novelSyncErpApp.inventoryTransaction.createdDate')">Created Date</span>
              <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" :field-name="'createdDate'"></jhi-sort-indicator>
            </th>
            <th scope="row" v-on:click="changeOrder('product.name')">
              <span v-text="$t('novelSyncErpApp.inventoryTransaction.product')">Product</span>
              <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" :field-name="'product.name'"></jhi-sort-indicator>
            </th>
            <th scope="row" v-on:click="changeOrder('warehouse.name')">
              <span v-text="$t('novelSyncErpApp.inventoryTransaction.warehouse')">Warehouse</span>
              <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" :field-name="'warehouse.name'"></jhi-sort-indicator>
            </th>
            <th scope="row"></th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="inventoryTransaction in inventoryTransactions" :key="inventoryTransaction.id" data-cy="entityTable">
            <td>
              <router-link :to="{ name: 'InventoryTransactionView', params: { inventoryTransactionId: inventoryTransaction.id } }">{{
                inventoryTransaction.id
              }}</router-link>
            </td>
            <td v-text="$t('novelSyncErpApp.TransactionType.' + inventoryTransaction.type)">{{ inventoryTransaction.type }}</td>
            <td>{{ inventoryTransaction.quantity }}</td>
            <td>{{ inventoryTransaction.unitCost }}</td>
            <td>{{ inventoryTransaction.referenceId }}</td>
            <td>{{ inventoryTransaction.createdDate ? $d(Date.parse(inventoryTransaction.createdDate), 'short') : '' }}</td>
            <td>
              <div v-if="inventoryTransaction.product">
                <router-link :to="{ name: 'ProductView', params: { productId: inventoryTransaction.product.id } }">{{
                  inventoryTransaction.product.name
                }}</router-link>
              </div>
            </td>
            <td>
              <div v-if="inventoryTransaction.warehouse">
                <router-link :to="{ name: 'WarehouseView', params: { warehouseId: inventoryTransaction.warehouse.id } }">{{
                  inventoryTransaction.warehouse.name
                }}</router-link>
              </div>
            </td>
            <td class="text-right">
              <div class="btn-group">
                <router-link
                  :to="{ name: 'InventoryTransactionView', params: { inventoryTransactionId: inventoryTransaction.id } }"
                  custom
                  v-slot="{ navigate }"
                >
                  <button @click="navigate" class="btn btn-info btn-sm details" data-cy="entityDetailsButton">
                    <font-awesome-icon icon="eye"></font-awesome-icon>
                    <span class="d-none d-md-inline" v-text="$t('entity.action.view')">View</span>
                  </button>
                </router-link>
                <router-link
                  :to="{ name: 'InventoryTransactionEdit', params: { inventoryTransactionId: inventoryTransaction.id } }"
                  custom
                  v-slot="{ navigate }"
                >
                  <button @click="navigate" class="btn btn-primary btn-sm edit" data-cy="entityEditButton">
                    <font-awesome-icon icon="pencil-alt"></font-awesome-icon>
                    <span class="d-none d-md-inline" v-text="$t('entity.action.edit')">Edit</span>
                  </button>
                </router-link>
                <b-button
                  v-on:click="prepareRemove(inventoryTransaction)"
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
          id="novelSyncErpApp.inventoryTransaction.delete.question"
          data-cy="inventoryTransactionDeleteDialogHeading"
          v-text="$t('entity.delete.title')"
          >Confirm delete operation</span
        ></span
      >
      <div class="modal-body">
        <p
          id="jhi-delete-inventoryTransaction-heading"
          v-text="$t('novelSyncErpApp.inventoryTransaction.delete.question', { id: removeId })"
        >
          Are you sure you want to delete this Inventory Transaction?
        </p>
      </div>
      <div slot="modal-footer">
        <button type="button" class="btn btn-secondary" v-text="$t('entity.action.cancel')" v-on:click="closeDialog()">Cancel</button>
        <button
          type="button"
          class="btn btn-primary"
          id="jhi-confirm-delete-inventoryTransaction"
          data-cy="entityConfirmDeleteButton"
          v-text="$t('entity.action.delete')"
          v-on:click="removeInventoryTransaction()"
        >
          Delete
        </button>
      </div>
    </b-modal>
    <div v-show="inventoryTransactions && inventoryTransactions.length > 0">
      <div class="row justify-content-center">
        <jhi-item-count :page="page" :total="queryCount" :itemsPerPage="itemsPerPage"></jhi-item-count>
      </div>
      <div class="row justify-content-center">
        <b-pagination size="md" :total-rows="totalItems" v-model="page" :per-page="itemsPerPage" :change="loadPage(page)"></b-pagination>
      </div>
    </div>
  </div>
</template>

<script lang="ts" src="./inventory-transaction.component.ts"></script>
