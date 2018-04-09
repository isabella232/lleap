import React from 'react'
import {PropTypes as T} from 'prop-types'

/**
 * Increase a count over time and display the time since the given timestamp
 *
 * @author Gaylor Bosson (gaylor.bosson@epfl.ch)
 */
export default class LastUpdate extends React.Component {

  static propTypes = {
    timestamp: T.number.isRequired,
    refresh: T.number
  };

  static defaultProps = {
    refresh: 1000
  };

  /**
   * @override
   * @see https://facebook.github.io/react/docs/react-component.html
   */
  componentDidMount() {
    const {refresh} = this.props;
    const self = this;
    this.refresh_counter = setInterval(() => self.forceUpdate(), refresh);
  }

  /**
   * @override
   * @see https://facebook.github.io/react/docs/react-component.html
   */
  componentWillUnmount() {
    clearInterval(this.refresh_counter);
  }

  /**
   * @override
   * @see https://facebook.github.io/react/docs/react-component.html
   * @returns {XML}
   */
  render() {
    const {timestamp} = this.props;
    const lastUpdate = Math.floor((Date.now() - timestamp) / 1000); // Timestamp is in ms

    return <span>{`${lastUpdate}s`}</span>;
  }

}
