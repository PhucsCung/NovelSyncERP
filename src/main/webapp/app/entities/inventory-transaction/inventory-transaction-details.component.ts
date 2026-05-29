import { Component, Vue, Inject } from 'vue-property-decorator';

import { IInventoryTransaction } from '@/shared/model/inventory-transaction.model';
import InventoryTransactionService from './inventory-transaction.service';
import AlertService from '@/shared/alert/alert.service';

@Component
export default class InventoryTransactionDetails extends Vue {
  @Inject('inventoryTransactionService') private inventoryTransactionService: () => InventoryTransactionService;
  @Inject('alertService') private alertService: () => AlertService;

  public inventoryTransaction: IInventoryTransaction = {};

  beforeRouteEnter(to, from, next) {
    next(vm => {
      if (to.params.inventoryTransactionId) {
        vm.retrieveInventoryTransaction(to.params.inventoryTransactionId);
      }
    });
  }

  public retrieveInventoryTransaction(inventoryTransactionId) {
    this.inventoryTransactionService()
      .find(inventoryTransactionId)
      .then(res => {
        this.inventoryTransaction = res;
      })
      .catch(error => {
        this.alertService().showHttpError(this, error.response);
      });
  }

  public previousState() {
    this.$router.go(-1);
  }
}
