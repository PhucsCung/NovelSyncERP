<template>
  <div class="row justify-content-center">
    <div class="col-8">
      <form name="editForm" role="form" novalidate v-on:submit.prevent="save()">
        <h2
          id="novelSyncErpApp.salesOrder.home.createOrEditLabel"
          data-cy="SalesOrderCreateUpdateHeading"
          v-text="$t('novelSyncErpApp.salesOrder.home.createOrEditLabel')"
        >
          Create or edit a SalesOrder
        </h2>
        <div>
          <div class="form-group" v-if="salesOrder.id">
            <label for="id" v-text="$t('global.field.id')">ID</label>
            <input type="text" class="form-control" id="id" name="id" v-model="salesOrder.id" readonly />
          </div>
          <div class="form-group">
            <label class="form-control-label" v-text="$t('novelSyncErpApp.salesOrder.orderCode')" for="sales-order-orderCode"
              >Order Code</label
            >
            <input
              type="text"
              class="form-control"
              name="orderCode"
              id="sales-order-orderCode"
              data-cy="orderCode"
              :class="{ valid: !$v.salesOrder.orderCode.$invalid, invalid: $v.salesOrder.orderCode.$invalid }"
              v-model="$v.salesOrder.orderCode.$model"
              required
            />
            <div v-if="$v.salesOrder.orderCode.$anyDirty && $v.salesOrder.orderCode.$invalid">
              <small class="form-text text-danger" v-if="!$v.salesOrder.orderCode.required" v-text="$t('entity.validation.required')">
                This field is required.
              </small>
            </div>
          </div>
          <div class="form-group">
            <label class="form-control-label" v-text="$t('novelSyncErpApp.salesOrder.totalAmount')" for="sales-order-totalAmount"
              >Total Amount</label
            >
            <input
              type="number"
              class="form-control"
              name="totalAmount"
              id="sales-order-totalAmount"
              data-cy="totalAmount"
              :class="{ valid: !$v.salesOrder.totalAmount.$invalid, invalid: $v.salesOrder.totalAmount.$invalid }"
              v-model.number="$v.salesOrder.totalAmount.$model"
            />
          </div>
          <div class="form-group">
            <label class="form-control-label" v-text="$t('novelSyncErpApp.salesOrder.status')" for="sales-order-status">Status</label>
            <select
              class="form-control"
              name="status"
              :class="{ valid: !$v.salesOrder.status.$invalid, invalid: $v.salesOrder.status.$invalid }"
              v-model="$v.salesOrder.status.$model"
              id="sales-order-status"
              data-cy="status"
              required
            >
              <option
                v-for="orderStatus in orderStatusValues"
                :key="orderStatus"
                v-bind:value="orderStatus"
                v-bind:label="$t('novelSyncErpApp.OrderStatus.' + orderStatus)"
              >
                {{ orderStatus }}
              </option>
            </select>
            <div v-if="$v.salesOrder.status.$anyDirty && $v.salesOrder.status.$invalid">
              <small class="form-text text-danger" v-if="!$v.salesOrder.status.required" v-text="$t('entity.validation.required')">
                This field is required.
              </small>
            </div>
          </div>
          <div class="form-group">
            <label class="form-control-label" v-text="$t('novelSyncErpApp.salesOrder.customer')" for="sales-order-customer">Customer</label>
            <select class="form-control" id="sales-order-customer" data-cy="customer" name="customer" v-model="salesOrder.customer">
              <option v-bind:value="null"></option>
              <option
                v-bind:value="salesOrder.customer && customerOption.id === salesOrder.customer.id ? salesOrder.customer : customerOption"
                v-for="customerOption in customers"
                :key="customerOption.id"
              >
                {{ customerOption.name }}
              </option>
            </select>
          </div>
          <div class="form-group">
            <label class="form-control-label" v-text="$t('novelSyncErpApp.salesOrder.employee')" for="sales-order-employee">Employee</label>
            <select class="form-control" id="sales-order-employee" data-cy="employee" name="employee" v-model="salesOrder.employee">
              <option v-bind:value="null"></option>
              <option
                v-bind:value="salesOrder.employee && employeeOption.id === salesOrder.employee.id ? salesOrder.employee : employeeOption"
                v-for="employeeOption in employees"
                :key="employeeOption.id"
              >
                {{ employeeOption.fullName }}
              </option>
            </select>
          </div>
          <div class="form-group">
            <label class="form-control-label" v-text="$t('novelSyncErpApp.salesOrder.warehouse')" for="sales-order-warehouse"
              >Warehouse</label
            >
            <select class="form-control" id="sales-order-warehouse" data-cy="warehouse" name="warehouse" v-model="salesOrder.warehouse">
              <option v-bind:value="null"></option>
              <option
                v-bind:value="
                  salesOrder.warehouse && warehouseOption.id === salesOrder.warehouse.id ? salesOrder.warehouse : warehouseOption
                "
                v-for="warehouseOption in warehouses"
                :key="warehouseOption.id"
              >
                {{ warehouseOption.name }}
              </option>
            </select>
          </div>
        </div>
        <div>
          <button type="button" id="cancel-save" data-cy="entityCreateCancelButton" class="btn btn-secondary" v-on:click="previousState()">
            <font-awesome-icon icon="ban"></font-awesome-icon>&nbsp;<span v-text="$t('entity.action.cancel')">Cancel</span>
          </button>
          <button
            type="submit"
            id="save-entity"
            data-cy="entityCreateSaveButton"
            :disabled="$v.salesOrder.$invalid || isSaving"
            class="btn btn-primary"
          >
            <font-awesome-icon icon="save"></font-awesome-icon>&nbsp;<span v-text="$t('entity.action.save')">Save</span>
          </button>
        </div>
      </form>
    </div>
  </div>
</template>
<script lang="ts" src="./sales-order-update.component.ts"></script>
