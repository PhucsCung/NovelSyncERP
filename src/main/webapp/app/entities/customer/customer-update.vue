<template>
  <div class="row justify-content-center">
    <div class="col-8">
      <form name="editForm" role="form" novalidate v-on:submit.prevent="save()">
        <h2
          id="novelSyncErpApp.customer.home.createOrEditLabel"
          data-cy="CustomerCreateUpdateHeading"
          v-text="$t('novelSyncErpApp.customer.home.createOrEditLabel')"
        >
          Create or edit a Customer
        </h2>
        <div>
          <div class="form-group" v-if="customer.id">
            <label for="id" v-text="$t('global.field.id')">ID</label>
            <input type="text" class="form-control" id="id" name="id" v-model="customer.id" readonly />
          </div>
          <div class="form-group">
            <label class="form-control-label" v-text="$t('novelSyncErpApp.customer.code')" for="customer-code">Code</label>
            <input
              type="text"
              class="form-control"
              name="code"
              id="customer-code"
              data-cy="code"
              :class="{ valid: !$v.customer.code.$invalid, invalid: $v.customer.code.$invalid }"
              v-model="$v.customer.code.$model"
              required
            />
            <div v-if="$v.customer.code.$anyDirty && $v.customer.code.$invalid">
              <small class="form-text text-danger" v-if="!$v.customer.code.required" v-text="$t('entity.validation.required')">
                This field is required.
              </small>
            </div>
          </div>
          <div class="form-group">
            <label class="form-control-label" v-text="$t('novelSyncErpApp.customer.name')" for="customer-name">Name</label>
            <input
              type="text"
              class="form-control"
              name="name"
              id="customer-name"
              data-cy="name"
              :class="{ valid: !$v.customer.name.$invalid, invalid: $v.customer.name.$invalid }"
              v-model="$v.customer.name.$model"
              required
            />
            <div v-if="$v.customer.name.$anyDirty && $v.customer.name.$invalid">
              <small class="form-text text-danger" v-if="!$v.customer.name.required" v-text="$t('entity.validation.required')">
                This field is required.
              </small>
            </div>
          </div>
          <div class="form-group">
            <label class="form-control-label" v-text="$t('novelSyncErpApp.customer.phone')" for="customer-phone">Phone</label>
            <input
              type="text"
              class="form-control"
              name="phone"
              id="customer-phone"
              data-cy="phone"
              :class="{ valid: !$v.customer.phone.$invalid, invalid: $v.customer.phone.$invalid }"
              v-model="$v.customer.phone.$model"
            />
          </div>
          <div class="form-group">
            <label class="form-control-label" v-text="$t('novelSyncErpApp.customer.creditLimit')" for="customer-creditLimit"
              >Credit Limit</label
            >
            <input
              type="number"
              class="form-control"
              name="creditLimit"
              id="customer-creditLimit"
              data-cy="creditLimit"
              :class="{ valid: !$v.customer.creditLimit.$invalid, invalid: $v.customer.creditLimit.$invalid }"
              v-model.number="$v.customer.creditLimit.$model"
            />
          </div>
          <div class="form-group">
            <label class="form-control-label" v-text="$t('novelSyncErpApp.customer.currentDebt')" for="customer-currentDebt"
              >Current Debt</label
            >
            <input
              type="number"
              class="form-control"
              name="currentDebt"
              id="customer-currentDebt"
              data-cy="currentDebt"
              :class="{ valid: !$v.customer.currentDebt.$invalid, invalid: $v.customer.currentDebt.$invalid }"
              v-model.number="$v.customer.currentDebt.$model"
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
            :disabled="$v.customer.$invalid || isSaving"
            class="btn btn-primary"
          >
            <font-awesome-icon icon="save"></font-awesome-icon>&nbsp;<span v-text="$t('entity.action.save')">Save</span>
          </button>
        </div>
      </form>
    </div>
  </div>
</template>
<script lang="ts" src="./customer-update.component.ts"></script>
