<template>
  <div>
    <h2 id="page-heading" data-cy="SalesOrderHeading">
      <span v-text="$t('novelSyncErpApp.salesOrder.home.title')" id="sales-order-heading">Sales Orders</span>
      <div class="d-flex justify-content-end">
        <button class="btn btn-info mr-2" v-on:click="handleSyncList" :disabled="isFetching">
          <font-awesome-icon icon="sync" :spin="isFetching"></font-awesome-icon>
          <span v-text="$t('novelSyncErpApp.salesOrder.home.refreshListLabel')">Refresh List</span>
        </button>
        <router-link :to="{ name: 'SalesOrderCreate' }" custom v-slot="{ navigate }">
          <button
            @click="navigate"
            id="jh-create-entity"
            data-cy="entityCreateButton"
            class="btn btn-primary jh-create-entity create-sales-order"
          >
            <font-awesome-icon icon="plus"></font-awesome-icon>
            <span v-text="$t('novelSyncErpApp.salesOrder.home.createLabel')"> Create a new Sales Order </span>
          </button>
        </router-link>
      </div>
    </h2>
    <br />
    <div class="alert alert-warning" v-if="!isFetching && salesOrders && salesOrders.length === 0">
      <span v-text="$t('novelSyncErpApp.salesOrder.home.notFound')">No salesOrders found</span>
    </div>
    <div class="table-responsive" v-if="salesOrders && salesOrders.length > 0">
      <table class="table table-striped" aria-describedby="salesOrders">
        <thead>
          <tr>
            <th scope="row" v-on:click="changeOrder('id')">
              <span v-text="$t('global.field.id')">ID</span>
              <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" :field-name="'id'"></jhi-sort-indicator>
            </th>
            <th scope="row" v-on:click="changeOrder('orderCode')">
              <span v-text="$t('novelSyncErpApp.salesOrder.orderCode')">Order Code</span>
              <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" :field-name="'orderCode'"></jhi-sort-indicator>
            </th>
            <th scope="row" v-on:click="changeOrder('totalAmount')">
              <span v-text="$t('novelSyncErpApp.salesOrder.totalAmount')">Total Amount</span>
              <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" :field-name="'totalAmount'"></jhi-sort-indicator>
            </th>
            <th scope="row" v-on:click="changeOrder('status')">
              <span v-text="$t('novelSyncErpApp.salesOrder.status')">Status</span>
              <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" :field-name="'status'"></jhi-sort-indicator>
            </th>
            <th scope="row" v-on:click="changeOrder('customer.name')">
              <span v-text="$t('novelSyncErpApp.salesOrder.customer')">Customer</span>
              <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" :field-name="'customer.name'"></jhi-sort-indicator>
            </th>
            <th scope="row" v-on:click="changeOrder('employee.fullName')">
              <span v-text="$t('novelSyncErpApp.salesOrder.employee')">Employee</span>
              <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" :field-name="'employee.fullName'"></jhi-sort-indicator>
            </th>
            <th scope="row" v-on:click="changeOrder('warehouse.name')">
              <span v-text="$t('novelSyncErpApp.salesOrder.warehouse')">Warehouse</span>
              <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" :field-name="'warehouse.name'"></jhi-sort-indicator>
            </th>
            <th scope="row"></th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="salesOrder in salesOrders" :key="salesOrder.id" data-cy="entityTable">
            <td>
              <router-link :to="{ name: 'SalesOrderView', params: { salesOrderId: salesOrder.id } }">{{ salesOrder.id }}</router-link>
            </td>
            <td>{{ salesOrder.orderCode }}</td>
            <td>{{ salesOrder.totalAmount }}</td>
            <td v-text="$t('novelSyncErpApp.OrderStatus.' + salesOrder.status)">{{ salesOrder.status }}</td>
            <td>
              <div v-if="salesOrder.customer">
                <router-link :to="{ name: 'CustomerView', params: { customerId: salesOrder.customer.id } }">{{
                  salesOrder.customer.name
                }}</router-link>
              </div>
            </td>
            <td>
              <div v-if="salesOrder.employee">
                <router-link :to="{ name: 'EmployeeView', params: { employeeId: salesOrder.employee.id } }">{{
                  salesOrder.employee.fullName
                }}</router-link>
              </div>
            </td>
            <td>
              <div v-if="salesOrder.warehouse">
                <router-link :to="{ name: 'WarehouseView', params: { warehouseId: salesOrder.warehouse.id } }">{{
                  salesOrder.warehouse.name
                }}</router-link>
              </div>
            </td>
            <td class="text-right">
              <div class="btn-group">
                <router-link :to="{ name: 'SalesOrderView', params: { salesOrderId: salesOrder.id } }" custom v-slot="{ navigate }">
                  <button @click="navigate" class="btn btn-info btn-sm details" data-cy="entityDetailsButton">
                    <font-awesome-icon icon="eye"></font-awesome-icon>
                    <span class="d-none d-md-inline" v-text="$t('entity.action.view')">View</span>
                  </button>
                </router-link>
                <router-link :to="{ name: 'SalesOrderEdit', params: { salesOrderId: salesOrder.id } }" custom v-slot="{ navigate }">
                  <button @click="navigate" class="btn btn-primary btn-sm edit" data-cy="entityEditButton">
                    <font-awesome-icon icon="pencil-alt"></font-awesome-icon>
                    <span class="d-none d-md-inline" v-text="$t('entity.action.edit')">Edit</span>
                  </button>
                </router-link>
                <b-button
                  v-on:click="prepareRemove(salesOrder)"
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
        ><span id="novelSyncErpApp.salesOrder.delete.question" data-cy="salesOrderDeleteDialogHeading" v-text="$t('entity.delete.title')"
          >Confirm delete operation</span
        ></span
      >
      <div class="modal-body">
        <p id="jhi-delete-salesOrder-heading" v-text="$t('novelSyncErpApp.salesOrder.delete.question', { id: removeId })">
          Are you sure you want to delete this Sales Order?
        </p>
      </div>
      <div slot="modal-footer">
        <button type="button" class="btn btn-secondary" v-text="$t('entity.action.cancel')" v-on:click="closeDialog()">Cancel</button>
        <button
          type="button"
          class="btn btn-primary"
          id="jhi-confirm-delete-salesOrder"
          data-cy="entityConfirmDeleteButton"
          v-text="$t('entity.action.delete')"
          v-on:click="removeSalesOrder()"
        >
          Delete
        </button>
      </div>
    </b-modal>
    <div v-show="salesOrders && salesOrders.length > 0">
      <div class="row justify-content-center">
        <jhi-item-count :page="page" :total="queryCount" :itemsPerPage="itemsPerPage"></jhi-item-count>
      </div>
      <div class="row justify-content-center">
        <b-pagination size="md" :total-rows="totalItems" v-model="page" :per-page="itemsPerPage" :change="loadPage(page)"></b-pagination>
      </div>
    </div>
  </div>
</template>

<script lang="ts" src="./sales-order.component.ts"></script>
