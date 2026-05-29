import { Component, Vue, Inject } from 'vue-property-decorator';

import { ITransferOrder } from '@/shared/model/transfer-order.model';
import TransferOrderService from './transfer-order.service';
import AlertService from '@/shared/alert/alert.service';

@Component
export default class TransferOrderDetails extends Vue {
  @Inject('transferOrderService') private transferOrderService: () => TransferOrderService;
  @Inject('alertService') private alertService: () => AlertService;

  public transferOrder: ITransferOrder = {};

  beforeRouteEnter(to, from, next) {
    next(vm => {
      if (to.params.transferOrderId) {
        vm.retrieveTransferOrder(to.params.transferOrderId);
      }
    });
  }

  public retrieveTransferOrder(transferOrderId) {
    this.transferOrderService()
      .find(transferOrderId)
      .then(res => {
        this.transferOrder = res;
      })
      .catch(error => {
        this.alertService().showHttpError(this, error.response);
      });
  }

  public previousState() {
    this.$router.go(-1);
  }
}
