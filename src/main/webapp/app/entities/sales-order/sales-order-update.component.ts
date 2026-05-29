import { Component, Vue, Inject } from 'vue-property-decorator';

import { required } from 'vuelidate/lib/validators';

import AlertService from '@/shared/alert/alert.service';

import SalesOrderLineService from '@/entities/sales-order-line/sales-order-line.service';
import { ISalesOrderLine } from '@/shared/model/sales-order-line.model';

import CustomerService from '@/entities/customer/customer.service';
import { ICustomer } from '@/shared/model/customer.model';

import EmployeeService from '@/entities/employee/employee.service';
import { IEmployee } from '@/shared/model/employee.model';

import WarehouseService from '@/entities/warehouse/warehouse.service';
import { IWarehouse } from '@/shared/model/warehouse.model';

import { ISalesOrder, SalesOrder } from '@/shared/model/sales-order.model';
import SalesOrderService from './sales-order.service';
import { OrderStatus } from '@/shared/model/enumerations/order-status.model';

const validations: any = {
  salesOrder: {
    orderCode: {
      required,
    },
    totalAmount: {},
    status: {
      required,
    },
  },
};

@Component({
  validations,
})
export default class SalesOrderUpdate extends Vue {
  @Inject('salesOrderService') private salesOrderService: () => SalesOrderService;
  @Inject('alertService') private alertService: () => AlertService;

  public salesOrder: ISalesOrder = new SalesOrder();

  @Inject('salesOrderLineService') private salesOrderLineService: () => SalesOrderLineService;

  public salesOrderLines: ISalesOrderLine[] = [];

  @Inject('customerService') private customerService: () => CustomerService;

  public customers: ICustomer[] = [];

  @Inject('employeeService') private employeeService: () => EmployeeService;

  public employees: IEmployee[] = [];

  @Inject('warehouseService') private warehouseService: () => WarehouseService;

  public warehouses: IWarehouse[] = [];
  public orderStatusValues: string[] = Object.keys(OrderStatus);
  public isSaving = false;
  public currentLanguage = '';

  beforeRouteEnter(to, from, next) {
    next(vm => {
      if (to.params.salesOrderId) {
        vm.retrieveSalesOrder(to.params.salesOrderId);
      }
      vm.initRelationships();
    });
  }

  created(): void {
    this.currentLanguage = this.$store.getters.currentLanguage;
    this.$store.watch(
      () => this.$store.getters.currentLanguage,
      () => {
        this.currentLanguage = this.$store.getters.currentLanguage;
      }
    );
  }

  public save(): void {
    this.isSaving = true;
    if (this.salesOrder.id) {
      this.salesOrderService()
        .update(this.salesOrder)
        .then(param => {
          this.isSaving = false;
          this.$router.go(-1);
          const message = this.$t('novelSyncErpApp.salesOrder.updated', { param: param.id });
          return (this.$root as any).$bvToast.toast(message.toString(), {
            toaster: 'b-toaster-top-center',
            title: 'Info',
            variant: 'info',
            solid: true,
            autoHideDelay: 5000,
          });
        })
        .catch(error => {
          this.isSaving = false;
          this.alertService().showHttpError(this, error.response);
        });
    } else {
      this.salesOrderService()
        .create(this.salesOrder)
        .then(param => {
          this.isSaving = false;
          this.$router.go(-1);
          const message = this.$t('novelSyncErpApp.salesOrder.created', { param: param.id });
          (this.$root as any).$bvToast.toast(message.toString(), {
            toaster: 'b-toaster-top-center',
            title: 'Success',
            variant: 'success',
            solid: true,
            autoHideDelay: 5000,
          });
        })
        .catch(error => {
          this.isSaving = false;
          this.alertService().showHttpError(this, error.response);
        });
    }
  }

  public retrieveSalesOrder(salesOrderId): void {
    this.salesOrderService()
      .find(salesOrderId)
      .then(res => {
        this.salesOrder = res;
      })
      .catch(error => {
        this.alertService().showHttpError(this, error.response);
      });
  }

  public previousState(): void {
    this.$router.go(-1);
  }

  public initRelationships(): void {
    this.salesOrderLineService()
      .retrieve()
      .then(res => {
        this.salesOrderLines = res.data;
      });
    this.customerService()
      .retrieve()
      .then(res => {
        this.customers = res.data;
      });
    this.employeeService()
      .retrieve()
      .then(res => {
        this.employees = res.data;
      });
    this.warehouseService()
      .retrieve()
      .then(res => {
        this.warehouses = res.data;
      });
  }
}
