import { IPurchaseOrderLine } from '@/shared/model/purchase-order-line.model';
import { ISupplier } from '@/shared/model/supplier.model';
import { IEmployee } from '@/shared/model/employee.model';
import { IWarehouse } from '@/shared/model/warehouse.model';

import { OrderStatus } from '@/shared/model/enumerations/order-status.model';
export interface IPurchaseOrder {
  id?: number;
  poCode?: string;
  totalAmount?: number | null;
  status?: OrderStatus;
  orderLines?: IPurchaseOrderLine[] | null;
  supplier?: ISupplier | null;
  employee?: IEmployee | null;
  warehouse?: IWarehouse | null;
}

export class PurchaseOrder implements IPurchaseOrder {
  constructor(
    public id?: number,
    public poCode?: string,
    public totalAmount?: number | null,
    public status?: OrderStatus,
    public orderLines?: IPurchaseOrderLine[] | null,
    public supplier?: ISupplier | null,
    public employee?: IEmployee | null,
    public warehouse?: IWarehouse | null
  ) {}
}
