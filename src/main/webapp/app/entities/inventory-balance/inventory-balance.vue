<template>
  <div>
    <h2 id="page-heading" data-cy="InventoryBalanceHeading">
      <span v-text="$t('novelSyncErpApp.inventoryBalance.home.title')" id="inventory-balance-heading">Inventory Balances</span>
      <div class="d-flex justify-content-end">
        <button class="btn btn-info mr-2" v-on:click="handleSyncList" :disabled="isFetching">
          <font-awesome-icon icon="sync" :spin="isFetching"></font-awesome-icon>
          <span v-text="$t('novelSyncErpApp.inventoryBalance.home.refreshListLabel')">Refresh List</span>
        </button>
        <router-link :to="{ name: 'InventoryBalanceCreate' }" custom v-slot="{ navigate }">
          <button
            @click="navigate"
            id="jh-create-entity"
            data-cy="entityCreateButton"
            class="btn btn-primary jh-create-entity create-inventory-balance"
          >
            <font-awesome-icon icon="plus"></font-awesome-icon>
            <span v-text="$t('novelSyncErpApp.inventoryBalance.home.createLabel')"> Create a new Inventory Balance </span>
          </button>
        </router-link>
      </div>
    </h2>
    <br />
    <div class="alert alert-warning" v-if="!isFetching && inventoryBalances && inventoryBalances.length === 0">
      <span v-text="$t('novelSyncErpApp.inventoryBalance.home.notFound')">No inventoryBalances found</span>
    </div>
    <div class="table-responsive" v-if="inventoryBalances && inventoryBalances.length > 0">
      <table class="table table-striped" aria-describedby="inventoryBalances">
        <thead>
          <tr>
            <th scope="row" v-on:click="changeOrder('id')">
              <span v-text="$t('global.field.id')">ID</span>
              <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" :field-name="'id'"></jhi-sort-indicator>
            </th>
            <th scope="row" v-on:click="changeOrder('quantity')">
              <span v-text="$t('novelSyncErpApp.inventoryBalance.quantity')">Quantity</span>
              <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" :field-name="'quantity'"></jhi-sort-indicator>
            </th>
            <th scope="row" v-on:click="changeOrder('product.name')">
              <span v-text="$t('novelSyncErpApp.inventoryBalance.product')">Product</span>
              <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" :field-name="'product.name'"></jhi-sort-indicator>
            </th>
            <th scope="row" v-on:click="changeOrder('warehouse.name')">
              <span v-text="$t('novelSyncErpApp.inventoryBalance.warehouse')">Warehouse</span>
              <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" :field-name="'warehouse.name'"></jhi-sort-indicator>
            </th>
            <th scope="row"></th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="inventoryBalance in inventoryBalances" :key="inventoryBalance.id" data-cy="entityTable">
            <td>
              <router-link :to="{ name: 'InventoryBalanceView', params: { inventoryBalanceId: inventoryBalance.id } }">{{
                inventoryBalance.id
              }}</router-link>
            </td>
            <td>{{ inventoryBalance.quantity }}</td>
            <td>
              <div v-if="inventoryBalance.product">
                <router-link :to="{ name: 'ProductView', params: { productId: inventoryBalance.product.id } }">{{
                  inventoryBalance.product.name
                }}</router-link>
              </div>
            </td>
            <td>
              <div v-if="inventoryBalance.warehouse">
                <router-link :to="{ name: 'WarehouseView', params: { warehouseId: inventoryBalance.warehouse.id } }">{{
                  inventoryBalance.warehouse.name
                }}</router-link>
              </div>
            </td>
            <td class="text-right">
              <div class="btn-group">
                <router-link
                  :to="{ name: 'InventoryBalanceView', params: { inventoryBalanceId: inventoryBalance.id } }"
                  custom
                  v-slot="{ navigate }"
                >
                  <button @click="navigate" class="btn btn-info btn-sm details" data-cy="entityDetailsButton">
                    <font-awesome-icon icon="eye"></font-awesome-icon>
                    <span class="d-none d-md-inline" v-text="$t('entity.action.view')">View</span>
                  </button>
                </router-link>
                <router-link
                  :to="{ name: 'InventoryBalanceEdit', params: { inventoryBalanceId: inventoryBalance.id } }"
                  custom
                  v-slot="{ navigate }"
                >
                  <button @click="navigate" class="btn btn-primary btn-sm edit" data-cy="entityEditButton">
                    <font-awesome-icon icon="pencil-alt"></font-awesome-icon>
                    <span class="d-none d-md-inline" v-text="$t('entity.action.edit')">Edit</span>
                  </button>
                </router-link>
                <b-button
                  v-on:click="prepareRemove(inventoryBalance)"
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
          id="novelSyncErpApp.inventoryBalance.delete.question"
          data-cy="inventoryBalanceDeleteDialogHeading"
          v-text="$t('entity.delete.title')"
          >Confirm delete operation</span
        ></span
      >
      <div class="modal-body">
        <p id="jhi-delete-inventoryBalance-heading" v-text="$t('novelSyncErpApp.inventoryBalance.delete.question', { id: removeId })">
          Are you sure you want to delete this Inventory Balance?
        </p>
      </div>
      <div slot="modal-footer">
        <button type="button" class="btn btn-secondary" v-text="$t('entity.action.cancel')" v-on:click="closeDialog()">Cancel</button>
        <button
          type="button"
          class="btn btn-primary"
          id="jhi-confirm-delete-inventoryBalance"
          data-cy="entityConfirmDeleteButton"
          v-text="$t('entity.action.delete')"
          v-on:click="removeInventoryBalance()"
        >
          Delete
        </button>
      </div>
    </b-modal>
    <div v-show="inventoryBalances && inventoryBalances.length > 0">
      <div class="row justify-content-center">
        <jhi-item-count :page="page" :total="queryCount" :itemsPerPage="itemsPerPage"></jhi-item-count>
      </div>
      <div class="row justify-content-center">
        <b-pagination size="md" :total-rows="totalItems" v-model="page" :per-page="itemsPerPage" :change="loadPage(page)"></b-pagination>
      </div>
    </div>
  </div>
</template>

<script lang="ts" src="./inventory-balance.component.ts"></script>
