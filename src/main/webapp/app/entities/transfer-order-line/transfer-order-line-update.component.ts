import { Component, Vue, Inject } from 'vue-property-decorator';

import { numeric, required } from 'vuelidate/lib/validators';

import AlertService from '@/shared/alert/alert.service';

import ProductService from '@/entities/product/product.service';
import { IProduct } from '@/shared/model/product.model';

import TransferOrderService from '@/entities/transfer-order/transfer-order.service';
import { ITransferOrder } from '@/shared/model/transfer-order.model';

import { ITransferOrderLine, TransferOrderLine } from '@/shared/model/transfer-order-line.model';
import TransferOrderLineService from './transfer-order-line.service';

const validations: any = {
  transferOrderLine: {
    quantity: {
      required,
      numeric,
    },
  },
};

@Component({
  validations,
})
export default class TransferOrderLineUpdate extends Vue {
  @Inject('transferOrderLineService') private transferOrderLineService: () => TransferOrderLineService;
  @Inject('alertService') private alertService: () => AlertService;

  public transferOrderLine: ITransferOrderLine = new TransferOrderLine();

  @Inject('productService') private productService: () => ProductService;

  public products: IProduct[] = [];

  @Inject('transferOrderService') private transferOrderService: () => TransferOrderService;

  public transferOrders: ITransferOrder[] = [];
  public isSaving = false;
  public currentLanguage = '';

  beforeRouteEnter(to, from, next) {
    next(vm => {
      if (to.params.transferOrderLineId) {
        vm.retrieveTransferOrderLine(to.params.transferOrderLineId);
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
    if (this.transferOrderLine.id) {
      this.transferOrderLineService()
        .update(this.transferOrderLine)
        .then(param => {
          this.isSaving = false;
          this.$router.go(-1);
          const message = this.$t('novelSyncErpApp.transferOrderLine.updated', { param: param.id });
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
      this.transferOrderLineService()
        .create(this.transferOrderLine)
        .then(param => {
          this.isSaving = false;
          this.$router.go(-1);
          const message = this.$t('novelSyncErpApp.transferOrderLine.created', { param: param.id });
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

  public retrieveTransferOrderLine(transferOrderLineId): void {
    this.transferOrderLineService()
      .find(transferOrderLineId)
      .then(res => {
        this.transferOrderLine = res;
      })
      .catch(error => {
        this.alertService().showHttpError(this, error.response);
      });
  }

  public previousState(): void {
    this.$router.go(-1);
  }

  public initRelationships(): void {
    this.productService()
      .retrieve()
      .then(res => {
        this.products = res.data;
      });
    this.transferOrderService()
      .retrieve()
      .then(res => {
        this.transferOrders = res.data;
      });
  }
}
