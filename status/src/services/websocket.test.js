import {CothorityWebsocket} from './websocket'
import CothorityMessages from '../lib/cothority-messages'
import Faker from 'faker'
import {Server} from 'mock-socket'

const MOCK_STATUS_RESPONSE = {
  server: {
    address: Faker.internet.ip(),
    description: Faker.lorem.sentence(),
    id: new Uint8Array([0, 0, 0, 0]),
    public: new Uint8Array([1, 1, 1, 1])
  },
  system: {
    Status: {
      field: {
        test: Faker.lorem.sentence()
      }
    }
  }
};

describe('services:websocket', () => {

  beforeAll(() => {
    const mockServer = new Server('ws://localhost/Status/Request');
    mockServer.on('message', () => {
      mockServer.send(CothorityMessages.encodeMessage('StatusResponse', MOCK_STATUS_RESPONSE));
    });

    const mockErrorServer = new Server('ws://localhost:1/Status/Request');
    mockErrorServer.on('message', () => {
      mockErrorServer.emit('error');
    });
  });

  it('should create and persist the socket', () => {
    expect.assertions(2);
    const service = new CothorityWebsocket();

    return new Promise((resolve) => {
      service.getStatus('localhost').then(() => {
        const socket = service.status.localhost;
        expect(socket).toBeDefined();

        service.getStatus('localhost').then(() => {
          expect(service.status.localhost).toBe(socket);

          resolve();
        });
      });
    });
  });

  it('should announce an error', () => {
    expect.assertions(1);
    const service = new CothorityWebsocket();

    return service.getStatus('localhost:1').catch(() => {
      expect(true).toBeTruthy();
    });
  });

  it('should create a new one in case of error', () => {
    expect.assertions(2);
    const service = new CothorityWebsocket();

    return new Promise((resolve) => {
      service.getStatus('localhost').then(() => {
        const socket = service.status.localhost;
        socket.close();

        service.getStatus('localhost').then(() => {
          expect(service.status.localhost).toBeDefined();
          expect(service.status.localhost !== socket).toBeTruthy();

          resolve();
        });
      });
    });
  });

  it('should decode a status response', () => {
    const service = new CothorityWebsocket();

    return service.getStatus('localhost').then((status) => {
      expect(status).toBeDefined();
      expect(status.server.address).toBe(MOCK_STATUS_RESPONSE.server.address);
      expect(status.server.description).toBe(MOCK_STATUS_RESPONSE.server.description);
      expect(status.server.id.toString()).toBe(MOCK_STATUS_RESPONSE.server.id.toString());
      expect(status.server.public.toString()).toBe(MOCK_STATUS_RESPONSE.server.public.toString());
      expect(status.system.Status.field.test).toBe(MOCK_STATUS_RESPONSE.system.Status.field.test);
    });
  });

  it('should handle a signature request', () => {
    expect.assertions(1);

    const service = new CothorityWebsocket();
    const address = Faker.internet.ip();
    const server = new Server(`ws://${address}/CoSi/SignatureRequest`);
    server.on('message', () => server.send(new Uint8Array([])));

    return service.getSignature(new Uint8Array([]), address, []).then((m) => {
      expect(m).toBeDefined();
    });
  });

  it('should handle a signature request error', () => {
    expect.assertions(1);

    const service = new CothorityWebsocket();
    const address = Faker.internet.ip();
    const server = new Server(`ws://${address}/CoSi/SignatureRequest`);
    server.on('message', () => server.emit('error'));

    return service.getSignature(new Uint8Array([]), address, []).catch((e) => {
      expect(e).toBeDefined();
    });
  });

  it('should send a store request', () => {
    expect.assertions(1);
    const service = new CothorityWebsocket();

    const server = new Server('ws://localhost/Skipchain/StoreSkipBlock');
    server.on('message', () => {
      server.send(new Uint8Array([]));
    });

    return service.storeNewBlock('localhost', new Uint8Array([]), [])
      .then((m) => {
        expect(m).toBeDefined();
      });
  });

  it('should manage a failed store request', () => {
    expect.assertions(1);
    const service = new CothorityWebsocket();

    const addr = Faker.internet.ip();
    const server = new Server(`ws://${addr}/Skipchain/StoreSkipBlock`);
    server.on('message', () => {
      server.emit('error');
    });

    return service.storeNewBlock(addr, new Uint8Array([]), [])
      .catch((e) => {
        expect(e).toBeDefined();
      });
  });

  it('should get the latest block', () => {
    expect.assertions(1);

    const service = new CothorityWebsocket();
    const address = Faker.internet.ip();
    const server = new Server(`ws://${address}/Skipchain/GetBlocks`);
    server.on('message', () => server.send(new Uint8Array([])));

    return service.getLatestBlock(address, new Uint8Array([])).then((m) => {
      expect(m).toBeDefined();
    });
  });

  it('should handle error for latest block requests', () => {
    expect.assertions(1);

    const service = new CothorityWebsocket();
    const address = Faker.internet.ip();
    const server = new Server(`ws://${address}/Skipchain/GetBlocks`);
    server.on('message', () => server.emit('error'));

    return service.getLatestBlock(address, new Uint8Array([])).catch((e) => {
      expect(e).toBeDefined();
    });
  });

  it('should get a random number', () => {
    expect.assertions(1);

    const service = new CothorityWebsocket();
    const address = Faker.internet.ip();
    const server = new Server(`ws://${address}/RandHound/RandRequest`);
    server.on('message', () => server.send(new Uint8Array([])));

    return service.getRandom(address).then((msg) => {
      expect(msg).toBeDefined();
    });
  });

  it('should manage an error when getting the random number', () => {
    expect.assertions(1);

    const service = new CothorityWebsocket();
    const address = Faker.internet.ip();
    const server = new Server(`ws://${address}/RandHound/RandRequest`);
    server.on('message', () => server.emit('error'));

    return service.getRandom(address).catch(e => expect(e).toBeDefined());
  });

});