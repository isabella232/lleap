import React from 'react'
import {PropTypes as T} from 'prop-types'
import FontAwesome from 'react-fontawesome'

import './module.css'

/**
 * Base component of a module. You can define the title of the module and its icon using the name
 * of an icon in http://fontawesome.io/icons/
 *
 * @author Gaylor Bosson (gaylor.bosson@epfl.ch)
 */
export default class Module extends React.Component {
  
  static propTypes = {
    className: T.string,
    title: T.string,
    icon: T.string
  };

  /**
   * @override
   * @see https://facebook.github.io/react/docs/react-component.html
   * @returns {XML}
   */
  render() {
    const {icon, title} = this.props;
    const className = `module ${this.props.className || ''}`;
    
    return (
      <div className={className}>
        <div className="module-header">
          <FontAwesome name={icon || ''} size="2x"/>
          {title}
        </div>
        <div className="module-body">
          {this.props.children}
        </div>
      </div>
    );
  }
}
