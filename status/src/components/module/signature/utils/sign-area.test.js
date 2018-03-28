jest.mock('../../../../services/status');
jest.mock('../../../../services/websocket');
jest.mock('../../../../services/genesis');

import React from 'react'
import {mount} from 'enzyme'
import {Button} from 'reactstrap'

import SignArea from './sign-area'
import CothorityWebsocket from '../../../../services/websocket'
import StatusService from '../../../../services/status'
import GenesisService from '../../../../services/genesis'

global.FileReader = class {
  readAsArrayBuffer() {
    this.onload({target: {result: new Uint8Array([])}});
  }
};
global.cryptoJS = {
  sha256: () => 'super-hash',
  aggregateKeys: () => 'super-key'
};

global.saveAs = jest.fn();

describe('module:signature:sign-area', () => {
  
  const MOCK_FILE = new File([], '');

  beforeEach(() => {
    StatusService.getAvailableRoster.mockClear();
    StatusService.getAvailableRoster.mockReturnValue([{
      system: {},
      server: {address: '127.0.0.1:7000'}
    }]);
    CothorityWebsocket.getSignature = jest.fn();
    GenesisService.unsubscribe.mockClear();
  });
  
  it('should render without crashing', () => {
    const wrapper = mount(<SignArea file={MOCK_FILE}/>);
    
    expect(wrapper.hasClass("sign-area")).toBeTruthy();
  });

  it('should subscribe and unsubscribe', () => {
    const wrapper = mount(<SignArea file={MOCK_FILE}/>);

    wrapper.instance().componentWillUnmount();
    expect(GenesisService.unsubscribe).toHaveBeenCalledTimes(1);
  });
  
  it('should trigger back event', () => {
    const onBack = jest.fn();
    const wrapper = mount(<SignArea file={MOCK_FILE} onBack={onBack}/>);
    
    wrapper.find(Button).at(0).simulate('click');
    
    expect(onBack).toHaveBeenCalledTimes(1);
  });
  
  it('should sign the file', () => {
    expect.assertions(1);

    CothorityWebsocket.getSignature.mockReturnValue(Promise.resolve({signature: 'super-signature'}));
    const wrapper = mount(<SignArea file={MOCK_FILE}/>);
    wrapper.setState({
      block: {Roster: {list: [{}]}}
    });
    
    wrapper.find(Button).at(1).simulate('click');
    
    return wrapper.instance()._signPromise.then(() => {
      expect(saveAs).toHaveBeenCalledTimes(1);
    });
  });

  it('should not sign the file', () => {
    expect.assertions(1);

    CothorityWebsocket.getSignature.mockReturnValue(Promise.resolve({signature: 'super-signature'}));
    const wrapper = mount(<SignArea file={MOCK_FILE}/>);
    wrapper.setState({
      block: {Roster: {list: [{},{},{},{}]}}
    });

    wrapper.find(Button).at(1).simulate('click');

    return wrapper.instance()._signPromise.catch((e) => {
      expect(e).toContain('Not enough');
    });
  });
  
  it('should display an error because of ws', () => {
    expect.assertions(1);
    
    CothorityWebsocket.getSignature.mockReturnValue(Promise.reject());

    const wrapper = mount(<SignArea file={MOCK_FILE}/>);
    wrapper.setState({
      block: {Roster: {list: [{}]}}
    });
    
    wrapper.find(Button).at(1).simulate('click');
    
    return wrapper.instance()._signPromise.catch(() => {
      expect(wrapper.text()).toContain('Oops');
    });
  });
  
});