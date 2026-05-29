import { Authority } from '@/shared/security/authority';
/* tslint:disable */
// prettier-ignore
const Entities = () => import('@/entities/entities.vue');

// prettier-ignore
const Department = () => import('@/entities/department/department.vue');
// prettier-ignore
const DepartmentUpdate = () => import('@/entities/department/department-update.vue');
// prettier-ignore
const DepartmentDetails = () => import('@/entities/department/department-details.vue');
// prettier-ignore
const Employee = () => import('@/entities/employee/employee.vue');
// prettier-ignore
const EmployeeUpdate = () => import('@/entities/employee/employee-update.vue');
// prettier-ignore
const EmployeeDetails = () => import('@/entities/employee/employee-details.vue');
// prettier-ignore
const Customer = () => import('@/entities/customer/customer.vue');
// prettier-ignore
const CustomerUpdate = () => import('@/entities/customer/customer-update.vue');
// prettier-ignore
const CustomerDetails = () => import('@/entities/customer/customer-details.vue');
// prettier-ignore
const Supplier = () => import('@/entities/supplier/supplier.vue');
// prettier-ignore
const SupplierUpdate = () => import('@/entities/supplier/supplier-update.vue');
// prettier-ignore
const SupplierDetails = () => import('@/entities/supplier/supplier-details.vue');
// prettier-ignore
const Category = () => import('@/entities/category/category.vue');
// prettier-ignore
const CategoryUpdate = () => import('@/entities/category/category-update.vue');
// prettier-ignore
const CategoryDetails = () => import('@/entities/category/category-details.vue');
// prettier-ignore
const Product = () => import('@/entities/product/product.vue');
// prettier-ignore
const ProductUpdate = () => import('@/entities/product/product-update.vue');
// prettier-ignore
const ProductDetails = () => import('@/entities/product/product-details.vue');
// prettier-ignore
const Warehouse = () => import('@/entities/warehouse/warehouse.vue');
// prettier-ignore
const WarehouseUpdate = () => import('@/entities/warehouse/warehouse-update.vue');
// prettier-ignore
const WarehouseDetails = () => import('@/entities/warehouse/warehouse-details.vue');
// prettier-ignore
const InventoryBalance = () => import('@/entities/inventory-balance/inventory-balance.vue');
// prettier-ignore
const InventoryBalanceUpdate = () => import('@/entities/inventory-balance/inventory-balance-update.vue');
// prettier-ignore
const InventoryBalanceDetails = () => import('@/entities/inventory-balance/inventory-balance-details.vue');
// prettier-ignore
const InventoryTransaction = () => import('@/entities/inventory-transaction/inventory-transaction.vue');
// prettier-ignore
const InventoryTransactionUpdate = () => import('@/entities/inventory-transaction/inventory-transaction-update.vue');
// prettier-ignore
const InventoryTransactionDetails = () => import('@/entities/inventory-transaction/inventory-transaction-details.vue');
// prettier-ignore
const SalesOrder = () => import('@/entities/sales-order/sales-order.vue');
// prettier-ignore
const SalesOrderUpdate = () => import('@/entities/sales-order/sales-order-update.vue');
// prettier-ignore
const SalesOrderDetails = () => import('@/entities/sales-order/sales-order-details.vue');
// prettier-ignore
const SalesOrderLine = () => import('@/entities/sales-order-line/sales-order-line.vue');
// prettier-ignore
const SalesOrderLineUpdate = () => import('@/entities/sales-order-line/sales-order-line-update.vue');
// prettier-ignore
const SalesOrderLineDetails = () => import('@/entities/sales-order-line/sales-order-line-details.vue');
// prettier-ignore
const PurchaseOrder = () => import('@/entities/purchase-order/purchase-order.vue');
// prettier-ignore
const PurchaseOrderUpdate = () => import('@/entities/purchase-order/purchase-order-update.vue');
// prettier-ignore
const PurchaseOrderDetails = () => import('@/entities/purchase-order/purchase-order-details.vue');
// prettier-ignore
const PurchaseOrderLine = () => import('@/entities/purchase-order-line/purchase-order-line.vue');
// prettier-ignore
const PurchaseOrderLineUpdate = () => import('@/entities/purchase-order-line/purchase-order-line-update.vue');
// prettier-ignore
const PurchaseOrderLineDetails = () => import('@/entities/purchase-order-line/purchase-order-line-details.vue');
// prettier-ignore
const TransferOrder = () => import('@/entities/transfer-order/transfer-order.vue');
// prettier-ignore
const TransferOrderUpdate = () => import('@/entities/transfer-order/transfer-order-update.vue');
// prettier-ignore
const TransferOrderDetails = () => import('@/entities/transfer-order/transfer-order-details.vue');
// prettier-ignore
const TransferOrderLine = () => import('@/entities/transfer-order-line/transfer-order-line.vue');
// prettier-ignore
const TransferOrderLineUpdate = () => import('@/entities/transfer-order-line/transfer-order-line-update.vue');
// prettier-ignore
const TransferOrderLineDetails = () => import('@/entities/transfer-order-line/transfer-order-line-details.vue');
// prettier-ignore
const Payment = () => import('@/entities/payment/payment.vue');
// prettier-ignore
const PaymentUpdate = () => import('@/entities/payment/payment-update.vue');
// prettier-ignore
const PaymentDetails = () => import('@/entities/payment/payment-details.vue');
// prettier-ignore
const Notification = () => import('@/entities/notification/notification.vue');
// prettier-ignore
const NotificationUpdate = () => import('@/entities/notification/notification-update.vue');
// prettier-ignore
const NotificationDetails = () => import('@/entities/notification/notification-details.vue');
// jhipster-needle-add-entity-to-router-import - JHipster will import entities to the router here

