<template>
  <div class="row justify-content-center">
    <div class="col-8">
      <form name="editForm" role="form" novalidate v-on:submit.prevent="save()">
        <h2
          id="novelSyncErpApp.warehouse.home.createOrEditLabel"
          data-cy="WarehouseCreateUpdateHeading"
          v-text="$t('novelSyncErpApp.warehouse.home.createOrEditLabel')"
        >
          Create or edit a Warehouse
        </h2>
        <div>
          <div class="form-group" v-if="warehouse.id">
            <label for="id" v-text="$t('global.field.id')">ID</label>
            <input type="text" class="form-control" id="id" name="id" v-model="warehouse.id" readonly />
          </div>
          <div class="form-group">
            <label class="form-control-label" v-text="$t('novelSyncErpApp.warehouse.code')" for="warehouse-code">Code</label>
            <input
              type="text"
              class="form-control"
              name="code"
              id="warehouse-code"
              data-cy="code"
              :class="{ valid: !$v.warehouse.code.$invalid, invalid: $v.warehouse.code.$invalid }"
              v-model="$v.warehouse.code.$model"
              required
            />
            <div v-if="$v.warehouse.code.$anyDirty && $v.warehouse.code.$invalid">
              <small class="form-text text-danger" v-if="!$v.warehouse.code.required" v-text="$t('entity.validation.required')">
                This field is required.
              </small>
            </div>
          </div>
          <div class="form-group">
            <label class="form-control-label" v-text="$t('novelSyncErpApp.warehouse.name')" for="warehouse-name">Name</label>
            <input
              type="text"
              class="form-control"
              name="name"
              id="warehouse-name"
              data-cy="name"
              :class="{ valid: !$v.warehouse.name.$invalid, invalid: $v.warehouse.name.$invalid }"
              v-model="$v.warehouse.name.$model"
              required
            />
            <div v-if="$v.warehouse.name.$anyDirty && $v.warehouse.name.$invalid">
              <small class="form-text text-danger" v-if="!$v.warehouse.name.required" v-text="$t('entity.validation.required')">
                This field is required.
              </small>
            </div>
          </div>
          <div class="form-group">
            <label class="form-control-label" v-text="$t('novelSyncErpApp.warehouse.address')" for="warehouse-address">Address</label>
            <input
              type="text"
              class="form-control"
              name="address"
              id="warehouse-address"
              data-cy="address"
              :class="{ valid: !$v.warehouse.address.$invalid, invalid: $v.warehouse.address.$invalid }"
              v-model="$v.warehouse.address.$model"
            />
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
            :disabled="$v.warehouse.$invalid || isSaving"
            class="btn btn-primary"
          >
            <font-awesome-icon icon="save"></font-awesome-icon>&nbsp;<span v-text="$t('entity.action.save')">Save</span>
          </button>
        </div>
      </form>
    </div>
  </div>
</template>
<script lang="ts" src="./warehouse-update.component.ts"></script>
