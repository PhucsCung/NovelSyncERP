/* tslint:disable max-line-length */
import { shallowMount, createLocalVue, Wrapper } from '@vue/test-utils';
import sinon, { SinonStubbedInstance } from 'sinon';
import VueRouter from 'vue-router';

import * as config from '@/shared/config/config';
import SalesOrderDetailComponent from '@/entities/sales-order/sales-order-details.vue';
import SalesOrderClass from '@/entities/sales-order/sales-order-details.component';
import SalesOrderService from '@/entities/sales-order/sales-order.service';
import router from '@/router';
import AlertService from '@/shared/alert/alert.service';

const localVue = createLocalVue();
localVue.use(VueRouter);

config.initVueApp(localVue);
const i18n = config.initI18N(localVue);
const store = config.initVueXStore(localVue);
localVue.component('font-awesome-icon', {});
localVue.component('router-link', {});

describe('Component Tests', () => {
  describe('SalesOrder Management Detail Component', () => {
    let wrapper: Wrapper<SalesOrderClass>;
    let comp: SalesOrderClass;
    let salesOrderServiceStub: SinonStubbedInstance<SalesOrderService>;

    beforeEach(() => {
      salesOrderServiceStub = sinon.createStubInstance<SalesOrderService>(SalesOrderService);

      wrapper = shallowMount<SalesOrderClass>(SalesOrderDetailComponent, {
        store,
        i18n,
        localVue,
        router,
        provide: { salesOrderService: () => salesOrderServiceStub, alertService: () => new AlertService() },
      });
      comp = wrapper.vm;
    });

    describe('OnInit', () => {
      it('Should call load all on init', async () => {
        // GIVEN
        const foundSalesOrder = { id: 123 };
        salesOrderServiceStub.find.resolves(foundSalesOrder);

        // WHEN
        comp.retrieveSalesOrder(123);
        await comp.$nextTick();

        // THEN
        expect(comp.salesOrder).toBe(foundSalesOrder);
      });
    });

    describe('Before route enter', () => {
      it('Should retrieve data', async () => {
        // GIVEN
        const foundSalesOrder = { id: 123 };
        salesOrderServiceStub.find.resolves(foundSalesOrder);

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
