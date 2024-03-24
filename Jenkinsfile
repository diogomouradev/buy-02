pipeline {
  agent any
  tools {
    maven 'FICK MAVEN'
    jdk 'JDK FICK'
  }
  environment {
    PROJECT_NAME = 'buy02'
    // PROJECT_VERSION will be dynamically set within a stage
  }
  stages {

     stage("checkout"){
         steps{
            checkout scm
         }
     }

     stage('Deploy to Production') {
        steps {
            script {
                ansiblePlaybook(
                  colorized: true,
                  credentialsId: 'ssh-key',
                  disableHostKeyChecking: true,
                  installation: 'Ansible',
                  inventory: '/etc/ansible',
                  playbook: './playbook.yml',
                  vaultTmpPath: '',
                  extraVars: [ansible_ssh_user: 'root']
              )
            }
        }
    }

  
      //  stage('Update Server') {
      //       steps {
      //           script {
      //               sshagent(credentials: ['ssh-key']) {
      //                   sh '''
      //                       ssh root@207.154.220.13 "
      //                           rm -rf buy-02 &&
      //                           git clone https://github.com/diogomouradev/buy-02.git &&
      //                           cd buy-02 &&
      //                           docker-compose --env-file .env.dev build &&
      //                           docker-compose --env-file .env.dev up -d
      //                       "
      //                   '''
      //               }
      //           }
      //       }
      //   }


    stage('SonarQube Analysis') {
      steps {
          script{
              sh "ls -la"
          }
        script {
          withSonarQubeEnv('buy-02') {
            sh """
              mvn clean verify sonar:sonar \
              -Dsonar.projectKey=buy-02 \
              -Dsonar.projectName='buy-02' \
              -Dsonar.host.url=http://161.35.64.134:9000 \
              -Dsonar.token=sqp_09eb5be2826964d040a9a688caee0522e5b96be3
            """
          }
          timeout(time: 1, unit: 'HOURS') {
            
              def qg = waitForQualityGate()
              if (qg.status != 'OK') {
                  error "Pipeline aborted due to quality gate failure: ${qg.status}"
              }
            }
        }
      }
    }

    stage('Run Tests: User Service') {
      steps {
        dir('user-service') {
          sh 'mvn test'
        }
      }
    }

    stage('Run Tests: Product Service') {
      steps {
        dir('product-service') {
          sh 'mvn test'
        }
      }
    }

    stage('Run Tests: Media Service') {
      steps {
        dir('media-service') {
          sh 'mvn test'
        }
      }
    }

    stage('Run Tests: Order Service') {
      steps {
        dir('order-service') {
          sh 'mvn test'
        }
      }
    }

    stage('Run Tests: Angular') {
      steps {
        script {
                    env.CHROME_BIN = '/usr/bin/google-chrome'
                }
        dir('angular') {
          sh 'export CHROME_BIN=/usr/bin/google-chrome'
          sh 'npm install'
          sh 'ng test --watch=false --progress=false --browsers ChromeHeadless'
        }
      }
    }

    stage('Extract Version') {
      steps {
        script {
          env.PROJECT_VERSION = sh(script: "mvn help:evaluate -Dexpression=project.version -q -DforceStdout", returnStdout: true).trim()
          echo "Project Version: ${env.PROJECT_VERSION}"
        }
      }
    }

    
  }

  

  post {
    success {
      mail to: 'diogomouralp1@gmail.com',
          subject: "Pipeline ${env.PROJECT_NAME} - Build # ${env.BUILD_NUMBER} - SUCCESS",
          body: "The pipeline was a SUCCESS. Check console output at ${env.BUILD_URL} to view the results."
    }
    failure {
      mail to: 'diogomouralp1@gmail.com',
          subject: "Pipeline ${env.PROJECT_NAME} - Build # ${env.BUILD_NUMBER} - FAILURE",
          body: "The pipeline was a FAILURE. Check console output at ${env.BUILD_URL} to view the results."
    }
  }
}

