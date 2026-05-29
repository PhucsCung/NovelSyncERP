/* tslint:disable max-line-length */
import { shallowMount, createLocalVue, Wrapper } from '@vue/test-utils';
import sinon, { SinonStubbedInstance } from 'sinon';
import Router from 'vue-router';
import { ToastPlugin } from 'bootstrap-vue';

import * as config from '@/shared/config/config';
import SalesOrderLineUpdateComponent from '@/entities/sales-order-line/sales-order-line-update.vue';
import SalesOrderLineClass from '@/entities/sales-order-line/sales-order-line-update.component';
import SalesOrderLineService from '@/entities/sales-order-line/sales-order-line.service';

import ProductService from '@/entities/product/product.service';

import SalesOrderService from '@/entities/sales-order/sales-order.service';
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
  describe('SalesOrderLine Management Update Component', () => {
    let wrapper: Wrapper<SalesOrderLineClass>;
    let comp: SalesOrderLineClass;
    let salesOrderLineServiceStub: SinonStubbedInstance<SalesOrderLineService>;

    beforeEach(() => {
      salesOrderLineServiceStub = sinon.createStubInstance<SalesOrderLineService>(SalesOrderLineService);

      wrapper = shallowMount<SalesOrderLineClass>(SalesOrderLineUpdateComponent, {
        store,
        i18n,
        localVue,
        router,
        provide: {
          salesOrderLineService: () => salesOrderLineServiceStub,
          alertService: () => new AlertService(),

          productService: () =>
            sinon.createStubInstance<ProductService>(ProductService, {
              retrieve: sinon.stub().resolves({}),
            } as any),

          salesOrderService: () =>
            sinon.createStubInstance<SalesOrderService>(SalesOrderService, {
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
        comp.salesOrderLine = entity;
        salesOrderLineServiceStub.update.resolves(entity);

        // WHEN
        comp.save();
        await comp.$nextTick();

        // THEN
        expect(salesOrderLineServiceStub.update.calledWith(entity)).toBeTruthy();
        expect(comp.isSaving).toEqual(false);
      });

      it('Should call create service on save for new entity', async () => {
        // GIVEN
        const entity = {};
        comp.salesOrderLine = entity;
        salesOrderLineServiceStub.create.resolves(entity);

        // WHEN
        comp.save();
        await comp.$nextTick();

        // THEN
        expect(salesOrderLineServiceStub.create.calledWith(entity)).toBeTruthy();
        expect(comp.isSaving).toEqual(false);
      });
    });

    describe('Before route enter', () => {
      it('Should retrieve data', async () => {
        // GIVEN
        const foundSalesOrderLine = { id: 123 };
        salesOrderLineServiceStub.find.resolves(foundSalesOrderLine);
        salesOrderLineServiceStub.retrieve.resolves([foundSalesOrderLine]);

        // WHEN
        comp.beforeRouteEnter({ params: { salesOrderLineId: 123 } }, null, cb => cb(comp));
        await comp.$nextTick();

        // THEN
        expect(comp.salesOrderLine).toBe(foundSalesOrderLine);
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
