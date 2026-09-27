// Tollgate - Abnormal Load Permit System
// Copyright (c) 2017 Pellowe Tarn Systems Ltd. All rights reserved.
// $Id: Jenkinsfile 1201 2018-04-11 15:38:22Z koduya $

properties([buildDiscarder(logRotator(numToKeepStr: '20'))])

node('maven') {
    def mvnHome = tool 'maven-3.5'
    env.JAVA_HOME = tool 'jdk8'
    env.PATH = "${env.JAVA_HOME}/bin:${mvnHome}/bin:${env.PATH}"

    try {
        stage('Checkout') {
            checkout scm
        }
        stage('Build') {
            sh 'mvn -B -Pdev clean verify'
        }
        stage('Archive') {
            archiveArtifacts artifacts: 'tollgate-app/target/*.jar', fingerprint: true
            junit allowEmptyResults: true, testResults: '**/target/surefire-reports/*.xml'
        }
    } catch (err) {
        mail to: 'tollgate-dev@pellowetarn.example',
             subject: "Tollgate build ${env.BUILD_NUMBER} failed",
             body: "See ${env.BUILD_URL}"
        throw err
    }
}
