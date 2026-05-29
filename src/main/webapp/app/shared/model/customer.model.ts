export interface ICustomer {
  id?: number;
  code?: string;
  name?: string;
  phone?: string | null;
  creditLimit?: number | null;
  currentDebt?: number | null;
}

export class Customer implements ICustomer {
  constructor(
    public id?: number,
    public code?: string,
    public name?: string,
    public phone?: string | null,
    public creditLimit?: number | null,
    public currentDebt?: number | null
  ) {}
}
