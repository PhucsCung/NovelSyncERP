import { Component, Vue, Inject } from 'vue-property-decorator';

import { IInventoryBalance } from '@/shared/model/inventory-balance.model';
import InventoryBalanceService from './inventory-balance.service';
import AlertService from '@/shared/alert/alert.service';

@Component
export default class InventoryBalanceDetails extends Vue {
  @Inject('inventoryBalanceService') private inventoryBalanceService: () => InventoryBalanceService;
  @Inject('alertService') private alertService: () => AlertService;

  public inventoryBalance: IInventoryBalance = {};

  beforeRouteEnter(to, from, next) {
    next(vm => {
      if (to.params.inventoryBalanceId) {
        vm.retrieveInventoryBalance(to.params.inventoryBalanceId);
      }
    });
  }

  public retrieveInventoryBalance(inventoryBalanceId) {
    this.inventoryBalanceService()
      .find(inventoryBalanceId)
      .then(res => {
        this.inventoryBalance = res;
      })
      .catch(error => {
        this.alertService().showHttpError(this, error.response);
      });
  }

  public previousState() {
    this.$router.go(-1);
  }
}
