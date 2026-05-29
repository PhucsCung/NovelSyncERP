import { ICategory } from '@/shared/model/category.model';

export interface IProduct {
  id?: number;
  sku?: string;
  name?: string;
  basePrice?: number;
  attributes?: string | null;
  category?: ICategory | null;
}

export class Product implements IProduct {
  constructor(
    public id?: number,
    public sku?: string,
    public name?: string,
    public basePrice?: number,
    public attributes?: string | null,
    public category?: ICategory | null
  ) {}
}
