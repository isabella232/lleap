jest.mock('../../services/iframe');

import React from 'react'
import {mount} from 'enzyme'

import HTMLIFrame from './html-iframe'
import IFrameService from '../../services/iframe'

describe('components:html-iframe:html-iframe', () => {

  beforeEach(() => {
    IFrameService.subscribe.mockClear();
    IFrameService.unsubscribe.mockClear();
    IFrameService.back.mockClear();
  });

  it('should render without crashing', () => {
    const wrapper = mount(<HTMLIFrame/>);
    wrapper.instance().onOpenHTML('test');

    expect(wrapper.hasClass("block-iframe")).toBeTruthy();
  });

  it('should subscribe and unsubscribe', () => {
    const wrapper = mount(<HTMLIFrame/>);

    expect(IFrameService.subscribe).toHaveBeenCalledTimes(1);

    wrapper.unmount();
    expect(IFrameService.unsubscribe).toHaveBeenCalledTimes(1);
  });

  it('should hide the iframe', () => {
    const wrapper = mount(<HTMLIFrame/>);
    wrapper.setState({
      html: 'test'
    });

    wrapper.find('a').simulate('click');
    expect(IFrameService.back).toHaveBeenCalledTimes(1);
    wrapper.instance().onCloseHTML();

    expect(wrapper.state('html')).toBeNull();
    expect(wrapper.find('iframe').exists()).toBeFalsy();
  });

});