jest.mock('./skipchain');

fetch = jest.fn();

import SkipChainService from './skipchain'
import {hex2buf} from '../utils/buffer'
import {GenesisService} from './genesis'

describe('services:genesis', () => {

  beforeEach(() => {
    SkipChainService.getLatestBlock.mockClear();
    fetch.mockClear();
    fetch.mockReturnValue(Promise.resolve({
      json() {
        return Promise.resolve({
          Blocks: [{
            GenesisID: '00',
            Data: 'https://google.ch',
            Servers: []
          }, {
            GenesisID: '11',
            Data: '',
            Servers: ['127.0.0.1:7000']
          }]
        });
      }
    }));
  });

  it('should manage the listeners', () => {
    const service = new GenesisService();
    const listener = {
      onGenesisUpdate: jest.fn(),
      onGenesisError: jest.fn()
    };

    service.subscribe(listener);
    expect(service.listeners.indexOf(listener)).toBe(0);

    service.updateGenesis();
    expect(listener.onGenesisUpdate).toHaveBeenCalledTimes(2);

    service.updateGenesis(new Error());
    expect(listener.onGenesisError).toHaveBeenCalledTimes(1);

    service.unsubscribe(listener);
    expect(service.listeners).toHaveLength(0);

    service.error = new Error();
    service.subscribe(listener);
    expect(listener.onGenesisError).toHaveBeenCalledTimes(2);

  });

  it('should update the genesis id', () => {
    SkipChainService.getLatestBlock.mockReturnValue(Promise.resolve());

    const service = new GenesisService();

    return service._fetch_request.then(() => {
      service.setCurrentGenesisID('11');
    });
  });

  it('should handle the error', () => {
    expect.assertions(1);

    SkipChainService.getLatestBlock.mockReturnValue(Promise.reject(new Error()));

    const service = new GenesisService();

    return new Promise((resolve) => {
      service._fetch_request.then(() => {
        service.setCurrentGenesisID('11');

        service._request.then(() => {
          expect(service.error).toBeDefined();
          resolve();
        });
      });
    });
  });

  it('should handle no block', () => {
    expect.assertions(1);

    const service = new GenesisService();

    return service._fetch_request.then(() => {
      service._fetchStatusForGenesisID('');

      expect(service.error).toBeDefined();
    });
  });

  it('should get the latest block with a genesis id', () => {
    fetch.mockReturnValue(Promise.resolve({
      json: () => Promise.resolve({
        GenesisID: '00',
        Servers: []
      })
    }));

    expect.assertions(1);

    SkipChainService.getLatestBlock.mockReturnValue(Promise.resolve([{
      Hash: hex2buf('00')
    }]));

    const service = new GenesisService();

    return service.getLatestFromGenesisID('00', '00').then(block => {
      expect(block).toBeDefined();
    });
  });

  it('should get the latest block', () => {
    fetch.mockReturnValue(Promise.resolve({
      json: () => Promise.resolve({
        GenesisID: '00',
        Servers: []
      })
    }));

    expect.assertions(1);

    const block = {};
    SkipChainService.getLatestBlock.mockReturnValue(Promise.resolve([{}, {}, {}, block]));

    const service = new GenesisService();

    return service.getLatestFromGenesisID('00').then(b => {
      expect(b).toBe(block);
    });
  });

  it('should reject when getting the latest block', () => {
    fetch.mockReturnValue(Promise.resolve({
      json: () => Promise.resolve({
        GenesisID: '00',
        Servers: []
      })
    }));

    expect.assertions(1);

    const error = new Error();
    SkipChainService.getLatestBlock.mockReturnValue(Promise.reject(error));

    const service = new GenesisService();
    return service.getLatestFromGenesisID('00').then(() => {}, e => {
      expect(e).toBe(error);
    });
  });

  it('should reject on fetch error', () => {
    const error = new Error();
    fetch.mockReturnValue(Promise.reject(error));

    expect.assertions(1);

    const service = new GenesisService();
    return service.getLatestFromGenesisID('00').then(() => {}, e => {
      expect(e).toBe(error);
    });
  });

});