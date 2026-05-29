import { Component, Vue, Inject } from 'vue-property-decorator';

import { required } from 'vuelidate/lib/validators';

import AlertService from '@/shared/alert/alert.service';

import { ISupplier, Supplier } from '@/shared/model/supplier.model';
import SupplierService from './supplier.service';

const validations: any = {
  supplier: {
    code: {
      required,
    },
    name: {
      required,
    },
    phone: {},
    currentDebt: {},
  },
};

@Component({
  validations,
})
export default class SupplierUpdate extends Vue {
  @Inject('supplierService') private supplierService: () => SupplierService;
  @Inject('alertService') private alertService: () => AlertService;

  public supplier: ISupplier = new Supplier();
  public isSaving = false;
  public currentLanguage = '';

  beforeRouteEnter(to, from, next) {
    next(vm => {
      if (to.params.supplierId) {
        vm.retrieveSupplier(to.params.supplierId);
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
    if (this.supplier.id) {
      this.supplierService()
        .update(this.supplier)
        .then(param => {
          this.isSaving = false;
          this.$router.go(-1);
          const message = this.$t('novelSyncErpApp.supplier.updated', { param: param.id });
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
      this.supplierService()
        .create(this.supplier)
        .then(param => {
          this.isSaving = false;
          this.$router.go(-1);
          const message = this.$t('novelSyncErpApp.supplier.created', { param: param.id });
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

  public retrieveSupplier(supplierId): void {
    this.supplierService()
      .find(supplierId)
      .then(res => {
        this.supplier = res;
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
