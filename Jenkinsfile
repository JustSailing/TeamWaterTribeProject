pipeline {
    agent any

    tools {
        nodejs 'NodeJS'
    }

    environment {
        BACKEND_DIRECTORY  = 'todo'
        FRONTEND_DIRECTORY = 'angular_todo'

        // Selenium will test the deployed S3 frontend.
        // Use the root URL because S3 may return 404 when /login is opened directly.
        FRONTEND_URL = 'http://water-tribe-angular-app.s3-website-us-east-1.amazonaws.com'

        AWS_REGION = 'us-east-1'
        S3_BUCKET  = 'water-tribe-angular-app'

        EC2_HOST = '54.221.103.90'
        EC2_USER = 'ec2-user'

        BACKEND_URL = 'http://54.221.103.90:8080'
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
                echo 'Checking out source code from the configured branch...'
                checkout scm
            }
        }

        stage('Verify Environment') {
            steps {
                echo 'Checking Java, Node.js, npm and Gradle...'

                bat '''
                    java -version
                    node --version
                    npm --version
                    aws --version
                '''

                dir("${BACKEND_DIRECTORY}") {
                    bat 'gradlew.bat --version'
                }
            }
        }

        stage('Clean Previous Builds') {
            steps {
                echo 'Cleaning previous build files...'

                dir("${BACKEND_DIRECTORY}") {
                    bat 'gradlew.bat clean --no-daemon'
                }

                dir("${FRONTEND_DIRECTORY}") {
                    bat '''
                        if exist dist rmdir /S /Q dist
                    '''
                }
            }
        }

        stage('Install Frontend Dependencies') {
            steps {
                echo 'Installing Angular dependencies...'

                dir("${FRONTEND_DIRECTORY}") {
                    bat 'npm ci'
                }
            }
        }

        stage('Build Frontend') {
            steps {
                echo 'Building Angular production application...'

                dir("${FRONTEND_DIRECTORY}") {
                    bat 'npm run build -- --configuration production'
                }
            }
        }

        stage('Build Backend') {
            steps {
                echo 'Building Spring Boot JAR...'

                dir("${BACKEND_DIRECTORY}") {
                    // Tests run later against the deployed application.
                    bat 'gradlew.bat bootJar -x test --no-daemon'
                }
            }
        }

        stage('Archive Build Artifacts') {
            steps {
                echo 'Archiving backend and frontend build artifacts...'

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

        stage('Deploy Backend to EC2') {
            steps {
                echo 'Deploying Spring Boot backend to EC2...'

                withCredentials([
                    sshUserPrivateKey(
                        credentialsId: 'ec2-ssh-key',
                        keyFileVariable: 'SSH_KEY',
                        usernameVariable: 'SSH_USER'
                    )
                ]) {
                    bat """
                        echo Securing temporary SSH private key...

                        attrib -R "%SSH_KEY%"
                        icacls "%SSH_KEY%" /inheritance:r
                        icacls "%SSH_KEY%" /remove:g "BUILTIN\\Users"
                        icacls "%SSH_KEY%" /grant:r "SYSTEM:R"
                        attrib +R "%SSH_KEY%"

                        echo Copying the new JAR to EC2...

                        scp -i "%SSH_KEY%" ^
                            -o StrictHostKeyChecking=no ^
                            todo\\build\\libs\\todo-0.0.1-SNAPSHOT.jar ^
                            %SSH_USER%@%EC2_HOST%:/home/%SSH_USER%/todo.jar

                        echo Stopping the previous backend process...

                        ssh -i "%SSH_KEY%" ^
                            -o StrictHostKeyChecking=no ^
                            %SSH_USER%@%EC2_HOST% ^
                            "pkill -f '[t]odo.jar' || true"

                        echo Starting the new backend process...

                        ssh -i "%SSH_KEY%" ^
                            -o StrictHostKeyChecking=no ^
                            %SSH_USER%@%EC2_HOST% ^
                            "nohup java -jar /home/%SSH_USER%/todo.jar > /home/%SSH_USER%/todo.log 2>&1 < /dev/null &"
                    """
                }
            }
        }

        stage('Wait for Backend') {
            steps {
                echo 'Waiting for the EC2 backend to start on port 8080...'

                powershell '''
                    $maximumAttempts = 40

                    for ($attempt = 1; $attempt -le $maximumAttempts; $attempt++) {
                        Write-Host "Backend check: attempt $attempt of $maximumAttempts"

                        $connection = Test-NetConnection `
                            -ComputerName $env:EC2_HOST `
                            -Port 8080 `
                            -WarningAction SilentlyContinue

                        if ($connection.TcpTestSucceeded) {
                            Write-Host "Backend is accepting connections on port 8080."
                            exit 0
                        }

                        Start-Sleep -Seconds 3
                    }

                    Write-Host "Backend did not become available on port 8080."
                    exit 1
                '''
            }
        }

        stage('Verify Backend Process') {
            steps {
                echo 'Confirming that todo.jar is running on EC2...'

                withCredentials([
                    sshUserPrivateKey(
                        credentialsId: 'ec2-ssh-key',
                        keyFileVariable: 'SSH_KEY',
                        usernameVariable: 'SSH_USER'
                    )
                ]) {
                    bat """
                        ssh -i "%SSH_KEY%" ^
                            -o StrictHostKeyChecking=no ^
                            %SSH_USER%@%EC2_HOST% ^
                            "ps -ef | grep '[t]odo.jar'"
                    """
                }
            }
        }

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
                    bat '''
                        aws s3 sync ^
                            angular_todo\\dist\\angular_todo\\browser ^
                            s3://%S3_BUCKET% ^
                            --delete ^
                            --region %AWS_REGION%
                    '''
                }
            }
        }

        stage('Wait for Deployed Frontend') {
            steps {
                echo 'Waiting for the S3 website to become available...'

                powershell '''
                    $maximumAttempts = 30

                    for ($attempt = 1; $attempt -le $maximumAttempts; $attempt++) {
                        try {
                            Write-Host "Frontend check: attempt $attempt of $maximumAttempts"

                            $response = Invoke-WebRequest `
                                -Uri $env:FRONTEND_URL `
                                -UseBasicParsing `
                                -TimeoutSec 10 `
                                -ErrorAction Stop

                            if ($response.StatusCode -eq 200) {
                                Write-Host "Deployed frontend is ready."
                                exit 0
                            }
                        }
                        catch {
                            Write-Host "Frontend is not ready yet: $($_.Exception.Message)"
                        }

                        Start-Sleep -Seconds 3
                    }

                    Write-Host "The deployed frontend did not become available."
                    exit 1
                '''
            }
        }

        stage('Run Production Tests') {
            steps {
                echo 'Running backend, API, Cucumber and Selenium tests...'
                echo "Selenium frontend URL: ${FRONTEND_URL}"
                echo "Deployed backend URL: ${BACKEND_URL}"

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
    }

    post {
        always {
            echo 'Archiving available test reports and logs...'

            archiveArtifacts(
                artifacts: 'todo/build/reports/tests/**/*',
                allowEmptyArchive: true
            )

            archiveArtifacts(
                artifacts: 'todo/build/reports/cucumber/**/*',
                allowEmptyArchive: true
            )

            echo "Pipeline completed with status: ${currentBuild.currentResult}"
        }

        success {
            echo '''
==================================================
                 PIPELINE SUCCESSFUL
==================================================
Frontend production build completed
Backend JAR build completed
Backend deployed to EC2
Frontend deployed to S3
EC2 backend became reachable
S3 frontend became reachable
Production Selenium/Cucumber tests passed
==================================================
'''
        }

        failure {
            echo '''
==================================================
                   PIPELINE FAILED
==================================================
Check the failed stage, Console Output and test reports.
==================================================
'''
        }
    }
}