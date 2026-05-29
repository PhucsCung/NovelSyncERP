import { Component, Provide, Vue } from 'vue-property-decorator';

import UserService from '@/entities/user/user.service';
import DepartmentService from './department/department.service';
import EmployeeService from './employee/employee.service';
import CustomerService from './customer/customer.service';
import SupplierService from './supplier/supplier.service';
import CategoryService from './category/category.service';
import ProductService from './product/product.service';
import WarehouseService from './warehouse/warehouse.service';
import InventoryBalanceService from './inventory-balance/inventory-balance.service';
import InventoryTransactionService from './inventory-transaction/inventory-transaction.service';
import SalesOrderService from './sales-order/sales-order.service';
import SalesOrderLineService from './sales-order-line/sales-order-line.service';
import PurchaseOrderService from './purchase-order/purchase-order.service';
import PurchaseOrderLineService from './purchase-order-line/purchase-order-line.service';
import TransferOrderService from './transfer-order/transfer-order.service';
import TransferOrderLineService from './transfer-order-line/transfer-order-line.service';
import PaymentService from './payment/payment.service';
import NotificationService from './notification/notification.service';
// jhipster-needle-add-entity-service-to-entities-component-import - JHipster will import entities services here

@Component
export default class Entities extends Vue {
  @Provide('userService') private userService = () => new UserService();
  @Provide('departmentService') private departmentService = () => new DepartmentService();
  @Provide('employeeService') private employeeService = () => new EmployeeService();
  @Provide('customerService') private customerService = () => new CustomerService();
  @Provide('supplierService') private supplierService = () => new SupplierService();
  @Provide('categoryService') private categoryService = () => new CategoryService();
  @Provide('productService') private productService = () => new ProductService();
  @Provide('warehouseService') private warehouseService = () => new WarehouseService();
  @Provide('inventoryBalanceService') private inventoryBalanceService = () => new InventoryBalanceService();
  @Provide('inventoryTransactionService') private inventoryTransactionService = () => new InventoryTransactionService();
  @Provide('salesOrderService') private salesOrderService = () => new SalesOrderService();
  @Provide('salesOrderLineService') private salesOrderLineService = () => new SalesOrderLineService();
  @Provide('purchaseOrderService') private purchaseOrderService = () => new PurchaseOrderService();
  @Provide('purchaseOrderLineService') private purchaseOrderLineService = () => new PurchaseOrderLineService();
  @Provide('transferOrderService') private transferOrderService = () => new TransferOrderService();
  @Provide('transferOrderLineService') private transferOrderLineService = () => new TransferOrderLineService();
  @Provide('paymentService') private paymentService = () => new PaymentService();
  @Provide('notificationService') private notificationService = () => new NotificationService();
  // jhipster-needle-add-entity-service-to-entities-component - JHipster will import entities services here
}
