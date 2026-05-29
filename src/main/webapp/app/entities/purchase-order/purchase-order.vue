<template>
  <div>
    <h2 id="page-heading" data-cy="PurchaseOrderHeading">
      <span v-text="$t('novelSyncErpApp.purchaseOrder.home.title')" id="purchase-order-heading">Purchase Orders</span>
      <div class="d-flex justify-content-end">
        <button class="btn btn-info mr-2" v-on:click="handleSyncList" :disabled="isFetching">
          <font-awesome-icon icon="sync" :spin="isFetching"></font-awesome-icon>
          <span v-text="$t('novelSyncErpApp.purchaseOrder.home.refreshListLabel')">Refresh List</span>
        </button>
        <router-link :to="{ name: 'PurchaseOrderCreate' }" custom v-slot="{ navigate }">
          <button
            @click="navigate"
            id="jh-create-entity"
            data-cy="entityCreateButton"
            class="btn btn-primary jh-create-entity create-purchase-order"
          >
            <font-awesome-icon icon="plus"></font-awesome-icon>
            <span v-text="$t('novelSyncErpApp.purchaseOrder.home.createLabel')"> Create a new Purchase Order </span>
          </button>
        </router-link>
      </div>
    </h2>
    <br />
    <div class="alert alert-warning" v-if="!isFetching && purchaseOrders && purchaseOrders.length === 0">
      <span v-text="$t('novelSyncErpApp.purchaseOrder.home.notFound')">No purchaseOrders found</span>
    </div>
    <div class="table-responsive" v-if="purchaseOrders && purchaseOrders.length > 0">
      <table class="table table-striped" aria-describedby="purchaseOrders">
        <thead>
          <tr>
            <th scope="row" v-on:click="changeOrder('id')">
              <span v-text="$t('global.field.id')">ID</span>
              <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" :field-name="'id'"></jhi-sort-indicator>
            </th>
            <th scope="row" v-on:click="changeOrder('poCode')">
              <span v-text="$t('novelSyncErpApp.purchaseOrder.poCode')">Po Code</span>
              <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" :field-name="'poCode'"></jhi-sort-indicator>
            </th>
            <th scope="row" v-on:click="changeOrder('totalAmount')">
              <span v-text="$t('novelSyncErpApp.purchaseOrder.totalAmount')">Total Amount</span>
              <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" :field-name="'totalAmount'"></jhi-sort-indicator>
            </th>
            <th scope="row" v-on:click="changeOrder('status')">
              <span v-text="$t('novelSyncErpApp.purchaseOrder.status')">Status</span>
              <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" :field-name="'status'"></jhi-sort-indicator>
            </th>
            <th scope="row" v-on:click="changeOrder('supplier.name')">
              <span v-text="$t('novelSyncErpApp.purchaseOrder.supplier')">Supplier</span>
              <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" :field-name="'supplier.name'"></jhi-sort-indicator>
            </th>
            <th scope="row" v-on:click="changeOrder('employee.fullName')">
              <span v-text="$t('novelSyncErpApp.purchaseOrder.employee')">Employee</span>
              <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" :field-name="'employee.fullName'"></jhi-sort-indicator>
            </th>
            <th scope="row" v-on:click="changeOrder('warehouse.name')">
              <span v-text="$t('novelSyncErpApp.purchaseOrder.warehouse')">Warehouse</span>
              <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" :field-name="'warehouse.name'"></jhi-sort-indicator>
            </th>
            <th scope="row"></th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="purchaseOrder in purchaseOrders" :key="purchaseOrder.id" data-cy="entityTable">
            <td>
              <router-link :to="{ name: 'PurchaseOrderView', params: { purchaseOrderId: purchaseOrder.id } }">{{
                purchaseOrder.id
              }}</router-link>
            </td>
            <td>{{ purchaseOrder.poCode }}</td>
            <td>{{ purchaseOrder.totalAmount }}</td>
            <td v-text="$t('novelSyncErpApp.OrderStatus.' + purchaseOrder.status)">{{ purchaseOrder.status }}</td>
            <td>
              <div v-if="purchaseOrder.supplier">
                <router-link :to="{ name: 'SupplierView', params: { supplierId: purchaseOrder.supplier.id } }">{{
                  purchaseOrder.supplier.name
                }}</router-link>
              </div>
            </td>
            <td>
              <div v-if="purchaseOrder.employee">
                <router-link :to="{ name: 'EmployeeView', params: { employeeId: purchaseOrder.employee.id } }">{{
                  purchaseOrder.employee.fullName
                }}</router-link>
              </div>
            </td>
            <td>
              <div v-if="purchaseOrder.warehouse">
                <router-link :to="{ name: 'WarehouseView', params: { warehouseId: purchaseOrder.warehouse.id } }">{{
                  purchaseOrder.warehouse.name
                }}</router-link>
              </div>
            </td>
            <td class="text-right">
              <div class="btn-group">
                <router-link
                  :to="{ name: 'PurchaseOrderView', params: { purchaseOrderId: purchaseOrder.id } }"
                  custom
                  v-slot="{ navigate }"
                >
                  <button @click="navigate" class="btn btn-info btn-sm details" data-cy="entityDetailsButton">
                    <font-awesome-icon icon="eye"></font-awesome-icon>
                    <span class="d-none d-md-inline" v-text="$t('entity.action.view')">View</span>
                  </button>
                </router-link>
                <router-link
                  :to="{ name: 'PurchaseOrderEdit', params: { purchaseOrderId: purchaseOrder.id } }"
                  custom
                  v-slot="{ navigate }"
                >
                  <button @click="navigate" class="btn btn-primary btn-sm edit" data-cy="entityEditButton">
                    <font-awesome-icon icon="pencil-alt"></font-awesome-icon>
                    <span class="d-none d-md-inline" v-text="$t('entity.action.edit')">Edit</span>
                  </button>
                </router-link>
                <b-button
                  v-on:click="prepareRemove(purchaseOrder)"
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
          id="novelSyncErpApp.purchaseOrder.delete.question"
          data-cy="purchaseOrderDeleteDialogHeading"
          v-text="$t('entity.delete.title')"
          >Confirm delete operation</span
        ></span
      >
      <div class="modal-body">
        <p id="jhi-delete-purchaseOrder-heading" v-text="$t('novelSyncErpApp.purchaseOrder.delete.question', { id: removeId })">
          Are you sure you want to delete this Purchase Order?
        </p>
      </div>
      <div slot="modal-footer">
        <button type="button" class="btn btn-secondary" v-text="$t('entity.action.cancel')" v-on:click="closeDialog()">Cancel</button>
        <button
          type="button"
          class="btn btn-primary"
          id="jhi-confirm-delete-purchaseOrder"
          data-cy="entityConfirmDeleteButton"
          v-text="$t('entity.action.delete')"
          v-on:click="removePurchaseOrder()"
        >
          Delete
        </button>
      </div>
    </b-modal>
    <div v-show="purchaseOrders && purchaseOrders.length > 0">
      <div class="row justify-content-center">
        <jhi-item-count :page="page" :total="queryCount" :itemsPerPage="itemsPerPage"></jhi-item-count>
      </div>
      <div class="row justify-content-center">
        <b-pagination size="md" :total-rows="totalItems" v-model="page" :per-page="itemsPerPage" :change="loadPage(page)"></b-pagination>
      </div>
    </div>
  </div>
</template>

<script lang="ts" src="./purchase-order.component.ts"></script>