export default {
  path: '/',
  component: Entities,
  children: [
    {
      path: 'department',
      name: 'Department',
      component: Department,
      meta: { authorities: [Authority.USER] },
    },
    {
      path: 'department/new',
      name: 'DepartmentCreate',
      component: DepartmentUpdate,
      meta: { authorities: [Authority.USER] },
    },
    {
      path: 'department/:departmentId/edit',
      name: 'DepartmentEdit',
      component: DepartmentUpdate,
      meta: { authorities: [Authority.USER] },
    },
    {
      path: 'department/:departmentId/view',
      name: 'DepartmentView',
      component: DepartmentDetails,
      meta: { authorities: [Authority.USER] },
    },
    {
      path: 'employee',
      name: 'Employee',
      component: Employee,
      meta: { authorities: [Authority.USER] },
    },
    {
      path: 'employee/new',
      name: 'EmployeeCreate',
      component: EmployeeUpdate,
      meta: { authorities: [Authority.USER] },
    },
    {
      path: 'employee/:employeeId/edit',
      name: 'EmployeeEdit',
      component: EmployeeUpdate,
      meta: { authorities: [Authority.USER] },
    },
    {
      path: 'employee/:employeeId/view',
      name: 'EmployeeView',
      component: EmployeeDetails,
      meta: { authorities: [Authority.USER] },
    },
    {
      path: 'customer',
      name: 'Customer',
      component: Customer,
      meta: { authorities: [Authority.USER] },
    },
    {
      path: 'customer/new',
      name: 'CustomerCreate',
      component: CustomerUpdate,
      meta: { authorities: [Authority.USER] },
    },
    {
      path: 'customer/:customerId/edit',
      name: 'CustomerEdit',
      component: CustomerUpdate,
      meta: { authorities: [Authority.USER] },
    },
    {
      path: 'customer/:customerId/view',
      name: 'CustomerView',
      component: CustomerDetails,
      meta: { authorities: [Authority.USER] },
    },
    {
      path: 'supplier',
      name: 'Supplier',
      component: Supplier,
      meta: { authorities: [Authority.USER] },
    },
    {
      path: 'supplier/new',
      name: 'SupplierCreate',
      component: SupplierUpdate,
      meta: { authorities: [Authority.USER] },
    },
    {
      path: 'supplier/:supplierId/edit',
      name: 'SupplierEdit',
      component: SupplierUpdate,
      meta: { authorities: [Authority.USER] },
    },
    {
      path: 'supplier/:supplierId/view',
      name: 'SupplierView',
      component: SupplierDetails,
      meta: { authorities: [Authority.USER] },
    },
    {
      path: 'category',
      name: 'Category',
      component: Category,
      meta: { authorities: [Authority.USER] },
    },
    {
      path: 'category/new',
      name: 'CategoryCreate',
      component: CategoryUpdate,
      meta: { authorities: [Authority.USER] },
    },
    {
      path: 'category/:categoryId/edit',
      name: 'CategoryEdit',
      component: CategoryUpdate,
      meta: { authorities: [Authority.USER] },
    },
    {
      path: 'category/:categoryId/view',
      name: 'CategoryView',
      component: CategoryDetails,
      meta: { authorities: [Authority.USER] },
    },
    {
      path: 'product',
      name: 'Product',
      component: Product,
      meta: { authorities: [Authority.USER] },
    },
    {
      path: 'product/new',
      name: 'ProductCreate',
      component: ProductUpdate,
      meta: { authorities: [Authority.USER] },
    },
    {
      path: 'product/:productId/edit',
      name: 'ProductEdit',
      component: ProductUpdate,
      meta: { authorities: [Authority.USER] },
    },
    {
      path: 'product/:productId/view',
      name: 'ProductView',
      component: ProductDetails,
      meta: { authorities: [Authority.USER] },
    },
    {
      path: 'warehouse',
      name: 'Warehouse',
      component: Warehouse,
      meta: { authorities: [Authority.USER] },
    },
    {
      path: 'warehouse/new',
      name: 'WarehouseCreate',
      component: WarehouseUpdate,
      meta: { authorities: [Authority.USER] },
    },
    {
      path: 'warehouse/:warehouseId/edit',
      name: 'WarehouseEdit',
      component: WarehouseUpdate,
      meta: { authorities: [Authority.USER] },
    },
    {
      path: 'warehouse/:warehouseId/view',
      name: 'WarehouseView',
      component: WarehouseDetails,
      meta: { authorities: [Authority.USER] },
    },
    {
      path: 'inventory-balance',
      name: 'InventoryBalance',
      component: InventoryBalance,
      meta: { authorities: [Authority.USER] },
    },
    {
      path: 'inventory-balance/new',
      name: 'InventoryBalanceCreate',
      component: InventoryBalanceUpdate,
      meta: { authorities: [Authority.USER] },
    },
    {
      path: 'inventory-balance/:inventoryBalanceId/edit',
      name: 'InventoryBalanceEdit',
      component: InventoryBalanceUpdate,
      meta: { authorities: [Authority.USER] },
    },
    {
      path: 'inventory-balance/:inventoryBalanceId/view',
      name: 'InventoryBalanceView',
      component: InventoryBalanceDetails,
      meta: { authorities: [Authority.USER] },
    },
    {
      path: 'inventory-transaction',
      name: 'InventoryTransaction',
      component: InventoryTransaction,
      meta: { authorities: [Authority.USER] },
    },
    {
      path: 'inventory-transaction/new',
      name: 'InventoryTransactionCreate',
      component: InventoryTransactionUpdate,
      meta: { authorities: [Authority.USER] },
    },
    {
      path: 'inventory-transaction/:inventoryTransactionId/edit',
      name: 'InventoryTransactionEdit',
      component: InventoryTransactionUpdate,
      meta: { authorities: [Authority.USER] },
    },
    {
      path: 'inventory-transaction/:inventoryTransactionId/view',
      name: 'InventoryTransactionView',
      component: InventoryTransactionDetails,
      meta: { authorities: [Authority.USER] },
    },
    {
      path: 'sales-order',
      name: 'SalesOrder',
      component: SalesOrder,
      meta: { authorities: [Authority.USER] },
    },
    {
      path: 'sales-order/new',
      name: 'SalesOrderCreate',
      component: SalesOrderUpdate,
      meta: { authorities: [Authority.USER] },
    },
    {
      path: 'sales-order/:salesOrderId/edit',
      name: 'SalesOrderEdit',
      component: SalesOrderUpdate,
      meta: { authorities: [Authority.USER] },
    },
    {
      path: 'sales-order/:salesOrderId/view',
      name: 'SalesOrderView',
      component: SalesOrderDetails,
      meta: { authorities: [Authority.USER] },
    },
    {
      path: 'sales-order-line',
      name: 'SalesOrderLine',
      component: SalesOrderLine,
      meta: { authorities: [Authority.USER] },
    },
    {
      path: 'sales-order-line/new',
      name: 'SalesOrderLineCreate',
      component: SalesOrderLineUpdate,
      meta: { authorities: [Authority.USER] },
    },
    {
      path: 'sales-order-line/:salesOrderLineId/edit',
      name: 'SalesOrderLineEdit',
      component: SalesOrderLineUpdate,
      meta: { authorities: [Authority.USER] },
    },
    {
      path: 'sales-order-line/:salesOrderLineId/view',
      name: 'SalesOrderLineView',
      component: SalesOrderLineDetails,
      meta: { authorities: [Authority.USER] },
    },
    {
      path: 'purchase-order',
      name: 'PurchaseOrder',
      component: PurchaseOrder,
      meta: { authorities: [Authority.USER] },
    },
    {
      path: 'purchase-order/new',
      name: 'PurchaseOrderCreate',
      component: PurchaseOrderUpdate,
      meta: { authorities: [Authority.USER] },
    },
    {
      path: 'purchase-order/:purchaseOrderId/edit',
      name: 'PurchaseOrderEdit',
      component: PurchaseOrderUpdate,
      meta: { authorities: [Authority.USER] },
    },
    {
      path: 'purchase-order/:purchaseOrderId/view',
      name: 'PurchaseOrderView',
      component: PurchaseOrderDetails,
      meta: { authorities: [Authority.USER] },
    },
    {
      path: 'purchase-order-line',
      name: 'PurchaseOrderLine',
      component: PurchaseOrderLine,
      meta: { authorities: [Authority.USER] },
    },
    {
      path: 'purchase-order-line/new',
      name: 'PurchaseOrderLineCreate',
      component: PurchaseOrderLineUpdate,
      meta: { authorities: [Authority.USER] },
    },
    {
      path: 'purchase-order-line/:purchaseOrderLineId/edit',
      name: 'PurchaseOrderLineEdit',
      component: PurchaseOrderLineUpdate,
      meta: { authorities: [Authority.USER] },
    },
    {
      path: 'purchase-order-line/:purchaseOrderLineId/view',
      name: 'PurchaseOrderLineView',
      component: PurchaseOrderLineDetails,
      meta: { authorities: [Authority.USER] },
    },
    {
      path: 'transfer-order',
      name: 'TransferOrder',
      component: TransferOrder,
      meta: { authorities: [Authority.USER] },
    },
    {
      path: 'transfer-order/new',
      name: 'TransferOrderCreate',
      component: TransferOrderUpdate,
      meta: { authorities: [Authority.USER] },
    },
    {
      path: 'transfer-order/:transferOrderId/edit',
      name: 'TransferOrderEdit',
      component: TransferOrderUpdate,
      meta: { authorities: [Authority.USER] },
    },
    {
      path: 'transfer-order/:transferOrderId/view',
      name: 'TransferOrderView',
      component: TransferOrderDetails,
      meta: { authorities: [Authority.USER] },
    },
    {
      path: 'transfer-order-line',
      name: 'TransferOrderLine',
      component: TransferOrderLine,
      meta: { authorities: [Authority.USER] },
    },
    {
      path: 'transfer-order-line/new',
      name: 'TransferOrderLineCreate',
      component: TransferOrderLineUpdate,
      meta: { authorities: [Authority.USER] },
    },
    {
      path: 'transfer-order-line/:transferOrderLineId/edit',
      name: 'TransferOrderLineEdit',
      component: TransferOrderLineUpdate,
      meta: { authorities: [Authority.USER] },
    },
    {
      path: 'transfer-order-line/:transferOrderLineId/view',
      name: 'TransferOrderLineView',
      component: TransferOrderLineDetails,
      meta: { authorities: [Authority.USER] },
    },
    {
      path: 'payment',
      name: 'Payment',
      component: Payment,
      meta: { authorities: [Authority.USER] },
    },
    {
      path: 'payment/new',
      name: 'PaymentCreate',
      component: PaymentUpdate,
      meta: { authorities: [Authority.USER] },
    },
    {
      path: 'payment/:paymentId/edit',
      name: 'PaymentEdit',
      component: PaymentUpdate,
      meta: { authorities: [Authority.USER] },
    },
    {
      path: 'payment/:paymentId/view',
      name: 'PaymentView',
      component: PaymentDetails,
      meta: { authorities: [Authority.USER] },
    },
    {
      path: 'notification',
      name: 'Notification',
      component: Notification,
      meta: { authorities: [Authority.USER] },
    },
    {
      path: 'notification/new',
      name: 'NotificationCreate',
      component: NotificationUpdate,
      meta: { authorities: [Authority.USER] },
    },
    {
      path: 'notification/:notificationId/edit',
      name: 'NotificationEdit',
      component: NotificationUpdate,
      meta: { authorities: [Authority.USER] },
    },
    {
      path: 'notification/:notificationId/view',
      name: 'NotificationView',
      component: NotificationDetails,
      meta: { authorities: [Authority.USER] },
    },
    // jhipster-needle-add-entity-to-router - JHipster will add entities to the router here
  ],
};
