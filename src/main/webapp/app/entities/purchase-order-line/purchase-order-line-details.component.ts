import { Component, Vue, Inject } from 'vue-property-decorator';

import { IPurchaseOrderLine } from '@/shared/model/purchase-order-line.model';
import PurchaseOrderLineService from './purchase-order-line.service';
import AlertService from '@/shared/alert/alert.service';

@Component
export default class PurchaseOrderLineDetails extends Vue {
  @Inject('purchaseOrderLineService') private purchaseOrderLineService: () => PurchaseOrderLineService;
  @Inject('alertService') private alertService: () => AlertService;

  public purchaseOrderLine: IPurchaseOrderLine = {};

  beforeRouteEnter(to, from, next) {
    next(vm => {
      if (to.params.purchaseOrderLineId) {
        vm.retrievePurchaseOrderLine(to.params.purchaseOrderLineId);
      }
    });
  }

  public retrievePurchaseOrderLine(purchaseOrderLineId) {
    this.purchaseOrderLineService()
      .find(purchaseOrderLineId)
      .then(res => {
        this.purchaseOrderLine = res;
      })
      .catch(error => {
        this.alertService().showHttpError(this, error.response);
      });
  }

  public previousState() {
    this.$router.go(-1);
  }
}
