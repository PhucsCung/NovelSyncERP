import { IProduct } from '@/shared/model/product.model';
import { IWarehouse } from '@/shared/model/warehouse.model';

export interface IInventoryBalance {
  id?: number;
  quantity?: number;
  product?: IProduct | null;
  warehouse?: IWarehouse | null;
}

export class InventoryBalance implements IInventoryBalance {
  constructor(public id?: number, public quantity?: number, public product?: IProduct | null, public warehouse?: IWarehouse | null) {}
}
