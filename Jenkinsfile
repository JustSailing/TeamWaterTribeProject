pipeline {
    agent any

    /*
     * IMPORTANT:
     * This must exactly match the NodeJS installation name under:
     * Manage Jenkins -> Tools -> NodeJS installations
     */
    tools {
        nodejs 'NodeJS'
    }

    environment {
        BACKEND_DIRECTORY = 'todo'
        FRONTEND_DIRECTORY = 'angular_todo'

        /*
         * Deployment configuration — not used yet.
         *
         * AWS_REGION = 'us-east-1'
         * S3_BUCKET = 'water-tribe-angular-app'
         * EC2_HOST = '18.209.57.58'
         * EC2_USER = 'ec2-user'
         */
    }

    options {
        timestamps()
        disableConcurrentBuilds()
        buildDiscarder(
            logRotator(
                numToKeepStr: '10',
                artifactNumToKeepStr: '5'
            )
        )
    }

    stages {
        stage('Checkout Source Code') {
            steps {
                echo 'Checking out source code from GitHub...'
                checkout scm
            }
        }

        stage('Verify Build Environment') {
            steps {
                echo 'Checking Java, Node.js and npm versions...'

                bat '''
                    java -version
                    node --version
                    npm --version
                '''

                dir("${BACKEND_DIRECTORY}") {
                    bat 'gradlew.bat --version'
                }
            }
        }

        stage('Backend Clean') {
            steps {
                echo 'Cleaning previous backend build files...'

                dir("${BACKEND_DIRECTORY}") {
                    bat 'gradlew.bat clean --no-daemon'
                }
            }
        }

        stage('Backend Tests') {
            steps {
                echo 'Running Spring Boot, JUnit, Cucumber, REST Assured and other Gradle tests...'

                dir("${BACKEND_DIRECTORY}") {
                    bat 'gradlew.bat test --no-daemon'
                }
            }

            post {
                always {
                    junit(
                        testResults: 'todo/build/test-results/test/*.xml',
                        allowEmptyResults: true,
                        keepLongStdio: true
                    )
                }
            }
        }

        stage('Backend Build') {
            steps {
                echo 'Building the Spring Boot executable JAR...'

                dir("${BACKEND_DIRECTORY}") {
                    /*
                     * Tests already ran in the previous stage,
                     * so we skip running them a second time.
                     */
                    bat 'gradlew.bat bootJar -x test --no-daemon'
                }
            }
        }

        stage('Frontend Install Dependencies') {
            steps {
                echo 'Installing Angular dependencies from package-lock.json...'

                dir("${FRONTEND_DIRECTORY}") {
                    bat 'npm ci'
                }
            }
        }

        stage('Frontend Tests') {
            steps {
                echo 'Running Angular unit tests with Vitest...'

                dir("${FRONTEND_DIRECTORY}") {
                    /*
                     * --watch=false makes the test process finish after one run,
                     * which is required for Jenkins.
                     */
                    bat 'npm test -- --watch=false'
                }
            }
        }

        stage('Frontend Build') {
            steps {
                echo 'Creating the production Angular build...'

                dir("${FRONTEND_DIRECTORY}") {
                    bat 'npm run build -- --configuration production'
                }
            }
        }

        stage('Archive Build Artifacts') {
            steps {
                echo 'Saving backend and frontend build artifacts in Jenkins...'

                archiveArtifacts(
                    artifacts: 'todo/build/libs/*.jar',
                    fingerprint: true,
                    allowEmptyArchive: false
                )

                archiveArtifacts(
                    artifacts: 'angular_todo/dist/**/*',
                    fingerprint: true,
                    allowEmptyArchive: false
                )
            }
        }

        /*
         * ============================================================
         * DEPLOYMENT STAGES — COMMENTED OUT FOR NOW
         * ============================================================
         *
         * Before enabling these stages, you need:
         *
         * 1. AWS CLI installed on the Jenkins computer.
         * 2. AWS Access Key ID and Secret Access Key stored in Jenkins.
         * 3. The EC2 SSH private key stored in Jenkins.
         * 4. An application directory created on EC2.
         * 5. The correct Angular dist output directory confirmed.
         *
         * Remove the surrounding block comment when ready.
         */

        /*
        stage('Deploy Frontend to S3') {
            steps {
                echo 'Deploying Angular frontend to S3...'

                withCredentials([
                    usernamePassword(
                        credentialsId: 'aws-credentials',
                        usernameVariable: 'AWS_ACCESS_KEY_ID',
                        passwordVariable: 'AWS_SECRET_ACCESS_KEY'
                    )
                ]) {
                    bat """
                        aws s3 sync ^
                        angular_todo\\dist\\angular-todo\\browser ^
                        s3://${S3_BUCKET} ^
                        --delete ^
                        --region ${AWS_REGION}
                    """
                }
            }
        }

        stage('Deploy Backend to EC2') {
            steps {
                echo 'Deploying Spring Boot backend to EC2...'

                sshagent(credentials: ['ec2-ssh-key']) {
                    bat """
                        scp -o StrictHostKeyChecking=no ^
                        todo\\build\\libs\\todo-0.0.1-SNAPSHOT.jar ^
                        ${EC2_USER}@${EC2_HOST}:/home/${EC2_USER}/app/todo.jar

                        ssh -o StrictHostKeyChecking=no ^
                        ${EC2_USER}@${EC2_HOST} ^
                        "pkill -f todo.jar || true; nohup java -jar /home/${EC2_USER}/app/todo.jar > /home/${EC2_USER}/app/todo.log 2>&1 &"
                    """
                }
            }
        }
        */
    }

    post {
        success {
            echo '''
            ==================================================
            PIPELINE SUCCESSFUL
            ==================================================
            Backend tests: Passed
            Backend JAR: Built and archived
            Frontend tests: Passed
            Frontend application: Built and archived
            Deployment: Currently disabled
            ==================================================
            '''
        }

        failure {
            echo '''
            ==================================================
            PIPELINE FAILED
            ==================================================
            Open this build and select "Console Output" to see
            which command or test failed.
            ==================================================
            '''
        }

        unstable {
            echo 'The pipeline completed, but one or more test results were unstable.'
        }

        always {
            echo "Pipeline completed with status: ${currentBuild.currentResult}"
        }
    }
}