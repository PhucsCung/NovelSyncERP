/* tslint:disable max-line-length */
import { shallowMount, createLocalVue, Wrapper } from '@vue/test-utils';
import sinon, { SinonStubbedInstance } from 'sinon';
import Router from 'vue-router';
import { ToastPlugin } from 'bootstrap-vue';

import * as config from '@/shared/config/config';
import TransferOrderUpdateComponent from '@/entities/transfer-order/transfer-order-update.vue';
import TransferOrderClass from '@/entities/transfer-order/transfer-order-update.component';
import TransferOrderService from '@/entities/transfer-order/transfer-order.service';

import TransferOrderLineService from '@/entities/transfer-order-line/transfer-order-line.service';

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
  describe('TransferOrder Management Update Component', () => {
    let wrapper: Wrapper<TransferOrderClass>;
    let comp: TransferOrderClass;
    let transferOrderServiceStub: SinonStubbedInstance<TransferOrderService>;

    beforeEach(() => {
      transferOrderServiceStub = sinon.createStubInstance<TransferOrderService>(TransferOrderService);

      wrapper = shallowMount<TransferOrderClass>(TransferOrderUpdateComponent, {
        store,
        i18n,
        localVue,
        router,
        provide: {
          transferOrderService: () => transferOrderServiceStub,
          alertService: () => new AlertService(),

          transferOrderLineService: () =>
            sinon.createStubInstance<TransferOrderLineService>(TransferOrderLineService, {
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
        comp.transferOrder = entity;
        transferOrderServiceStub.update.resolves(entity);

        // WHEN
        comp.save();
        await comp.$nextTick();

        // THEN
        expect(transferOrderServiceStub.update.calledWith(entity)).toBeTruthy();
        expect(comp.isSaving).toEqual(false);
      });

      it('Should call create service on save for new entity', async () => {
        // GIVEN
        const entity = {};
        comp.transferOrder = entity;
        transferOrderServiceStub.create.resolves(entity);

        // WHEN
        comp.save();
        await comp.$nextTick();

        // THEN
        expect(transferOrderServiceStub.create.calledWith(entity)).toBeTruthy();
        expect(comp.isSaving).toEqual(false);
      });
    });

    describe('Before route enter', () => {
      it('Should retrieve data', async () => {
        // GIVEN
        const foundTransferOrder = { id: 123 };
        transferOrderServiceStub.find.resolves(foundTransferOrder);
        transferOrderServiceStub.retrieve.resolves([foundTransferOrder]);

        // WHEN
        comp.beforeRouteEnter({ params: { transferOrderId: 123 } }, null, cb => cb(comp));
        await comp.$nextTick();

        // THEN
        expect(comp.transferOrder).toBe(foundTransferOrder);
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
