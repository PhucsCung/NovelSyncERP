<template>
  <div class="row justify-content-center">
    <div class="col-8">
      <div v-if="notification">
        <h2 class="jh-entity-heading" data-cy="notificationDetailsHeading">
          <span v-text="$t('novelSyncErpApp.notification.detail.title')">Notification</span> {{ notification.id }}
        </h2>
        <dl class="row jh-entity-details">
          <dt>
            <span v-text="$t('novelSyncErpApp.notification.title')">Title</span>
          </dt>
          <dd>
            <span>{{ notification.title }}</span>
          </dd>
          <dt>
            <span v-text="$t('novelSyncErpApp.notification.message')">Message</span>
          </dt>
          <dd>
            <span>{{ notification.message }}</span>
          </dd>
          <dt>
            <span v-text="$t('novelSyncErpApp.notification.isRead')">Is Read</span>
          </dt>
          <dd>
            <span>{{ notification.isRead }}</span>
          </dd>
          <dt>
            <span v-text="$t('novelSyncErpApp.notification.type')">Type</span>
          </dt>
          <dd>
            <span v-text="$t('novelSyncErpApp.NotificationType.' + notification.type)">{{ notification.type }}</span>
          </dd>
          <dt>
            <span v-text="$t('novelSyncErpApp.notification.referenceId')">Reference Id</span>
          </dt>
          <dd>
            <span>{{ notification.referenceId }}</span>
          </dd>
          <dt>
            <span v-text="$t('novelSyncErpApp.notification.createdAt')">Created At</span>
          </dt>
          <dd>
            <span v-if="notification.createdAt">{{ $d(Date.parse(notification.createdAt), 'long') }}</span>
          </dd>
          <dt>
            <span v-text="$t('novelSyncErpApp.notification.recipient')">Recipient</span>
          </dt>
          <dd>
            <div v-if="notification.recipient">
              <router-link :to="{ name: 'EmployeeView', params: { employeeId: notification.recipient.id } }">{{
                notification.recipient.fullName
              }}</router-link>
            </div>
          </dd>
        </dl>
        <button type="submit" v-on:click.prevent="previousState()" class="btn btn-info" data-cy="entityDetailsBackButton">
          <font-awesome-icon icon="arrow-left"></font-awesome-icon>&nbsp;<span v-text="$t('entity.action.back')"> Back</span>
        </button>
        <router-link
          v-if="notification.id"
          :to="{ name: 'NotificationEdit', params: { notificationId: notification.id } }"
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

<script lang="ts" src="./notification-details.component.ts"></script>
