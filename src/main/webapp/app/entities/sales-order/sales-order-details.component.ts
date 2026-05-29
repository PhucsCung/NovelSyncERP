import { Component, Vue, Inject } from 'vue-property-decorator';

import { ISalesOrder } from '@/shared/model/sales-order.model';
import SalesOrderService from './sales-order.service';
import AlertService from '@/shared/alert/alert.service';

@Component
export default class SalesOrderDetails extends Vue {
  @Inject('salesOrderService') private salesOrderService: () => SalesOrderService;
  @Inject('alertService') private alertService: () => AlertService;

  public salesOrder: ISalesOrder = {};

  beforeRouteEnter(to, from, next) {
    next(vm => {
      if (to.params.salesOrderId) {
        vm.retrieveSalesOrder(to.params.salesOrderId);
      }
    });
  }

  public retrieveSalesOrder(salesOrderId) {
    this.salesOrderService()
      .find(salesOrderId)
      .then(res => {
        this.salesOrder = res;
      })
      .catch(error => {
        this.alertService().showHttpError(this, error.response);
      });
  }

  public previousState() {
    this.$router.go(-1);
  }
}
