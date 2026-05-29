import { IProduct } from '@/shared/model/product.model';
import { ISalesOrder } from '@/shared/model/sales-order.model';

export interface ISalesOrderLine {
  id?: number;
  quantity?: number;
  unitPrice?: number;
  product?: IProduct | null;
  salesOrder?: ISalesOrder | null;
}

export class SalesOrderLine implements ISalesOrderLine {
  constructor(
    public id?: number,
    public quantity?: number,
    public unitPrice?: number,
    public product?: IProduct | null,
    public salesOrder?: ISalesOrder | null
  ) {}
}
