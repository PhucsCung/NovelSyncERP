/* tslint:disable max-line-length */
import { shallowMount, createLocalVue, Wrapper } from '@vue/test-utils';
import sinon, { SinonStubbedInstance } from 'sinon';
import Router from 'vue-router';
import { ToastPlugin } from 'bootstrap-vue';

import * as config from '@/shared/config/config';
import PurchaseOrderUpdateComponent from '@/entities/purchase-order/purchase-order-update.vue';
import PurchaseOrderClass from '@/entities/purchase-order/purchase-order-update.component';
import PurchaseOrderService from '@/entities/purchase-order/purchase-order.service';

import PurchaseOrderLineService from '@/entities/purchase-order-line/purchase-order-line.service';

import SupplierService from '@/entities/supplier/supplier.service';

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
  describe('PurchaseOrder Management Update Component', () => {
    let wrapper: Wrapper<PurchaseOrderClass>;
    let comp: PurchaseOrderClass;
    let purchaseOrderServiceStub: SinonStubbedInstance<PurchaseOrderService>;

    beforeEach(() => {
      purchaseOrderServiceStub = sinon.createStubInstance<PurchaseOrderService>(PurchaseOrderService);

      wrapper = shallowMount<PurchaseOrderClass>(PurchaseOrderUpdateComponent, {
        store,
        i18n,
        localVue,
        router,
        provide: {
          purchaseOrderService: () => purchaseOrderServiceStub,
          alertService: () => new AlertService(),

          purchaseOrderLineService: () =>
            sinon.createStubInstance<PurchaseOrderLineService>(PurchaseOrderLineService, {
              retrieve: sinon.stub().resolves({}),
            } as any),

          supplierService: () =>
            sinon.createStubInstance<SupplierService>(SupplierService, {
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
        comp.purchaseOrder = entity;
        purchaseOrderServiceStub.update.resolves(entity);

        // WHEN
        comp.save();
        await comp.$nextTick();

        // THEN
        expect(purchaseOrderServiceStub.update.calledWith(entity)).toBeTruthy();
        expect(comp.isSaving).toEqual(false);
      });

      it('Should call create service on save for new entity', async () => {
        // GIVEN
        const entity = {};
        comp.purchaseOrder = entity;
        purchaseOrderServiceStub.create.resolves(entity);

        // WHEN
        comp.save();
        await comp.$nextTick();

        // THEN
        expect(purchaseOrderServiceStub.create.calledWith(entity)).toBeTruthy();
        expect(comp.isSaving).toEqual(false);
      });
    });

    describe('Before route enter', () => {
      it('Should retrieve data', async () => {
        // GIVEN
        const foundPurchaseOrder = { id: 123 };
        purchaseOrderServiceStub.find.resolves(foundPurchaseOrder);
        purchaseOrderServiceStub.retrieve.resolves([foundPurchaseOrder]);

        // WHEN
        comp.beforeRouteEnter({ params: { purchaseOrderId: 123 } }, null, cb => cb(comp));
        await comp.$nextTick();

        // THEN
        expect(comp.purchaseOrder).toBe(foundPurchaseOrder);
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
