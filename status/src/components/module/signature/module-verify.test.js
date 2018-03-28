jest.mock('../../../services/genesis');

import React from 'react'
import Faker from 'faker'
import {mount} from 'enzyme'

import ModuleVerify from './module-verify'
import GenesisService from '../../../services/genesis'

global.FileReader = class {
  readAsText() {
    this.onload({
      target: {
        result: JSON.stringify({
          signature: '000000',
          hash: '11',
          genesisID: 'aa',
          blockID: 'bb',
          offlineServers: ['0.0.0.0:1000']
        })
      }
    });
  }

  readAsArrayBuffer() {
    this.onload({target: {result: new Uint8Array([])}});
  }
};
global.cryptoJS = {
  sha256() {
    return 'super-hash'
  },
  verify() {
    return true
  },
  aggregateKeys() {
    return new Uint8Array([]);
  }
};

describe('components:module:signature:module-verify', () => {

  const MOCK_FILE = new File([], '');

  beforeEach(() => {
    GenesisService.getLatestFromGenesisID.mockClear();
    GenesisService.getLatestFromGenesisID.mockReturnValue(Promise.resolve({
      Data: new Uint8Array([0, 0, 0, 0]),
      Roster: {
        list: [{address: '1.1.1.1:1000'},{address: '1.1.1.1:1002'},{address: '1.1.1.1:1004'}]
      }
    }));
  });

  it('should render without crashing', () => {
    const wrapper = mount(<ModuleVerify/>);
    expect(wrapper.hasClass("module-verify")).toBeTruthy();
  });

  it('should handle the dropping', () => {
    const wrapper = mount(<ModuleVerify/>);
    wrapper.instance().handleFileDrop(MOCK_FILE);

    expect(wrapper.instance().state.file).toBe(MOCK_FILE);
  });

  it('should verify the signature', () => {
    expect.assertions(1);

    const wrapper = mount(<ModuleVerify/>);
    wrapper.instance().handleFileDrop(MOCK_FILE);
    return wrapper.instance().handleFileDrop(MOCK_FILE)
      .then(() => {
        expect(wrapper.instance().state.isSignatureCorrect).toBeTruthy();
      });
  });

  it('should fail to verify the signature', () => {
    const error = new Error("error");
    GenesisService.getLatestFromGenesisID.mockClear();
    GenesisService.getLatestFromGenesisID.mockReturnValue(Promise.reject(error));

    expect.assertions(1);

    const wrapper = mount(<ModuleVerify/>);
    wrapper.instance().handleFileDrop(MOCK_FILE);

    return wrapper.instance().handleFileDrop(MOCK_FILE).then(() => {
      expect(wrapper.state('error')).toBe(error.message);
    });
  });

  it('should reset the state', () => {
    const wrapper = mount(<ModuleVerify/>);
    wrapper.instance().handleFileDrop(MOCK_FILE);
    expect(wrapper.instance().state.file).toBeDefined();

    wrapper.instance().handleFileReset();
    expect(wrapper.instance().state.file).toBeUndefined();
  });

  it('should display the error', () => {
    const wrapper = mount(<ModuleVerify/>);
    const error = Faker.lorem.sentence();
    wrapper.setState({error: error});

    expect(wrapper.text()).toContain(error);
  });

});