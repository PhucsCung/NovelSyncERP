export interface ICategory {
  id?: number;
  code?: string;
  name?: string;
}

export class Category implements ICategory {
  constructor(public id?: number, public code?: string, public name?: string) {}
}
