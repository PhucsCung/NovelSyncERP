import { Component, Vue, Inject } from 'vue-property-decorator';

import { required } from 'vuelidate/lib/validators';

import AlertService from '@/shared/alert/alert.service';

import TransferOrderLineService from '@/entities/transfer-order-line/transfer-order-line.service';
import { ITransferOrderLine } from '@/shared/model/transfer-order-line.model';

import WarehouseService from '@/entities/warehouse/warehouse.service';
import { IWarehouse } from '@/shared/model/warehouse.model';

import { ITransferOrder, TransferOrder } from '@/shared/model/transfer-order.model';
import TransferOrderService from './transfer-order.service';
import { OrderStatus } from '@/shared/model/enumerations/order-status.model';

const validations: any = {
  transferOrder: {
    transferCode: {
      required,
    },
    status: {
      required,
    },
  },
};

@Component({
  validations,
})
export default class TransferOrderUpdate extends Vue {
  @Inject('transferOrderService') private transferOrderService: () => TransferOrderService;
  @Inject('alertService') private alertService: () => AlertService;

  public transferOrder: ITransferOrder = new TransferOrder();

  @Inject('transferOrderLineService') private transferOrderLineService: () => TransferOrderLineService;

  public transferOrderLines: ITransferOrderLine[] = [];

  @Inject('warehouseService') private warehouseService: () => WarehouseService;

  public warehouses: IWarehouse[] = [];
  public orderStatusValues: string[] = Object.keys(OrderStatus);
  public isSaving = false;
  public currentLanguage = '';

  beforeRouteEnter(to, from, next) {
    next(vm => {
      if (to.params.transferOrderId) {
        vm.retrieveTransferOrder(to.params.transferOrderId);
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
    if (this.transferOrder.id) {
      this.transferOrderService()
        .update(this.transferOrder)
        .then(param => {
          this.isSaving = false;
          this.$router.go(-1);
          const message = this.$t('novelSyncErpApp.transferOrder.updated', { param: param.id });
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
      this.transferOrderService()
        .create(this.transferOrder)
        .then(param => {
          this.isSaving = false;
          this.$router.go(-1);
          const message = this.$t('novelSyncErpApp.transferOrder.created', { param: param.id });
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

  public retrieveTransferOrder(transferOrderId): void {
    this.transferOrderService()
      .find(transferOrderId)
      .then(res => {
        this.transferOrder = res;
      })
      .catch(error => {
        this.alertService().showHttpError(this, error.response);
      });
  }

  public previousState(): void {
    this.$router.go(-1);
  }

  public initRelationships(): void {
    this.transferOrderLineService()
      .retrieve()
      .then(res => {
        this.transferOrderLines = res.data;
      });
    this.warehouseService()
      .retrieve()
      .then(res => {
        this.warehouses = res.data;
      });
  }
}
