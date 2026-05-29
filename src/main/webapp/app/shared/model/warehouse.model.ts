export interface IWarehouse {
  id?: number;
  code?: string;
  name?: string;
  address?: string | null;
}

export class Warehouse implements IWarehouse {
  constructor(public id?: number, public code?: string, public name?: string, public address?: string | null) {}
}
