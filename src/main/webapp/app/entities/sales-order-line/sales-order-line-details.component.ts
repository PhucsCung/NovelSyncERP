import { Component, Vue, Inject } from 'vue-property-decorator';

import { ISalesOrderLine } from '@/shared/model/sales-order-line.model';
import SalesOrderLineService from './sales-order-line.service';
import AlertService from '@/shared/alert/alert.service';

@Component
export default class SalesOrderLineDetails extends Vue {
  @Inject('salesOrderLineService') private salesOrderLineService: () => SalesOrderLineService;
  @Inject('alertService') private alertService: () => AlertService;

  public salesOrderLine: ISalesOrderLine = {};

  beforeRouteEnter(to, from, next) {
    next(vm => {
      if (to.params.salesOrderLineId) {
        vm.retrieveSalesOrderLine(to.params.salesOrderLineId);
      }
    });
  }

  public retrieveSalesOrderLine(salesOrderLineId) {
    this.salesOrderLineService()
      .find(salesOrderLineId)
      .then(res => {
        this.salesOrderLine = res;
      })
      .catch(error => {
        this.alertService().showHttpError(this, error.response);
      });
  }

  public previousState() {
    this.$router.go(-1);
  }
}
