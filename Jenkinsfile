// Pipeline de los 2 microservicios con JDK 21 y Maven 3.9.x
// Requiere en Jenkins (Manage Jenkins > Tools):
//   JDK installations   -> nombre: jdk21          (Temurin / OpenJDK 21)
//   Maven installations -> nombre: maven-3.9.9    (Apache Maven 3.9.9)
pipeline {
    agent any

    tools {
        jdk 'jdk21'
        maven 'maven-3.9.9'
    }

    options {
        timestamps()
        buildDiscarder(logRotator(numToKeepStr: '10'))
    }

    stages {
        stage('Verificar versiones') {
            steps {
                script {
                    if (isUnix()) { sh 'java -version && mvn -version' }
                    else          { bat 'java -version && mvn -version' }
                }
            }
        }

        stage('Backend: compilar y probar (Java 21)') {
            steps {
                dir('Ejercicio5MyAPIGestor') {
                    script {
                        if (isUnix()) { sh 'mvn -B clean verify' }
                        else          { bat 'mvn -B clean verify' }
                    }
                }
            }
            post {
                always {
                    junit allowEmptyResults: true, testResults: 'Ejercicio5MyAPIGestor/target/surefire-reports/*.xml'
                }
            }
        }

        // El cliente comparte el puerto 8081 con el backend en sus pruebas automáticas
        // (backend simulado), por eso corre después y no en paralelo.
        stage('Cliente: compilar y probar (Java 21)') {
            steps {
                dir('Ejercicio5Cliente') {
                    script {
                        if (isUnix()) { sh 'mvn -B clean verify' }
                        else          { bat 'mvn -B clean verify' }
                    }
                }
            }
            post {
                always {
                    junit allowEmptyResults: true, testResults: 'Ejercicio5Cliente/target/surefire-reports/*.xml'
                }
            }
        }

        // Para la práctica de SonarQube: descomentar y ajustar el nombre del servidor configurado en Jenkins
        // stage('SonarQube') {
        //     steps {
        //         withSonarQubeEnv('sonarqube') {
        //             dir('Ejercicio5MyAPIGestor') { bat 'mvn -B sonar:sonar' }
        //         }
        //     }
        // }
    }

    post {
        success { echo 'Compilación y pruebas con Java 21: OK' }
        failure { echo 'Falló la compilación o las pruebas con Java 21' }
    }
}
