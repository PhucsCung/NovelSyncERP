import { IUser } from '@/shared/model/user.model';
import { IWarehouse } from '@/shared/model/warehouse.model';
import { IDepartment } from '@/shared/model/department.model';

export interface IEmployee {
  id?: number;
  fullName?: string;
  phone?: string | null;
  user?: IUser | null;
  manager?: IEmployee | null;
  scopedWarehouse?: IWarehouse | null;
  department?: IDepartment | null;
}

export class Employee implements IEmployee {
  constructor(
    public id?: number,
    public fullName?: string,
    public phone?: string | null,
    public user?: IUser | null,
    public manager?: IEmployee | null,
    public scopedWarehouse?: IWarehouse | null,
    public department?: IDepartment | null
  ) {}
}
