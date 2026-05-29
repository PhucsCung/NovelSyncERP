<template>
  <div class="row justify-content-center">
    <div class="col-8">
      <form name="editForm" role="form" novalidate v-on:submit.prevent="save()">
        <h2
          id="novelSyncErpApp.inventoryBalance.home.createOrEditLabel"
          data-cy="InventoryBalanceCreateUpdateHeading"
          v-text="$t('novelSyncErpApp.inventoryBalance.home.createOrEditLabel')"
        >
          Create or edit a InventoryBalance
        </h2>
        <div>
          <div class="form-group" v-if="inventoryBalance.id">
            <label for="id" v-text="$t('global.field.id')">ID</label>
            <input type="text" class="form-control" id="id" name="id" v-model="inventoryBalance.id" readonly />
          </div>
          <div class="form-group">
            <label class="form-control-label" v-text="$t('novelSyncErpApp.inventoryBalance.quantity')" for="inventory-balance-quantity"
              >Quantity</label
            >
            <input
              type="number"
              class="form-control"
              name="quantity"
              id="inventory-balance-quantity"
              data-cy="quantity"
              :class="{ valid: !$v.inventoryBalance.quantity.$invalid, invalid: $v.inventoryBalance.quantity.$invalid }"
              v-model.number="$v.inventoryBalance.quantity.$model"
              required
            />
            <div v-if="$v.inventoryBalance.quantity.$anyDirty && $v.inventoryBalance.quantity.$invalid">
              <small class="form-text text-danger" v-if="!$v.inventoryBalance.quantity.required" v-text="$t('entity.validation.required')">
                This field is required.
              </small>
              <small class="form-text text-danger" v-if="!$v.inventoryBalance.quantity.numeric" v-text="$t('entity.validation.number')">
                This field should be a number.
              </small>
            </div>
          </div>
          <div class="form-group">
            <label class="form-control-label" v-text="$t('novelSyncErpApp.inventoryBalance.product')" for="inventory-balance-product"
              >Product</label
            >
            <select class="form-control" id="inventory-balance-product" data-cy="product" name="product" v-model="inventoryBalance.product">
              <option v-bind:value="null"></option>
              <option
                v-bind:value="
                  inventoryBalance.product && productOption.id === inventoryBalance.product.id ? inventoryBalance.product : productOption
                "
                v-for="productOption in products"
                :key="productOption.id"
              >
                {{ productOption.name }}
              </option>
            </select>
          </div>
          <div class="form-group">
            <label class="form-control-label" v-text="$t('novelSyncErpApp.inventoryBalance.warehouse')" for="inventory-balance-warehouse"
              >Warehouse</label
            >
            <select
              class="form-control"
              id="inventory-balance-warehouse"
              data-cy="warehouse"
              name="warehouse"
              v-model="inventoryBalance.warehouse"
            >
              <option v-bind:value="null"></option>
              <option
                v-bind:value="
                  inventoryBalance.warehouse && warehouseOption.id === inventoryBalance.warehouse.id
                    ? inventoryBalance.warehouse
                    : warehouseOption
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
            :disabled="$v.inventoryBalance.$invalid || isSaving"
            class="btn btn-primary"
          >
            <font-awesome-icon icon="save"></font-awesome-icon>&nbsp;<span v-text="$t('entity.action.save')">Save</span>
          </button>
        </div>
      </form>
    </div>
  </div>
</template>
<script lang="ts" src="./inventory-balance-update.component.ts"></script>
