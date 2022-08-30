 export CODEARTIFACT_AUTH_TOKEN=`aws codeartifact get-authorization-token --domain websight --domain-owner 299371835903 --query authorizationToken --output text`
 mvn deploy:deploy-file -DpomFile=pom.xml -Dfile=target/websight.io.org.apache.sling.bundleresource.impl-2.3.5-eda8d286cb84f94077d1e481f063bd0ef6ca7b18.jar \
    -DrepositoryId=websight-maven-repo \
    -Durl=https://websight-299371835903.d.codeartifact.eu-central-1.amazonaws.com/maven/maven-repo
