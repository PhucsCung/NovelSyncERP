<template>
  <div class="row justify-content-center">
    <div class="col-8">
      <form name="editForm" role="form" novalidate v-on:submit.prevent="save()">
        <h2
          id="novelSyncErpApp.supplier.home.createOrEditLabel"
          data-cy="SupplierCreateUpdateHeading"
          v-text="$t('novelSyncErpApp.supplier.home.createOrEditLabel')"
        >
          Create or edit a Supplier
        </h2>
        <div>
          <div class="form-group" v-if="supplier.id">
            <label for="id" v-text="$t('global.field.id')">ID</label>
            <input type="text" class="form-control" id="id" name="id" v-model="supplier.id" readonly />
          </div>
          <div class="form-group">
            <label class="form-control-label" v-text="$t('novelSyncErpApp.supplier.code')" for="supplier-code">Code</label>
            <input
              type="text"
              class="form-control"
              name="code"
              id="supplier-code"
              data-cy="code"
              :class="{ valid: !$v.supplier.code.$invalid, invalid: $v.supplier.code.$invalid }"
              v-model="$v.supplier.code.$model"
              required
            />
            <div v-if="$v.supplier.code.$anyDirty && $v.supplier.code.$invalid">
              <small class="form-text text-danger" v-if="!$v.supplier.code.required" v-text="$t('entity.validation.required')">
                This field is required.
              </small>
            </div>
          </div>
          <div class="form-group">
            <label class="form-control-label" v-text="$t('novelSyncErpApp.supplier.name')" for="supplier-name">Name</label>
            <input
              type="text"
              class="form-control"
              name="name"
              id="supplier-name"
              data-cy="name"
              :class="{ valid: !$v.supplier.name.$invalid, invalid: $v.supplier.name.$invalid }"
              v-model="$v.supplier.name.$model"
              required
            />
            <div v-if="$v.supplier.name.$anyDirty && $v.supplier.name.$invalid">
              <small class="form-text text-danger" v-if="!$v.supplier.name.required" v-text="$t('entity.validation.required')">
                This field is required.
              </small>
            </div>
          </div>
          <div class="form-group">
            <label class="form-control-label" v-text="$t('novelSyncErpApp.supplier.phone')" for="supplier-phone">Phone</label>
            <input
              type="text"
              class="form-control"
              name="phone"
              id="supplier-phone"
              data-cy="phone"
              :class="{ valid: !$v.supplier.phone.$invalid, invalid: $v.supplier.phone.$invalid }"
              v-model="$v.supplier.phone.$model"
            />
          </div>
          <div class="form-group">
            <label class="form-control-label" v-text="$t('novelSyncErpApp.supplier.currentDebt')" for="supplier-currentDebt"
              >Current Debt</label
            >
            <input
              type="number"
              class="form-control"
              name="currentDebt"
              id="supplier-currentDebt"
              data-cy="currentDebt"
              :class="{ valid: !$v.supplier.currentDebt.$invalid, invalid: $v.supplier.currentDebt.$invalid }"
              v-model.number="$v.supplier.currentDebt.$model"
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
            :disabled="$v.supplier.$invalid || isSaving"
            class="btn btn-primary"
          >
            <font-awesome-icon icon="save"></font-awesome-icon>&nbsp;<span v-text="$t('entity.action.save')">Save</span>
          </button>
        </div>
      </form>
    </div>
  </div>
</template>
<script lang="ts" src="./supplier-update.component.ts"></script>
