<template>
  <div class="row justify-content-center">
    <div class="col-8">
      <div v-if="employee">
        <h2 class="jh-entity-heading" data-cy="employeeDetailsHeading">
          <span v-text="$t('novelSyncErpApp.employee.detail.title')">Employee</span> {{ employee.id }}
        </h2>
        <dl class="row jh-entity-details">
          <dt>
            <span v-text="$t('novelSyncErpApp.employee.fullName')">Full Name</span>
          </dt>
          <dd>
            <span>{{ employee.fullName }}</span>
          </dd>
          <dt>
            <span v-text="$t('novelSyncErpApp.employee.phone')">Phone</span>
          </dt>
          <dd>
            <span>{{ employee.phone }}</span>
          </dd>
          <dt>
            <span v-text="$t('novelSyncErpApp.employee.user')">User</span>
          </dt>
          <dd>
            {{ employee.user ? employee.user.login : '' }}
          </dd>
          <dt>
            <span v-text="$t('novelSyncErpApp.employee.manager')">Manager</span>
          </dt>
          <dd>
            <div v-if="employee.manager">
              <router-link :to="{ name: 'EmployeeView', params: { employeeId: employee.manager.id } }">{{
                employee.manager.fullName
              }}</router-link>
            </div>
          </dd>
          <dt>
            <span v-text="$t('novelSyncErpApp.employee.scopedWarehouse')">Scoped Warehouse</span>
          </dt>
          <dd>
            <div v-if="employee.scopedWarehouse">
              <router-link :to="{ name: 'WarehouseView', params: { warehouseId: employee.scopedWarehouse.id } }">{{
                employee.scopedWarehouse.name
              }}</router-link>
            </div>
          </dd>
          <dt>
            <span v-text="$t('novelSyncErpApp.employee.department')">Department</span>
          </dt>
          <dd>
            <div v-if="employee.department">
              <router-link :to="{ name: 'DepartmentView', params: { departmentId: employee.department.id } }">{{
                employee.department.id
              }}</router-link>
            </div>
          </dd>
        </dl>
        <button type="submit" v-on:click.prevent="previousState()" class="btn btn-info" data-cy="entityDetailsBackButton">
          <font-awesome-icon icon="arrow-left"></font-awesome-icon>&nbsp;<span v-text="$t('entity.action.back')"> Back</span>
        </button>
        <router-link v-if="employee.id" :to="{ name: 'EmployeeEdit', params: { employeeId: employee.id } }" custom v-slot="{ navigate }">
          <button @click="navigate" class="btn btn-primary">
            <font-awesome-icon icon="pencil-alt"></font-awesome-icon>&nbsp;<span v-text="$t('entity.action.edit')"> Edit</span>
          </button>
        </router-link>
      </div>
    </div>
  </div>
</template>

<script lang="ts" src="./employee-details.component.ts"></script>
