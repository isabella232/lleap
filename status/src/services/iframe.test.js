jest.mock('./genesis');

import {IFrameService} from './iframe'

describe('services:iframe', () => {

  it('should listen for message', () => {
    const service = new IFrameService();
    service.open = jest.fn();

    const event = new CustomEvent('message');
    window.dispatchEvent(event);

    expect(service.open).toHaveBeenCalledTimes(0);

    event.data = 'skipchain://blabla';
    window.dispatchEvent(event);

    expect(service.open).toHaveBeenCalledTimes(1);
  });

  it('should manage the listeners', () => {
    expect.assertions(4);

    const listener = {
      onOpenHTML: jest.fn(),
      onCloseHTML: jest.fn()
    };
    const dummyListener = {};
    const service = new IFrameService();

    service.subscribe(listener);
    service.subscribe(listener);
    expect(service.listeners).toHaveLength(1);

    service.unsubscribe(listener);
    service.unsubscribe(listener);
    expect(service.listeners).toHaveLength(0);

    service.subscribe(listener);
    service.subscribe(dummyListener);
    service.back();
    expect(listener.onCloseHTML).toHaveBeenCalledTimes(1);

    return service.open('1').then(() => {
      expect(listener.onOpenHTML).toHaveBeenCalledTimes(1);
    });
  });

});