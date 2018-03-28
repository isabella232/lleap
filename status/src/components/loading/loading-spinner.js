import React from 'react'
import FontAwesome from 'react-fontawesome'

import './loading-spinner.css'

/**
 * Helper component to display a loading animation
 *
 * @author Gaylor Bosson (gaylor.bosson@epfl.ch)
 */
export default class LoadingSpinner extends React.Component {

  /**
   * @override
   * @see https://facebook.github.io/react/docs/react-component.html
   * @returns {XML}
   */
  render() {
    return (
      <div className="loading-spinner">
        <FontAwesome name="circle-o-notch" size="3x" spin/>
      </div>
    );
  }
}