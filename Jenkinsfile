pipeline {
    agent{
        label 'jenkins-workers'
    }

    options {
        timestamps()
        buildDiscarder(logRotator(numToKeepStr: "10"))
    }

    environment {
        BUILD_TAG = sh label: 'Generating build tag', returnStdout: true, script: 'python3 scripts/tag.py ${GIT_BRANCH} ${BUILD_NUMBER} ${GIT_COMMIT}'
        ECR_REPO_DIR = "gp2gp"
        DOCKER_IMAGE = "${DOCKER_REGISTRY}/${ECR_REPO_DIR}:${BUILD_TAG}"
    }

    stages {
        stage('Dockerfile lint') {
            steps {
                script {
                    sh '''
                        curl -sLO https://github.com/hadolint/hadolint/releases/download/v1.19.0/hadolint-Linux-x86_64
                        chmod +x hadolint-Linux-x86_64 && mv hadolint-Linux-x86_64 /usr/local/bin/hadolint
                    '''
                    if (sh(label: 'Running dockerfile lint with hadolint', script: 'hadolint docker/service/Dockerfile', returnStatus: true) != 0) {error("Dockerfile linting failed")}
                }
            }
        }

        stage('Tests') {
            steps {
                script {
                    sh '''
                        docker-compose -f docker/docker-compose.yml -f docker/docker-compose-tests.yml build
                        docker-compose -f docker/docker-compose.yml -f docker/docker-compose-tests.yml up --exit-code-from gp2gp
                        docker cp tests:/home/gradle/service/build .
                    '''

                    archiveArtifacts artifacts: 'build/reports/**/*.*', fingerprint: true
                    junit '**/build/test-results/**/*.xml'
                    recordIssues(
                        enabledForFailure: true,
                        tools: [
                            checkStyle(pattern: 'build/reports/checkstyle/*.xml'),
                            spotBugs(pattern: 'build/reports/spotbugs/*.xml')
                        ]
                    )
                }
            }
            post {
                always {
                    step([
                        $class : 'JacocoPublisher',
                        execPattern : '**/build/jacoco/*.exec',
                        classPattern : '**/build/classes/java',
                        sourcePattern : 'src/main/java',
                        exclusionPattern : '**/*Test.class'
                    ])
            sh "rm -rf build"
                    sh "docker-compose -f docker/docker-compose.yml -f docker/docker-compose-tests.yml down"
                }
            }
        }

        stage('Build Docker Image') {
            steps {
                script {
                    if (sh(label: 'Running gp2gp docker build', script: 'docker build -f docker/service/Dockerfile -t ${DOCKER_IMAGE} .', returnStatus: true) != 0) {error("Failed to build gp2gp Docker image")}
                }
            }
        }

        stage('Security Scan') {
            steps {
                script {
                    sh '''
                        curl -sLO https://github.com/aquasecurity/trivy/releases/download/v0.14.0/trivy_0.14.0_Linux-64bit.tar.gz
                        tar xf trivy_0.14.0_Linux-64bit.tar.gz
                        chmod +x trivy && mv trivy /usr/local/bin
                    '''
                    if (sh(label: 'Running security scan with trivy', script: 'trivy -q i --ignore-unfixed --exit-code 1 ${DOCKER_IMAGE}', returnStatus: true) != 0) {error("There are fixable vulnerabilities present")}
                }
            }
        }

        stage('Push Image') {
            when {
                expression { currentBuild.resultIsBetterOrEqualTo('SUCCESS') }
            }
            steps {
                script {
                    if (ecrLogin(TF_STATE_BUCKET_REGION) != 0 )  { error("Docker login to ECR failed") }
                    String dockerPushCommand = "docker push ${DOCKER_IMAGE}"
                    if (sh (label: "Pushing image", script: dockerPushCommand, returnStatus: true) !=0) { error("Docker push gp2gp image failed") }
                }
            }
        }

        stage('E2E Tests') {
            steps {
                sh '''
                    docker-compose -f docker/docker-compose.yml -f docker/docker-compose-e2e-tests.yml build
                    docker-compose -f docker/docker-compose.yml -f docker/docker-compose-e2e-tests.yml up --exit-code-from gp2gp-e2e-tests
                    docker cp e2e-tests:/home/gradle/e2e-tests/build .
                    mv build e2e-build
                '''
                archiveArtifacts artifacts: 'e2e-build/reports/**/*.*', fingerprint: true
                junit '**/e2e-build/test-results/**/*.xml'
            }
            post {
                always {
                    sh "rm -rf e2e-build"
                    sh "docker-compose -f docker/docker-compose.yml -f docker/docker-compose-e2e-tests.yml down"
                }
            }
        }
    }
    post {
        always {
            sh label: 'Remove images created by docker-compose', script: 'docker-compose -f docker/docker-compose.yml -f docker/docker-compose-tests.yml down --rmi local'
            sh label: 'Remove exited containers', script: 'docker rm $(docker ps -a -f status=exited -q)'
            sh label: 'Remove images tagged with current BUILD_TAG', script: 'docker image rm -f $(docker images "*/*:*${BUILD_TAG}" -q) $(docker images "*/*/*:*${BUILD_TAG}" -q) || true'
        }
    }
}

int ecrLogin(String aws_region) {
    String ecrCommand = "aws ecr get-login --region ${aws_region}"
    String dockerLogin = sh (label: "Getting Docker login from ECR", script: ecrCommand, returnStdout: true).replace("-e none","") // some parameters that AWS provides and docker does not recognize
    return sh(label: "Logging in with Docker", script: dockerLogin, returnStatus: true)
}
