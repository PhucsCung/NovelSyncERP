import { Component, Vue, Inject } from 'vue-property-decorator';

import { required } from 'vuelidate/lib/validators';

import AlertService from '@/shared/alert/alert.service';

import PurchaseOrderLineService from '@/entities/purchase-order-line/purchase-order-line.service';
import { IPurchaseOrderLine } from '@/shared/model/purchase-order-line.model';

import SupplierService from '@/entities/supplier/supplier.service';
import { ISupplier } from '@/shared/model/supplier.model';

import EmployeeService from '@/entities/employee/employee.service';
import { IEmployee } from '@/shared/model/employee.model';

import WarehouseService from '@/entities/warehouse/warehouse.service';
import { IWarehouse } from '@/shared/model/warehouse.model';

import { IPurchaseOrder, PurchaseOrder } from '@/shared/model/purchase-order.model';
import PurchaseOrderService from './purchase-order.service';
import { OrderStatus } from '@/shared/model/enumerations/order-status.model';

const validations: any = {
  purchaseOrder: {
    poCode: {
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
export default class PurchaseOrderUpdate extends Vue {
  @Inject('purchaseOrderService') private purchaseOrderService: () => PurchaseOrderService;
  @Inject('alertService') private alertService: () => AlertService;

  public purchaseOrder: IPurchaseOrder = new PurchaseOrder();

  @Inject('purchaseOrderLineService') private purchaseOrderLineService: () => PurchaseOrderLineService;

  public purchaseOrderLines: IPurchaseOrderLine[] = [];

  @Inject('supplierService') private supplierService: () => SupplierService;

  public suppliers: ISupplier[] = [];

  @Inject('employeeService') private employeeService: () => EmployeeService;

  public employees: IEmployee[] = [];

  @Inject('warehouseService') private warehouseService: () => WarehouseService;

  public warehouses: IWarehouse[] = [];
  public orderStatusValues: string[] = Object.keys(OrderStatus);
  public isSaving = false;
  public currentLanguage = '';

  beforeRouteEnter(to, from, next) {
    next(vm => {
      if (to.params.purchaseOrderId) {
        vm.retrievePurchaseOrder(to.params.purchaseOrderId);
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
    if (this.purchaseOrder.id) {
      this.purchaseOrderService()
        .update(this.purchaseOrder)
        .then(param => {
          this.isSaving = false;
          this.$router.go(-1);
          const message = this.$t('novelSyncErpApp.purchaseOrder.updated', { param: param.id });
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
      this.purchaseOrderService()
        .create(this.purchaseOrder)
        .then(param => {
          this.isSaving = false;
          this.$router.go(-1);
          const message = this.$t('novelSyncErpApp.purchaseOrder.created', { param: param.id });
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

  public retrievePurchaseOrder(purchaseOrderId): void {
    this.purchaseOrderService()
      .find(purchaseOrderId)
      .then(res => {
        this.purchaseOrder = res;
      })
      .catch(error => {
        this.alertService().showHttpError(this, error.response);
      });
  }

  public previousState(): void {
    this.$router.go(-1);
  }

  public initRelationships(): void {
    this.purchaseOrderLineService()
      .retrieve()
      .then(res => {
        this.purchaseOrderLines = res.data;
      });
    this.supplierService()
      .retrieve()
      .then(res => {
        this.suppliers = res.data;
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
