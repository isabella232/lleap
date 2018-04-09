import React from 'react'
import {mount} from 'enzyme'

import DropFileArea from './drop-file-area'

describe(DropFileArea, () => {
  
  it('should render without crashing', () => {
    const wrapper = mount(<DropFileArea/>);
    // Check the reference of the input
    wrapper.find('.drop-file-container').simulate('click');
    
    expect(wrapper.hasClass("drop-file-area")).toBeTruthy();
  });
  
  it('should trigger a drop event', () => {
    const onFileDrop = jest.fn();
    const wrapper = mount(<DropFileArea onFileDrop={onFileDrop}/>);
    
    const file = {};
    wrapper.find('input').simulate('change', {target: {files: [file]}});
    expect(onFileDrop).toHaveBeenCalledTimes(1);
    expect(onFileDrop).toBeCalledWith(file);
  });
  
  it('should trigger a drop event', () => {
    const onFileDrop = jest.fn();
    const wrapper = mount(<DropFileArea onFileDrop={onFileDrop}/>);
    
    const file = {};
    wrapper.find('.drop-file-container').simulate('drop', {dataTransfer: {files: [file]}});
    
    expect(onFileDrop).toHaveBeenCalledTimes(1);
    expect(onFileDrop).toBeCalledWith(file);
  });
  
  it('should prevent event when drag over', () => {
    const wrapper = mount(<DropFileArea/>);
    const e = {
      preventDefault: jest.fn()
    };
    wrapper.find('.drop-file-container').simulate('dragOver', e);
    
    expect(e.preventDefault).toHaveBeenCalledTimes(1);
  })
  
});