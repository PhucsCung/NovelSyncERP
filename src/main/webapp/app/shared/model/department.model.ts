import { IEmployee } from '@/shared/model/employee.model';

export interface IDepartment {
  id?: number;
  code?: string;
  name?: string;
  employees?: IEmployee[] | null;
}

export class Department implements IDepartment {
  constructor(public id?: number, public code?: string, public name?: string, public employees?: IEmployee[] | null) {}
}
