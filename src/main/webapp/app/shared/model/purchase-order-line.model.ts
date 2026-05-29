import { IProduct } from '@/shared/model/product.model';
import { IPurchaseOrder } from '@/shared/model/purchase-order.model';

export interface IPurchaseOrderLine {
  id?: number;
  quantity?: number;
  unitPrice?: number;
  product?: IProduct | null;
  purchaseOrder?: IPurchaseOrder | null;
}

export class PurchaseOrderLine implements IPurchaseOrderLine {
  constructor(
    public id?: number,
    public quantity?: number,
    public unitPrice?: number,
    public product?: IProduct | null,
    public purchaseOrder?: IPurchaseOrder | null
  ) {}
}
