package com.myorg;

import software.amazon.awscdk.CfnOutput;
import software.amazon.awscdk.CfnParameter;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.services.iam.IRole;
import software.constructs.Construct;
import software.amazon.awscdk.services.ec2.*;
import software.amazon.awscdk.services.iam.Role;

import java.util.List;
import java.util.Map;

public class Ec2InstanceStack extends Stack {
    public Ec2InstanceStack(final Construct scope, final String id, final StackProps props) {
        super(scope, id, props);

        // Parameters
        CfnParameter instanceName = CfnParameter.Builder.create(this, "InstanceName")
                .type("String")
                .defaultValue("MV Reemplazar")
                .description("Nombre de la instancia a crear")
                .build();

        CfnParameter amiId = CfnParameter.Builder.create(this, "AMI")
                .type("String")
                .defaultValue("ami-0aa28dab1f2852040")
                .description("ID de AMI")
                .build();

        // Security Group
        IVpc vpc = Vpc.fromLookup(this, "VPC", VpcLookupOptions.builder()
                .isDefault(true)
                .build());

        SecurityGroup securityGroup = SecurityGroup.Builder.create(this, "InstanceSecurityGroup")
                .vpc(vpc)
                .description("Permitir trafico SSH y HTTP desde cualquier lugar")
                .allowAllOutbound(true)
                .build();

        securityGroup.addIngressRule(Peer.anyIpv4(), Port.tcp(22), "Allow SSH");
        securityGroup.addIngressRule(Peer.anyIpv4(), Port.tcp(80), "Allow HTTP");

        // Existing IAM Role ARN
        String existingRoleArn = "arn:aws:iam::171286946604:role/LabRole";
        IRole role = Role.fromRoleArn(this, "ExistingRole", existingRoleArn);

        // EC2 Instance
        Instance ec2Instance = Instance.Builder.create(this, "EC2Instance")
                .vpc(vpc)
                .instanceType(InstanceType.of(InstanceClass.T2, InstanceSize.MICRO))
                .machineImage(MachineImage.genericLinux(Map.of("us-east-1", amiId.getValueAsString())))
                .securityGroup(securityGroup)
                .keyName("vockey")
                .blockDevices(List.of(BlockDevice.builder()
                        .deviceName("/dev/sda1")
                        .volume(BlockDeviceVolume.ebs(20))
                        .build()))
                .role(role)
                .build();

        ec2Instance.addUserData(
                "#!/bin/bash",
                "cd /var/www/html/",
                "git clone https://github.com/utec-cc-2024-2-test/websimple.git",
                "git clone https://github.com/utec-cc-2024-2-test/webplantilla.git",
                "ls -l"
        );

        // Outputs
        CfnOutput.Builder.create(this, "InstanceId")
                .description("ID de la instancia EC2")
                .value(ec2Instance.getInstanceId())
                .build();

        CfnOutput.Builder.create(this, "InstancePublicIP")
                .description("IP publica de la instancia")
                .value(ec2Instance.getInstancePublicIp())
                .build();

        CfnOutput.Builder.create(this, "websimpleURL")
                .description("URL de websimple")
                .value("http://" + ec2Instance.getInstancePublicIp() + "/websimple")
                .build();

        CfnOutput.Builder.create(this, "webplantillaURL")
                .description("URL de webplantilla")
                .value("http://" + ec2Instance.getInstancePublicIp() + "/webplantilla")
                .build();
    }
}
