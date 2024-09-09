import * as cdk from 'aws-cdk-lib';
import { Stack, StackProps } from 'aws-cdk-lib';
import { Construct } from 'constructs';
import * as ec2 from 'aws-cdk-lib/aws-ec2';
import { CfnOutput } from 'aws-cdk-lib';

export class Ec2InstanceStack extends Stack {
  constructor(scope: Construct, id: string, props?: StackProps) {
    super(scope, id, props);

    // Parameters
    const instanceName = new cdk.CfnParameter(this, 'InstanceName', {
      type: 'String',
      default: 'MV Reemplazar',
      description: 'Nombre de la instancia a crear',
    });

    const amiId = new cdk.CfnParameter(this, 'AMI', {
      type: 'String',
      default: 'ami-0aa28dab1f2852040',
      description: 'ID de AMI',
    });

    // Security Group
    const securityGroup = new ec2.SecurityGroup(this, 'InstanceSecurityGroup', {
      vpc: ec2.Vpc.fromLookup(this, 'VPC', { isDefault: true }),
      description: 'Permitir trafico SSH y HTTP desde cualquier lugar',
      allowAllOutbound: true,
    });

    securityGroup.addIngressRule(ec2.Peer.anyIpv4(), ec2.Port.tcp(22), 'Allow SSH');
    securityGroup.addIngressRule(ec2.Peer.anyIpv4(), ec2.Port.tcp(80), 'Allow HTTP');

    // EC2 Instance
    const ec2Instance = new ec2.Instance(this, 'EC2Instance', {
      vpc: ec2.Vpc.fromLookup(this, 'VPC2', { isDefault: true }),
      instanceType: ec2.InstanceType.of(ec2.InstanceClass.T2, ec2.InstanceSize.MICRO),
      machineImage: ec2.MachineImage.genericLinux({
        'us-east-1': amiId.valueAsString,
      }),
      securityGroup: securityGroup,
      keyName: 'vockey', // Key pair
      blockDevices: [
        {
          deviceName: '/dev/sda1',
          volume: ec2.BlockDeviceVolume.ebs(20), // 20 GB EBS Volume
        },
      ],
    });

    // User Data to execute commands at launch
    ec2Instance.addUserData(
      `#!/bin/bash
      cd /var/www/html/
      git clone https://github.com/utec-cc-2024-2-test/websimple.git
      git clone https://github.com/utec-cc-2024-2-test/webplantilla.git
      ls -l`
    );

    // Outputs
    new CfnOutput(this, 'InstanceId', {
      description: 'ID de la instancia EC2',
      value: ec2Instance.instanceId,
    });

    new CfnOutput(this, 'InstancePublicIP', {
      description: 'IP publica de la instancia',
      value: ec2Instance.instancePublicIp,
    });

    new CfnOutput(this, 'websimpleURL', {
      description: 'URL de websimple',
      value: `http://${ec2Instance.instancePublicIp}/websimple`,
    });

    new CfnOutput(this, 'webplantillaURL', {
      description: 'URL de webplantilla',
      value: `http://${ec2Instance.instancePublicIp}/webplantilla`,
    });
  }
}
