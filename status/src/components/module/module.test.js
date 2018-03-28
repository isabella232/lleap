import React from 'react'
import {mount} from 'enzyme'
import Faker from 'faker'
import FontAwesome from 'react-fontawesome'

import Module from './module'

describe(Module, () => {
  
  it('should render without crashing', () => {
    const wrapper = mount(<Module/>);
    expect(wrapper.hasClass("module")).toBeTruthy();
  });
  
  it('should display the title property', () => {
    const title = Faker.lorem.word();
    const wrapper = mount(<Module title={title}/>);
    
    expect(wrapper.text()).toBe(title);
  });
  
  it('should have the icon', () => {
    const wrapper = mount(<Module icon="lock"/>);
    const fa = wrapper.find(FontAwesome);
    
    expect(fa.getNode().props.name).toBe('lock');
  });
  
  class ChildTest extends React.Component {
    render() {
      return <div></div>
    }
  }
  
  it('should render the children', () => {
    const wrapper = mount(<Module><ChildTest/></Module>);
    expect(wrapper.find(ChildTest).exists()).toBeTruthy();
  });
  
});