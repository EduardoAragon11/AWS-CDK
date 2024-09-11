package com.myorg;

import software.amazon.awscdk.App;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.Environment;

public class CdkJavaApp {
    public static void main(final String[] args) {
        App app = new App();

        new Ec2InstanceStack(app, "InstanceStack", StackProps.builder()
                .env(Environment.builder()
                        .account("171286946604")
                        .region("us-east-1")
                        .build())
                .build());

        app.synth();
    }
}
