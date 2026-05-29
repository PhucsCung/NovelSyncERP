import { IProduct } from '@/shared/model/product.model';
import { ITransferOrder } from '@/shared/model/transfer-order.model';

export interface ITransferOrderLine {
  id?: number;
  quantity?: number;
  product?: IProduct | null;
  transferOrder?: ITransferOrder | null;
}

export class TransferOrderLine implements ITransferOrderLine {
  constructor(
    public id?: number,
    public quantity?: number,
    public product?: IProduct | null,
    public transferOrder?: ITransferOrder | null
  ) {}
}
