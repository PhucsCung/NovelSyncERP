<template>
  <div class="row justify-content-center">
    <div class="col-8">
      <div v-if="product">
        <h2 class="jh-entity-heading" data-cy="productDetailsHeading">
          <span v-text="$t('novelSyncErpApp.product.detail.title')">Product</span> {{ product.id }}
        </h2>
        <dl class="row jh-entity-details">
          <dt>
            <span v-text="$t('novelSyncErpApp.product.sku')">Sku</span>
          </dt>
          <dd>
            <span>{{ product.sku }}</span>
          </dd>
          <dt>
            <span v-text="$t('novelSyncErpApp.product.name')">Name</span>
          </dt>
          <dd>
            <span>{{ product.name }}</span>
          </dd>
          <dt>
            <span v-text="$t('novelSyncErpApp.product.basePrice')">Base Price</span>
          </dt>
          <dd>
            <span>{{ product.basePrice }}</span>
          </dd>
          <dt>
            <span v-text="$t('novelSyncErpApp.product.attributes')">Attributes</span>
          </dt>
          <dd>
            <span>{{ product.attributes }}</span>
          </dd>
          <dt>
            <span v-text="$t('novelSyncErpApp.product.category')">Category</span>
          </dt>
          <dd>
            <div v-if="product.category">
              <router-link :to="{ name: 'CategoryView', params: { categoryId: product.category.id } }">{{
                product.category.name
              }}</router-link>
            </div>
          </dd>
        </dl>
        <button type="submit" v-on:click.prevent="previousState()" class="btn btn-info" data-cy="entityDetailsBackButton">
          <font-awesome-icon icon="arrow-left"></font-awesome-icon>&nbsp;<span v-text="$t('entity.action.back')"> Back</span>
        </button>
        <router-link v-if="product.id" :to="{ name: 'ProductEdit', params: { productId: product.id } }" custom v-slot="{ navigate }">
          <button @click="navigate" class="btn btn-primary">
            <font-awesome-icon icon="pencil-alt"></font-awesome-icon>&nbsp;<span v-text="$t('entity.action.edit')"> Edit</span>
          </button>
        </router-link>
      </div>
    </div>
  </div>
</template>

<script lang="ts" src="./product-details.component.ts"></script>
