import { Component, Vue, Inject } from 'vue-property-decorator';

import { IWarehouse } from '@/shared/model/warehouse.model';
import WarehouseService from './warehouse.service';
import AlertService from '@/shared/alert/alert.service';

@Component
export default class WarehouseDetails extends Vue {
  @Inject('warehouseService') private warehouseService: () => WarehouseService;
  @Inject('alertService') private alertService: () => AlertService;

  public warehouse: IWarehouse = {};

  beforeRouteEnter(to, from, next) {
    next(vm => {
      if (to.params.warehouseId) {
        vm.retrieveWarehouse(to.params.warehouseId);
      }
    });
  }

  public retrieveWarehouse(warehouseId) {
    this.warehouseService()
      .find(warehouseId)
      .then(res => {
        this.warehouse = res;
      })
      .catch(error => {
        this.alertService().showHttpError(this, error.response);
      });
  }

  public previousState() {
    this.$router.go(-1);
  }
}
