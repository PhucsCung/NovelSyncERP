import { ITransferOrderLine } from '@/shared/model/transfer-order-line.model';
import { IWarehouse } from '@/shared/model/warehouse.model';

import { OrderStatus } from '@/shared/model/enumerations/order-status.model';
export interface ITransferOrder {
  id?: number;
  transferCode?: string;
  status?: OrderStatus;
  orderLines?: ITransferOrderLine[] | null;
  fromWarehouse?: IWarehouse | null;
  toWarehouse?: IWarehouse | null;
}

export class TransferOrder implements ITransferOrder {
  constructor(
    public id?: number,
    public transferCode?: string,
    public status?: OrderStatus,
    public orderLines?: ITransferOrderLine[] | null,
    public fromWarehouse?: IWarehouse | null,
    public toWarehouse?: IWarehouse | null
  ) {}
}
