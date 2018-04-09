jest.mock('../../../services/genesis');
jest.mock('../../../services/iframe');

import React from 'react'
import {mount} from 'enzyme'

import ModuleHTML from './module-html'
import IFrameService from '../../../services/iframe'
import GenesisService from '../../../services/genesis'

describe('components:module:html:module-html', () => {

  beforeEach(() => {
    IFrameService.open.mockClear();
    GenesisService.unsubscribe.mockClear();
  });

  it('should render without crashing', () => {
    const wrapper = mount(<ModuleHTML/>);

    expect(wrapper.hasClass("module-html")).toBeTruthy();

    wrapper.instance().componentWillUnmount();
    expect(GenesisService.unsubscribe).toHaveBeenCalledTimes(1);
  });

  it('should open the html page', () => {
    const wrapper = mount(<ModuleHTML/>);

    wrapper.find('.module-html-item').at(0).simulate('click');

    expect(IFrameService.open).toHaveBeenCalledTimes(1);
  });

});