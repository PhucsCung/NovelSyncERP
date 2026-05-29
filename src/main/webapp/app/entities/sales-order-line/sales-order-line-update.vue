<template>
  <div class="row justify-content-center">
    <div class="col-8">
      <form name="editForm" role="form" novalidate v-on:submit.prevent="save()">
        <h2
          id="novelSyncErpApp.salesOrderLine.home.createOrEditLabel"
          data-cy="SalesOrderLineCreateUpdateHeading"
          v-text="$t('novelSyncErpApp.salesOrderLine.home.createOrEditLabel')"
        >
          Create or edit a SalesOrderLine
        </h2>
        <div>
          <div class="form-group" v-if="salesOrderLine.id">
            <label for="id" v-text="$t('global.field.id')">ID</label>
            <input type="text" class="form-control" id="id" name="id" v-model="salesOrderLine.id" readonly />
          </div>
          <div class="form-group">
            <label class="form-control-label" v-text="$t('novelSyncErpApp.salesOrderLine.quantity')" for="sales-order-line-quantity"
              >Quantity</label
            >
            <input
              type="number"
              class="form-control"
              name="quantity"
              id="sales-order-line-quantity"
              data-cy="quantity"
              :class="{ valid: !$v.salesOrderLine.quantity.$invalid, invalid: $v.salesOrderLine.quantity.$invalid }"
              v-model.number="$v.salesOrderLine.quantity.$model"
              required
            />
            <div v-if="$v.salesOrderLine.quantity.$anyDirty && $v.salesOrderLine.quantity.$invalid">
              <small class="form-text text-danger" v-if="!$v.salesOrderLine.quantity.required" v-text="$t('entity.validation.required')">
                This field is required.
              </small>
              <small class="form-text text-danger" v-if="!$v.salesOrderLine.quantity.numeric" v-text="$t('entity.validation.number')">
                This field should be a number.
              </small>
            </div>
          </div>
          <div class="form-group">
            <label class="form-control-label" v-text="$t('novelSyncErpApp.salesOrderLine.unitPrice')" for="sales-order-line-unitPrice"
              >Unit Price</label
            >
            <input
              type="number"
              class="form-control"
              name="unitPrice"
              id="sales-order-line-unitPrice"
              data-cy="unitPrice"
              :class="{ valid: !$v.salesOrderLine.unitPrice.$invalid, invalid: $v.salesOrderLine.unitPrice.$invalid }"
              v-model.number="$v.salesOrderLine.unitPrice.$model"
              required
            />
            <div v-if="$v.salesOrderLine.unitPrice.$anyDirty && $v.salesOrderLine.unitPrice.$invalid">
              <small class="form-text text-danger" v-if="!$v.salesOrderLine.unitPrice.required" v-text="$t('entity.validation.required')">
                This field is required.
              </small>
              <small class="form-text text-danger" v-if="!$v.salesOrderLine.unitPrice.numeric" v-text="$t('entity.validation.number')">
                This field should be a number.
              </small>
            </div>
          </div>
          <div class="form-group">
            <label class="form-control-label" v-text="$t('novelSyncErpApp.salesOrderLine.product')" for="sales-order-line-product"
              >Product</label
            >
            <select class="form-control" id="sales-order-line-product" data-cy="product" name="product" v-model="salesOrderLine.product">
              <option v-bind:value="null"></option>
              <option
                v-bind:value="
                  salesOrderLine.product && productOption.id === salesOrderLine.product.id ? salesOrderLine.product : productOption
                "
                v-for="productOption in products"
                :key="productOption.id"
              >
                {{ productOption.name }}
              </option>
            </select>
          </div>
          <div class="form-group">
            <label class="form-control-label" v-text="$t('novelSyncErpApp.salesOrderLine.salesOrder')" for="sales-order-line-salesOrder"
              >Sales Order</label
            >
            <select
              class="form-control"
              id="sales-order-line-salesOrder"
              data-cy="salesOrder"
              name="salesOrder"
              v-model="salesOrderLine.salesOrder"
            >
              <option v-bind:value="null"></option>
              <option
                v-bind:value="
                  salesOrderLine.salesOrder && salesOrderOption.id === salesOrderLine.salesOrder.id
                    ? salesOrderLine.salesOrder
                    : salesOrderOption
                "
                v-for="salesOrderOption in salesOrders"
                :key="salesOrderOption.id"
              >
                {{ salesOrderOption.id }}
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
            :disabled="$v.salesOrderLine.$invalid || isSaving"
            class="btn btn-primary"
          >
            <font-awesome-icon icon="save"></font-awesome-icon>&nbsp;<span v-text="$t('entity.action.save')">Save</span>
          </button>
        </div>
      </form>
    </div>
  </div>
</template>
<script lang="ts" src="./sales-order-line-update.component.ts"></script>
