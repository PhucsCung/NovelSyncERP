import { Component, Vue, Inject } from 'vue-property-decorator';

import { numeric, required } from 'vuelidate/lib/validators';

import AlertService from '@/shared/alert/alert.service';

import ProductService from '@/entities/product/product.service';
import { IProduct } from '@/shared/model/product.model';

import WarehouseService from '@/entities/warehouse/warehouse.service';
import { IWarehouse } from '@/shared/model/warehouse.model';

import { IInventoryBalance, InventoryBalance } from '@/shared/model/inventory-balance.model';
import InventoryBalanceService from './inventory-balance.service';

const validations: any = {
  inventoryBalance: {
    quantity: {
      required,
      numeric,
    },
  },
};

@Component({
  validations,
})
export default class InventoryBalanceUpdate extends Vue {
  @Inject('inventoryBalanceService') private inventoryBalanceService: () => InventoryBalanceService;
  @Inject('alertService') private alertService: () => AlertService;

  public inventoryBalance: IInventoryBalance = new InventoryBalance();

  @Inject('productService') private productService: () => ProductService;

  public products: IProduct[] = [];

  @Inject('warehouseService') private warehouseService: () => WarehouseService;

  public warehouses: IWarehouse[] = [];
  public isSaving = false;
  public currentLanguage = '';

  beforeRouteEnter(to, from, next) {
    next(vm => {
      if (to.params.inventoryBalanceId) {
        vm.retrieveInventoryBalance(to.params.inventoryBalanceId);
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
    if (this.inventoryBalance.id) {
      this.inventoryBalanceService()
        .update(this.inventoryBalance)
        .then(param => {
          this.isSaving = false;
          this.$router.go(-1);
          const message = this.$t('novelSyncErpApp.inventoryBalance.updated', { param: param.id });
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
      this.inventoryBalanceService()
        .create(this.inventoryBalance)
        .then(param => {
          this.isSaving = false;
          this.$router.go(-1);
          const message = this.$t('novelSyncErpApp.inventoryBalance.created', { param: param.id });
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

  public retrieveInventoryBalance(inventoryBalanceId): void {
    this.inventoryBalanceService()
      .find(inventoryBalanceId)
      .then(res => {
        this.inventoryBalance = res;
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
    this.warehouseService()
      .retrieve()
      .then(res => {
        this.warehouses = res.data;
      });
  }
}
