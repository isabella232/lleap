jest.mock('./websocket');

const MOCK_HASH = new Uint8Array([1, 1, 1, 1]);

global.cryptoJS = {
  hashSkipBlock() {
    return MOCK_HASH;
  },

  verifyForwardLink(o) {
    return !o.signature;
  }
};

import CothorityWS from './websocket'

import {SkipChainService} from './skipchain'

describe(SkipChainService, () => {

  it('get the latest blocks', () => {
    expect.assertions(1);

    const service = new SkipChainService();

    return service.getLatestBlock(['localhost'], 'id').then((m) => {
      expect(m).toBeDefined();
    });
  });

  it('must warn wrong parameters', () => {
    expect.assertions(1);

    const service = new SkipChainService();

    return service.getLatestBlock([], 'id').catch((e) => {
      expect(e).toBeDefined();
    });
  });

  it('should test every server', () => {
    const service = new SkipChainService();
    service._getUpdates = jest.fn().mockReturnValueOnce(Promise.reject()).mockReturnValueOnce(Promise.resolve([]));

    return service.getLatestBlock(['l1', 'l2'], 'id').then((m) => {
      expect(service._getUpdates).toHaveBeenCalledTimes(2);
      expect(m).toBeDefined();
    });
  });

  it('should reject if there is no server available', () => {
    const service = new SkipChainService();
    service._getUpdates = jest.fn().mockReturnValue(Promise.reject());

    return service.getLatestBlock(['l'], 'id').catch((e) => {
      expect(e).toBeDefined();
    });
  });

  it('should verify the block', () => {
    const service = new SkipChainService();

    const blocks = [{
      Hash: MOCK_HASH,
      Roster: {list: []},
      ForwardLink: [{}],
      BackLinkIDs: []
    }, {
      Hash: MOCK_HASH,
      Roster: {list: []},
      ForwardLink: [],
      BackLinkIDs: [MOCK_HASH]
    }];

    expect(service._verifyUpdates(blocks)).toBeTruthy();
  });

  it('should not verify wrong hash', () => {
    const service = new SkipChainService();

    const blocks = [{
      Hash: new Uint8Array([2]),
      Roster: {list: []},
      ForwardLink: [],
      BackLinkIds: []
    }];

    expect(service._verifyUpdates(blocks)).toBeFalsy();
    expect(blocks[0]._hash_verified).toBeFalsy();
  });

  it('should not verify wrong forward links', () => {
    const service = new SkipChainService();

    const blocks = [{
      Hash: MOCK_HASH,
      Roster: {list: []},
      ForwardLink: [{Signature: 'signature'}]
    }];

    expect(service._verifyUpdates(blocks)).toBeFalsy();
    expect(blocks[0]._hash_verified).toBeTruthy();
    expect(blocks[0]._signature_verified).toBeFalsy();
  });

  it('should not verify wrong back-links', () => {
    const service = new SkipChainService();

    const blocks = [{
      Hash: MOCK_HASH,
      Roster: {list: []},
      ForwardLink: [],
      BackLinkIDs: []
    }, {
      Hash: MOCK_HASH,
      Roster: {list: []},
      ForwardLink: [],
      BackLinkIDs: [new Uint8Array([0])]
    },{
      Hash: new Uint8Array([0]),
    }];

    expect(service._verifyUpdates(blocks));
    expect(blocks[1]._hash_verified).toBeTruthy();
    expect(blocks[1]._signature_verified).toBeTruthy();
    expect(blocks[1]._backlink_verified).toBeFalsy();
  });

  it('should handle bad responses', () => {
    expect.assertions(1);

    CothorityWS.getLatestBlock.mockReturnValueOnce(Promise.resolve({}));
    CothorityWS.getLatestBlock.mockReturnValueOnce(Promise.resolve({Update: [{}]}));

    const service = new SkipChainService();

    return service.getLatestBlock([{},{}], 'id').catch((e) => {
      expect(e).toBeDefined();
    });
  });

});