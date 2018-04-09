import React from 'react'
import {Container, Row, Col} from 'reactstrap'

import ServersStatus from '../components/servers-status/servers-status'
import HTMLIFrame from '../components/html-iframe/html-iframe'
import ModuleSign from '../components/module/signature/module-sign'
import ModuleVerify from '../components/module/signature/module-verify'
import ModuleSkipChain from '../components/module/skipchain/module-skipchain'
// import ModuleHTML from '../components/module/html/module-html'
import ModuleRandom from '../components/module/random/module-random'

import './home.css';

/**
 * @author Gaylor Bosson (gaylor.bosson@epfl.ch)
 *
 * Index of the website where we display everything in a single page
 */
class Home extends React.Component {

  /**
   * @override
   * @returns {XML}
   <Col>
     <ModuleHTML/>
   </Col>
   */
  render() {
    return (
      <div className="cothority-app">
        <HTMLIFrame/>

        <Container fluid={true}>
          <Row noGutters>
            <Col>
              <ModuleSkipChain/>
            </Col>
          </Row>
          <Row>
            <Col>
              <ServersStatus/>
            </Col>
          </Row>
        </Container>
      </div>
    );
  }
}

export default Home;
