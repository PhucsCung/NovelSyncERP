<template>
  <div class="row justify-content-center">
    <div class="col-8">
      <form name="editForm" role="form" novalidate v-on:submit.prevent="save()">
        <h2
          id="novelSyncErpApp.transferOrderLine.home.createOrEditLabel"
          data-cy="TransferOrderLineCreateUpdateHeading"
          v-text="$t('novelSyncErpApp.transferOrderLine.home.createOrEditLabel')"
        >
          Create or edit a TransferOrderLine
        </h2>
        <div>
          <div class="form-group" v-if="transferOrderLine.id">
            <label for="id" v-text="$t('global.field.id')">ID</label>
            <input type="text" class="form-control" id="id" name="id" v-model="transferOrderLine.id" readonly />
          </div>
          <div class="form-group">
            <label class="form-control-label" v-text="$t('novelSyncErpApp.transferOrderLine.quantity')" for="transfer-order-line-quantity"
              >Quantity</label
            >
            <input
              type="number"
              class="form-control"
              name="quantity"
              id="transfer-order-line-quantity"
              data-cy="quantity"
              :class="{ valid: !$v.transferOrderLine.quantity.$invalid, invalid: $v.transferOrderLine.quantity.$invalid }"
              v-model.number="$v.transferOrderLine.quantity.$model"
              required
            />
            <div v-if="$v.transferOrderLine.quantity.$anyDirty && $v.transferOrderLine.quantity.$invalid">
              <small class="form-text text-danger" v-if="!$v.transferOrderLine.quantity.required" v-text="$t('entity.validation.required')">
                This field is required.
              </small>
              <small class="form-text text-danger" v-if="!$v.transferOrderLine.quantity.numeric" v-text="$t('entity.validation.number')">
                This field should be a number.
              </small>
            </div>
          </div>
          <div class="form-group">
            <label class="form-control-label" v-text="$t('novelSyncErpApp.transferOrderLine.product')" for="transfer-order-line-product"
              >Product</label
            >
            <select
              class="form-control"
              id="transfer-order-line-product"
              data-cy="product"
              name="product"
              v-model="transferOrderLine.product"
            >
              <option v-bind:value="null"></option>
              <option
                v-bind:value="
                  transferOrderLine.product && productOption.id === transferOrderLine.product.id ? transferOrderLine.product : productOption
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
              v-text="$t('novelSyncErpApp.transferOrderLine.transferOrder')"
              for="transfer-order-line-transferOrder"
              >Transfer Order</label
            >
            <select
              class="form-control"
              id="transfer-order-line-transferOrder"
              data-cy="transferOrder"
              name="transferOrder"
              v-model="transferOrderLine.transferOrder"
            >
              <option v-bind:value="null"></option>
              <option
                v-bind:value="
                  transferOrderLine.transferOrder && transferOrderOption.id === transferOrderLine.transferOrder.id
                    ? transferOrderLine.transferOrder
                    : transferOrderOption
                "
                v-for="transferOrderOption in transferOrders"
                :key="transferOrderOption.id"
              >
                {{ transferOrderOption.id }}
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
            :disabled="$v.transferOrderLine.$invalid || isSaving"
            class="btn btn-primary"
          >
            <font-awesome-icon icon="save"></font-awesome-icon>&nbsp;<span v-text="$t('entity.action.save')">Save</span>
          </button>
        </div>
      </form>
    </div>
  </div>
</template>
<script lang="ts" src="./transfer-order-line-update.component.ts"></script>
