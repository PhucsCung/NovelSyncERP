/* tslint:disable max-line-length */
import { shallowMount, createLocalVue, Wrapper } from '@vue/test-utils';
import sinon, { SinonStubbedInstance } from 'sinon';
import Router from 'vue-router';
import { ToastPlugin } from 'bootstrap-vue';

import * as config from '@/shared/config/config';
import PurchaseOrderLineUpdateComponent from '@/entities/purchase-order-line/purchase-order-line-update.vue';
import PurchaseOrderLineClass from '@/entities/purchase-order-line/purchase-order-line-update.component';
import PurchaseOrderLineService from '@/entities/purchase-order-line/purchase-order-line.service';

import ProductService from '@/entities/product/product.service';

import PurchaseOrderService from '@/entities/purchase-order/purchase-order.service';
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
  describe('PurchaseOrderLine Management Update Component', () => {
    let wrapper: Wrapper<PurchaseOrderLineClass>;
    let comp: PurchaseOrderLineClass;
    let purchaseOrderLineServiceStub: SinonStubbedInstance<PurchaseOrderLineService>;

    beforeEach(() => {
      purchaseOrderLineServiceStub = sinon.createStubInstance<PurchaseOrderLineService>(PurchaseOrderLineService);

      wrapper = shallowMount<PurchaseOrderLineClass>(PurchaseOrderLineUpdateComponent, {
        store,
        i18n,
        localVue,
        router,
        provide: {
          purchaseOrderLineService: () => purchaseOrderLineServiceStub,
          alertService: () => new AlertService(),

          productService: () =>
            sinon.createStubInstance<ProductService>(ProductService, {
              retrieve: sinon.stub().resolves({}),
            } as any),

          purchaseOrderService: () =>
            sinon.createStubInstance<PurchaseOrderService>(PurchaseOrderService, {
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
        comp.purchaseOrderLine = entity;
        purchaseOrderLineServiceStub.update.resolves(entity);

        // WHEN
        comp.save();
        await comp.$nextTick();

        // THEN
        expect(purchaseOrderLineServiceStub.update.calledWith(entity)).toBeTruthy();
        expect(comp.isSaving).toEqual(false);
      });

      it('Should call create service on save for new entity', async () => {
        // GIVEN
        const entity = {};
        comp.purchaseOrderLine = entity;
        purchaseOrderLineServiceStub.create.resolves(entity);

        // WHEN
        comp.save();
        await comp.$nextTick();

        // THEN
        expect(purchaseOrderLineServiceStub.create.calledWith(entity)).toBeTruthy();
        expect(comp.isSaving).toEqual(false);
      });
    });

    describe('Before route enter', () => {
      it('Should retrieve data', async () => {
        // GIVEN
        const foundPurchaseOrderLine = { id: 123 };
        purchaseOrderLineServiceStub.find.resolves(foundPurchaseOrderLine);
        purchaseOrderLineServiceStub.retrieve.resolves([foundPurchaseOrderLine]);

        // WHEN
        comp.beforeRouteEnter({ params: { purchaseOrderLineId: 123 } }, null, cb => cb(comp));
        await comp.$nextTick();

        // THEN
        expect(comp.purchaseOrderLine).toBe(foundPurchaseOrderLine);
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
