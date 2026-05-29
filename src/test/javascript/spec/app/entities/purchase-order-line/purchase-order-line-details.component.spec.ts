/* tslint:disable max-line-length */
import { shallowMount, createLocalVue, Wrapper } from '@vue/test-utils';
import sinon, { SinonStubbedInstance } from 'sinon';
import VueRouter from 'vue-router';

import * as config from '@/shared/config/config';
import PurchaseOrderLineDetailComponent from '@/entities/purchase-order-line/purchase-order-line-details.vue';
import PurchaseOrderLineClass from '@/entities/purchase-order-line/purchase-order-line-details.component';
import PurchaseOrderLineService from '@/entities/purchase-order-line/purchase-order-line.service';
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
  describe('PurchaseOrderLine Management Detail Component', () => {
    let wrapper: Wrapper<PurchaseOrderLineClass>;
    let comp: PurchaseOrderLineClass;
    let purchaseOrderLineServiceStub: SinonStubbedInstance<PurchaseOrderLineService>;

    beforeEach(() => {
      purchaseOrderLineServiceStub = sinon.createStubInstance<PurchaseOrderLineService>(PurchaseOrderLineService);

      wrapper = shallowMount<PurchaseOrderLineClass>(PurchaseOrderLineDetailComponent, {
        store,
        i18n,
        localVue,
        router,
        provide: { purchaseOrderLineService: () => purchaseOrderLineServiceStub, alertService: () => new AlertService() },
      });
      comp = wrapper.vm;
    });

    describe('OnInit', () => {
      it('Should call load all on init', async () => {
        // GIVEN
        const foundPurchaseOrderLine = { id: 123 };
        purchaseOrderLineServiceStub.find.resolves(foundPurchaseOrderLine);

        // WHEN
        comp.retrievePurchaseOrderLine(123);
        await comp.$nextTick();

        // THEN
        expect(comp.purchaseOrderLine).toBe(foundPurchaseOrderLine);
      });
    });

    describe('Before route enter', () => {
      it('Should retrieve data', async () => {
        // GIVEN
        const foundPurchaseOrderLine = { id: 123 };
        purchaseOrderLineServiceStub.find.resolves(foundPurchaseOrderLine);

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
