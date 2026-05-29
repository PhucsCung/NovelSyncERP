<template>
  <div class="row justify-content-center">
    <div class="col-8">
      <div v-if="supplier">
        <h2 class="jh-entity-heading" data-cy="supplierDetailsHeading">
          <span v-text="$t('novelSyncErpApp.supplier.detail.title')">Supplier</span> {{ supplier.id }}
        </h2>
        <dl class="row jh-entity-details">
          <dt>
            <span v-text="$t('novelSyncErpApp.supplier.code')">Code</span>
          </dt>
          <dd>
            <span>{{ supplier.code }}</span>
          </dd>
          <dt>
            <span v-text="$t('novelSyncErpApp.supplier.name')">Name</span>
          </dt>
          <dd>
            <span>{{ supplier.name }}</span>
          </dd>
          <dt>
            <span v-text="$t('novelSyncErpApp.supplier.phone')">Phone</span>
          </dt>
          <dd>
            <span>{{ supplier.phone }}</span>
          </dd>
          <dt>
            <span v-text="$t('novelSyncErpApp.supplier.currentDebt')">Current Debt</span>
          </dt>
          <dd>
            <span>{{ supplier.currentDebt }}</span>
          </dd>
        </dl>
        <button type="submit" v-on:click.prevent="previousState()" class="btn btn-info" data-cy="entityDetailsBackButton">
          <font-awesome-icon icon="arrow-left"></font-awesome-icon>&nbsp;<span v-text="$t('entity.action.back')"> Back</span>
        </button>
        <router-link v-if="supplier.id" :to="{ name: 'SupplierEdit', params: { supplierId: supplier.id } }" custom v-slot="{ navigate }">
          <button @click="navigate" class="btn btn-primary">
            <font-awesome-icon icon="pencil-alt"></font-awesome-icon>&nbsp;<span v-text="$t('entity.action.edit')"> Edit</span>
          </button>
        </router-link>
      </div>
    </div>
  </div>
</template>

<script lang="ts" src="./supplier-details.component.ts"></script>
