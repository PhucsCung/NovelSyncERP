<template>
  <div class="row justify-content-center">
    <div class="col-8">
      <form name="editForm" role="form" novalidate v-on:submit.prevent="save()">
        <h2
          id="novelSyncErpApp.transferOrder.home.createOrEditLabel"
          data-cy="TransferOrderCreateUpdateHeading"
          v-text="$t('novelSyncErpApp.transferOrder.home.createOrEditLabel')"
        >
          Create or edit a TransferOrder
        </h2>
        <div>
          <div class="form-group" v-if="transferOrder.id">
            <label for="id" v-text="$t('global.field.id')">ID</label>
            <input type="text" class="form-control" id="id" name="id" v-model="transferOrder.id" readonly />
          </div>
          <div class="form-group">
            <label class="form-control-label" v-text="$t('novelSyncErpApp.transferOrder.transferCode')" for="transfer-order-transferCode"
              >Transfer Code</label
            >
            <input
              type="text"
              class="form-control"
              name="transferCode"
              id="transfer-order-transferCode"
              data-cy="transferCode"
              :class="{ valid: !$v.transferOrder.transferCode.$invalid, invalid: $v.transferOrder.transferCode.$invalid }"
              v-model="$v.transferOrder.transferCode.$model"
              required
            />
            <div v-if="$v.transferOrder.transferCode.$anyDirty && $v.transferOrder.transferCode.$invalid">
              <small class="form-text text-danger" v-if="!$v.transferOrder.transferCode.required" v-text="$t('entity.validation.required')">
                This field is required.
              </small>
            </div>
          </div>
          <div class="form-group">
            <label class="form-control-label" v-text="$t('novelSyncErpApp.transferOrder.status')" for="transfer-order-status">Status</label>
            <select
              class="form-control"
              name="status"
              :class="{ valid: !$v.transferOrder.status.$invalid, invalid: $v.transferOrder.status.$invalid }"
              v-model="$v.transferOrder.status.$model"
              id="transfer-order-status"
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
            <div v-if="$v.transferOrder.status.$anyDirty && $v.transferOrder.status.$invalid">
              <small class="form-text text-danger" v-if="!$v.transferOrder.status.required" v-text="$t('entity.validation.required')">
                This field is required.
              </small>
            </div>
          </div>
          <div class="form-group">
            <label class="form-control-label" v-text="$t('novelSyncErpApp.transferOrder.fromWarehouse')" for="transfer-order-fromWarehouse"
              >From Warehouse</label
            >
            <select
              class="form-control"
              id="transfer-order-fromWarehouse"
              data-cy="fromWarehouse"
              name="fromWarehouse"
              v-model="transferOrder.fromWarehouse"
            >
              <option v-bind:value="null"></option>
              <option
                v-bind:value="
                  transferOrder.fromWarehouse && warehouseOption.id === transferOrder.fromWarehouse.id
                    ? transferOrder.fromWarehouse
                    : warehouseOption
                "
                v-for="warehouseOption in warehouses"
                :key="warehouseOption.id"
              >
                {{ warehouseOption.name }}
              </option>
            </select>
          </div>
          <div class="form-group">
            <label class="form-control-label" v-text="$t('novelSyncErpApp.transferOrder.toWarehouse')" for="transfer-order-toWarehouse"
              >To Warehouse</label
            >
            <select
              class="form-control"
              id="transfer-order-toWarehouse"
              data-cy="toWarehouse"
              name="toWarehouse"
              v-model="transferOrder.toWarehouse"
            >
              <option v-bind:value="null"></option>
              <option
                v-bind:value="
                  transferOrder.toWarehouse && warehouseOption.id === transferOrder.toWarehouse.id
                    ? transferOrder.toWarehouse
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
            :disabled="$v.transferOrder.$invalid || isSaving"
            class="btn btn-primary"
          >
            <font-awesome-icon icon="save"></font-awesome-icon>&nbsp;<span v-text="$t('entity.action.save')">Save</span>
          </button>
        </div>
      </form>
    </div>
  </div>
</template>
<script lang="ts" src="./transfer-order-update.component.ts"></script>
