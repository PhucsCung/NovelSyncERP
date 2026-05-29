<template>
  <div>
    <h2 id="page-heading" data-cy="SupplierHeading">
      <span v-text="$t('novelSyncErpApp.supplier.home.title')" id="supplier-heading">Suppliers</span>
      <div class="d-flex justify-content-end">
        <button class="btn btn-info mr-2" v-on:click="handleSyncList" :disabled="isFetching">
          <font-awesome-icon icon="sync" :spin="isFetching"></font-awesome-icon>
          <span v-text="$t('novelSyncErpApp.supplier.home.refreshListLabel')">Refresh List</span>
        </button>
        <router-link :to="{ name: 'SupplierCreate' }" custom v-slot="{ navigate }">
          <button
            @click="navigate"
            id="jh-create-entity"
            data-cy="entityCreateButton"
            class="btn btn-primary jh-create-entity create-supplier"
          >
            <font-awesome-icon icon="plus"></font-awesome-icon>
            <span v-text="$t('novelSyncErpApp.supplier.home.createLabel')"> Create a new Supplier </span>
          </button>
        </router-link>
      </div>
    </h2>
    <br />
    <div class="alert alert-warning" v-if="!isFetching && suppliers && suppliers.length === 0">
      <span v-text="$t('novelSyncErpApp.supplier.home.notFound')">No suppliers found</span>
    </div>
    <div class="table-responsive" v-if="suppliers && suppliers.length > 0">
      <table class="table table-striped" aria-describedby="suppliers">
        <thead>
          <tr>
            <th scope="row" v-on:click="changeOrder('id')">
              <span v-text="$t('global.field.id')">ID</span>
              <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" :field-name="'id'"></jhi-sort-indicator>
            </th>
            <th scope="row" v-on:click="changeOrder('code')">
              <span v-text="$t('novelSyncErpApp.supplier.code')">Code</span>
              <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" :field-name="'code'"></jhi-sort-indicator>
            </th>
            <th scope="row" v-on:click="changeOrder('name')">
              <span v-text="$t('novelSyncErpApp.supplier.name')">Name</span>
              <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" :field-name="'name'"></jhi-sort-indicator>
            </th>
            <th scope="row" v-on:click="changeOrder('phone')">
              <span v-text="$t('novelSyncErpApp.supplier.phone')">Phone</span>
              <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" :field-name="'phone'"></jhi-sort-indicator>
            </th>
            <th scope="row" v-on:click="changeOrder('currentDebt')">
              <span v-text="$t('novelSyncErpApp.supplier.currentDebt')">Current Debt</span>
              <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" :field-name="'currentDebt'"></jhi-sort-indicator>
            </th>
            <th scope="row"></th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="supplier in suppliers" :key="supplier.id" data-cy="entityTable">
            <td>
              <router-link :to="{ name: 'SupplierView', params: { supplierId: supplier.id } }">{{ supplier.id }}</router-link>
            </td>
            <td>{{ supplier.code }}</td>
            <td>{{ supplier.name }}</td>
            <td>{{ supplier.phone }}</td>
            <td>{{ supplier.currentDebt }}</td>
            <td class="text-right">
              <div class="btn-group">
                <router-link :to="{ name: 'SupplierView', params: { supplierId: supplier.id } }" custom v-slot="{ navigate }">
                  <button @click="navigate" class="btn btn-info btn-sm details" data-cy="entityDetailsButton">
                    <font-awesome-icon icon="eye"></font-awesome-icon>
                    <span class="d-none d-md-inline" v-text="$t('entity.action.view')">View</span>
                  </button>
                </router-link>
                <router-link :to="{ name: 'SupplierEdit', params: { supplierId: supplier.id } }" custom v-slot="{ navigate }">
                  <button @click="navigate" class="btn btn-primary btn-sm edit" data-cy="entityEditButton">
                    <font-awesome-icon icon="pencil-alt"></font-awesome-icon>
                    <span class="d-none d-md-inline" v-text="$t('entity.action.edit')">Edit</span>
                  </button>
                </router-link>
                <b-button
                  v-on:click="prepareRemove(supplier)"
                  variant="danger"
                  class="btn btn-sm"
                  data-cy="entityDeleteButton"
                  v-b-modal.removeEntity
                >
                  <font-awesome-icon icon="times"></font-awesome-icon>
                  <span class="d-none d-md-inline" v-text="$t('entity.action.delete')">Delete</span>
                </b-button>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
    <b-modal ref="removeEntity" id="removeEntity">
      <span slot="modal-title"
        ><span id="novelSyncErpApp.supplier.delete.question" data-cy="supplierDeleteDialogHeading" v-text="$t('entity.delete.title')"
          >Confirm delete operation</span
        ></span
      >
      <div class="modal-body">
        <p id="jhi-delete-supplier-heading" v-text="$t('novelSyncErpApp.supplier.delete.question', { id: removeId })">
          Are you sure you want to delete this Supplier?
        </p>
      </div>
      <div slot="modal-footer">
        <button type="button" class="btn btn-secondary" v-text="$t('entity.action.cancel')" v-on:click="closeDialog()">Cancel</button>
        <button
          type="button"
          class="btn btn-primary"
          id="jhi-confirm-delete-supplier"
          data-cy="entityConfirmDeleteButton"
          v-text="$t('entity.action.delete')"
          v-on:click="removeSupplier()"
        >
          Delete
        </button>
      </div>
    </b-modal>
    <div v-show="suppliers && suppliers.length > 0">
      <div class="row justify-content-center">
        <jhi-item-count :page="page" :total="queryCount" :itemsPerPage="itemsPerPage"></jhi-item-count>
      </div>
      <div class="row justify-content-center">
        <b-pagination size="md" :total-rows="totalItems" v-model="page" :per-page="itemsPerPage" :change="loadPage(page)"></b-pagination>
      </div>
    </div>
  </div>
</template>

<script lang="ts" src="./supplier.component.ts"></script>
