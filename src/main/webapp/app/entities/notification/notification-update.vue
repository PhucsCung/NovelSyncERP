<template>
  <div class="row justify-content-center">
    <div class="col-8">
      <form name="editForm" role="form" novalidate v-on:submit.prevent="save()">
        <h2
          id="novelSyncErpApp.notification.home.createOrEditLabel"
          data-cy="NotificationCreateUpdateHeading"
          v-text="$t('novelSyncErpApp.notification.home.createOrEditLabel')"
        >
          Create or edit a Notification
        </h2>
        <div>
          <div class="form-group" v-if="notification.id">
            <label for="id" v-text="$t('global.field.id')">ID</label>
            <input type="text" class="form-control" id="id" name="id" v-model="notification.id" readonly />
          </div>
          <div class="form-group">
            <label class="form-control-label" v-text="$t('novelSyncErpApp.notification.title')" for="notification-title">Title</label>
            <input
              type="text"
              class="form-control"
              name="title"
              id="notification-title"
              data-cy="title"
              :class="{ valid: !$v.notification.title.$invalid, invalid: $v.notification.title.$invalid }"
              v-model="$v.notification.title.$model"
              required
            />
            <div v-if="$v.notification.title.$anyDirty && $v.notification.title.$invalid">
              <small class="form-text text-danger" v-if="!$v.notification.title.required" v-text="$t('entity.validation.required')">
                This field is required.
              </small>
            </div>
          </div>
          <div class="form-group">
            <label class="form-control-label" v-text="$t('novelSyncErpApp.notification.message')" for="notification-message">Message</label>
            <input
              type="text"
              class="form-control"
              name="message"
              id="notification-message"
              data-cy="message"
              :class="{ valid: !$v.notification.message.$invalid, invalid: $v.notification.message.$invalid }"
              v-model="$v.notification.message.$model"
              required
            />
            <div v-if="$v.notification.message.$anyDirty && $v.notification.message.$invalid">
              <small class="form-text text-danger" v-if="!$v.notification.message.required" v-text="$t('entity.validation.required')">
                This field is required.
              </small>
              <small
                class="form-text text-danger"
                v-if="!$v.notification.message.maxLength"
                v-text="$t('entity.validation.maxlength', { max: 1000 })"
              >
                This field cannot be longer than 1000 characters.
              </small>
            </div>
          </div>
          <div class="form-group">
            <label class="form-control-label" v-text="$t('novelSyncErpApp.notification.isRead')" for="notification-isRead">Is Read</label>
            <input
              type="checkbox"
              class="form-check"
              name="isRead"
              id="notification-isRead"
              data-cy="isRead"
              :class="{ valid: !$v.notification.isRead.$invalid, invalid: $v.notification.isRead.$invalid }"
              v-model="$v.notification.isRead.$model"
              required
            />
            <div v-if="$v.notification.isRead.$anyDirty && $v.notification.isRead.$invalid">
              <small class="form-text text-danger" v-if="!$v.notification.isRead.required" v-text="$t('entity.validation.required')">
                This field is required.
              </small>
            </div>
          </div>
          <div class="form-group">
            <label class="form-control-label" v-text="$t('novelSyncErpApp.notification.type')" for="notification-type">Type</label>
            <select
              class="form-control"
              name="type"
              :class="{ valid: !$v.notification.type.$invalid, invalid: $v.notification.type.$invalid }"
              v-model="$v.notification.type.$model"
              id="notification-type"
              data-cy="type"
              required
            >
              <option
                v-for="notificationType in notificationTypeValues"
                :key="notificationType"
                v-bind:value="notificationType"
                v-bind:label="$t('novelSyncErpApp.NotificationType.' + notificationType)"
              >
                {{ notificationType }}
              </option>
            </select>
            <div v-if="$v.notification.type.$anyDirty && $v.notification.type.$invalid">
              <small class="form-text text-danger" v-if="!$v.notification.type.required" v-text="$t('entity.validation.required')">
                This field is required.
              </small>
            </div>
          </div>
          <div class="form-group">
            <label class="form-control-label" v-text="$t('novelSyncErpApp.notification.referenceId')" for="notification-referenceId"
              >Reference Id</label
            >
            <input
              type="number"
              class="form-control"
              name="referenceId"
              id="notification-referenceId"
              data-cy="referenceId"
              :class="{ valid: !$v.notification.referenceId.$invalid, invalid: $v.notification.referenceId.$invalid }"
              v-model.number="$v.notification.referenceId.$model"
            />
          </div>
          <div class="form-group">
            <label class="form-control-label" v-text="$t('novelSyncErpApp.notification.createdAt')" for="notification-createdAt"
              >Created At</label
            >
            <div class="d-flex">
              <input
                id="notification-createdAt"
                data-cy="createdAt"
                type="datetime-local"
                class="form-control"
                name="createdAt"
                :class="{ valid: !$v.notification.createdAt.$invalid, invalid: $v.notification.createdAt.$invalid }"
                required
                :value="convertDateTimeFromServer($v.notification.createdAt.$model)"
                @change="updateInstantField('createdAt', $event)"
              />
            </div>
            <div v-if="$v.notification.createdAt.$anyDirty && $v.notification.createdAt.$invalid">
              <small class="form-text text-danger" v-if="!$v.notification.createdAt.required" v-text="$t('entity.validation.required')">
                This field is required.
              </small>
              <small
                class="form-text text-danger"
                v-if="!$v.notification.createdAt.ZonedDateTimelocal"
                v-text="$t('entity.validation.ZonedDateTimelocal')"
              >
                This field should be a date and time.
              </small>
            </div>
          </div>
          <div class="form-group">
            <label class="form-control-label" v-text="$t('novelSyncErpApp.notification.recipient')" for="notification-recipient"
              >Recipient</label
            >
            <select class="form-control" id="notification-recipient" data-cy="recipient" name="recipient" v-model="notification.recipient">
              <option v-bind:value="null"></option>
              <option
                v-bind:value="
                  notification.recipient && employeeOption.id === notification.recipient.id ? notification.recipient : employeeOption
                "
                v-for="employeeOption in employees"
                :key="employeeOption.id"
              >
                {{ employeeOption.fullName }}
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
            :disabled="$v.notification.$invalid || isSaving"
            class="btn btn-primary"
          >
            <font-awesome-icon icon="save"></font-awesome-icon>&nbsp;<span v-text="$t('entity.action.save')">Save</span>
          </button>
        </div>
      </form>
    </div>
  </div>
</template>
<script lang="ts" src="./notification-update.component.ts"></script>
