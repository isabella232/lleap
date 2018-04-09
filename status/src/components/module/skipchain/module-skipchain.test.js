jest.mock('../../../services/genesis');

import React from 'react'
import {mount} from 'enzyme'
import Faker from 'faker'
import {Input} from 'reactstrap'
import ByteBuffer from 'bytebuffer'

import ModuleSkipchain from './module-skipchain'
import GenesisService from '../../../services/genesis'

describe('components:module:skipchain:module-skipchain', () => {

  beforeEach(() => {
    GenesisService.subscribe = (listener) => {
      listener.onGenesisUpdate([], [{GenesisID: 'id1', Data: generateData()},{GenesisID: 'id2'}], '');
    };

    GenesisService.unsubscribe = jest.fn();

    GenesisService.setCurrentGenesisID = jest.fn();
  });

  it('should render without crashing', () => {
    const wrapper = mount(<ModuleSkipchain/>);

    expect(wrapper.hasClass("module-skip-chain")).toBeTruthy();

    wrapper.instance().componentWillUnmount();
    expect(GenesisService.unsubscribe).toHaveBeenCalledTimes(1);
  });

  it('should display the list of genesis blocks', () => {
    const wrapper = mount(<ModuleSkipchain/>);

    expect(wrapper.text()).toContain('id1');
    expect(wrapper.text()).toContain('id2');

    wrapper.find(Input).at(1).simulate('change');

    expect(GenesisService.setCurrentGenesisID).toBeCalledWith('id2');
  });

  it('should show the errors', () => {
    const error = Faker.lorem.sentence();
    GenesisService.subscribe = (listener) => {
      listener.onGenesisError(new Error(error));
    };

    const wrapper = mount(<ModuleSkipchain/>);

    expect(wrapper.text()).toContain(error);
  });

});

function generateData() {
  return ByteBuffer.fromHex('00000000000000000000000000000000000000000000').toArrayBuffer();
}