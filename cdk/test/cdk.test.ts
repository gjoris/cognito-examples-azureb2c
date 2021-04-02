import { expect as expectCDK, matchTemplate, MatchStyle } from '@aws-cdk/assert';
import * as cdk from '@aws-cdk/core';
import * as Cdk from '../lib/application2-stack';

test('Empty Stack', () => {
    const app = new cdk.App();
    // WHEN
    const stack = new Cdk.Application2Stack(app, 'MyTestStack');
    // THEN
    expectCDK(stack).to(matchTemplate({
      "Resources": {}
    }, MatchStyle.EXACT))
});
