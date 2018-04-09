import React from 'react'
import {mount} from 'enzyme'

import LoadingSpinner from './loading-spinner'

describe(LoadingSpinner, () => {

  it('should render without crashing', () => {
    const wrapper = mount(<LoadingSpinner/>);
    expect(wrapper.hasClass("loading-spinner")).toBeTruthy();
  });

});