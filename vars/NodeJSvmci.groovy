pipeline {
    agent { node { label 'AGENT-1' } }
    environment{
        // declare varaiables here so it applicable to every stages
        packageVersion = ''
    }
    parameters {
        string(name: 'component', defaultValue: '', description: 'Which component?')
    }
    stages {
        stage('Get version'){
            steps{
                def packageJSON = readJSON(file: 'package.json')
                def packageJSONVersion = packageJSON.version
                echo "version: ${packageJSONVersion}"

            }
        }
        stage('Install depdencies') {
            steps {
                sh 'npm install'
            }
        }
        stage('Unit test') {
            steps {
                echo "unit testing is done here"
            }
        }
        //sonar-scanner command expect sonar-project.properties should be available
        // stage('Sonar Scan') {
        //     steps {
        //         sh 'ls -ltr'
        //         sh 'sonar-scanner'
        //     }
        // }
        stage('Build') {
            steps {
                sh 'ls -ltr'
                sh "zip -r "${params.component}.zip" ./* --exclude=.git --exclude=.zip"
            }
        }
        stage('sast') {
            steps {
                echo "sast done"
                echo "package versions: $packageVersion"
            }
        }

        // install pipeline utility steps plugin
        stage('Publish Artifact') {
            steps {
                nexusArtifactUploader(
                    nexusVersion: 'nexus3',
                    protocol: 'http',
                    nexusUrl: '52.71.253.240:8081/',
                    groupId: 'com.roboshop',
                    version: "$packageVersion",
                    repository: "${params.component}",
                    credentialsId: 'nexus-auth',
                    artifacts: [
                        [artifactId:"${params.component}",
                        classifier: '',
                        file: "${params.component}.zip",
                        type: 'zip']
                    ]
                )
            }
        }
        // here i need to configure downstream job. i have to pass package version for deployement
        // this job will wait untill downstreamm job is over.
        // push this changes into branch
        stage('Deploy') {
            steps {
                script{
                    echo "Deployment"
                    def params = [
                        string(name: 'version', value: "$packageVersion") //deploy expected this formate
                    ]
                    build job: "../${params.component}-deploy", wait = true, parameters: params 
            }
        }
    }
    }
    post{
        always{
            echo 'cleaning up workspace'
            deleteDir()
        }
    }
}