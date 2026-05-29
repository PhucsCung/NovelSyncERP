/* tslint:disable max-line-length */
import { shallowMount, createLocalVue, Wrapper } from '@vue/test-utils';
import sinon, { SinonStubbedInstance } from 'sinon';
import Router from 'vue-router';
import { ToastPlugin } from 'bootstrap-vue';

import * as config from '@/shared/config/config';
import SalesOrderUpdateComponent from '@/entities/sales-order/sales-order-update.vue';
import SalesOrderClass from '@/entities/sales-order/sales-order-update.component';
import SalesOrderService from '@/entities/sales-order/sales-order.service';

import SalesOrderLineService from '@/entities/sales-order-line/sales-order-line.service';

import CustomerService from '@/entities/customer/customer.service';

import EmployeeService from '@/entities/employee/employee.service';

import WarehouseService from '@/entities/warehouse/warehouse.service';
import AlertService from '@/shared/alert/alert.service';

const localVue = createLocalVue();

config.initVueApp(localVue);
const i18n = config.initI18N(localVue);
const store = config.initVueXStore(localVue);
const router = new Router();
localVue.use(Router);
localVue.use(ToastPlugin);
localVue.component('font-awesome-icon', {});
localVue.component('b-input-group', {});
localVue.component('b-input-group-prepend', {});
localVue.component('b-form-datepicker', {});
localVue.component('b-form-input', {});

describe('Component Tests', () => {
  describe('SalesOrder Management Update Component', () => {
    let wrapper: Wrapper<SalesOrderClass>;
    let comp: SalesOrderClass;
    let salesOrderServiceStub: SinonStubbedInstance<SalesOrderService>;

    beforeEach(() => {
      salesOrderServiceStub = sinon.createStubInstance<SalesOrderService>(SalesOrderService);

      wrapper = shallowMount<SalesOrderClass>(SalesOrderUpdateComponent, {
        store,
        i18n,
        localVue,
        router,
        provide: {
          salesOrderService: () => salesOrderServiceStub,
          alertService: () => new AlertService(),

          salesOrderLineService: () =>
            sinon.createStubInstance<SalesOrderLineService>(SalesOrderLineService, {
              retrieve: sinon.stub().resolves({}),
            } as any),

          customerService: () =>
            sinon.createStubInstance<CustomerService>(CustomerService, {
              retrieve: sinon.stub().resolves({}),
            } as any),

          employeeService: () =>
            sinon.createStubInstance<EmployeeService>(EmployeeService, {
              retrieve: sinon.stub().resolves({}),
            } as any),

          warehouseService: () =>
            sinon.createStubInstance<WarehouseService>(WarehouseService, {
              retrieve: sinon.stub().resolves({}),
            } as any),
        },
      });
      comp = wrapper.vm;
    });

    describe('save', () => {
      it('Should call update service on save for existing entity', async () => {
        // GIVEN
        const entity = { id: 123 };
        comp.salesOrder = entity;
        salesOrderServiceStub.update.resolves(entity);

        // WHEN
        comp.save();
        await comp.$nextTick();

        // THEN
        expect(salesOrderServiceStub.update.calledWith(entity)).toBeTruthy();
        expect(comp.isSaving).toEqual(false);
      });

      it('Should call create service on save for new entity', async () => {
        // GIVEN
        const entity = {};
        comp.salesOrder = entity;
        salesOrderServiceStub.create.resolves(entity);

        // WHEN
        comp.save();
        await comp.$nextTick();

        // THEN
        expect(salesOrderServiceStub.create.calledWith(entity)).toBeTruthy();
        expect(comp.isSaving).toEqual(false);
      });
    });

    describe('Before route enter', () => {
      it('Should retrieve data', async () => {
        // GIVEN
        const foundSalesOrder = { id: 123 };
        salesOrderServiceStub.find.resolves(foundSalesOrder);
        salesOrderServiceStub.retrieve.resolves([foundSalesOrder]);

        // WHEN
        comp.beforeRouteEnter({ params: { salesOrderId: 123 } }, null, cb => cb(comp));
        await comp.$nextTick();

        // THEN
        expect(comp.salesOrder).toBe(foundSalesOrder);
      });
    });

    describe('Previous state', () => {
      it('Should go previous state', async () => {
        comp.previousState();
        await comp.$nextTick();

        expect(comp.$router.currentRoute.fullPath).toContain('/');
      });
    });
  });
});
