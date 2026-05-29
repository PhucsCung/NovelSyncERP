<template>
  <div class="row justify-content-center">
    <div class="col-8">
      <form name="editForm" role="form" novalidate v-on:submit.prevent="save()">
        <h2
          id="novelSyncErpApp.payment.home.createOrEditLabel"
          data-cy="PaymentCreateUpdateHeading"
          v-text="$t('novelSyncErpApp.payment.home.createOrEditLabel')"
        >
          Create or edit a Payment
        </h2>
        <div>
          <div class="form-group" v-if="payment.id">
            <label for="id" v-text="$t('global.field.id')">ID</label>
            <input type="text" class="form-control" id="id" name="id" v-model="payment.id" readonly />
          </div>
          <div class="form-group">
            <label class="form-control-label" v-text="$t('novelSyncErpApp.payment.paymentCode')" for="payment-paymentCode"
              >Payment Code</label
            >
            <input
              type="text"
              class="form-control"
              name="paymentCode"
              id="payment-paymentCode"
              data-cy="paymentCode"
              :class="{ valid: !$v.payment.paymentCode.$invalid, invalid: $v.payment.paymentCode.$invalid }"
              v-model="$v.payment.paymentCode.$model"
              required
            />
            <div v-if="$v.payment.paymentCode.$anyDirty && $v.payment.paymentCode.$invalid">
              <small class="form-text text-danger" v-if="!$v.payment.paymentCode.required" v-text="$t('entity.validation.required')">
                This field is required.
              </small>
            </div>
          </div>
          <div class="form-group">
            <label class="form-control-label" v-text="$t('novelSyncErpApp.payment.type')" for="payment-type">Type</label>
            <select
              class="form-control"
              name="type"
              :class="{ valid: !$v.payment.type.$invalid, invalid: $v.payment.type.$invalid }"
              v-model="$v.payment.type.$model"
              id="payment-type"
              data-cy="type"
              required
            >
              <option
                v-for="paymentType in paymentTypeValues"
                :key="paymentType"
                v-bind:value="paymentType"
                v-bind:label="$t('novelSyncErpApp.PaymentType.' + paymentType)"
              >
                {{ paymentType }}
              </option>
            </select>
            <div v-if="$v.payment.type.$anyDirty && $v.payment.type.$invalid">
              <small class="form-text text-danger" v-if="!$v.payment.type.required" v-text="$t('entity.validation.required')">
                This field is required.
              </small>
            </div>
          </div>
          <div class="form-group">
            <label class="form-control-label" v-text="$t('novelSyncErpApp.payment.amount')" for="payment-amount">Amount</label>
            <input
              type="number"
              class="form-control"
              name="amount"
              id="payment-amount"
              data-cy="amount"
              :class="{ valid: !$v.payment.amount.$invalid, invalid: $v.payment.amount.$invalid }"
              v-model.number="$v.payment.amount.$model"
              required
            />
            <div v-if="$v.payment.amount.$anyDirty && $v.payment.amount.$invalid">
              <small class="form-text text-danger" v-if="!$v.payment.amount.required" v-text="$t('entity.validation.required')">
                This field is required.
              </small>
              <small class="form-text text-danger" v-if="!$v.payment.amount.numeric" v-text="$t('entity.validation.number')">
                This field should be a number.
              </small>
            </div>
          </div>
          <div class="form-group">
            <label class="form-control-label" v-text="$t('novelSyncErpApp.payment.referenceOrderId')" for="payment-referenceOrderId"
              >Reference Order Id</label
            >
            <input
              type="number"
              class="form-control"
              name="referenceOrderId"
              id="payment-referenceOrderId"
              data-cy="referenceOrderId"
              :class="{ valid: !$v.payment.referenceOrderId.$invalid, invalid: $v.payment.referenceOrderId.$invalid }"
              v-model.number="$v.payment.referenceOrderId.$model"
            />
          </div>
          <div class="form-group">
            <label class="form-control-label" v-text="$t('novelSyncErpApp.payment.createdAt')" for="payment-createdAt">Created At</label>
            <div class="d-flex">
              <input
                id="payment-createdAt"
                data-cy="createdAt"
                type="datetime-local"
                class="form-control"
                name="createdAt"
                :class="{ valid: !$v.payment.createdAt.$invalid, invalid: $v.payment.createdAt.$invalid }"
                required
                :value="convertDateTimeFromServer($v.payment.createdAt.$model)"
                @change="updateInstantField('createdAt', $event)"
              />
            </div>
            <div v-if="$v.payment.createdAt.$anyDirty && $v.payment.createdAt.$invalid">
              <small class="form-text text-danger" v-if="!$v.payment.createdAt.required" v-text="$t('entity.validation.required')">
                This field is required.
              </small>
              <small
                class="form-text text-danger"
                v-if="!$v.payment.createdAt.ZonedDateTimelocal"
                v-text="$t('entity.validation.ZonedDateTimelocal')"
              >
                This field should be a date and time.
              </small>
            </div>
          </div>
          <div class="form-group">
            <label class="form-control-label" v-text="$t('novelSyncErpApp.payment.customer')" for="payment-customer">Customer</label>
            <select class="form-control" id="payment-customer" data-cy="customer" name="customer" v-model="payment.customer">
              <option v-bind:value="null"></option>
              <option
                v-bind:value="payment.customer && customerOption.id === payment.customer.id ? payment.customer : customerOption"
                v-for="customerOption in customers"
                :key="customerOption.id"
              >
                {{ customerOption.name }}
              </option>
            </select>
          </div>
          <div class="form-group">
            <label class="form-control-label" v-text="$t('novelSyncErpApp.payment.supplier')" for="payment-supplier">Supplier</label>
            <select class="form-control" id="payment-supplier" data-cy="supplier" name="supplier" v-model="payment.supplier">
              <option v-bind:value="null"></option>
              <option
                v-bind:value="payment.supplier && supplierOption.id === payment.supplier.id ? payment.supplier : supplierOption"
                v-for="supplierOption in suppliers"
                :key="supplierOption.id"
              >
                {{ supplierOption.name }}
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
            :disabled="$v.payment.$invalid || isSaving"
            class="btn btn-primary"
          >
            <font-awesome-icon icon="save"></font-awesome-icon>&nbsp;<span v-text="$t('entity.action.save')">Save</span>
          </button>
        </div>
      </form>
    </div>
  </div>
</template>
<script lang="ts" src="./payment-update.component.ts"></script>
