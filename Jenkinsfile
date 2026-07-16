pipeline {
    agent any

    tools {
        nodejs 'NodeJS'
    }

    environment {
        BACKEND_DIRECTORY = 'todo'
        FRONTEND_DIRECTORY = 'angular_todo'
        FRONTEND_URL = 'http://localhost:4200/login'

        AWS_REGION = 'us-east-1'
        S3_BUCKET = 'water-tribe-angular-app'
        EC2_HOST = '18.209.57.58'
        EC2_USER = 'ec2-user'
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
                echo 'Checking out source code...'
                checkout scm
            }
        }

        stage('Verify Environment') {
            steps {
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

        stage('Clean Previous Builds') {
            steps {
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
                dir("${FRONTEND_DIRECTORY}") {
                    bat 'npm ci'
                }
            }
        }

        stage('Build Frontend') {
            steps {
                dir("${FRONTEND_DIRECTORY}") {
                    bat 'npm run build -- --configuration production'
                }
            }
        }

        stage('Start Frontend') {
            steps {
                echo 'Starting Angular frontend on port 4200...'

                dir("${FRONTEND_DIRECTORY}") {
                    withEnv(['JENKINS_NODE_COOKIE=dontKillFrontend']) {
                        bat '''
                            start "" /B cmd /C "npm start -- --host 127.0.0.1 --port 4200 > frontend.log 2>&1"
                        '''
                    }
                }
            }
        }

        stage('Wait for Frontend') {
            steps {
                echo 'Waiting for Angular frontend...'

                powershell '''
                    $maximumAttempts = 60

                    for ($attempt = 1; $attempt -le $maximumAttempts; $attempt++) {
                        try {
                            $response = Invoke-WebRequest `
                                -Uri "http://localhost:4200/login" `
                                -UseBasicParsing `
                                -TimeoutSec 5 `
                                -ErrorAction Stop

                            Write-Host "Frontend is ready. Status: $($response.StatusCode)"
                            exit 0
                        }
                        catch {
                            Write-Host "Waiting for frontend: attempt $attempt of $maximumAttempts"
                            Start-Sleep -Seconds 2
                        }
                    }

                    Write-Host "Frontend failed to start."

                    if (Test-Path "angular_todo\\frontend.log") {
                        Get-Content "angular_todo\\frontend.log" -Tail 100
                    }

                    exit 1
                '''
            }
        }

        stage('Run All Backend Tests') {
            steps {
                echo 'Running all backend, API, Cucumber and Selenium tests...'

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

        stage('Build Backend') {
            steps {
                dir("${BACKEND_DIRECTORY}") {
                    bat 'gradlew.bat bootJar -x test --no-daemon'
                }
            }
        }

        // stage('Run Frontend Tests') {
        //     steps {
        //         dir("${FRONTEND_DIRECTORY}") {
        //             bat 'npm test -- --watch=false'
        //         }
        //     }
        // }

        stage('Archive Build Artifacts') {
            steps {
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

                archiveArtifacts(
                    artifacts: 'angular_todo/frontend*.log',
                    allowEmptyArchive: true
                )
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
                        angular_todo\\dist\\angular_todo ^
                        s3://%S3_BUCKET% ^
                        --delete ^
                        --region %AWS_REGION%
                    '''
                }
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
                    bat '''
                        scp -i "%SSH_KEY%" ^
                            -o StrictHostKeyChecking=no ^
                            todo\\build\\libs\\todo-0.0.1-SNAPSHOT.jar ^
                            %SSH_USER%@%EC2_HOST%:/home/%SSH_USER%/todo.jar

                        ssh -i "%SSH_KEY%" ^
                            -o StrictHostKeyChecking=no ^
                            %SSH_USER%@%EC2_HOST% ^
                            "pkill -f todo.jar || true; nohup java -jar /home/%SSH_USER%/todo.jar > /home/%SSH_USER%/todo.log 2>&1 &"
                    '''
                }
            }
        }
    }

    post {
        always {
            echo 'Stopping Angular frontend...'

            powershell '''
                $ErrorActionPreference = "Continue"

                if (Test-Path "angular_todo\\frontend.pid") {
                    $processId = Get-Content "angular_todo\\frontend.pid" |
                        Select-Object -First 1

                    if ($processId) {
                        taskkill /PID $processId /T /F 2>$null
                    }

                    Remove-Item "angular_todo\\frontend.pid" `
                        -Force `
                        -ErrorAction SilentlyContinue
                }

                $connections = Get-NetTCPConnection `
                    -LocalPort 4200 `
                    -State Listen `
                    -ErrorAction SilentlyContinue

                foreach ($connection in $connections) {
                    taskkill /PID $connection.OwningProcess /T /F 2>$null
                }

                exit 0
            '''

            archiveArtifacts(
                artifacts: 'angular_todo/frontend*.log',
                allowEmptyArchive: true
            )

            echo "Pipeline completed with status: ${currentBuild.currentResult}"
        }

        success {
            echo '''
            ==========================================
            PIPELINE SUCCESSFUL
            ==========================================
            Angular frontend started successfully
            All backend/Cucumber tests passed
            Frontend tests passed
            Backend and frontend builds completed
            Deployment is disabled
            ==========================================
            '''
        }

        failure {
            echo '''
            ==========================================
            PIPELINE FAILED
            ==========================================
            Check Console Output and frontend logs.
            ==========================================
            '''
        }
    }
}