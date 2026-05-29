/* tslint:disable max-line-length */
import { shallowMount, createLocalVue, Wrapper } from '@vue/test-utils';
import sinon, { SinonStubbedInstance } from 'sinon';
import VueRouter from 'vue-router';

import * as config from '@/shared/config/config';
import InventoryTransactionDetailComponent from '@/entities/inventory-transaction/inventory-transaction-details.vue';
import InventoryTransactionClass from '@/entities/inventory-transaction/inventory-transaction-details.component';
import InventoryTransactionService from '@/entities/inventory-transaction/inventory-transaction.service';
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
  describe('InventoryTransaction Management Detail Component', () => {
    let wrapper: Wrapper<InventoryTransactionClass>;
    let comp: InventoryTransactionClass;
    let inventoryTransactionServiceStub: SinonStubbedInstance<InventoryTransactionService>;

    beforeEach(() => {
      inventoryTransactionServiceStub = sinon.createStubInstance<InventoryTransactionService>(InventoryTransactionService);

      wrapper = shallowMount<InventoryTransactionClass>(InventoryTransactionDetailComponent, {
        store,
        i18n,
        localVue,
        router,
        provide: { inventoryTransactionService: () => inventoryTransactionServiceStub, alertService: () => new AlertService() },
      });
      comp = wrapper.vm;
    });

    describe('OnInit', () => {
      it('Should call load all on init', async () => {
        // GIVEN
        const foundInventoryTransaction = { id: 123 };
        inventoryTransactionServiceStub.find.resolves(foundInventoryTransaction);

        // WHEN
        comp.retrieveInventoryTransaction(123);
        await comp.$nextTick();

        // THEN
        expect(comp.inventoryTransaction).toBe(foundInventoryTransaction);
      });
    });

    describe('Before route enter', () => {
      it('Should retrieve data', async () => {
        // GIVEN
        const foundInventoryTransaction = { id: 123 };
        inventoryTransactionServiceStub.find.resolves(foundInventoryTransaction);

        // WHEN
        comp.beforeRouteEnter({ params: { inventoryTransactionId: 123 } }, null, cb => cb(comp));
        await comp.$nextTick();

        // THEN
        expect(comp.inventoryTransaction).toBe(foundInventoryTransaction);
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
