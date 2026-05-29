/* tslint:disable max-line-length */
import { shallowMount, createLocalVue, Wrapper } from '@vue/test-utils';
import sinon, { SinonStubbedInstance } from 'sinon';
import VueRouter from 'vue-router';

import * as config from '@/shared/config/config';
import TransferOrderDetailComponent from '@/entities/transfer-order/transfer-order-details.vue';
import TransferOrderClass from '@/entities/transfer-order/transfer-order-details.component';
import TransferOrderService from '@/entities/transfer-order/transfer-order.service';
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
  describe('TransferOrder Management Detail Component', () => {
    let wrapper: Wrapper<TransferOrderClass>;
    let comp: TransferOrderClass;
    let transferOrderServiceStub: SinonStubbedInstance<TransferOrderService>;

    beforeEach(() => {
      transferOrderServiceStub = sinon.createStubInstance<TransferOrderService>(TransferOrderService);

      wrapper = shallowMount<TransferOrderClass>(TransferOrderDetailComponent, {
        store,
        i18n,
        localVue,
        router,
        provide: { transferOrderService: () => transferOrderServiceStub, alertService: () => new AlertService() },
      });
      comp = wrapper.vm;
    });

    describe('OnInit', () => {
      it('Should call load all on init', async () => {
        // GIVEN
        const foundTransferOrder = { id: 123 };
        transferOrderServiceStub.find.resolves(foundTransferOrder);

        // WHEN
        comp.retrieveTransferOrder(123);
        await comp.$nextTick();

        // THEN
        expect(comp.transferOrder).toBe(foundTransferOrder);
      });
    });

    describe('Before route enter', () => {
      it('Should retrieve data', async () => {
        // GIVEN
        const foundTransferOrder = { id: 123 };
        transferOrderServiceStub.find.resolves(foundTransferOrder);

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
