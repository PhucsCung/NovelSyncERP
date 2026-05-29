import { IProduct } from '@/shared/model/product.model';
import { IWarehouse } from '@/shared/model/warehouse.model';

import { TransactionType } from '@/shared/model/enumerations/transaction-type.model';
export interface IInventoryTransaction {
  id?: number;
  type?: TransactionType;
  quantity?: number;
  unitCost?: number | null;
  referenceId?: number | null;
  createdDate?: Date;
  product?: IProduct | null;
  warehouse?: IWarehouse | null;
}

export class InventoryTransaction implements IInventoryTransaction {
  constructor(
    public id?: number,
    public type?: TransactionType,
    public quantity?: number,
    public unitCost?: number | null,
    public referenceId?: number | null,
    public createdDate?: Date,
    public product?: IProduct | null,
    public warehouse?: IWarehouse | null
  ) {}
}
