/* tslint:disable max-line-length */
import { shallowMount, createLocalVue, Wrapper } from '@vue/test-utils';
import sinon, { SinonStubbedInstance } from 'sinon';
import VueRouter from 'vue-router';

import * as config from '@/shared/config/config';
import SalesOrderLineDetailComponent from '@/entities/sales-order-line/sales-order-line-details.vue';
import SalesOrderLineClass from '@/entities/sales-order-line/sales-order-line-details.component';
import SalesOrderLineService from '@/entities/sales-order-line/sales-order-line.service';
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
  describe('SalesOrderLine Management Detail Component', () => {
    let wrapper: Wrapper<SalesOrderLineClass>;
    let comp: SalesOrderLineClass;
    let salesOrderLineServiceStub: SinonStubbedInstance<SalesOrderLineService>;

    beforeEach(() => {
      salesOrderLineServiceStub = sinon.createStubInstance<SalesOrderLineService>(SalesOrderLineService);

      wrapper = shallowMount<SalesOrderLineClass>(SalesOrderLineDetailComponent, {
        store,
        i18n,
        localVue,
        router,
        provide: { salesOrderLineService: () => salesOrderLineServiceStub, alertService: () => new AlertService() },
      });
      comp = wrapper.vm;
    });

    describe('OnInit', () => {
      it('Should call load all on init', async () => {
        // GIVEN
        const foundSalesOrderLine = { id: 123 };
        salesOrderLineServiceStub.find.resolves(foundSalesOrderLine);

        // WHEN
        comp.retrieveSalesOrderLine(123);
        await comp.$nextTick();

        // THEN
        expect(comp.salesOrderLine).toBe(foundSalesOrderLine);
      });
    });

    describe('Before route enter', () => {
      it('Should retrieve data', async () => {
        // GIVEN
        const foundSalesOrderLine = { id: 123 };
        salesOrderLineServiceStub.find.resolves(foundSalesOrderLine);

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
