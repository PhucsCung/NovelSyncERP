<template>
  <div>
    <b-dropdown-item to="/department">
      <font-awesome-icon icon="asterisk" />
      <span v-text="$t('global.menu.entities.department')">Department</span>
    </b-dropdown-item>
    <b-dropdown-item to="/employee">
      <font-awesome-icon icon="asterisk" />
      <span v-text="$t('global.menu.entities.employee')">Employee</span>
    </b-dropdown-item>
    <b-dropdown-item to="/customer">
      <font-awesome-icon icon="asterisk" />
      <span v-text="$t('global.menu.entities.customer')">Customer</span>
    </b-dropdown-item>
    <b-dropdown-item to="/supplier">
      <font-awesome-icon icon="asterisk" />
      <span v-text="$t('global.menu.entities.supplier')">Supplier</span>
    </b-dropdown-item>
    <b-dropdown-item to="/category">
      <font-awesome-icon icon="asterisk" />
      <span v-text="$t('global.menu.entities.category')">Category</span>
    </b-dropdown-item>
    <b-dropdown-item to="/product">
      <font-awesome-icon icon="asterisk" />
      <span v-text="$t('global.menu.entities.product')">Product</span>
    </b-dropdown-item>
    <b-dropdown-item to="/warehouse">
      <font-awesome-icon icon="asterisk" />
      <span v-text="$t('global.menu.entities.warehouse')">Warehouse</span>
    </b-dropdown-item>
    <b-dropdown-item to="/inventory-balance">
      <font-awesome-icon icon="asterisk" />
      <span v-text="$t('global.menu.entities.inventoryBalance')">Inventory Balance</span>
    </b-dropdown-item>
    <b-dropdown-item to="/inventory-transaction">
      <font-awesome-icon icon="asterisk" />
      <span v-text="$t('global.menu.entities.inventoryTransaction')">Inventory Transaction</span>
    </b-dropdown-item>
    <b-dropdown-item to="/sales-order">
      <font-awesome-icon icon="asterisk" />
      <span v-text="$t('global.menu.entities.salesOrder')">Sales Order</span>
    </b-dropdown-item>
    <b-dropdown-item to="/sales-order-line">
      <font-awesome-icon icon="asterisk" />
      <span v-text="$t('global.menu.entities.salesOrderLine')">Sales Order Line</span>
    </b-dropdown-item>
    <b-dropdown-item to="/purchase-order">
      <font-awesome-icon icon="asterisk" />
      <span v-text="$t('global.menu.entities.purchaseOrder')">Purchase Order</span>
    </b-dropdown-item>
    <b-dropdown-item to="/purchase-order-line">
      <font-awesome-icon icon="asterisk" />
      <span v-text="$t('global.menu.entities.purchaseOrderLine')">Purchase Order Line</span>
    </b-dropdown-item>
    <b-dropdown-item to="/transfer-order">
      <font-awesome-icon icon="asterisk" />
      <span v-text="$t('global.menu.entities.transferOrder')">Transfer Order</span>
    </b-dropdown-item>
    <b-dropdown-item to="/transfer-order-line">
      <font-awesome-icon icon="asterisk" />
      <span v-text="$t('global.menu.entities.transferOrderLine')">Transfer Order Line</span>
    </b-dropdown-item>
    <b-dropdown-item to="/payment">
      <font-awesome-icon icon="asterisk" />
      <span v-text="$t('global.menu.entities.payment')">Payment</span>
    </b-dropdown-item>
    <b-dropdown-item to="/notification">
      <font-awesome-icon icon="asterisk" />
      <span v-text="$t('global.menu.entities.notification')">Notification</span>
    </b-dropdown-item>
    <!-- jhipster-needle-add-entity-to-menu - JHipster will add entities to the menu here -->
  </div>
</template>

<script lang="ts" src="./entities-menu.component.ts"></script>
