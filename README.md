# DISCLAIMER

This is an example of an integration between Spring Boot and IAM, using a Web Identity (in this case: OIDC token from Azure B2C).

### This is **NOT** official AWS code.
### This is **NOT** supported code.
### This is **NOT** vetted code.
#### (This is **NOT** clean code.)
### This code **SHOULD NOT be used in ANY production system**.

Any usage of this code is under full and sole responsibility of the end user.


# What does this code do?

It will show the user:

- Integration with Azure B2C
- Use the resulting ID token to do an AssumeRoleWithWebIdentity on AWS IAM
- Use the session credentials (STS token) returned by IAM to call S3 and Athena

# How to configure?

Refer to the `src/main/resources/application.yml` file, all required attributes are marked and self-explanatory.

The `IndexController` contains a query on Athena - that query is reused from the example in the documentation: https://docs.aws.amazon.com/athena/latest/ug/getting-started.html .

# Other requirements

- In Azure, you need to set up an Azure B2C tenant. Documentation: https://docs.microsoft.com/en-us/azure/active-directory-b2c/tutorial-create-tenant .
- In your AWS account, you need to configure Azure B2C as an OpenID Connect identity provider. Documentation: https://docs.aws.amazon.com/IAM/latest/UserGuide/id_roles_providers_create_oidc.html .
- In your AWS account, you need to provision a role to assume. This role needs to have a trust relationship with the ARN of the configured Identity Provider in IAM.

# CDK

It also contains very basic CDK code to quickly deploy this on an ECS Fargate cluster. This can be found in the `/cdk` folder. The included Dockerfile is fully self-contained, so running a `cdk deploy` will act as a deployment pipeline.




