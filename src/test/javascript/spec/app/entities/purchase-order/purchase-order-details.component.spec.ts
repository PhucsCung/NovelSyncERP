/* tslint:disable max-line-length */
import { shallowMount, createLocalVue, Wrapper } from '@vue/test-utils';
import sinon, { SinonStubbedInstance } from 'sinon';
import VueRouter from 'vue-router';

import * as config from '@/shared/config/config';
import PurchaseOrderDetailComponent from '@/entities/purchase-order/purchase-order-details.vue';
import PurchaseOrderClass from '@/entities/purchase-order/purchase-order-details.component';
import PurchaseOrderService from '@/entities/purchase-order/purchase-order.service';
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
  describe('PurchaseOrder Management Detail Component', () => {
    let wrapper: Wrapper<PurchaseOrderClass>;
    let comp: PurchaseOrderClass;
    let purchaseOrderServiceStub: SinonStubbedInstance<PurchaseOrderService>;

    beforeEach(() => {
      purchaseOrderServiceStub = sinon.createStubInstance<PurchaseOrderService>(PurchaseOrderService);

      wrapper = shallowMount<PurchaseOrderClass>(PurchaseOrderDetailComponent, {
        store,
        i18n,
        localVue,
        router,
        provide: { purchaseOrderService: () => purchaseOrderServiceStub, alertService: () => new AlertService() },
      });
      comp = wrapper.vm;
    });

    describe('OnInit', () => {
      it('Should call load all on init', async () => {
        // GIVEN
        const foundPurchaseOrder = { id: 123 };
        purchaseOrderServiceStub.find.resolves(foundPurchaseOrder);

        // WHEN
        comp.retrievePurchaseOrder(123);
        await comp.$nextTick();

        // THEN
        expect(comp.purchaseOrder).toBe(foundPurchaseOrder);
      });
    });

    describe('Before route enter', () => {
      it('Should retrieve data', async () => {
        // GIVEN
        const foundPurchaseOrder = { id: 123 };
        purchaseOrderServiceStub.find.resolves(foundPurchaseOrder);

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
