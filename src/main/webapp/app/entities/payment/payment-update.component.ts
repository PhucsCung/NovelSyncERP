import { Component, Vue, Inject } from 'vue-property-decorator';

import { required, decimal } from 'vuelidate/lib/validators';
import dayjs from 'dayjs';
import { DATE_TIME_LONG_FORMAT } from '@/shared/date/filters';

import AlertService from '@/shared/alert/alert.service';

import CustomerService from '@/entities/customer/customer.service';
import { ICustomer } from '@/shared/model/customer.model';

import SupplierService from '@/entities/supplier/supplier.service';
import { ISupplier } from '@/shared/model/supplier.model';

import { IPayment, Payment } from '@/shared/model/payment.model';
import PaymentService from './payment.service';
import { PaymentType } from '@/shared/model/enumerations/payment-type.model';

const validations: any = {
  payment: {
    paymentCode: {
      required,
    },
    type: {
      required,
    },
    amount: {
      required,
      decimal,
    },
    referenceOrderId: {},
    createdAt: {
      required,
    },
  },
};

@Component({
  validations,
})
export default class PaymentUpdate extends Vue {
  @Inject('paymentService') private paymentService: () => PaymentService;
  @Inject('alertService') private alertService: () => AlertService;

  public payment: IPayment = new Payment();

  @Inject('customerService') private customerService: () => CustomerService;

  public customers: ICustomer[] = [];

  @Inject('supplierService') private supplierService: () => SupplierService;

  public suppliers: ISupplier[] = [];
  public paymentTypeValues: string[] = Object.keys(PaymentType);
  public isSaving = false;
  public currentLanguage = '';

  beforeRouteEnter(to, from, next) {
    next(vm => {
      if (to.params.paymentId) {
        vm.retrievePayment(to.params.paymentId);
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
    if (this.payment.id) {
      this.paymentService()
        .update(this.payment)
        .then(param => {
          this.isSaving = false;
          this.$router.go(-1);
          const message = this.$t('novelSyncErpApp.payment.updated', { param: param.id });
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
      this.paymentService()
        .create(this.payment)
        .then(param => {
          this.isSaving = false;
          this.$router.go(-1);
          const message = this.$t('novelSyncErpApp.payment.created', { param: param.id });
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

  public convertDateTimeFromServer(date: Date): string {
    if (date && dayjs(date).isValid()) {
      return dayjs(date).format(DATE_TIME_LONG_FORMAT);
    }
    return null;
  }

  public updateInstantField(field, event) {
    if (event.target.value) {
      this.payment[field] = dayjs(event.target.value, DATE_TIME_LONG_FORMAT);
    } else {
      this.payment[field] = null;
    }
  }

  public updateZonedDateTimeField(field, event) {
    if (event.target.value) {
      this.payment[field] = dayjs(event.target.value, DATE_TIME_LONG_FORMAT);
    } else {
      this.payment[field] = null;
    }
  }

  public retrievePayment(paymentId): void {
    this.paymentService()
      .find(paymentId)
      .then(res => {
        res.createdAt = new Date(res.createdAt);
        this.payment = res;
      })
      .catch(error => {
        this.alertService().showHttpError(this, error.response);
      });
  }

  public previousState(): void {
    this.$router.go(-1);
  }

  public initRelationships(): void {
    this.customerService()
      .retrieve()
      .then(res => {
        this.customers = res.data;
      });
    this.supplierService()
      .retrieve()
      .then(res => {
        this.suppliers = res.data;
      });
  }
}
