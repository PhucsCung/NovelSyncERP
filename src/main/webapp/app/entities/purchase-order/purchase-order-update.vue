<template>
  <div class="row justify-content-center">
    <div class="col-8">
      <form name="editForm" role="form" novalidate v-on:submit.prevent="save()">
        <h2
          id="novelSyncErpApp.purchaseOrder.home.createOrEditLabel"
          data-cy="PurchaseOrderCreateUpdateHeading"
          v-text="$t('novelSyncErpApp.purchaseOrder.home.createOrEditLabel')"
        >
          Create or edit a PurchaseOrder
        </h2>
        <div>
          <div class="form-group" v-if="purchaseOrder.id">
            <label for="id" v-text="$t('global.field.id')">ID</label>
            <input type="text" class="form-control" id="id" name="id" v-model="purchaseOrder.id" readonly />
          </div>
          <div class="form-group">
            <label class="form-control-label" v-text="$t('novelSyncErpApp.purchaseOrder.poCode')" for="purchase-order-poCode"
              >Po Code</label
            >
            <input
              type="text"
              class="form-control"
              name="poCode"
              id="purchase-order-poCode"
              data-cy="poCode"
              :class="{ valid: !$v.purchaseOrder.poCode.$invalid, invalid: $v.purchaseOrder.poCode.$invalid }"
              v-model="$v.purchaseOrder.poCode.$model"
              required
            />
            <div v-if="$v.purchaseOrder.poCode.$anyDirty && $v.purchaseOrder.poCode.$invalid">
              <small class="form-text text-danger" v-if="!$v.purchaseOrder.poCode.required" v-text="$t('entity.validation.required')">
                This field is required.
              </small>
            </div>
          </div>
          <div class="form-group">
            <label class="form-control-label" v-text="$t('novelSyncErpApp.purchaseOrder.totalAmount')" for="purchase-order-totalAmount"
              >Total Amount</label
            >
            <input
              type="number"
              class="form-control"
              name="totalAmount"
              id="purchase-order-totalAmount"
              data-cy="totalAmount"
              :class="{ valid: !$v.purchaseOrder.totalAmount.$invalid, invalid: $v.purchaseOrder.totalAmount.$invalid }"
              v-model.number="$v.purchaseOrder.totalAmount.$model"
            />
          </div>
          <div class="form-group">
            <label class="form-control-label" v-text="$t('novelSyncErpApp.purchaseOrder.status')" for="purchase-order-status">Status</label>
            <select
              class="form-control"
              name="status"
              :class="{ valid: !$v.purchaseOrder.status.$invalid, invalid: $v.purchaseOrder.status.$invalid }"
              v-model="$v.purchaseOrder.status.$model"
              id="purchase-order-status"
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
            <div v-if="$v.purchaseOrder.status.$anyDirty && $v.purchaseOrder.status.$invalid">
              <small class="form-text text-danger" v-if="!$v.purchaseOrder.status.required" v-text="$t('entity.validation.required')">
                This field is required.
              </small>
            </div>
          </div>
          <div class="form-group">
            <label class="form-control-label" v-text="$t('novelSyncErpApp.purchaseOrder.supplier')" for="purchase-order-supplier"
              >Supplier</label
            >
            <select class="form-control" id="purchase-order-supplier" data-cy="supplier" name="supplier" v-model="purchaseOrder.supplier">
              <option v-bind:value="null"></option>
              <option
                v-bind:value="
                  purchaseOrder.supplier && supplierOption.id === purchaseOrder.supplier.id ? purchaseOrder.supplier : supplierOption
                "
                v-for="supplierOption in suppliers"
                :key="supplierOption.id"
              >
                {{ supplierOption.name }}
              </option>
            </select>
          </div>
          <div class="form-group">
            <label class="form-control-label" v-text="$t('novelSyncErpApp.purchaseOrder.employee')" for="purchase-order-employee"
              >Employee</label
            >
            <select class="form-control" id="purchase-order-employee" data-cy="employee" name="employee" v-model="purchaseOrder.employee">
              <option v-bind:value="null"></option>
              <option
                v-bind:value="
                  purchaseOrder.employee && employeeOption.id === purchaseOrder.employee.id ? purchaseOrder.employee : employeeOption
                "
                v-for="employeeOption in employees"
                :key="employeeOption.id"
              >
                {{ employeeOption.fullName }}
              </option>
            </select>
          </div>
          <div class="form-group">
            <label class="form-control-label" v-text="$t('novelSyncErpApp.purchaseOrder.warehouse')" for="purchase-order-warehouse"
              >Warehouse</label
            >
            <select
              class="form-control"
              id="purchase-order-warehouse"
              data-cy="warehouse"
              name="warehouse"
              v-model="purchaseOrder.warehouse"
            >
              <option v-bind:value="null"></option>
              <option
                v-bind:value="
                  purchaseOrder.warehouse && warehouseOption.id === purchaseOrder.warehouse.id ? purchaseOrder.warehouse : warehouseOption
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
            :disabled="$v.purchaseOrder.$invalid || isSaving"
            class="btn btn-primary"
          >
            <font-awesome-icon icon="save"></font-awesome-icon>&nbsp;<span v-text="$t('entity.action.save')">Save</span>
          </button>
        </div>
      </form>
    </div>
  </div>
</template>
<script lang="ts" src="./purchase-order-update.component.ts"></script>
