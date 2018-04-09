import React from 'react'
import {mount} from 'enzyme'

import ModuleSign from './module-sign'

describe(ModuleSign, () => {
  
  it('should render without crashing', () => {
    const wrapper = mount(<ModuleSign/>);
    expect(wrapper.hasClass("module-sign")).toBeTruthy();
  });
  
  it('should manage drop and back', () => {
    const wrapper = mount(<ModuleSign/>);
    
    const file = new File([], '');
    wrapper.instance().handleFileDropped(file);
    expect(wrapper.instance().state.file).toBe(file);
    
    wrapper.instance().handleBack();
    expect(wrapper.instance().state.file).toBeUndefined();
  });
  
});