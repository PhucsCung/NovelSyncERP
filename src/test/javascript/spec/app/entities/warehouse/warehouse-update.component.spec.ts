/* tslint:disable max-line-length */
import { shallowMount, createLocalVue, Wrapper } from '@vue/test-utils';
import sinon, { SinonStubbedInstance } from 'sinon';
import Router from 'vue-router';
import { ToastPlugin } from 'bootstrap-vue';

import * as config from '@/shared/config/config';
import WarehouseUpdateComponent from '@/entities/warehouse/warehouse-update.vue';
import WarehouseClass from '@/entities/warehouse/warehouse-update.component';
import WarehouseService from '@/entities/warehouse/warehouse.service';

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
  describe('Warehouse Management Update Component', () => {
    let wrapper: Wrapper<WarehouseClass>;
    let comp: WarehouseClass;
    let warehouseServiceStub: SinonStubbedInstance<WarehouseService>;

    beforeEach(() => {
      warehouseServiceStub = sinon.createStubInstance<WarehouseService>(WarehouseService);

      wrapper = shallowMount<WarehouseClass>(WarehouseUpdateComponent, {
        store,
        i18n,
        localVue,
        router,
        provide: {
          warehouseService: () => warehouseServiceStub,
          alertService: () => new AlertService(),
        },
      });
      comp = wrapper.vm;
    });

    describe('save', () => {
      it('Should call update service on save for existing entity', async () => {
        // GIVEN
        const entity = { id: 123 };
        comp.warehouse = entity;
        warehouseServiceStub.update.resolves(entity);

        // WHEN
        comp.save();
        await comp.$nextTick();

        // THEN
        expect(warehouseServiceStub.update.calledWith(entity)).toBeTruthy();
        expect(comp.isSaving).toEqual(false);
      });

      it('Should call create service on save for new entity', async () => {
        // GIVEN
        const entity = {};
        comp.warehouse = entity;
        warehouseServiceStub.create.resolves(entity);

        // WHEN
        comp.save();
        await comp.$nextTick();

        // THEN
        expect(warehouseServiceStub.create.calledWith(entity)).toBeTruthy();
        expect(comp.isSaving).toEqual(false);
      });
    });

    describe('Before route enter', () => {
      it('Should retrieve data', async () => {
        // GIVEN
        const foundWarehouse = { id: 123 };
        warehouseServiceStub.find.resolves(foundWarehouse);
        warehouseServiceStub.retrieve.resolves([foundWarehouse]);

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
