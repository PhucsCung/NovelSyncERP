import { Component, Vue, Inject } from 'vue-property-decorator';

import { IPayment } from '@/shared/model/payment.model';
import PaymentService from './payment.service';
import AlertService from '@/shared/alert/alert.service';

@Component
export default class PaymentDetails extends Vue {
  @Inject('paymentService') private paymentService: () => PaymentService;
  @Inject('alertService') private alertService: () => AlertService;

  public payment: IPayment = {};

  beforeRouteEnter(to, from, next) {
    next(vm => {
      if (to.params.paymentId) {
        vm.retrievePayment(to.params.paymentId);
      }
    });
  }

  public retrievePayment(paymentId) {
    this.paymentService()
      .find(paymentId)
      .then(res => {
        this.payment = res;
      })
      .catch(error => {
        this.alertService().showHttpError(this, error.response);
      });
  }

  public previousState() {
    this.$router.go(-1);
  }
}
