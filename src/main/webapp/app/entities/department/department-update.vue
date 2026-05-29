<template>
  <div class="row justify-content-center">
    <div class="col-8">
      <form name="editForm" role="form" novalidate v-on:submit.prevent="save()">
        <h2
          id="novelSyncErpApp.department.home.createOrEditLabel"
          data-cy="DepartmentCreateUpdateHeading"
          v-text="$t('novelSyncErpApp.department.home.createOrEditLabel')"
        >
          Create or edit a Department
        </h2>
        <div>
          <div class="form-group" v-if="department.id">
            <label for="id" v-text="$t('global.field.id')">ID</label>
            <input type="text" class="form-control" id="id" name="id" v-model="department.id" readonly />
          </div>
          <div class="form-group">
            <label class="form-control-label" v-text="$t('novelSyncErpApp.department.code')" for="department-code">Code</label>
            <input
              type="text"
              class="form-control"
              name="code"
              id="department-code"
              data-cy="code"
              :class="{ valid: !$v.department.code.$invalid, invalid: $v.department.code.$invalid }"
              v-model="$v.department.code.$model"
              required
            />
            <div v-if="$v.department.code.$anyDirty && $v.department.code.$invalid">
              <small class="form-text text-danger" v-if="!$v.department.code.required" v-text="$t('entity.validation.required')">
                This field is required.
              </small>
            </div>
          </div>
          <div class="form-group">
            <label class="form-control-label" v-text="$t('novelSyncErpApp.department.name')" for="department-name">Name</label>
            <input
              type="text"
              class="form-control"
              name="name"
              id="department-name"
              data-cy="name"
              :class="{ valid: !$v.department.name.$invalid, invalid: $v.department.name.$invalid }"
              v-model="$v.department.name.$model"
              required
            />
            <div v-if="$v.department.name.$anyDirty && $v.department.name.$invalid">
              <small class="form-text text-danger" v-if="!$v.department.name.required" v-text="$t('entity.validation.required')">
                This field is required.
              </small>
            </div>
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
            :disabled="$v.department.$invalid || isSaving"
            class="btn btn-primary"
          >
            <font-awesome-icon icon="save"></font-awesome-icon>&nbsp;<span v-text="$t('entity.action.save')">Save</span>
          </button>
        </div>
      </form>
    </div>
  </div>
</template>
<script lang="ts" src="./department-update.component.ts"></script>
