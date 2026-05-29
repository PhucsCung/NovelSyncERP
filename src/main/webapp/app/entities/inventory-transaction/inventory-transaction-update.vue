<template>
  <div class="row justify-content-center">
    <div class="col-8">
      <form name="editForm" role="form" novalidate v-on:submit.prevent="save()">
        <h2
          id="novelSyncErpApp.inventoryTransaction.home.createOrEditLabel"
          data-cy="InventoryTransactionCreateUpdateHeading"
          v-text="$t('novelSyncErpApp.inventoryTransaction.home.createOrEditLabel')"
        >
          Create or edit a InventoryTransaction
        </h2>
        <div>
          <div class="form-group" v-if="inventoryTransaction.id">
            <label for="id" v-text="$t('global.field.id')">ID</label>
            <input type="text" class="form-control" id="id" name="id" v-model="inventoryTransaction.id" readonly />
          </div>
          <div class="form-group">
            <label class="form-control-label" v-text="$t('novelSyncErpApp.inventoryTransaction.type')" for="inventory-transaction-type"
              >Type</label
            >
            <select
              class="form-control"
              name="type"
              :class="{ valid: !$v.inventoryTransaction.type.$invalid, invalid: $v.inventoryTransaction.type.$invalid }"
              v-model="$v.inventoryTransaction.type.$model"
              id="inventory-transaction-type"
              data-cy="type"
              required
            >
              <option
                v-for="transactionType in transactionTypeValues"
                :key="transactionType"
                v-bind:value="transactionType"
                v-bind:label="$t('novelSyncErpApp.TransactionType.' + transactionType)"
              >
                {{ transactionType }}
              </option>
            </select>
            <div v-if="$v.inventoryTransaction.type.$anyDirty && $v.inventoryTransaction.type.$invalid">
              <small class="form-text text-danger" v-if="!$v.inventoryTransaction.type.required" v-text="$t('entity.validation.required')">
                This field is required.
              </small>
            </div>
          </div>
          <div class="form-group">
            <label
              class="form-control-label"
              v-text="$t('novelSyncErpApp.inventoryTransaction.quantity')"
              for="inventory-transaction-quantity"
              >Quantity</label
            >
            <input
              type="number"
              class="form-control"
              name="quantity"
              id="inventory-transaction-quantity"
              data-cy="quantity"
              :class="{ valid: !$v.inventoryTransaction.quantity.$invalid, invalid: $v.inventoryTransaction.quantity.$invalid }"
              v-model.number="$v.inventoryTransaction.quantity.$model"
              required
            />
            <div v-if="$v.inventoryTransaction.quantity.$anyDirty && $v.inventoryTransaction.quantity.$invalid">
              <small
                class="form-text text-danger"
                v-if="!$v.inventoryTransaction.quantity.required"
                v-text="$t('entity.validation.required')"
              >
                This field is required.
              </small>
              <small class="form-text text-danger" v-if="!$v.inventoryTransaction.quantity.numeric" v-text="$t('entity.validation.number')">
                This field should be a number.
              </small>
            </div>
          </div>
          <div class="form-group">
            <label
              class="form-control-label"
              v-text="$t('novelSyncErpApp.inventoryTransaction.unitCost')"
              for="inventory-transaction-unitCost"
              >Unit Cost</label
            >
            <input
              type="number"
              class="form-control"
              name="unitCost"
              id="inventory-transaction-unitCost"
              data-cy="unitCost"
              :class="{ valid: !$v.inventoryTransaction.unitCost.$invalid, invalid: $v.inventoryTransaction.unitCost.$invalid }"
              v-model.number="$v.inventoryTransaction.unitCost.$model"
            />
          </div>
          <div class="form-group">
            <label
              class="form-control-label"
              v-text="$t('novelSyncErpApp.inventoryTransaction.referenceId')"
              for="inventory-transaction-referenceId"
              >Reference Id</label
            >
            <input
              type="number"
              class="form-control"
              name="referenceId"
              id="inventory-transaction-referenceId"
              data-cy="referenceId"
              :class="{ valid: !$v.inventoryTransaction.referenceId.$invalid, invalid: $v.inventoryTransaction.referenceId.$invalid }"
              v-model.number="$v.inventoryTransaction.referenceId.$model"
            />
          </div>
          <div class="form-group">
            <label
              class="form-control-label"
              v-text="$t('novelSyncErpApp.inventoryTransaction.createdDate')"
              for="inventory-transaction-createdDate"
              >Created Date</label
            >
            <div class="d-flex">
              <input
                id="inventory-transaction-createdDate"
                data-cy="createdDate"
                type="datetime-local"
                class="form-control"
                name="createdDate"
                :class="{ valid: !$v.inventoryTransaction.createdDate.$invalid, invalid: $v.inventoryTransaction.createdDate.$invalid }"
                required
                :value="convertDateTimeFromServer($v.inventoryTransaction.createdDate.$model)"
                @change="updateInstantField('createdDate', $event)"
              />
            </div>
            <div v-if="$v.inventoryTransaction.createdDate.$anyDirty && $v.inventoryTransaction.createdDate.$invalid">
              <small
                class="form-text text-danger"
                v-if="!$v.inventoryTransaction.createdDate.required"
                v-text="$t('entity.validation.required')"
              >
                This field is required.
              </small>
              <small
                class="form-text text-danger"
                v-if="!$v.inventoryTransaction.createdDate.ZonedDateTimelocal"
                v-text="$t('entity.validation.ZonedDateTimelocal')"
              >
                This field should be a date and time.
              </small>
            </div>
          </div>
          <div class="form-group">
            <label
              class="form-control-label"
              v-text="$t('novelSyncErpApp.inventoryTransaction.product')"
              for="inventory-transaction-product"
              >Product</label
            >
            <select
              class="form-control"
              id="inventory-transaction-product"
              data-cy="product"
              name="product"
              v-model="inventoryTransaction.product"
            >
              <option v-bind:value="null"></option>
              <option
                v-bind:value="
                  inventoryTransaction.product && productOption.id === inventoryTransaction.product.id
                    ? inventoryTransaction.product
                    : productOption
                "
                v-for="productOption in products"
                :key="productOption.id"
              >
                {{ productOption.name }}
              </option>
            </select>
          </div>
          <div class="form-group">
            <label
              class="form-control-label"
              v-text="$t('novelSyncErpApp.inventoryTransaction.warehouse')"
              for="inventory-transaction-warehouse"
              >Warehouse</label
            >
            <select
              class="form-control"
              id="inventory-transaction-warehouse"
              data-cy="warehouse"
              name="warehouse"
              v-model="inventoryTransaction.warehouse"
            >
              <option v-bind:value="null"></option>
              <option
                v-bind:value="
                  inventoryTransaction.warehouse && warehouseOption.id === inventoryTransaction.warehouse.id
                    ? inventoryTransaction.warehouse
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
            :disabled="$v.inventoryTransaction.$invalid || isSaving"
            class="btn btn-primary"
          >
            <font-awesome-icon icon="save"></font-awesome-icon>&nbsp;<span v-text="$t('entity.action.save')">Save</span>
          </button>
        </div>
      </form>
    </div>
  </div>
</template>
<script lang="ts" src="./inventory-transaction-update.component.ts"></script>
