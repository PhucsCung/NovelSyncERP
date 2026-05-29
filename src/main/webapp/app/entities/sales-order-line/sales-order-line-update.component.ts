import { Component, Vue, Inject } from 'vue-property-decorator';

import { numeric, required, decimal } from 'vuelidate/lib/validators';

import AlertService from '@/shared/alert/alert.service';

import ProductService from '@/entities/product/product.service';
import { IProduct } from '@/shared/model/product.model';

import SalesOrderService from '@/entities/sales-order/sales-order.service';
import { ISalesOrder } from '@/shared/model/sales-order.model';

import { ISalesOrderLine, SalesOrderLine } from '@/shared/model/sales-order-line.model';
import SalesOrderLineService from './sales-order-line.service';

const validations: any = {
  salesOrderLine: {
    quantity: {
      required,
      numeric,
    },
    unitPrice: {
      required,
      decimal,
    },
  },
};

@Component({
  validations,
})
export default class SalesOrderLineUpdate extends Vue {
  @Inject('salesOrderLineService') private salesOrderLineService: () => SalesOrderLineService;
  @Inject('alertService') private alertService: () => AlertService;

  public salesOrderLine: ISalesOrderLine = new SalesOrderLine();

  @Inject('productService') private productService: () => ProductService;

  public products: IProduct[] = [];

  @Inject('salesOrderService') private salesOrderService: () => SalesOrderService;

  public salesOrders: ISalesOrder[] = [];
  public isSaving = false;
  public currentLanguage = '';

  beforeRouteEnter(to, from, next) {
    next(vm => {
      if (to.params.salesOrderLineId) {
        vm.retrieveSalesOrderLine(to.params.salesOrderLineId);
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
    if (this.salesOrderLine.id) {
      this.salesOrderLineService()
        .update(this.salesOrderLine)
        .then(param => {
          this.isSaving = false;
          this.$router.go(-1);
          const message = this.$t('novelSyncErpApp.salesOrderLine.updated', { param: param.id });
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
      this.salesOrderLineService()
        .create(this.salesOrderLine)
        .then(param => {
          this.isSaving = false;
          this.$router.go(-1);
          const message = this.$t('novelSyncErpApp.salesOrderLine.created', { param: param.id });
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

  public retrieveSalesOrderLine(salesOrderLineId): void {
    this.salesOrderLineService()
      .find(salesOrderLineId)
      .then(res => {
        this.salesOrderLine = res;
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
    this.salesOrderService()
      .retrieve()
      .then(res => {
        this.salesOrders = res.data;
      });
  }
}
