jest.mock('../../../services/websocket');

import React from 'react'
import {mount} from 'enzyme'

import ModuleRandom from './module-random'
import WebSocketService from '../../../services/websocket'

describe('components:module:random:module-random', () => {

  beforeEach(() => {
    WebSocketService.getRandom.mockClear();
    WebSocketService.getRandom.mockReturnValue(Promise.resolve({
      R: 0,
      T: {
        time: 123456789
      }
    }));
  });

  it('should render without crashing', () => {
    const wrapper = mount(<ModuleRandom/>);

    expect(wrapper.hasClass('module-random')).toBeTruthy();
  });

  it('should manage the counter', () => {
    const wrapper = mount(<ModuleRandom/>);
    wrapper.instance()._timestamp = Date.now();

    wrapper.instance()._checkCountDown();
    expect(wrapper.state('counter')).toBeLessThan(1);

    wrapper.instance()._timestamp = 0;
    wrapper.instance()._triggerRandomUpdate = jest.fn();
    wrapper.instance()._checkCountDown();
    expect(wrapper.instance()._triggerRandomUpdate).toHaveBeenCalledTimes(1);
  });

  it('should show an error', () => {
    expect.assertions(1);

    WebSocketService.getRandom.mockClear();
    WebSocketService.getRandom.mockReturnValue(Promise.reject());

    const wrapper = mount(<ModuleRandom/>);
    return wrapper.instance()._triggerRandomUpdate().then(() => {
      expect(wrapper.state('error')).toBe('Oops, something went wrong.');
    });
  });

});