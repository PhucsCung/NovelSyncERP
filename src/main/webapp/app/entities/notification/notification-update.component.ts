import { Component, Vue, Inject } from 'vue-property-decorator';

import { required, maxLength } from 'vuelidate/lib/validators';
import dayjs from 'dayjs';
import { DATE_TIME_LONG_FORMAT } from '@/shared/date/filters';

import AlertService from '@/shared/alert/alert.service';

import EmployeeService from '@/entities/employee/employee.service';
import { IEmployee } from '@/shared/model/employee.model';

import { INotification, Notification } from '@/shared/model/notification.model';
import NotificationService from './notification.service';
import { NotificationType } from '@/shared/model/enumerations/notification-type.model';

const validations: any = {
  notification: {
    title: {
      required,
    },
    message: {
      required,
      maxLength: maxLength(1000),
    },
    isRead: {
      required,
    },
    type: {
      required,
    },
    referenceId: {},
    createdAt: {
      required,
    },
  },
};

@Component({
  validations,
})
export default class NotificationUpdate extends Vue {
  @Inject('notificationService') private notificationService: () => NotificationService;
  @Inject('alertService') private alertService: () => AlertService;

  public notification: INotification = new Notification();

  @Inject('employeeService') private employeeService: () => EmployeeService;

  public employees: IEmployee[] = [];
  public notificationTypeValues: string[] = Object.keys(NotificationType);
  public isSaving = false;
  public currentLanguage = '';

  beforeRouteEnter(to, from, next) {
    next(vm => {
      if (to.params.notificationId) {
        vm.retrieveNotification(to.params.notificationId);
      }
      vm.initRelationships();
    });
  }

  created(): void {
    this.currentLanguage = this.$store.getters.currentLanguage;
    this.$store.watch(
      () => this.$store.getters.currentLanguage,
      () => {
        this.currentLanguage = this.$store.getters.currentLanguage;
      }
    );
  }

  public save(): void {
    this.isSaving = true;
    if (this.notification.id) {
      this.notificationService()
        .update(this.notification)
        .then(param => {
          this.isSaving = false;
          this.$router.go(-1);
          const message = this.$t('novelSyncErpApp.notification.updated', { param: param.id });
          return (this.$root as any).$bvToast.toast(message.toString(), {
            toaster: 'b-toaster-top-center',
            title: 'Info',
            variant: 'info',
            solid: true,
            autoHideDelay: 5000,
          });
        })
        .catch(error => {
          this.isSaving = false;
          this.alertService().showHttpError(this, error.response);
        });
    } else {
      this.notificationService()
        .create(this.notification)
        .then(param => {
          this.isSaving = false;
          this.$router.go(-1);
          const message = this.$t('novelSyncErpApp.notification.created', { param: param.id });
          (this.$root as any).$bvToast.toast(message.toString(), {
            toaster: 'b-toaster-top-center',
            title: 'Success',
            variant: 'success',
            solid: true,
            autoHideDelay: 5000,
          });
        })
        .catch(error => {
          this.isSaving = false;
          this.alertService().showHttpError(this, error.response);
        });
    }
  }

  public convertDateTimeFromServer(date: Date): string {
    if (date && dayjs(date).isValid()) {
      return dayjs(date).format(DATE_TIME_LONG_FORMAT);
    }
    return null;
  }

  public updateInstantField(field, event) {
    if (event.target.value) {
      this.notification[field] = dayjs(event.target.value, DATE_TIME_LONG_FORMAT);
    } else {
      this.notification[field] = null;
    }
  }

  public updateZonedDateTimeField(field, event) {
    if (event.target.value) {
      this.notification[field] = dayjs(event.target.value, DATE_TIME_LONG_FORMAT);
    } else {
      this.notification[field] = null;
    }
  }

  public retrieveNotification(notificationId): void {
    this.notificationService()
      .find(notificationId)
      .then(res => {
        res.createdAt = new Date(res.createdAt);
        this.notification = res;
      })
      .catch(error => {
        this.alertService().showHttpError(this, error.response);
      });
  }

  public previousState(): void {
    this.$router.go(-1);
  }

  public initRelationships(): void {
    this.employeeService()
      .retrieve()
      .then(res => {
        this.employees = res.data;
      });
  }
}
