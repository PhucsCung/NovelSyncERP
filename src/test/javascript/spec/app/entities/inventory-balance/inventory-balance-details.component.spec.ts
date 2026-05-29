/* tslint:disable max-line-length */
import { shallowMount, createLocalVue, Wrapper } from '@vue/test-utils';
import sinon, { SinonStubbedInstance } from 'sinon';
import VueRouter from 'vue-router';

import * as config from '@/shared/config/config';
import InventoryBalanceDetailComponent from '@/entities/inventory-balance/inventory-balance-details.vue';
import InventoryBalanceClass from '@/entities/inventory-balance/inventory-balance-details.component';
import InventoryBalanceService from '@/entities/inventory-balance/inventory-balance.service';
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
  describe('InventoryBalance Management Detail Component', () => {
    let wrapper: Wrapper<InventoryBalanceClass>;
    let comp: InventoryBalanceClass;
    let inventoryBalanceServiceStub: SinonStubbedInstance<InventoryBalanceService>;

    beforeEach(() => {
      inventoryBalanceServiceStub = sinon.createStubInstance<InventoryBalanceService>(InventoryBalanceService);

      wrapper = shallowMount<InventoryBalanceClass>(InventoryBalanceDetailComponent, {
        store,
        i18n,
        localVue,
        router,
        provide: { inventoryBalanceService: () => inventoryBalanceServiceStub, alertService: () => new AlertService() },
      });
      comp = wrapper.vm;
    });

    describe('OnInit', () => {
      it('Should call load all on init', async () => {
        // GIVEN
        const foundInventoryBalance = { id: 123 };
        inventoryBalanceServiceStub.find.resolves(foundInventoryBalance);

        // WHEN
        comp.retrieveInventoryBalance(123);
        await comp.$nextTick();

        // THEN
        expect(comp.inventoryBalance).toBe(foundInventoryBalance);
      });
    });

    describe('Before route enter', () => {
      it('Should retrieve data', async () => {
        // GIVEN
        const foundInventoryBalance = { id: 123 };
        inventoryBalanceServiceStub.find.resolves(foundInventoryBalance);

        // WHEN
        comp.beforeRouteEnter({ params: { inventoryBalanceId: 123 } }, null, cb => cb(comp));
        await comp.$nextTick();

        // THEN
        expect(comp.inventoryBalance).toBe(foundInventoryBalance);
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
