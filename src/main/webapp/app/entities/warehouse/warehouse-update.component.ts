import { Component, Vue, Inject } from 'vue-property-decorator';

import { required } from 'vuelidate/lib/validators';

import AlertService from '@/shared/alert/alert.service';

import { IWarehouse, Warehouse } from '@/shared/model/warehouse.model';
import WarehouseService from './warehouse.service';

const validations: any = {
  warehouse: {
    code: {
      required,
    },
    name: {
      required,
    },
    address: {},
  },
};

@Component({
  validations,
})
export default class WarehouseUpdate extends Vue {
  @Inject('warehouseService') private warehouseService: () => WarehouseService;
  @Inject('alertService') private alertService: () => AlertService;

  public warehouse: IWarehouse = new Warehouse();
  public isSaving = false;
  public currentLanguage = '';

  beforeRouteEnter(to, from, next) {
    next(vm => {
      if (to.params.warehouseId) {
        vm.retrieveWarehouse(to.params.warehouseId);
      }
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
    if (this.warehouse.id) {
      this.warehouseService()
        .update(this.warehouse)
        .then(param => {
          this.isSaving = false;
          this.$router.go(-1);
          const message = this.$t('novelSyncErpApp.warehouse.updated', { param: param.id });
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
      this.warehouseService()
        .create(this.warehouse)
        .then(param => {
          this.isSaving = false;
          this.$router.go(-1);
          const message = this.$t('novelSyncErpApp.warehouse.created', { param: param.id });
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

  public retrieveWarehouse(warehouseId): void {
    this.warehouseService()
      .find(warehouseId)
      .then(res => {
        this.warehouse = res;
      })
      .catch(error => {
        this.alertService().showHttpError(this, error.response);
      });
  }

  public previousState(): void {
    this.$router.go(-1);
  }

  public initRelationships(): void {}
}
