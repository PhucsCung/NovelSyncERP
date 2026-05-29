import { ISalesOrderLine } from '@/shared/model/sales-order-line.model';
import { ICustomer } from '@/shared/model/customer.model';
import { IEmployee } from '@/shared/model/employee.model';
import { IWarehouse } from '@/shared/model/warehouse.model';

import { OrderStatus } from '@/shared/model/enumerations/order-status.model';
export interface ISalesOrder {
  id?: number;
  orderCode?: string;
  totalAmount?: number | null;
  status?: OrderStatus;
  orderLines?: ISalesOrderLine[] | null;
  customer?: ICustomer | null;
  employee?: IEmployee | null;
  warehouse?: IWarehouse | null;
}

export class SalesOrder implements ISalesOrder {
  constructor(
    public id?: number,
    public orderCode?: string,
    public totalAmount?: number | null,
    public status?: OrderStatus,
    public orderLines?: ISalesOrderLine[] | null,
    public customer?: ICustomer | null,
    public employee?: IEmployee | null,
    public warehouse?: IWarehouse | null
  ) {}
}
