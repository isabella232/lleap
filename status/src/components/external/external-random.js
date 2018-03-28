import React from 'react'

import './external-random.css'
import ModuleRandom from '../module/random/module-random'

/**
 * Stand-alone version of the random module with tweaked styles
 *
 * @author Gaylor Bosson (gaylor.bosson@epfl.ch)
 */
export default class ExternalModuleRandom extends React.Component {

  /**
   * @override
   * @see https://facebook.github.io/react/docs/react-component.html
   * @returns {XML}
   */
  render () {
    return (
      <div className="external-module-random">
        <ModuleRandom randomRefreshInterval={30 * 1000} title="" icon="" showTimestamp={false}/>
      </div>
    );
  }
}
