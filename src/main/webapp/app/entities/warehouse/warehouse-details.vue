<template>
  <div class="row justify-content-center">
    <div class="col-8">
      <div v-if="warehouse">
        <h2 class="jh-entity-heading" data-cy="warehouseDetailsHeading">
          <span v-text="$t('novelSyncErpApp.warehouse.detail.title')">Warehouse</span> {{ warehouse.id }}
        </h2>
        <dl class="row jh-entity-details">
          <dt>
            <span v-text="$t('novelSyncErpApp.warehouse.code')">Code</span>
          </dt>
          <dd>
            <span>{{ warehouse.code }}</span>
          </dd>
          <dt>
            <span v-text="$t('novelSyncErpApp.warehouse.name')">Name</span>
          </dt>
          <dd>
            <span>{{ warehouse.name }}</span>
          </dd>
          <dt>
            <span v-text="$t('novelSyncErpApp.warehouse.address')">Address</span>
          </dt>
          <dd>
            <span>{{ warehouse.address }}</span>
          </dd>
        </dl>
        <button type="submit" v-on:click.prevent="previousState()" class="btn btn-info" data-cy="entityDetailsBackButton">
          <font-awesome-icon icon="arrow-left"></font-awesome-icon>&nbsp;<span v-text="$t('entity.action.back')"> Back</span>
        </button>
        <router-link
          v-if="warehouse.id"
          :to="{ name: 'WarehouseEdit', params: { warehouseId: warehouse.id } }"
          custom
          v-slot="{ navigate }"
        >
          <button @click="navigate" class="btn btn-primary">
            <font-awesome-icon icon="pencil-alt"></font-awesome-icon>&nbsp;<span v-text="$t('entity.action.edit')"> Edit</span>
          </button>
        </router-link>
      </div>
    </div>
  </div>
</template>

<script lang="ts" src="./warehouse-details.component.ts"></script>
