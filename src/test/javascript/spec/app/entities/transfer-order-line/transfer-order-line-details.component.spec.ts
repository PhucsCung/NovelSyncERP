/* tslint:disable max-line-length */
import { shallowMount, createLocalVue, Wrapper } from '@vue/test-utils';
import sinon, { SinonStubbedInstance } from 'sinon';
import VueRouter from 'vue-router';

import * as config from '@/shared/config/config';
import TransferOrderLineDetailComponent from '@/entities/transfer-order-line/transfer-order-line-details.vue';
import TransferOrderLineClass from '@/entities/transfer-order-line/transfer-order-line-details.component';
import TransferOrderLineService from '@/entities/transfer-order-line/transfer-order-line.service';
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
  describe('TransferOrderLine Management Detail Component', () => {
    let wrapper: Wrapper<TransferOrderLineClass>;
    let comp: TransferOrderLineClass;
    let transferOrderLineServiceStub: SinonStubbedInstance<TransferOrderLineService>;

    beforeEach(() => {
      transferOrderLineServiceStub = sinon.createStubInstance<TransferOrderLineService>(TransferOrderLineService);

      wrapper = shallowMount<TransferOrderLineClass>(TransferOrderLineDetailComponent, {
        store,
        i18n,
        localVue,
        router,
        provide: { transferOrderLineService: () => transferOrderLineServiceStub, alertService: () => new AlertService() },
      });
      comp = wrapper.vm;
    });

    describe('OnInit', () => {
      it('Should call load all on init', async () => {
        // GIVEN
        const foundTransferOrderLine = { id: 123 };
        transferOrderLineServiceStub.find.resolves(foundTransferOrderLine);

        // WHEN
        comp.retrieveTransferOrderLine(123);
        await comp.$nextTick();

        // THEN
        expect(comp.transferOrderLine).toBe(foundTransferOrderLine);
      });
    });

    describe('Before route enter', () => {
      it('Should retrieve data', async () => {
        // GIVEN
        const foundTransferOrderLine = { id: 123 };
        transferOrderLineServiceStub.find.resolves(foundTransferOrderLine);

        // WHEN
        comp.beforeRouteEnter({ params: { transferOrderLineId: 123 } }, null, cb => cb(comp));
        await comp.$nextTick();

        // THEN
        expect(comp.transferOrderLine).toBe(foundTransferOrderLine);
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
