/* tslint:disable max-line-length */
import { shallowMount, createLocalVue, Wrapper } from '@vue/test-utils';
import sinon, { SinonStubbedInstance } from 'sinon';
import { ToastPlugin } from 'bootstrap-vue';

import * as config from '@/shared/config/config';
import InventoryBalanceComponent from '@/entities/inventory-balance/inventory-balance.vue';
import InventoryBalanceClass from '@/entities/inventory-balance/inventory-balance.component';
import InventoryBalanceService from '@/entities/inventory-balance/inventory-balance.service';
import AlertService from '@/shared/alert/alert.service';

const localVue = createLocalVue();
localVue.use(ToastPlugin);

config.initVueApp(localVue);
const i18n = config.initI18N(localVue);
const store = config.initVueXStore(localVue);
localVue.component('font-awesome-icon', {});
localVue.component('b-badge', {});
localVue.component('jhi-sort-indicator', {});
localVue.directive('b-modal', {});
localVue.component('b-button', {});
localVue.component('router-link', {});

const bModalStub = {
  render: () => {},
  methods: {
    hide: () => {},
    show: () => {},
  },
};

describe('Component Tests', () => {
  describe('InventoryBalance Management Component', () => {
    let wrapper: Wrapper<InventoryBalanceClass>;
    let comp: InventoryBalanceClass;
    let inventoryBalanceServiceStub: SinonStubbedInstance<InventoryBalanceService>;

    beforeEach(() => {
      inventoryBalanceServiceStub = sinon.createStubInstance<InventoryBalanceService>(InventoryBalanceService);
      inventoryBalanceServiceStub.retrieve.resolves({ headers: {} });

      wrapper = shallowMount<InventoryBalanceClass>(InventoryBalanceComponent, {
        store,
        i18n,
        localVue,
        stubs: { jhiItemCount: true, bPagination: true, bModal: bModalStub as any },
        provide: {
          inventoryBalanceService: () => inventoryBalanceServiceStub,
          alertService: () => new AlertService(),
        },
      });
      comp = wrapper.vm;
    });

    it('Should call load all on init', async () => {
      // GIVEN
      inventoryBalanceServiceStub.retrieve.resolves({ headers: {}, data: [{ id: 123 }] });

      // WHEN
      comp.retrieveAllInventoryBalances();
      await comp.$nextTick();

      // THEN
      expect(inventoryBalanceServiceStub.retrieve.called).toBeTruthy();
      expect(comp.inventoryBalances[0]).toEqual(expect.objectContaining({ id: 123 }));
    });

    it('should load a page', async () => {
      // GIVEN
      inventoryBalanceServiceStub.retrieve.resolves({ headers: {}, data: [{ id: 123 }] });
      comp.previousPage = 1;

      // WHEN
      comp.loadPage(2);
      await comp.$nextTick();

      // THEN
      expect(inventoryBalanceServiceStub.retrieve.called).toBeTruthy();
      expect(comp.inventoryBalances[0]).toEqual(expect.objectContaining({ id: 123 }));
    });

    it('should not load a page if the page is the same as the previous page', () => {
      // GIVEN
      inventoryBalanceServiceStub.retrieve.reset();
      comp.previousPage = 1;

      // WHEN
      comp.loadPage(1);

      // THEN
      expect(inventoryBalanceServiceStub.retrieve.called).toBeFalsy();
    });

    it('should re-initialize the page', async () => {
      // GIVEN
      inventoryBalanceServiceStub.retrieve.reset();
      inventoryBalanceServiceStub.retrieve.resolves({ headers: {}, data: [{ id: 123 }] });

      // WHEN
      comp.loadPage(2);
      await comp.$nextTick();
      comp.clear();
      await comp.$nextTick();

      // THEN
      expect(inventoryBalanceServiceStub.retrieve.callCount).toEqual(3);
      expect(comp.page).toEqual(1);
      expect(comp.inventoryBalances[0]).toEqual(expect.objectContaining({ id: 123 }));
    });

    it('should calculate the sort attribute for an id', () => {
      // WHEN
      const result = comp.sort();

      // THEN
      expect(result).toEqual(['id,asc']);
    });

    it('should calculate the sort attribute for a non-id attribute', () => {
      // GIVEN
      comp.propOrder = 'name';

      // WHEN
      const result = comp.sort();

      // THEN
      expect(result).toEqual(['name,asc', 'id']);
    });
    it('Should call delete service on confirmDelete', async () => {
      // GIVEN
      inventoryBalanceServiceStub.delete.resolves({});

      // WHEN
      comp.prepareRemove({ id: 123 });
      expect(inventoryBalanceServiceStub.retrieve.callCount).toEqual(1);

      comp.removeInventoryBalance();
      await comp.$nextTick();

      // THEN
      expect(inventoryBalanceServiceStub.delete.called).toBeTruthy();
      expect(inventoryBalanceServiceStub.retrieve.callCount).toEqual(2);
    });
  });
});
