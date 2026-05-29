<template>
  <div class="row justify-content-center">
    <div class="col-8">
      <form name="editForm" role="form" novalidate v-on:submit.prevent="save()">
        <h2
          id="novelSyncErpApp.employee.home.createOrEditLabel"
          data-cy="EmployeeCreateUpdateHeading"
          v-text="$t('novelSyncErpApp.employee.home.createOrEditLabel')"
        >
          Create or edit a Employee
        </h2>
        <div>
          <div class="form-group" v-if="employee.id">
            <label for="id" v-text="$t('global.field.id')">ID</label>
            <input type="text" class="form-control" id="id" name="id" v-model="employee.id" readonly />
          </div>
          <div class="form-group">
            <label class="form-control-label" v-text="$t('novelSyncErpApp.employee.fullName')" for="employee-fullName">Full Name</label>
            <input
              type="text"
              class="form-control"
              name="fullName"
              id="employee-fullName"
              data-cy="fullName"
              :class="{ valid: !$v.employee.fullName.$invalid, invalid: $v.employee.fullName.$invalid }"
              v-model="$v.employee.fullName.$model"
              required
            />
            <div v-if="$v.employee.fullName.$anyDirty && $v.employee.fullName.$invalid">
              <small class="form-text text-danger" v-if="!$v.employee.fullName.required" v-text="$t('entity.validation.required')">
                This field is required.
              </small>
            </div>
          </div>
          <div class="form-group">
            <label class="form-control-label" v-text="$t('novelSyncErpApp.employee.phone')" for="employee-phone">Phone</label>
            <input
              type="text"
              class="form-control"
              name="phone"
              id="employee-phone"
              data-cy="phone"
              :class="{ valid: !$v.employee.phone.$invalid, invalid: $v.employee.phone.$invalid }"
              v-model="$v.employee.phone.$model"
            />
          </div>
          <div class="form-group">
            <label class="form-control-label" v-text="$t('novelSyncErpApp.employee.user')" for="employee-user">User</label>
            <select class="form-control" id="employee-user" data-cy="user" name="user" v-model="employee.user">
              <option v-bind:value="null"></option>
              <option
                v-bind:value="employee.user && userOption.id === employee.user.id ? employee.user : userOption"
                v-for="userOption in users"
                :key="userOption.id"
              >
                {{ userOption.login }}
              </option>
            </select>
          </div>
          <div class="form-group">
            <label class="form-control-label" v-text="$t('novelSyncErpApp.employee.manager')" for="employee-manager">Manager</label>
            <select class="form-control" id="employee-manager" data-cy="manager" name="manager" v-model="employee.manager">
              <option v-bind:value="null"></option>
              <option
                v-bind:value="employee.manager && employeeOption.id === employee.manager.id ? employee.manager : employeeOption"
                v-for="employeeOption in employees"
                :key="employeeOption.id"
              >
                {{ employeeOption.fullName }}
              </option>
            </select>
          </div>
          <div class="form-group">
            <label class="form-control-label" v-text="$t('novelSyncErpApp.employee.scopedWarehouse')" for="employee-scopedWarehouse"
              >Scoped Warehouse</label
            >
            <select
              class="form-control"
              id="employee-scopedWarehouse"
              data-cy="scopedWarehouse"
              name="scopedWarehouse"
              v-model="employee.scopedWarehouse"
            >
              <option v-bind:value="null"></option>
              <option
                v-bind:value="
                  employee.scopedWarehouse && warehouseOption.id === employee.scopedWarehouse.id
                    ? employee.scopedWarehouse
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
            <label class="form-control-label" v-text="$t('novelSyncErpApp.employee.department')" for="employee-department"
              >Department</label
            >
            <select class="form-control" id="employee-department" data-cy="department" name="department" v-model="employee.department">
              <option v-bind:value="null"></option>
              <option
                v-bind:value="
                  employee.department && departmentOption.id === employee.department.id ? employee.department : departmentOption
                "
                v-for="departmentOption in departments"
                :key="departmentOption.id"
              >
                {{ departmentOption.id }}
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
            :disabled="$v.employee.$invalid || isSaving"
            class="btn btn-primary"
          >
            <font-awesome-icon icon="save"></font-awesome-icon>&nbsp;<span v-text="$t('entity.action.save')">Save</span>
          </button>
        </div>
      </form>
    </div>
  </div>
</template>
<script lang="ts" src="./employee-update.component.ts"></script>
