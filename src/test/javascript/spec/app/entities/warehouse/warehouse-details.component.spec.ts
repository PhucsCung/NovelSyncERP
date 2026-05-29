/* tslint:disable max-line-length */
import { shallowMount, createLocalVue, Wrapper } from '@vue/test-utils';
import sinon, { SinonStubbedInstance } from 'sinon';
import VueRouter from 'vue-router';

import * as config from '@/shared/config/config';
import WarehouseDetailComponent from '@/entities/warehouse/warehouse-details.vue';
import WarehouseClass from '@/entities/warehouse/warehouse-details.component';
import WarehouseService from '@/entities/warehouse/warehouse.service';
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
  describe('Warehouse Management Detail Component', () => {
    let wrapper: Wrapper<WarehouseClass>;
    let comp: WarehouseClass;
    let warehouseServiceStub: SinonStubbedInstance<WarehouseService>;

    beforeEach(() => {
      warehouseServiceStub = sinon.createStubInstance<WarehouseService>(WarehouseService);

      wrapper = shallowMount<WarehouseClass>(WarehouseDetailComponent, {
        store,
        i18n,
        localVue,
        router,
        provide: { warehouseService: () => warehouseServiceStub, alertService: () => new AlertService() },
      });
      comp = wrapper.vm;
    });

    describe('OnInit', () => {
      it('Should call load all on init', async () => {
        // GIVEN
        const foundWarehouse = { id: 123 };
        warehouseServiceStub.find.resolves(foundWarehouse);

        // WHEN
        comp.retrieveWarehouse(123);
        await comp.$nextTick();

        // THEN
        expect(comp.warehouse).toBe(foundWarehouse);
      });
    });

    describe('Before route enter', () => {
      it('Should retrieve data', async () => {
        // GIVEN
        const foundWarehouse = { id: 123 };
        warehouseServiceStub.find.resolves(foundWarehouse);

        // WHEN
        comp.beforeRouteEnter({ params: { warehouseId: 123 } }, null, cb => cb(comp));
        await comp.$nextTick();

        // THEN
        expect(comp.warehouse).toBe(foundWarehouse);
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
