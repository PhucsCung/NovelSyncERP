export interface ISupplier {
  id?: number;
  code?: string;
  name?: string;
  phone?: string | null;
  currentDebt?: number | null;
}

export class Supplier implements ISupplier {
  constructor(
    public id?: number,
    public code?: string,
    public name?: string,
    public phone?: string | null,
    public currentDebt?: number | null
  ) {}
}
