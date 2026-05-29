<template>
  <div class="row justify-content-center">
    <div class="col-8">
      <form name="editForm" role="form" novalidate v-on:submit.prevent="save()">
        <h2
          id="novelSyncErpApp.product.home.createOrEditLabel"
          data-cy="ProductCreateUpdateHeading"
          v-text="$t('novelSyncErpApp.product.home.createOrEditLabel')"
        >
          Create or edit a Product
        </h2>
        <div>
          <div class="form-group" v-if="product.id">
            <label for="id" v-text="$t('global.field.id')">ID</label>
            <input type="text" class="form-control" id="id" name="id" v-model="product.id" readonly />
          </div>
          <div class="form-group">
            <label class="form-control-label" v-text="$t('novelSyncErpApp.product.sku')" for="product-sku">Sku</label>
            <input
              type="text"
              class="form-control"
              name="sku"
              id="product-sku"
              data-cy="sku"
              :class="{ valid: !$v.product.sku.$invalid, invalid: $v.product.sku.$invalid }"
              v-model="$v.product.sku.$model"
              required
            />
            <div v-if="$v.product.sku.$anyDirty && $v.product.sku.$invalid">
              <small class="form-text text-danger" v-if="!$v.product.sku.required" v-text="$t('entity.validation.required')">
                This field is required.
              </small>
            </div>
          </div>
          <div class="form-group">
            <label class="form-control-label" v-text="$t('novelSyncErpApp.product.name')" for="product-name">Name</label>
            <input
              type="text"
              class="form-control"
              name="name"
              id="product-name"
              data-cy="name"
              :class="{ valid: !$v.product.name.$invalid, invalid: $v.product.name.$invalid }"
              v-model="$v.product.name.$model"
              required
            />
            <div v-if="$v.product.name.$anyDirty && $v.product.name.$invalid">
              <small class="form-text text-danger" v-if="!$v.product.name.required" v-text="$t('entity.validation.required')">
                This field is required.
              </small>
            </div>
          </div>
          <div class="form-group">
            <label class="form-control-label" v-text="$t('novelSyncErpApp.product.basePrice')" for="product-basePrice">Base Price</label>
            <input
              type="number"
              class="form-control"
              name="basePrice"
              id="product-basePrice"
              data-cy="basePrice"
              :class="{ valid: !$v.product.basePrice.$invalid, invalid: $v.product.basePrice.$invalid }"
              v-model.number="$v.product.basePrice.$model"
              required
            />
            <div v-if="$v.product.basePrice.$anyDirty && $v.product.basePrice.$invalid">
              <small class="form-text text-danger" v-if="!$v.product.basePrice.required" v-text="$t('entity.validation.required')">
                This field is required.
              </small>
              <small class="form-text text-danger" v-if="!$v.product.basePrice.numeric" v-text="$t('entity.validation.number')">
                This field should be a number.
              </small>
            </div>
          </div>
          <div class="form-group">
            <label class="form-control-label" v-text="$t('novelSyncErpApp.product.attributes')" for="product-attributes">Attributes</label>
            <textarea
              class="form-control"
              name="attributes"
              id="product-attributes"
              data-cy="attributes"
              :class="{ valid: !$v.product.attributes.$invalid, invalid: $v.product.attributes.$invalid }"
              v-model="$v.product.attributes.$model"
            ></textarea>
          </div>
          <div class="form-group">
            <label class="form-control-label" v-text="$t('novelSyncErpApp.product.category')" for="product-category">Category</label>
            <select class="form-control" id="product-category" data-cy="category" name="category" v-model="product.category">
              <option v-bind:value="null"></option>
              <option
                v-bind:value="product.category && categoryOption.id === product.category.id ? product.category : categoryOption"
                v-for="categoryOption in categories"
                :key="categoryOption.id"
              >
                {{ categoryOption.name }}
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
            :disabled="$v.product.$invalid || isSaving"
            class="btn btn-primary"
          >
            <font-awesome-icon icon="save"></font-awesome-icon>&nbsp;<span v-text="$t('entity.action.save')">Save</span>
          </button>
        </div>
      </form>
    </div>
  </div>
</template>
<script lang="ts" src="./product-update.component.ts"></script>
