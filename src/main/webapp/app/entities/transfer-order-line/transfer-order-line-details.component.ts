import { Component, Vue, Inject } from 'vue-property-decorator';

import { ITransferOrderLine } from '@/shared/model/transfer-order-line.model';
import TransferOrderLineService from './transfer-order-line.service';
import AlertService from '@/shared/alert/alert.service';

@Component
export default class TransferOrderLineDetails extends Vue {
  @Inject('transferOrderLineService') private transferOrderLineService: () => TransferOrderLineService;
  @Inject('alertService') private alertService: () => AlertService;

  public transferOrderLine: ITransferOrderLine = {};

  beforeRouteEnter(to, from, next) {
    next(vm => {
      if (to.params.transferOrderLineId) {
        vm.retrieveTransferOrderLine(to.params.transferOrderLineId);
      }
    });
  }

  public retrieveTransferOrderLine(transferOrderLineId) {
    this.transferOrderLineService()
      .find(transferOrderLineId)
      .then(res => {
        this.transferOrderLine = res;
      })
      .catch(error => {
        this.alertService().showHttpError(this, error.response);
      });
  }

  public previousState() {
    this.$router.go(-1);
  }
}
