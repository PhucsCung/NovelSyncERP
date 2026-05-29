import { ICustomer } from '@/shared/model/customer.model';
import { ISupplier } from '@/shared/model/supplier.model';

import { PaymentType } from '@/shared/model/enumerations/payment-type.model';
export interface IPayment {
  id?: number;
  paymentCode?: string;
  type?: PaymentType;
  amount?: number;
  referenceOrderId?: number | null;
  createdAt?: Date;
  customer?: ICustomer | null;
  supplier?: ISupplier | null;
}

export class Payment implements IPayment {
  constructor(
    public id?: number,
    public paymentCode?: string,
    public type?: PaymentType,
    public amount?: number,
    public referenceOrderId?: number | null,
    public createdAt?: Date,
    public customer?: ICustomer | null,
    public supplier?: ISupplier | null
  ) {}
}
