import { IEmployee } from '@/shared/model/employee.model';

import { NotificationType } from '@/shared/model/enumerations/notification-type.model';
export interface INotification {
  id?: number;
  title?: string;
  message?: string;
  isRead?: boolean;
  type?: NotificationType;
  referenceId?: number | null;
  createdAt?: Date;
  recipient?: IEmployee | null;
}

export class Notification implements INotification {
  constructor(
    public id?: number,
    public title?: string,
    public message?: string,
    public isRead?: boolean,
    public type?: NotificationType,
    public referenceId?: number | null,
    public createdAt?: Date,
    public recipient?: IEmployee | null
  ) {
    this.isRead = this.isRead ?? false;
  }
}
