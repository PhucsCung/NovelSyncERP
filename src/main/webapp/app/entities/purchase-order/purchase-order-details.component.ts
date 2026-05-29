import { Component, Vue, Inject } from 'vue-property-decorator';

import { IPurchaseOrder } from '@/shared/model/purchase-order.model';
import PurchaseOrderService from './purchase-order.service';
import AlertService from '@/shared/alert/alert.service';

@Component
export default class PurchaseOrderDetails extends Vue {
  @Inject('purchaseOrderService') private purchaseOrderService: () => PurchaseOrderService;
  @Inject('alertService') private alertService: () => AlertService;

  public purchaseOrder: IPurchaseOrder = {};

  beforeRouteEnter(to, from, next) {
    next(vm => {
      if (to.params.purchaseOrderId) {
        vm.retrievePurchaseOrder(to.params.purchaseOrderId);
      }
    });
  }

  public retrievePurchaseOrder(purchaseOrderId) {
    this.purchaseOrderService()
      .find(purchaseOrderId)
      .then(res => {
        this.purchaseOrder = res;
      })
      .catch(error => {
        this.alertService().showHttpError(this, error.response);
      });
  }

  public previousState() {
    this.$router.go(-1);
  }
}
