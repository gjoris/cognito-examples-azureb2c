import * as cdk from '@aws-cdk/core';
import {Cluster, ContainerImage} from "@aws-cdk/aws-ecs";
import {Duration} from "@aws-cdk/core";
import {ApplicationLoadBalancedFargateService} from "@aws-cdk/aws-ecs-patterns";
import {Vpc} from "@aws-cdk/aws-ec2";

export class Application2Stack extends cdk.Stack {
  constructor(scope: cdk.Construct, id: string, props?: cdk.StackProps) {
    super(scope, id, props);

    // The code that defines your stack goes here
    const vpc = new Vpc(this, "vpc-application2", {});

    const cluster = new Cluster(this, "cluster-application2", {
      vpc: vpc
    });

    let fargateService = new ApplicationLoadBalancedFargateService(this, "fargate-service-application2", {
      desiredCount: 1,
      cluster: cluster,
      publicLoadBalancer: true,
      minHealthyPercent: 50,
      maxHealthyPercent: 200,
      taskImageOptions: {
        image: ContainerImage.fromAsset("../"),
        containerPort: 8080,
        containerName: "application2"
      }
    });

    fargateService.targetGroup.configureHealthCheck({
      interval: Duration.seconds(5),
      healthyThresholdCount: 2,
      timeout: Duration.seconds(4),
      healthyHttpCodes: "200"
    });

    fargateService.targetGroup.setAttribute("deregistration_delay.timeout_seconds", "0");
  }
}
